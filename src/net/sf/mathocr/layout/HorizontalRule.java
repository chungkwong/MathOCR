/* HorizontalRule.java
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
 * A data structure representing horizontal line
 */
public final class HorizontalRule implements LogicalBlock{
	public static final int HEADER_LINE=0,TABLE_LINE=1,FOOTER_LINE=2;
	int left,right,top,bottom,type;
	/**
	 * Construct a Paragraph
	 * @param left minimum x coordinate of bounding box of this block
	 * @param right maximum x coordinate of bounding box of this block
	 * @param top minimum y coordinate of bounding box of this block
	 * @param bottom maximum y coordinate of bounding box of this block
	 * @param type HorizontalRule.HEADER_LINE, HorizontalRule.TABLE_LINE or HorizontalRule.FOOTER_LINE
	 */
	public HorizontalRule(int left,int right,int top,int bottom,int type){
		this.left=left;
		this.right=right;
		this.top=top;
		this.bottom=bottom;
		this.type=type;
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
	 * Get the type of the rule
	 * @return HorizontalRule.HEADER_LINE, HorizontalRule.TABLE_LINE or HorizontalRule.FOOTER_LINE
	 */
	public int getType(){
		return type;
	}
	public boolean isFloating(){
		return false;
	}
}