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
/**
 *
 * @author Chan Chung Kwong
 */
public class UnderOver extends Span{
	private final Span content, under, over;
	/**
	 * Create a hated expression
	 *
	 * @param content
	 * @param under
	 */
	public UnderOver(Span content,Span under,Span over){
		super(BoundBox.union(content.getBox(),under.getBox()),content.getBaseLine());
		this.content=content;
		this.under=under;
		this.over=over;
	}
	/**
	 *
	 * @return under
	 */
	public Span getUnder(){
		return under;
	}
	/**
	 *
	 * @return over
	 */
	public Span getOver(){
		return over;
	}
	/**
	 * @return content
	 */
	public Span getContent(){
		return content;
	}
	@Override
	public String toString(){
		return "("+content.toString()+")_["+under.toString()+"]"+")^["+over.toString()+"]";
	}
	@Override
	public boolean isBaseLineReliable(){
		return content.isBaseLineReliable();
	}
	@Override
	public int getFontSize(){
		return content.getFontSize();
	}
}
