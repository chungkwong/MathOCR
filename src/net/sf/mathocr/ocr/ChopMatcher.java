/* ChopMatcher.java
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
import net.sf.mathocr.common.*;;
/**
 * Matcher using crossing feature
 */
public final class ChopMatcher extends StableMatcher<String>{
	/**
	 * Construct a ChopMatcher
	 */
	public ChopMatcher(){
	}
	/**
	 * Get the crossing feature of ele
	 * @param ele to be matched
	 * @return crossing feature
	 */
	public String getCharacteristic(ConnectedComponent ele){
		return ele.getHorizontalChar()+";"+ele.getVerticalChar();
	}
}