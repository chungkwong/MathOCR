/* Block.java
 * =========================================================================
 * This file is originally part of the MathOCR Project
 *
 * Copyright (C) 2014,2015 Chan Chung Kwong
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
/**
 * A data structure representing physical block
 */
public class Block{
	boolean centering;
	int left,right,top,bottom;
	ArrayList<ConnectedComponent> pool;
	Page page;
	BlockType type;
	List<TextLine> lines;
	/**
	 * Construct a Block
	 * @param page the page that the block belong to
	 * @param pool the ConnectedComponent inside the block
	 * @param left minimum x coordinate of bounding box of this block
	 * @param right maximum x coordinate of bounding box of this block
	 * @param top minimum y coordinate of bounding box of this block
	 * @param bottom maximum y coordinate of bounding box of this block
	 * @param centering if the block is centering
	 */
	public Block(Page page,ArrayList<ConnectedComponent> pool,int left,int right,int top,int bottom,boolean centering){
		this.left=left;
		this.right=right;
		this.top=top;
		this.bottom=bottom;
		this.pool=pool;
		this.page=page;
		this.centering=centering;
	}
	/**
	 * Get minimum x coordinate of bounding box of this block
	 * @return the coordinate
	 */
	public int getLeft(){
		return left;
	}
	/**
	 * Get maximum x coordinate of bounding box of this block
	 * @return the coordinate
	 */
	public int getRight(){
		return right;
	}
	/**
	 * Get minimum y coordinate of bounding box of this block
	 * @return the coordinate
	 */
	public int getTop(){
		return top;
	}
	/**
	 * Get maximum y coordinate of bounding box of this block
	 * @return the coordinate
	 */
	public int getBottom(){
		return bottom;
	}
	/**
	 * Check if the block is centering in the page
	 * @return the result
	 */
	public boolean isCentering(){
		return centering;
	}
	/**
	 * Get the width of the block
	 * @return the width
	 */
	public int getWidth(){
		return right-left+1;
	}
	/**
	 * Get the height of the block
	 * @return the height
	 */
	public int getHeight(){
		return bottom-top+1;
	}
	/**
	 * Merge with another block
	 */
	public void merge(Block block){
		pool.addAll(block.pool);
		left=Math.min(left,block.left);
		right=Math.max(right,block.right);
		top=Math.min(top,block.top);
		bottom=Math.max(bottom,block.bottom);
	}
	/**
	 * Split this element vertically
	 * @param y the coordinate of the line to be used to split
	 */
	public Block splitVertically(int y){
		ArrayList<ConnectedComponent> lst=new ArrayList<ConnectedComponent>(pool.size());
		ListIterator<ConnectedComponent> iter=pool.listIterator();
		while(iter.hasNext()){
			ConnectedComponent ele=iter.next();
			if(ele.getTop()>=y){
				iter.remove();
				lst.add(ele);
			}
		}
		Block block=new Block(page,lst,left,right,y,bottom,centering);
		bottom=y-1;
		return block;
	}
	/**
	 * Split this block horizontally
	 * @param x the coordinate of the line to be used to split
	 */
	public Block splitHorizontally(int x){
		ArrayList<ConnectedComponent> lst=new ArrayList<ConnectedComponent>(pool.size());
		ListIterator<ConnectedComponent> iter=pool.listIterator();
		while(iter.hasNext()){
			ConnectedComponent ele=iter.next();
			if(ele.getLeft()>=x){
				iter.remove();
				lst.add(ele);
			}
		}
		centering=false;
		Block block=new Block(page,lst,x,right,top,bottom,false);
		right=x-1;
		return block;
	}
	/**
	 * Get ConnectedComponent inside the block
	 * @return the list of  ConnectedComponent
	 */
	public ArrayList<ConnectedComponent> getComponents(){
		return pool;
	}
	/**
	 * Extract text lines from this block
	 */
	public void extractTextLines(){
		if(type instanceof TextBlock)
			lines=TextBlock.extractTextLines(this);
	}
	/**
	 * Get the text lines
	 * @return the text lines
	 */
	public List<TextLine> getTextLines(){
		return lines;
	}
	/**
	 * Set the type of the block
	 * @param type the type
	 */
	public void setType(BlockType type){
		this.type=type;
	}
	/**
	 * Get the type of the block
	 * @return the type
	 */
	public BlockType getType(){
		return type;
	}
	/**
	 * Get the page that the block belong to
	 * @return the page
	 */
	public Page getPage(){
		return page;
	}
	/**
	 * Produce logical blocks from this physical block
	 */
	public void recognize(){
		type.recognize(this);
	}
}