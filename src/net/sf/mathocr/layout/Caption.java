/* Caption.java
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
import java.util.regex.*;
/**
 * A data structure representing caption
 */
public final class Caption extends TextLike{
   static final Pattern[] knownStyle=new Pattern[]{Pattern.compile("图\\s[0-9]:?(.*)"),Pattern.compile("表\\s[0-9]:?(.*)")
   ,Pattern.compile("Fig\\.[0-9](.*)"),Pattern.compile("Figure\\s[0-9]:?(.*)"),Pattern.compile("Table\\s[0-9]:?(.*)")};
	public Caption(TextLine line){
		super(line);
	}
	/**
	 * Construct a Listing
	 * @param content content in the block
	 * @param noStart if the block is possibly continued from a previous block
	 * @param noEnd if the block is possibly to be continued
	 * @param left minimum x coordinate of bounding box of this block
	 * @param right maximum x coordinate of bounding box of this block
	 * @param top minimum y coordinate of bounding box of this block
	 * @param bottom maximum y coordinate of bounding box of this block
	 */
	public Caption(String content,boolean noStart,boolean noEnd,int left,int right,int top,int bottom){
		super(content,noStart,noEnd,left,right,top,bottom);
	}
	/**
	 * Check if a String indicate a caption
	 * @param str to be checked
	 * @return result
	 */
	public static boolean isCaption(String str){
		for(int i=0;i<knownStyle.length;i++)
			if(knownStyle[i].matcher(str).matches())
				return true;
		return false;
	}
	/**
	 * Get content without caption prefix
	 * @return content without label
	 */
	public String getContentNoPrefix(){
		for(int i=0;i<knownStyle.length;i++){
			Matcher matcher=knownStyle[i].matcher(content);
			if(matcher.matches())
				return matcher.group(1);
		}
		return content;
	}
}