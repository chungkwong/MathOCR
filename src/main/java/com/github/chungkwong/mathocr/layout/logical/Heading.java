/* Heading.java
 * =========================================================================
 * This file is originally part of the MathOCR Project
 *
 * Copyright (C) 2015 Chan Chung Kwong
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 */
package com.github.chungkwong.mathocr.layout.logical;
import com.github.chungkwong.mathocr.text.structure.Line;
import java.util.*;
import java.util.regex.*;
import java.util.stream.*;
/**
 * A data structure representing heading
 */
public final class Heading extends TextBlock{
	private static final String[] levelName=new String[]{"part","chapter","section","subsection","subsubsection"};
	public static final int LV_PART=0, LV_CHAPTER=1, LV_SECTION=2, LV_SUBSECTION=3, LV_SUBSUBSECTION=4;
	private static final Pattern[] knownStyle=new Pattern[]{Pattern.compile("[0-9]+\\s?\\.?\\s(.*)"),Pattern.compile("[0-9]+\\s?\\.\\s?[0-9]+\\.?\\s(.*)"),
		Pattern.compile("[0-9]+\\s?\\.\\s?[0-9]+\\s?\\.\\s?[0-9]+\\.?\\s(.*)"),Pattern.compile("Chapter\\s[0-9]+(.*)"),Pattern.compile("第\\s?[0-9]+\\s?章(.*)"),
		Pattern.compile("第\\s?[〇一二三四五六七八九十]+\\s?章")};
	private static final int[] corrLevel=new int[]{2,3,4,1,1,1};
	private final int style;
	/**
	 * Construct a Listing
	 *
	 * @param textLike
	 */
	public Heading(TextBlock textLike){
		super(textLike.getLines(),textLike.getIndent(),textLike.getFontSize(),
				false,false,textLike.getBox(),false);
		for(int i=0;i<knownStyle.length;i++){
			if(knownStyle[i].matcher(getBeginning()).matches()){
				style=corrLevel[i];
				return;
			}
		}
		style=-1;
	}
	/**
	 * Create a heading
	 *
	 * @param style
	 * @param textLike
	 */
	public Heading(int style,TextBlock textLike){
		super(textLike.getLines(),textLike.getIndent(),textLike.getFontSize(),
				false,false,textLike.getBox(),false);
		this.style=style;
	}
	/**
	 * Check the level
	 *
	 * @return the level
	 */
	public int getLevel(){
		return style;
	}
	/**
	 * Compare level with another Heading
	 *
	 * @param lst another Heading
	 * @return 0 if at the same level, otherwise positive if this has lower
	 * level
	 */
	public int compareLevel(Heading h){
		if(style!=-1&&h.style!=-1){
			return style-h.style;
		}
		if(getFontSize()>h.getFontSize()*0.8&&h.getFontSize()>getFontSize()*0.8){
			return 0;
		}
		return h.getFontSize()-getFontSize();
	}
	/**
	 * Get the level name in LaTeX
	 *
	 * @param level the level
	 * @return the name
	 */
	public String getLevelName(int level){
		if(level>4){
			level=4;
		}else if(level<0){
			level=0;
		}
		return levelName[level]+(style==-1?"*":"");
	}
	/**
	 * Get the content without label
	 *
	 * @return content without label
	 */
	public List<Line> getContentNoPrefix(){
		for(int i=0;i<knownStyle.length;i++){
			Matcher matcher=knownStyle[i].matcher(getBeginning());
			if(matcher.matches()){
				long number=matcher.group(1).codePoints().count();
				List<Line> lines=new ArrayList<>(getLines().size());
				lines.add(new Line(getLines().get(0).getSpans().stream().skip(number).collect(Collectors.toList())));
				lines.addAll(getLines().subList(1,getLines().size()));
				return lines;
			}
		}
		return getLines();
	}
}
