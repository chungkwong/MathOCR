/* TextBlock.java
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
import java.util.*;
import net.sf.mathocr.common.*;
import static net.sf.mathocr.Environment.env;
public final class TextBlock implements BlockType{
	/** A text block*/
	public static final TextBlock DEFAULT_BLOCK=new TextBlock();
	private static final boolean[][] possCont=new boolean[][]{
		{true,false,false,false,false},
		{false,true,false,false,false},
		{false,false,false,false,false},
		{false,false,true,false,true},
		{false,false,true,false,true}
	};
	/**
	 * Extract text lines from a box using projection profile
	 * @param block the block
	 */
	public static List<TextLine> extractTextLines(Block block){
		ArrayList<ConnectedComponent> pool=block.getComponents();
		Collections.sort(pool);
		ArrayList<Integer> breakpoints=new ArrayList<Integer>();
		int b=-1,sum=0,count=0;
		for(ConnectedComponent ele:pool){
			if(ele.getTop()<=b){
				if(ele.getBottom()>=b)
					b=ele.getBottom()+1;//should +1 be drop?
			}else{
				if(b!=-1){
					breakpoints.add(b);
					sum+=ele.getTop()-b;
				}
				breakpoints.add(ele.getTop());
				b=ele.getBottom()+1;//should +1 be drop?
				++count;
			}
		}
		breakpoints.add(b);
		ArrayList<TextLine> lines=new ArrayList<TextLine>(count);
		if(count<=1){
			lines.add(new TextLine(block,pool,block.getLeft(),block.getRight(),block.getTop(),block.getBottom()));
		}else{
			ListIterator<ConnectedComponent> it=pool.listIterator();
			int thre=0;//sum/(count-1)/4;
			int start=breakpoints.get(0);
			ConnectedComponent ele=it.next();
			for(int i=1;i<=count;i++){
				int end=breakpoints.get((i<<1)-1);
				if(i==count||breakpoints.get(i<<1)-end>=thre){
					ArrayList<ConnectedComponent> lst=new ArrayList<ConnectedComponent>();
					int left=Integer.MAX_VALUE,right=0;
					while(ele!=null&&ele.getBottom()<=end){
						lst.add(ele);
						if(ele.getLeft()<left)
							left=ele.getLeft();
						if(ele.getRight()>right)
							right=ele.getRight();
						if(it.hasNext())
							ele=it.next();
						else
							ele=null;
					}
					lines.add(new TextLine(block,lst,left,right,start,end-1));//-1 should be droped if +1 above is droped?
					if(i!=count)
						start=breakpoints.get(i<<1);
				}
			}
		}
		return lines;
	}
	/**
	 * Logical layout analysis on a physical textual block
	 * @param block the block
	 */
	public void recognize(Block block){
		Page page=block.getPage();
		boolean footnote=false;
		LinkedList<LogicalBlock> logBlocks=page.getLogicalBlocks();
		int leftbound=page.getLeftBound(),rightbound=page.getRightBound(),height=page.getHeight(),pageno=page.getPageNumber();
		if(!logBlocks.isEmpty()){
			LogicalBlock last=logBlocks.getLast();
			if(last instanceof TextLike&&last.getLeft()<=block.getRight()&&block.getLeft()<=last.getRight())
				((TextLike)logBlocks.getLast()).setNoEnd(false);
		}
		if(block.getTextLines()==null)
			block.extractTextLines();
		List<TextLine> textlines=block.getTextLines();
		ListIterator<TextLine> lines=textlines.listIterator();
		TextLine line=lines.hasNext()?lines.next():null;
		while(line!=null){
			if(line.getConnectedComponents().size()==1){
				ConnectedComponent ele=line.getConnectedComponents().get(0);
				if(ele.getWidth()>20*ele.getHeight()&&ele.getWidth()>(rightbound-leftbound)/16&&ele.getDensity()>0.5){
					if(ele.getWidth()>(rightbound-leftbound)*31/32&&ele.getTop()<height/8&&logBlocks.size()<=2){
						logBlocks.clear();
						page.titlefound=false;
						line=lines.hasNext()?lines.next():null;
						continue;
					}else if(ele.getWidth()<(rightbound-leftbound)/3&&ele.getLeft()-block.getLeft()<(rightbound-leftbound)/32){
						footnote=true;
						line=lines.hasNext()?lines.next():null;
						continue;
					}
				}
			}
			line.recognize();
			String content=line.getText();
			int lstType=Listing.testItem(line.getText()),align=line.getAlignment();
			TextLike lb=null;
			if(line.isDisplayFormula()){
				lb=new Paragraph(line);
				lb.setNoStart(true);
				lb.setNoEnd(true);
			}else if(Caption.isCaption(content)){
				lb=new Caption(line);
			}else if(align==TextLine.ALIGN_PAGE_CENTER&&pageno==0&&!page.titlefound){
				lb=new Title(line);
				page.titlefound=true;
			}else if(align==TextLine.ALIGN_PAGE_CENTER&&!logBlocks.isEmpty()&&logBlocks.getLast() instanceof Title){
				lb=new Author(line);
			}else if(align==TextLine.ALIGN_PAGE_CENTER||align==TextLine.ALIGN_CENTER
			||(((align==TextLine.ALIGN_LEFT&&!line.isStarting())||(textlines.size()==1&&line.getWidth()<(rightbound-leftbound)/3))&&!line.isEndedProperly())){
				lb=new Heading(line);
			}else if(lstType!=0){
				lb=new Listing(line,lstType);
			}else{
				lb=new Paragraph(line);
			}
			line=lines.hasNext()?lines.next():null;
			while(line!=null){
				line.recognize();
				if(!possCont[align][line.getAlignment()]&&!(lb instanceof Paragraph&&line.isDisplayFormula()))
					break;
				lb.addTextLine(line);
				align=line.getAlignment();
				line=lines.hasNext()?lines.next():null;
			}
			logBlocks.addLast(lb);
		}
	}
	public String toString(){
		return "TEXT";
	}
}