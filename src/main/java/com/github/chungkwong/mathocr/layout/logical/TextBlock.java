/* TextLike.java
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
import com.github.chungkwong.mathocr.common.BoundBox;
import java.util.*;
import java.util.stream.*;
/**
 * A data structure representing textual logical block
 */
public class TextBlock extends LogicalBlock{
	private boolean noStart, noEnd;
	private final List<Line> lines;
	private int indent, fontSize;
	/**
	 * Construct a TextLike
	 *
	 * @param lines content in the block
	 * @param indent indent
	 * @param fontsize font size
	 * @param noStart if the block is possibly continued from a previous block
	 * @param noEnd if the block is possibly to be continued
	 * @param box
	 * @param floating
	 */
	public TextBlock(List<Line> lines,int indent,int fontsize,boolean noStart,boolean noEnd,BoundBox box,boolean floating){
		super(box,floating);
		this.noStart=noStart;
		this.noEnd=noEnd;
		this.lines=lines;
		this.indent=indent;
		this.fontSize=fontsize;
	}
	/**
	 * Get the text content
	 *
	 * @return the content
	 */
	public List<Line> getLines(){
		return lines;
	}
	/**
	 * Check if the block is possibly continued from a previous block
	 *
	 * @return result
	 */
	public boolean isNoStart(){
		return noStart;
	}
	/**
	 * Check if the block is possibly to be continued
	 *
	 * @return result
	 */
	public boolean isNoEnd(){
		return noEnd;
	}
	/**
	 * @return indented amount
	 */
	public int getIndent(){
		return indent;
	}
	/**
	 * @return font size
	 */
	public int getFontSize(){
		return fontSize;
	}
	/**
	 *
	 * @param noEnd
	 */
	public void setNoEnd(boolean noEnd){
		this.noEnd=noEnd;
	}
	/**
	 *
	 * @param noStart
	 */
	public void setNoStart(boolean noStart){
		this.noStart=noStart;
	}
	String getBeginning(){
		return lines.isEmpty()?"":lines.get(0).toString();
	}
	String getEnding(){
		return lines.isEmpty()?"":lines.get(lines.size()-1).toString();
	}
	@Override
	public String toString(){
		return lines.stream().map((line)->line.toString()).collect(Collectors.joining("\n"));
	}
}
