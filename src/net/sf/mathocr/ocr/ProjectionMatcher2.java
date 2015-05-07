/* ProjectionMatcher2.java
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
import net.sf.mathocr.common.*;
/**
 * Matcher using horizontal projection
 */
public final class ProjectionMatcher2 extends CorrelationMatcher{
	/**
	 * Construct a ProjectionMatcher2
	 */
	public ProjectionMatcher2(){
	}
	/**
	 * Get horizontal projection to be matched
	 * @param ele to be matched
	 * @return horizontal projection
	 */
	public byte[] getCharacteristic(ConnectedComponent ele){
		int top=ele.getTop();
		byte[] pro=new byte[ele.getHeight()+1];
		for(RunLength rl:ele.getRunLengths())
			pro[rl.getY()-top]+=rl.getCount()+1;
		pro[pro.length-1]=(byte)ele.getWidth();
		return pro;
	}
	/**
	 * Get horizontal projection to be matched
	 * @param ele known glyph
	 * @return horizontal projection
	 */
	public byte[] getCharacteristic(Glyph ele){
		return getCharacteristic(ele.getConnectedComponent());
	}
}