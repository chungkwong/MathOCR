/* Listing.java
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
 * A data structure representing listing item
 */
public final class Listing extends TextBlock{
	static final Pattern[] itemizeFormat=new Pattern[]{Pattern.compile("(\\-)\\s(.+)")};
	static final Pattern[] enumerateFormat=new Pattern[]{Pattern.compile("(\\d+)\\.\\s(.+)"),Pattern.compile("(\\([a-z]\\))\\s(.+)"),
		Pattern.compile("x{0,2}+(i|ii|iii|iv|v|vi|vii|viii|ix|x)\\.\\s(.+)")};
	int sty;
	/**
	 * Construct a Listing
	 *
	 * @param sty the style of the block
	 * @param textLike
	 */
	public Listing(int sty,TextBlock textLike){
		super(textLike.getLines(),textLike.getIndent(),textLike.getFontSize(),
				textLike.isNoStart(),textLike.isNoEnd(),textLike.getBox(),false);
		this.sty=sty;
	}
	/**
	 * Check the style of the listing
	 *
	 * @param entry the text
	 */
	public static int testItem(String entry){
		for(int i=0;i<itemizeFormat.length;i++){
			if(itemizeFormat[i].matcher(entry).matches()){
				return i+1;
			}
		}
		for(int i=0;i<enumerateFormat.length;i++){
			if(enumerateFormat[i].matcher(entry).matches()){
				return -i-1;
			}
		}
		return 0;
	}
	/**
	 * Compare level with another listing item
	 *
	 * @param lst another listing item
	 * @return 0 if at the same level, otherwise positive if this has more
	 * indent
	 */
	public int compareLevel(Listing lst){
		if(sty==lst.sty){
			return 0;
		}
		return getIndent()-lst.getIndent();
	}
	/**
	 * Check if the listing item is numbered
	 *
	 * @return
	 */
	public boolean isNumbered(){
		return sty<0;
	}
	/**
	 * Get the content without label
	 *
	 * @return content without label
	 */
	public List<Line> getContentNoPrefix(){
		String content=getBeginning();
		Matcher matcher=null;
		if(sty>0){
			matcher=itemizeFormat[sty-1].matcher(content);
		}else if(sty<0){
			matcher=enumerateFormat[-sty-1].matcher(content);
		}else{
			return getLines();
		}
		matcher.matches();
		int number=content.codePointCount(0,matcher.start(2));
		List<Line> lines=new ArrayList<>(getLines().size());
		lines.add(new Line(getLines().get(0).getSpans().stream().skip(number).collect(Collectors.toList())));
		lines.addAll(getLines().subList(1,getLines().size()));
		return lines;
	}
}
