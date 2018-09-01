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
package com.github.chungkwong.mathocr.text;
import com.github.chungkwong.mathocr.layout.physical.PhysicalBlock;
import com.github.chungkwong.mathocr.common.BoundBox;
import com.github.chungkwong.mathocr.common.ConnectedComponent;
import java.util.*;
/**
 * Unrecognized text line
 *
 * @author Chan Chung Kwong
 */
public class TextLine extends PhysicalBlock{
	public static final int ALIGN_PAGE_CENTER=0, ALIGN_CENTER=1, ALIGN_LEFT=2, ALIGN_RIGHT=3, ALIGN_FULL=4;
	private final int alignment;
	/**
	 * Create a text line
	 *
	 * @param components contained in the line
	 * @param box the bounding box
	 * @param alignment alignment of the line
	 */
	public TextLine(List<ConnectedComponent> components,BoundBox box,int alignment){
		super(components,box);
		this.alignment=alignment;
	}
	/**
	 * @return alignment
	 */
	public int getAlignment(){
		return alignment;
	}
	@Override
	public TextLine splitVertically(int y){
		PhysicalBlock part=super.splitVertically(y);
		return new TextLine(part.getComponents(),part.getBox(),alignment);
	}
}
