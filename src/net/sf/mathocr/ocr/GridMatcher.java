/* GridMatcher.java
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
package net.sf.mathocr.ocr;
import net.sf.mathocr.common.*;
/**
 * A matcher based on grid characteristic
 */
public final class GridMatcher extends MSEMatcher{
	/**
	 * Construct a GridMatcher
	 */
	public GridMatcher(){
		numberOfVariables=9;
	}
	/**
	 * Get a grid characteristic to be matched
	 * @param ele to be matched
	 * @return the grid characteristic
	 */
	public float[] getCharacteristic(ConnectedComponent ele){
		return ele.getGrid();
	}
	/**
	 * Get a grid characteristic to be matched
	 * @param ele known glyph
	 * @return the grid characteristic
	 */
	public float[] getCharacteristic(Glyph ele){
		return ele.getGrid();
	}
}