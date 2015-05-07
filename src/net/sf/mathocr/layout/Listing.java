/* Listing.java
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
 * A data structure representing listing item
 */
public final class Listing extends TextLike{
	static final Pattern[] itemizeFormat=new Pattern[]{Pattern.compile("(\\-)\\s(.+)")};
	static final Pattern[] enumerateFormat=new Pattern[]{Pattern.compile("(\\d+)\\.\\s(.+)"),Pattern.compile("(\\([a-z]\\))\\s(.+)")
	,Pattern.compile("x{0,2}+(i|ii|iii|iv|v|vi|vii|viii|ix|x)\\.\\s(.+)")};
	int indent;
	int sty;
	/**
	 * Construct a Listing containing a single line
	 * @param line the text line
	 * @param sty the style of the block
	 */
	public Listing(TextLine line,int sty){
		super(line);
		this.sty=sty;
		this.indent=line.getIndent();
	}
	/**
	 * Construct a Listing
	 * @param content content in the block
	 * @param sty the style of the block
	 * @param indent the indent of the block
	 * @param noStart if the block is possibly continued from a previous block
	 * @param noEnd if the block is possibly to be continued
	 * @param left minimum x coordinate of bounding box of this block
	 * @param right maximum x coordinate of bounding box of this block
	 * @param top minimum y coordinate of bounding box of this block
	 * @param bottom maximum y coordinate of bounding box of this block
	 */
	public Listing(String content,int sty,int indent,boolean noStart,boolean noEnd,int left,int right,int top,int bottom){
		super(content,noStart,noEnd,left,right,top,bottom);
		this.sty=sty;
		this.indent=indent;
	}
	/**
	 * Check the style of the listing
	 * @param entry the text
	 */
	public static int testItem(String entry){
		for(int i=0;i<itemizeFormat.length;i++)
			if(itemizeFormat[i].matcher(entry).matches())
				return i+1;
		for(int i=0;i<enumerateFormat.length;i++)
			if(enumerateFormat[i].matcher(entry).matches())
				return -i-1;
		return 0;
	}
	/**
	 * Compare level with another listing item
	 * @param lst another listing item
	 * @return 0 if at the same level, otherwise positive if this has more indent
	 */
	public int compareLevel(Listing lst){
		if(sty==lst.sty)
			return 0;
		return indent-lst.indent;
	}
	/**
	 * Check if the listing item is numbered
	 */
	public boolean isNumbered(){
		return sty<0;
	}
	/**
	 * Get the indent of the listing item
	 */
	public int getIndent(){
		return indent;
	}
	public boolean isNoStart(){
		return false;
	}
	/**
	 * Get the content without label
	 * @return content without label
	 */
	public String getContentNoPrefix(){
		String content=getContent();
		Matcher matcher=null;
		if(sty>0)
			matcher=itemizeFormat[sty-1].matcher(content);
		else if(sty<0)
			matcher=enumerateFormat[-sty-1].matcher(content);
		else
			return content;
		matcher.matches();
		return matcher.group(2);
	}
}