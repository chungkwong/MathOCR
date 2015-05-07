/* TextLike.java
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
/**
 * A data structure representing textual logical block
 */
public class TextLike implements LogicalBlock{
	protected int left,right,top,bottom;
	protected boolean noStart,noEnd;
	protected String content;
	/**
	 * Construct a TextLike
	 * @param content content in the block
	 * @param noStart if the block is possibly continued from a previous block
	 * @param noEnd if the block is possibly to be continued
	 * @param left minimum x coordinate of bounding box of this block
	 * @param right maximum x coordinate of bounding box of this block
	 * @param top minimum y coordinate of bounding box of this block
	 * @param bottom maximum y coordinate of bounding box of this block
	 */
	public TextLike(String content,boolean noStart,boolean noEnd,int left,int right,int top,int bottom){
		this.noStart=noStart;
		this.noEnd=noEnd;
		this.left=left;
		this.right=right;
		this.top=top;
		this.bottom=bottom;
		this.content=content;
	}
	/**
	 * Construct a TextLike containing a single text line
	 * @param line the text line
	 */
	public TextLike(TextLine line){
		this.content=line.getText();
		left=line.getLeft();
		right=line.getRight();
		top=line.getTop();
		bottom=line.getBottom();
		noStart=!line.isStarting()||line.isDisplayFormula();
		noEnd=!line.isEnded()||line.isDisplayFormula();
	}
	/**
	 *	Get the text content
	 * @return the content
	 */
	public String getContent(){
		return content;
	}
	/**
	 * Set content
	 * @param content the content to be set
	 */
	public void setContent(String content){
		this.content=content;
	}
	/**
	 * Add one more text line
	 * @param line the text line
	 */
	public void addTextLine(TextLine line){
		if(!content.endsWith("-"))
			content+=" ";
		content+=" "+line.getText();
		left=Math.min(left,line.getLeft());
		right=Math.max(right,line.getRight());
		bottom=Math.max(bottom,line.getBottom());
		noEnd=!line.isEnded()||line.isDisplayFormula();
	}
	/**
	 * Check if the block is possibly continued from a previous block
	 * @return result
	 */
	public boolean isNoStart(){
		return noStart;
	}
	/**
	 * Check if the block is possibly to be continued
	 * @return result
	 */
	public boolean isNoEnd(){
		return noEnd;
	}
	/**
	 * Set if the block is possibly continued from a previous block
	 * @param noStart value
	 */
	public void setNoStart(boolean noStart){
		this.noStart=noStart;
	}
	/**
	 * Set if the block is to be continued
	 * @param noStart value
	 */
	public void setNoEnd(boolean noEnd){
		this.noEnd=noEnd;
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
	public boolean isFloating(){
		return false;
	}
}