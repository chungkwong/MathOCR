/* Caption.java
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
 * A data structure representing caption
 */
public final class Caption extends TextBlock{
	static final Pattern[] knownStyle=new Pattern[]{Pattern.compile("图\\s[0-9]:?(.*)"),Pattern.compile("表\\s[0-9]:?(.*)"),
		Pattern.compile("Fig\\.[0-9](.*)"),Pattern.compile("Figure\\s[0-9]:?(.*)"),Pattern.compile("Table\\s[0-9]:?(.*)")};
	/**
	 * Construct a Listing
	 *
	 * @param textLike
	 */
	public Caption(TextBlock textLike){
		super(textLike.getLines(),textLike.getIndent(),textLike.getFontSize(),
				textLike.isNoStart(),textLike.isNoEnd(),textLike.getBox(),true);
	}
	/**
	 * Check if a String indicate a caption
	 *
	 * @param str to be checked
	 * @return result
	 */
	public static boolean isCaption(String str){
		for(int i=0;i<knownStyle.length;i++){
			if(knownStyle[i].matcher(str).matches()){
				return true;
			}
		}
		return false;
	}
	/**
	 * Get content without caption prefix
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
