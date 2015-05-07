/* LogicalBlock.java
 * =========================================================================
 * This file is originally part of the MathOCR Project
 *
 * Copyright (C) 2015 Chan Chung Kwong
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 */
package net.sf.mathocr.layout;
/**
 * A interface representing logical block
 */
public interface LogicalBlock{
	/**
	 * Check if the block is floating
	 */
	public boolean isFloating();
	/**
	 * Get minimum x coordinate of bounding box of this block
	 * @return the coordinate
	 */
	public int getLeft();
	/**
	 * Get maximum x coordinate of bounding box of this block
	 * @return the coordinate
	 */
	public int getRight();
	/**
	 * Get minimum y coordinate of bounding box of this block
	 * @return the coordinate
	 */
	public int getTop();
	/**
	 * Get maximum y coordinate of bounding box of this block
	 * @return the coordinate
	 */
	public int getBottom();
}