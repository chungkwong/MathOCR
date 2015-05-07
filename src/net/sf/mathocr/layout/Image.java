/* Image.java
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
 * A data structure representing image
 */
public final class Image implements LogicalBlock{
	String path,caption;
	int left,right,top,bottom;
	/**
	 * Construct a Image
	 * @param path the path to the image file
	 * @param caption the caption
	 * @param left minimum x coordinate of bounding box of this block
	 * @param right maximum x coordinate of bounding box of this block
	 * @param top minimum y coordinate of bounding box of this block
	 * @param bottom maximum y coordinate of bounding box of this block
	 */
	public Image(String path,String caption,int left,int right,int top,int bottom){
		this.path=path;
		this.caption=caption;
		this.left=left;
		this.right=right;
		this.top=top;
		this.bottom=bottom;
	}
	/**
	 * Get the path to the image file
	 * @return the path
	 */
	public String getPath(){
		return path;
	}
	/**
	 * Get the caption of the image
	 * @return the caption
	 */
	public String getCaption(){
		return caption;
	}
	/**
	 * Set the path to the image
	 * @param caption the caption
	 */
	public void setCaption(String caption){
		this.caption=caption;
	}
	public boolean isFloating(){
		return true;
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
}