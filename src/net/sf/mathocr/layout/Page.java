/* Page.java
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
import java.net.*;
import java.awt.image.*;
import java.util.*;
import javax.imageio.*;
import net.sf.mathocr.common.*;
import net.sf.mathocr.preprocess.*;
import net.sf.mathocr.ocr.*;
/**
 * A data structure representing page
 */
public final class Page{
	Object src;
	BufferedImage inputImage,modifiedImage;
	ComponentPool components;
	LinkedList<Block> blocks;
	LinkedList<LogicalBlock> logBlocks=new LinkedList<LogicalBlock>();
	List<String> footnotes=new LinkedList<String>();
	int leftbound,rightbound,topbound,bottombound,pageno=-1;
	boolean titlefound=false;
	Document doc;
	/**
	 * Construct a Page
	 * @param src the input image,it can be a File, InputStream, URL or BufferedImage
	 * @param doc the Document that the page belong to
	 */
	public Page(Object src,Document doc){
		this.src=src;
		this.doc=doc;
	}
	/**
	 * Load the input image
	 */
	public void load(){
		try{
			if(src instanceof File)
				inputImage=ImageIO.read((File)src);
			else if(src instanceof InputStream)
				inputImage=ImageIO.read((InputStream)src);
			else if(src instanceof URL)
				inputImage=ImageIO.read((URL)src);
			else if(src instanceof BufferedImage)
				inputImage=(BufferedImage)src;
			else
				throw new RuntimeException("Input type not supported");
			resetModifiedImage();
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	/**
	 * Undo the preprocess
	 */
	public void resetModifiedImage(){
		//ColorModel cm=inputImage.getColorModel();
		//modifiedImage=new BufferedImage(cm,inputImage.copyData(null),cm.isAlphaPremultiplied(),null);
		//System.out.println(System.currentTimeMillis());
		modifiedImage=new Grayscale().preprocess(inputImage);
		//System.out.println(System.currentTimeMillis());
	}
	/**
	 * Get input image
	 * @return the image
	 */
	public BufferedImage getInputImage(){
		return inputImage;
	}
	/**
	 * Get preprocessed image
	 * @return the image
	 */
	public BufferedImage getModifiedImage(){
		return modifiedImage;
	}
	/**
	 * Preprocess the preprocessed image
	 * @param preprocessor the Preprocessor
	 */
	public void preprocess(Preprocessor preprocessor){
		modifiedImage=preprocessor.preprocess(modifiedImage);
	}
	/**
	 * Preprocess the input image
	 * @param preprocessor the Preprocessor
	 */
	public void preprocessInput(Preprocessor preprocessor){
		inputImage=preprocessor.preprocess(inputImage);
	}
	/**
	 * Preprocess the preprocessed image and input image using default procedure
	 */
	public void preprocessByDefault(){
		CombinedPreprocessor preprocessor=CombinedPreprocessor.getDefaultCombinedPreprocessor();
		//System.out.println(System.currentTimeMillis());
		modifiedImage=preprocessor.preprocess(modifiedImage);
		//System.out.println(System.currentTimeMillis());
		double angle=SkewCorrect.detectSkew(modifiedImage);
		//System.out.println(System.currentTimeMillis());
		if(Math.abs(angle)>Math.PI/900){
			SkewCorrect correct=new SkewCorrect(angle);
			preprocess(correct);
			preprocessInput(correct);
		}
		//System.out.println(System.currentTimeMillis());
	}
	/**
	 * Conduct connected component analysis
	 */
	public void componentAnalysis(){
		components=new ComponentPool(modifiedImage);
	}
	/**
	 * Remove all connected components that touch bound of the image(they are likely to be noise)
	 */
	public void cleanPageEdges(){
		ListIterator<ConnectedComponent> iter=components.getComponents().listIterator();
		while(iter.hasNext()){
			ConnectedComponent ele=iter.next();
			if(ele.getLeft()==0||ele.getTop()==0||ele.getRight()==modifiedImage.getWidth()-1||ele.getBottom()==modifiedImage.getHeight()-1)
				iter.remove();
		}
	}
	/**
	 * Get all the ConnectedComponent
	 * @return all the ConnectedComponent
	 */
	public ComponentPool getComponentPool(){
		return components;
	}
	/**
	 * Page segment
	 * @param method segment method
	 */
	public void segment(SegmentMethod method){
		blocks=method.segment(this);
		components=null;
	}
	/**
	 * Get all the physical blocks
	 * @return the physical blocks
	 */
	public List<Block> getBlocks(){
		return blocks;
	}
	/**
	 * Set the bounding box of the page
	 * @param leftbound the left bound
	 * @param rightbound the right bound
	 * @param topbound the top bound
	 * @param bottombound the bottom bound
	 */
	public void setBound(int leftbound,int rightbound,int topbound,int bottombound){
		this.leftbound=leftbound;
		this.rightbound=rightbound;
		this.topbound=topbound;
		this.bottombound=bottombound;
	}
	/**
	 * Get the left bound of the bounding box of the page
	 * @return the left bound
	 */
	public int getLeftBound(){
		return leftbound;
	}
	/**
	 * Get the right bound of the bounding box of the page
	 * @return the right bound
	 */
	public int getRightBound(){
		return rightbound;
	}
	/**
	 * Get the width of the page in the Document
	 * @return width
	 */
	public int getWidth(){
		return modifiedImage.getWidth();
	}
	/**
	 * Get the height of the page
	 * @return height
	 */
	public int getHeight(){
		return modifiedImage.getHeight();
	}
	/**
	 * Get the page number of the page in the Document
	 * @return page number
	 */
	public int getPageNumber(){
		if(pageno==-1)
			pageno=doc.getPages().indexOf(this);
		return pageno;
	}
	/**
	 * Check if ele is likely a speical character
	 * @param ele to be checked
	 * @return result
	 */
	private static final boolean isPossibleSpecial(ConnectedComponent ele){
		if(!SpecialMatcher.usable)
			return false;
		if(SpecialMatcher.isPossibleRootSign(ele))
			return true;
		return false;
	}
	/**
	 * Filter out some components that are likely noise
	 */
	public void filterNoiseComponent(){
		components.filterNoise(modifiedImage.getWidth(),modifiedImage.getHeight());
	}
	/**
	 * Classify physical blocks, mainly use the size of the components
	 */
	public void regionClassify(){
		int thre=(rightbound-leftbound)/10;
		ConnectedComponent e=null;
		for(Block block:blocks)
			if(block.getType()==null&&!block.getComponents().isEmpty()){
				boolean isGraphics=false;
				boolean isTable=false;
				int sum=0,max=0,count=0,cw=0;
				for(ConnectedComponent ele:block.getComponents()){
					int h=ele.getHeight();
					sum+=h;
					++count;
					if(h>max){//&&!isPossibleSpecial(ele)
						max=h;
						e=ele;
						cw=ele.getWidth();
					}
				}
					/*if(ele.getWidth()>=thre||ele.getHeight()>=thre)
						if(ele.getLeft()==block.getLeft()&&ele.getRight()==block.getRight()&&ele.getTop()==block.getTop()&&ele.getBottom()==block.getBottom()){
							if(blocks.size()>1||ele.getWidth()<=20*ele.getHeight()||ele.getDensity()<=0.8){
								isTable=true;
								break;
							}
						int count=0;
						for(ConnectedComponent ele2:block.getComponents())
							if(ele.getLeft()<=ele2.getRight()&&ele2.getLeft()<=ele.getRight()&&ele.getTop()<=ele2.getBottom()&&ele2.getTop()<=ele.getBottom())
								++count;
						if(count>=50){
							isGraphics=true;
							break;
						}
					}*/
				if(max>sum*15/count||(max>block.getHeight()*2/3&&cw>block.getWidth()*2/3&&cw>thre&&!isPossibleSpecial(e)))
					isGraphics=true;
				if(isTable)
					block.setType(TableBlock.DEFAULT_BLOCK);
				else if(isGraphics)
					block.setType(ImageBlock.DEFAULT_BLOCK);
				else
					block.setType(TextBlock.DEFAULT_BLOCK);
			}
	}
	/**
	 * Sort physical blocks to their reading order
	 */
	public void readingOrderSort(){
		int n=blocks.size();
		boolean[][] adj=new boolean[n][n];
		int[] deg=new int[n];
		LinkedList<Integer> avail=new LinkedList<Integer>();
		ListIterator<Block> iter2=blocks.listIterator();
		for(int i=0;i<n;i++)
			adj[i][i]=true;
		while(iter2.hasNext()){
			Block block2=iter2.next();
			int ind2=iter2.previousIndex();
			ListIterator<Block> iter1=blocks.listIterator();
			while(iter1.hasNext()){
				Block block1=iter1.next();
				int ind1=iter1.previousIndex();
				if(block2.getLeft()<=block1.getRight()&&block1.getLeft()<=block2.getRight()&&block1.getTop()<block2.getTop()&&!adj[ind1][ind2]){
					adj[ind1][ind2]=true;
					++deg[ind2];
					for(int i=0;i<n;i++)
						for(int j=0;j<n;j++)
							if(adj[i][ind1]&&adj[ind2][j]&&!adj[i][j]){
								adj[i][j]=true;
								++deg[j];
							}
				}
			}
		}
		iter2=blocks.listIterator();
		while(iter2.hasNext()){
			Block block2=iter2.next();
			int ind2=iter2.previousIndex();
			ListIterator<Block> iter1=blocks.listIterator();
			while(iter1.hasNext()){
				Block block1=iter1.next();
				int ind1=iter1.previousIndex();
				if(!adj[ind1][ind2]&&!adj[ind2][ind1]&&block1.getRight()<block2.getLeft()){
					adj[ind1][ind2]=true;
					++deg[ind2];
					for(int i=0;i<n;i++)
						for(int j=0;j<n;j++)
							if(adj[i][ind1]&&adj[ind2][j]&&!adj[i][j]){
								adj[i][j]=true;
								++deg[j];
							}
				}
			}
			if(deg[ind2]==0)
				avail.addFirst(ind2);
		}
		LinkedList<Block> newblocks=new LinkedList<Block>();
		int countdown=n;
		while(!avail.isEmpty()){
			int ind=avail.removeLast();
			newblocks.add(blocks.get(ind));
			for(int i=0;i<n;i++)
				if(adj[ind][i]){
					adj[ind][i]=false;
					if(--deg[i]==0)
						avail.addFirst(i);
				}
			--countdown;
		}
		if(countdown==0)
			blocks=newblocks;
		else
			System.err.println("Reading order sort failed, please report this bug");
	}
	/**
	 * Conduct logical layout analysis
	 */
	public void produceLogicalBlock(){
		while(!blocks.isEmpty()){
			Block block=blocks.removeFirst();
			block.recognize();
		}
	}
	/**
	 * Get all the logical blocks
	 * @return the logical blocks
	 */
	public LinkedList<LogicalBlock> getLogicalBlocks(){
		return logBlocks;
	}
	/**
	 * Get all the footnotes
	 * @return the footnotes
	 */
	public List<String> getFootNotes(){
		return footnotes;
	}
	/**
	 * Get the input source
	 * @return source
	 */
	public Object getSource(){
		return src;
	}
	/**
	 * Get the document that the page belong to
	 * @return the document
	 */
	public Document getDocument(){
		return doc;
	}
	/**
	 * Clean all the reference to images
	 */
	public void cleanImage(){
		inputImage=null;
		modifiedImage=null;
		src=null;
	}
}