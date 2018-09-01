/* MomentsMatcher.java
 * =========================================================================
 * This file is originally part of the MathOCR Project
 *
 * Copyright (C) 2014,2015 Chan Chung Kwong
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
import net.sf.mathocr.common.*;;
/**
 * Matcher using moments
 */
public final class MomentsMatcher extends MSEMatcher{
	/**
	 * Construct a MomentsMatcher
	 */
	public MomentsMatcher(){
		numberOfVariables=5;
	}
	/**
	 * Get a characteristic to be matched
	 * @param ele to be matched
	 * @return the characteristic
	 */
	public float[] getCharacteristic(ConnectedComponent ele){
		return new float[]{ele.getCenterX(),ele.getCenterY(),ele.getCentralMoment(2,0),ele.getCentralMoment(1,1),ele.getCentralMoment(0,2)};
	}
	/**
	 * Get a characteristic to be matched
	 * @param ele known glyph
	 * @return the characteristic
	 */
	public float[] getCharacteristic(Glyph ele){
		return ele.getMoments();
	}
}