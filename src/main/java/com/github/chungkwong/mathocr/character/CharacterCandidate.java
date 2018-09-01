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
package com.github.chungkwong.mathocr.character;
import com.github.chungkwong.mathocr.common.BoundBox;
/**
 *
 * @author Chan Chung Kwong
 */
public class CharacterCandidate implements Comparable<CharacterCandidate>{
	private final int codePoint;
	private final double score;
	private final BoundBox box;
	private final int baseLine;
	private final String family;
	private final int fontSize;
	private final int style;
	public CharacterCandidate(int codePoint,double score,BoundBox box,int baseLine,String family,int fontSize,int style){
		this.codePoint=codePoint;
		this.score=score;
		this.box=box;
		this.baseLine=baseLine;
		this.family=family;
		this.fontSize=fontSize;
		this.style=style;
	}
	public int getCodePoint(){
		return codePoint;
	}
	public int getBaseLine(){
		return baseLine;
	}
	public BoundBox getBox(){
		return box;
	}
	public String getFamily(){
		return family;
	}
	public int getFontSize(){
		return fontSize;
	}
	public int getStyle(){
		return style;
	}
	public double getScore(){
		return score;
	}
	@Override
	public int compareTo(CharacterCandidate o){
		return Double.compare(o.score,score);
	}
	@Override
	public String toString(){
		StringBuilder buf=new StringBuilder();
		buf.appendCodePoint(codePoint).append(' ');
		buf.append(score).append(' ');
		buf.append(family).append(' ');
		buf.append(fontSize).append(' ');
		buf.append(style);
		return buf.toString();
	}
}
