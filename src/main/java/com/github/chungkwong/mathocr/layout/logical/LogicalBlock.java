/* LogicalBlock.java
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
import com.github.chungkwong.mathocr.common.BoundBox;
/**
 * A interface representing logical block
 */
public class LogicalBlock{
	private final BoundBox box;
	private final boolean floating;
	/**
	 * Create a logical block
	 *
	 * @param box
	 * @param floating
	 */
	public LogicalBlock(BoundBox box,boolean floating){
		this.box=box;
		this.floating=floating;
	}
	/**
	 * Check if the block is floating
	 *
	 * @return true if the block may be floating
	 */
	public boolean isFloating(){
		return floating;
	}
	/**
	 * Get the bounding box of the block
	 *
	 * @return the box
	 */
	public BoundBox getBox(){
		return box;
	}
}
