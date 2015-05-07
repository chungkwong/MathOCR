/* Title.java
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
 * A data structure representing title
 */
public final class Title extends TextLike{
	/**
	 * Construct a Title
	 * @param content content in the block
	 * @param noStart if the block is possibly continued from a previous block
	 * @param noEnd if the block is possibly to be continued
	 * @param left minimum x coordinate of bounding box of this block
	 * @param right maximum x coordinate of bounding box of this block
	 * @param top minimum y coordinate of bounding box of this block
	 * @param bottom maximum y coordinate of bounding box of this block
	 */
	public Title(String content,boolean noStart,boolean noEnd,int left,int right,int top,int bottom){
		super(content,noStart,noEnd,left,right,top,bottom);
	}
	/**
	 * Construct a Title containing a single text line
	 * @param line the text line
	 */
	public Title(TextLine line){
		super(line);
	}
	public boolean isNoStart(){
		return false;
	}
	public boolean isNoEnd(){
		return false;
	}
}