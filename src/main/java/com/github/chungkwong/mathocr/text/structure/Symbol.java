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
import com.github.chungkwong.mathocr.character.CharacterCandidate;
import com.github.chungkwong.mathocr.text.structure.Symbol;
/**
 * Symbol
 *
 * @author Chan Chung Kwong
 */
public class Symbol extends Span{
	public static final int DEFAULT_STYLE=0;
	public static final int DEFAULT_SIZE=12;
	public static final String DEFAULT_FAMILY="Serif";
	private final int codePoint;
	private final String family;
	private final int fontsize;
	private final int style;
	/**
	 * Create a symbol
	 *
	 * @param candidate
	 */
	public Symbol(CharacterCandidate candidate){
		super(candidate.getBox(),candidate.getBaseLine());
		this.codePoint=candidate.getCodePoint();
		this.family=candidate.getFamily();
		this.fontsize=candidate.getFontSize();
		this.style=candidate.getStyle();
	}
	public Symbol(int codePoint,String family,int fontsize,int style,BoundBox box,int baseline){
		super(box,baseline);
		this.codePoint=codePoint;
		this.family=family;
		this.fontsize=fontsize;
		this.style=style;
	}
	/**
	 * @return Unicode code point
	 */
	public int getCodePoint(){
		return codePoint;
	}
	/**
	 * @return Font family
	 */
	public String getFamily(){
		return family;
	}
	/**
	 *
	 * @return Font size
	 */
	@Override
	public int getFontSize(){
		return fontsize;
	}
	/**
	 * @return Font style
	 */
	public int getStyle(){
		return style;
	}
	@Override
	public String toString(){
		return new String(new int[]{codePoint},0,1);
	}
	/**
	 * Get unformatted character
	 *
	 * @param codePoint
	 * @param box
	 * @return a line
	 */
	public static Symbol fromCodePoint(int codePoint,BoundBox box){
		return new Symbol(codePoint,DEFAULT_FAMILY,DEFAULT_SIZE,DEFAULT_STYLE,box,box.getBottom());
	}
	@Override
	public boolean isBaseLineReliable(){
		return true;
	}
}
