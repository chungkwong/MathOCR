/*
 * Copyright (C) 2018 Chan Chung Kwong
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.chungkwong.mathocr.text.structure;
import com.github.chungkwong.mathocr.common.BoundBox;
import java.util.*;
import java.util.stream.*;
/**
 * Span lying horizontally
 *
 * @author Chan Chung Kwong
 */
public class Line extends Span{
	private final List<Span> spans;
	/**
	 * Create a line
	 *
	 * @param spans spans
	 */
	public Line(List<Span> spans){
		super(BoundBox.union(spans.stream().map((span)->span.getBox()).toArray(BoundBox[]::new)),
				calculateBaseline(spans));
		this.spans=spans;
	}
	private static int calculateBaseline(List<Span> spans){
		return (int)spans.stream().filter((span)->span.isBaseLineReliable()&&!(span instanceof Script)).
				mapToInt((span)->span.getBaseLine()).average().orElseGet(()
				->spans.stream().filter((span)->!(span instanceof Script)).mapToInt((span)->span.getBaseLine()).average().orElse(-1));
	}
	/**
	 * @return spans in the line
	 */
	public List<Span> getSpans(){
		return spans;
	}
	@Override
	public String toString(){
		return spans.stream().map((s)->s.toString()).collect(Collectors.joining());
	}
	/**
	 * Get unformatted lines
	 *
	 * @param text content of lines
	 * @return lines
	 */
	public static List<Line> fromText(String text){
		String[] lines=text.split("\\r?\\n|\\r");
		return Arrays.stream(lines).map((line)->fromLine(line,null)).collect(Collectors.toList());
	}
	/**
	 * Get unformatted line
	 *
	 * @param line content of the line
	 * @param box bounding box
	 * @return a line
	 */
	public static Line fromLine(CharSequence line,BoundBox box){
		return new Line(line.codePoints().mapToObj((c)->Symbol.fromCodePoint(c,box)).collect(Collectors.toList()));
	}
	@Override
	public boolean isBaseLineReliable(){
		return spans.stream().anyMatch((span)->span.isBaseLineReliable());
	}
	@Override
	public int getFontSize(){
		return median(spans.stream().mapToInt((span)->span.getFontSize()).toArray());
	}
	private int median(int... num){
		if(num.length==0){
			return Symbol.DEFAULT_SIZE;
		}
		Arrays.sort(num);
		return num[num.length/2];
	}
}
