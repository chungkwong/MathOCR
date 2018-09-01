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
public class CharacterPrototype{
	private final int codePoint;
	private final BoundBox box;
	private final String family;
	private final int fontSize;
	private final int style;
	public CharacterPrototype(int codePoint,BoundBox box,String family,int fontSize,int style){
		this.codePoint=codePoint;
		this.box=box;
		this.family=family;
		this.fontSize=(box.getWidth()>0||box.getHeight()>0)
				?fontSize*1024/(Math.max(box.getWidth(),box.getHeight())):1024;
		this.style=style;
	}
	CharacterPrototype(int codePoint,BoundBox box,String family,int fontSize,int style,boolean internal){
		this.codePoint=codePoint;
		this.box=box;
		this.family=family;
		this.fontSize=fontSize;
		this.style=style;
	}
	public int getCodePoint(){
		return codePoint;
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
	public CharacterCandidate toCandidate(BoundBox component,double score){
		int baseline=component.getTop()-getBox().getTop()*component.getHeight()/getBox().getHeight();
		int fontsize=getFontSize()*Math.max(component.getWidth(),component.getHeight())/1024;
		return new CharacterCandidate(getCodePoint(),score,component,baseline,getFamily(),fontsize,getStyle());
	}
	@Override
	public boolean equals(Object obj){
		return obj instanceof CharacterPrototype&&((CharacterPrototype)obj).codePoint==codePoint;
	}
	@Override
	public int hashCode(){
		int hash=7;
		hash=89*hash+this.codePoint;
		return hash;
	}
}
