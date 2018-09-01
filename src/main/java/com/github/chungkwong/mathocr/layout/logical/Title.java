/* Title.java
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
/**
 * A data structure representing title
 */
public final class Title extends TextBlock{
	/**
	 * Construct a Title
	 *
	 * @param textLike
	 */
	public Title(TextBlock textLike){
		super(textLike.getLines(),textLike.getIndent(),textLike.getFontSize(),false,false,textLike.getBox(),false);
	}
}
