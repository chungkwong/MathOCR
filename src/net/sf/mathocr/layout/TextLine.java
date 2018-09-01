/* TextLine.java
 * =========================================================================
 * This file is originally part of the MathOCR Project
 *
 * Copyright (C) 2015 Chan Chung Kwong
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 */
package net.sf.mathocr.layout;
import java.io.*;
import java.util.*;
import javax.imageio.*;
import net.sf.mathocr.common.*;
import net.sf.mathocr.ocr.*;
import net.sf.mathocr.ofr.*;
import static net.sf.mathocr.Environment.env;
/**
 * A data structure representing text line
 */
public final class TextLine{
	//public static final int NATIVE=0,TESSERACT=1,OCRAD=2,GOCR=3;
	public static final int ALIGN_PAGE_CENTER=0,ALIGN_CENTER=1,ALIGN_LEFT=2,ALIGN_RIGHT=3,ALIGN_FULL=4;
	static final char[] endMarks=new char[]{'.','?','!',';',':','。','？','！','；','：'};
	Block block;
	ArrayList<ConnectedComponent> pool;
	String text;
	boolean equation=false;
	int left,right,top,bottom,fontsize,align;
	/**
	 * Construct a TextLine
	 * @param block the physical block
	 * @param pool the ConnectedCompnent inside the line
	 * @param left minimum x coordinate of bounding box of this block
	 * @param right maximum x coordinate of bounding box of this block
	 * @param top minimum y coordinate of bounding box of this block
	 * @param bottom maximum y coordinate of bounding box of this block
	 */
	public TextLine(Block block,ArrayList<ConnectedComponent> pool,int left,int right,int top,int bottom){
		this.block=block;
		this.pool=pool;
		this.left=left;
		this.right=right;
		this.top=top;
		this.bottom=bottom;
		fontsize=bottom-top+1;
	}
	/**
	 * Get the ConnectedComponent inside the line
	 * @return the ConnectedComponent
	 */
	public ArrayList<ConnectedComponent> getConnectedComponents(){
		return pool;
	}
	/**
	 * Recognize the textline using external program
	 * @param prefix the path of the command before input file name
	 * @param suffix the path of the command after input file name
	 * @return the result
	 */
	public String recognizeByCommand(String prefix,String suffix){
		try{
			File file=File.createTempFile("mathocr",".pbm");
			file.deleteOnExit();
			ImageIO.write(block.getPage().getModifiedImage().getSubimage(left,top,right-left+1,bottom-top+1),"pnm",file);
			Process process=Runtime.getRuntime().exec(prefix+file.getAbsolutePath()+suffix);
			process.waitFor();
			BufferedReader in=new BufferedReader(new InputStreamReader(process.getInputStream()));
			StringBuilder buf=new StringBuilder();
			String str;
			while((str=in.readLine())!=null)
				buf.append(str);
			return Convertor.plainToLaTeX(buf.toString());
		}catch(Exception ex){
			System.err.println("Check if "+prefix+" is installed.");
			ex.printStackTrace();
		}
		return "";
	}
	/**
	 * Recognize the text line using GNU Ocrad
	 * @return the result
	 */
	public String recognizeByOcrad(){
		return recognizeByCommand("ocrad ","");
	}
	/**
	 * Recognize the text line using GOCR
	 * @return the result
	 */
	public String recognizeByGOCR(){
		return recognizeByCommand("gocr ","");
	}
	/**
	 * Recognize the text line using Tesseract
	 * @return the result
	 */
	public String recognizeByTesseract(){
		return recognizeByCommand("tesseract "," stdout "+env.getString("TESSERACT_PARAMETER"));
	}
	/**
	 * Merge with another TextLine
	 * @param line another TextLine
	 */
	public void mergeWith(TextLine line){
		left=Math.min(left,line.left);
		right=Math.max(right,line.right);
		top=Math.min(top,line.top);
		bottom=Math.max(bottom,line.bottom);
		pool.addAll(line.pool);
	}
	/**
	 * Split the Text into two
	 * @param y the y coordinate spliting this TextLine
	 * @return the lower line
	 */
	public TextLine splitAt(int y){
		ArrayList<ConnectedComponent> lst=new ArrayList<ConnectedComponent>(pool.size());
		ListIterator<ConnectedComponent> iter=pool.listIterator();
		int xmin=right,xmax=left,xmin2=right,xmax2=left,ymin=bottom,ymax=top;
		while(iter.hasNext()){
			ConnectedComponent ele=iter.next();
			if(ele.getTop()>=y){
				iter.remove();
				lst.add(ele);
				xmin2=Math.min(xmin2,ele.getLeft());
				xmax2=Math.max(xmax2,ele.getRight());
				ymin=Math.min(ymin,ele.getTop());
			}/*else if(ele.getBottom()>=y){
				ConnectedComponent ele2=ele.splitVertically(y-1);
				lst.add(ele2);
				xmin2=Math.min(xmin2,ele2.getLeft());
				xmax2=Math.max(xmax2,ele2.getRight());
				xmin=Math.min(xmin,ele.getLeft());
				xmax=Math.max(xmax,ele.getRight());
				ymax=y-1;
				ymin=y;
			}*/else{
				xmin=Math.min(xmin,ele.getLeft());
				xmax=Math.max(xmax,ele.getRight());
				ymax=Math.max(ymax,ele.getBottom());
			}
		}
		left=xmin;
		right=xmax;
		TextLine line=new TextLine(block,lst,xmin2,xmax2,ymin,bottom);
		bottom=ymax;
		return line;
	}
	/**
	 * Recognize using default engine
	 */
	public void recognize(){
		if(text==null){
			switch(env.getString("OCR_ENGINE").toUpperCase()){
				case "NATIVE":
					CharactersLine cline=new CharactersLine(this);
					checkEquation(cline);
					LogicalLine lline=new LogicalLine(cline,equation);
					text=lline.recognize();
					if(equation){
						text="\\begin{equation}"+text.substring(2,text.length()-2)+"\\end{equation}";
					}
					fontsize=lline.getFontSize();
					break;
				case "TESSERACT":
					text=recognizeByTesseract();
					break;
				case "OCRAD":
					text=recognizeByOcrad();
					break;
				case "GOCR":
					text=recognizeByGOCR();
					break;
			}
			int leftpagemargin=left-block.getPage().getLeftBound(),rightpagemargin=block.getPage().getRightBound()-right;
			int leftmargin=left-block.getLeft(),rightmargin=block.getRight()-right;
			if(leftpagemargin<=rightpagemargin+2*fontsize&&rightpagemargin<=leftpagemargin+2*fontsize&&leftpagemargin>fontsize&&rightpagemargin>fontsize)
				align=ALIGN_PAGE_CENTER;
			else if(leftmargin>rightmargin+fontsize)
				align=ALIGN_RIGHT;
			else if(rightmargin>leftmargin+fontsize)
				align=ALIGN_LEFT;
			else if(leftmargin>fontsize&&rightmargin>fontsize)
				align=ALIGN_CENTER;
			else
				align=ALIGN_FULL;
		}
	}
	public int getLeft(){
		return left;
	}
	public int getRight(){
		return right;
	}
	public int getTop(){
		return top;
	}
	public int getBottom(){
		return bottom;
	}
	/**
	 * Get the width of the line
	 * @return the width
	 */
	public int getWidth(){
		return right-left+1;
	}
	/**
	 * Get the height of the line
	 * @return the height
	 */
	public int getHeight(){
		return bottom-top+1;
	}
	/**
	 * Get the alignment of the line
	 * @return ALIGN_PAGE_CENTER,ALIGN_CENTER,ALIGN_LEFT,ALIGN_RIGHT or ALIGN_FULL
	 */
	public int getAlignment(){
		return align;
	}
	/**
	 * Check if the line is a display formula
	 * @return the result
	 */
	public boolean isDisplayFormula(){
		return equation||text.startsWith("$$");
	}
	/**
	 * Check if the line is a opening line
	 * @return the result
	 */
	public boolean isStarting(){
		return left-block.getLeft()>fontsize;
	}
	/**
	 * Check if the line is a ending line
	 * @return the result
	 */
	public boolean isEnded(){
		return block.getRight()-right>fontsize;
	}
	/**
	 * Check if the line is ended with a ending punctuation mark
	 * @return the result
	 */
	public boolean isEndedProperly(){
		String str=text.trim();
		if(!str.isEmpty()){
			char mark=str.charAt(str.length()-1);
			for(char c:endMarks)
				if(mark==c)
					return true;
		}
		return false;
	}
	/**
	 * Get the indent from the left
	 * @return the indent
	 */
	public int getIndent(){
		return left-block.getLeft();
	}
	/**
	 * Get the font size of the line
	 * @return font size
	 */
	public int getFontSize(){
		return fontsize;
	}
	/**
	 * Get the recognition result
	 * @return the result
	 */
	public String getText(){
		return text;
	}
	private void checkEquation(CharactersLine cline){
		List<Char> chars=cline.getCharacters();
		if(chars.isEmpty())
			return;
		Char first=chars.get(0),last=chars.get(chars.size()-1);
		if(first.getLeft()-block.getLeft()<first.getWidth()&&!first.getCandidates().isEmpty()&&first.getCandidates().first().getSymbol().toString().equals("(")){
			int right=block.getRight();
			ListIterator<Char> iter=chars.listIterator(1);
			while(iter.hasNext()){
				Char curr=iter.next();
				if(!curr.getCandidates().isEmpty()&&curr.getCandidates().first().getSymbol().toString().equals(")")){
					right=curr.getRight();
					break;
				}
			}
			if(iter.hasNext()&&iter.next().getLeft()-right>(right-first.getLeft())){
				while(iter.hasPrevious()){
					iter.previous();
					iter.remove();
				}
				equation=true;
				return;
			}
		}
		if(block.getRight()-last.getRight()<last.getWidth()&&!last.getCandidates().isEmpty()&&last.getCandidates().first().getSymbol().toString().equals(")")){
			int left=block.getLeft();
			ListIterator<Char> iter=chars.listIterator(chars.size()-1);
			while(iter.hasPrevious()){
				Char curr=iter.previous();
				if(!curr.getCandidates().isEmpty()&&curr.getCandidates().first().getSymbol().toString().equals("(")){
					left=curr.getLeft();
					break;
				}
			}
			if(iter.hasPrevious()&&left-iter.previous().getRight()>(last.getRight()-left)){
				while(iter.hasNext()){
					iter.next();
					iter.remove();
				}
				equation=true;
				return;
			}
		}
	}
}