/* Heading.java
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
 * A data structure representing heading
 */
public final class Heading extends TextLike{
	static final String[] levelName=new String[]{"part","chapter","section","subsection","subsubsection"};
	public static final int LV_PART=0,LV_CHAPTER=1,LV_SECTION=2,LV_SUBSECTION=3,LV_SUBSUBSECTION=4;
	static final Pattern[] knownStyle=new Pattern[]{Pattern.compile("[0-9]+\\s?\\.?\\s(.*)"),Pattern.compile("[0-9]+\\s?\\.\\s?[0-9]+\\.?\\s(.*)")
	,Pattern.compile("[0-9]+\\s?\\.\\s?[0-9]+\\s?\\.\\s?[0-9]+\\.?\\s(.*)"),Pattern.compile("Chapter\\s[0-9]+(.*)"),Pattern.compile("第\\s?[0-9]+\\s?章(.*)")
	,Pattern.compile("第\\s?[〇一二三四五六七八九十]+\\s?章")};
	static final int[] corrLevel=new int[]{2,3,4,1,1,1};
	int fontsize;
	int style;
	/**
	 * Construct a Heading containing a single line
	 * @param line the text line
	 */
	public Heading(TextLine line){
		super(line);
		fontsize=line.getFontSize();
		checkStyle();
	}
	/**
	 * Construct a Listing
	 * @param content content in the block
	 * @param fontsize fontsize used in the block
	 * @param noStart if the block is possibly continued from a previous block
	 * @param noEnd if the block is possibly to be continued
	 * @param left minimum x coordinate of bounding box of this block
	 * @param right maximum x coordinate of bounding box of this block
	 * @param top minimum y coordinate of bounding box of this block
	 * @param bottom maximum y coordinate of bounding box of this block
	 */
	public Heading(String content,int fontsize,boolean noStart,boolean noEnd,int left,int right,int top,int bottom){
		super(content,noStart,noEnd,left,right,top,bottom);
		this.fontsize=fontsize;
		checkStyle();
	}
	/**
	 * Gauss level of the heading
	 */
	void checkStyle(){
		for(int i=0;i<knownStyle.length;i++)
			if(knownStyle[i].matcher(content).matches()){
				style=corrLevel[i];
				return;
			}
		style=-1;
	}
	/**
	 * Check the level
	 * @return the level
	 */
	public int getLevel(){
		return style;
	}
	/**
	 * Get the font size being used
	 * @return the font size
	 */
	public int getFontSize(){
		return fontsize;
	}
	/**
	 * Compare level with another Heading
	 * @param lst another Heading
	 * @return 0 if at the same level, otherwise positive if this has lower level
	 */
	public int compareLevel(Heading h){
		if(style!=-1&&h.style!=-1)
			return style-h.style;
		if(fontsize>h.fontsize*0.8&&h.fontsize>fontsize*0.8)
			return 0;
		return h.fontsize-fontsize;
	}
	/**
	 * Get the level name in LaTeX
	 * @param level the level
	 * @return the name
	 */
	public String getLevelName(int level){
		if(level>4)
			level=4;
		else if(level<0)
			level=0;
		return levelName[level]+(style==-1?"*":"");
	}
	public boolean isNoStart(){
		return false;
	}
	public boolean isNoEnd(){
		return false;
	}
	/**
	 * Get the content without label
	 * @return content without label
	 */
	public String getContentNoPrefix(){
		String content=getContent();
		for(int i=0;i<knownStyle.length;i++){
			Matcher matcher=knownStyle[i].matcher(content);
			if(matcher.matches())
				return matcher.group(1);
		}
		return content;
	}
}