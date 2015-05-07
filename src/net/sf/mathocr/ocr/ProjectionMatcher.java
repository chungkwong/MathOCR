/* ProjectionMatcher.java
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
 * Matcher using vertical projection
 */
public final class ProjectionMatcher extends CorrelationMatcher{
	/**
	 * Construct a ProjectionMatcher
	 */
	public ProjectionMatcher(){
	}
	/**
	 * Get vertical projection to be matched
	 * @param ele to be matched
	 * @return vertical projection
	 */
	public byte[] getCharacteristic(ConnectedComponent ele){
		int left=ele.getLeft();
		byte[] pro=new byte[ele.getWidth()+1];
		for(RunLength rl:ele.getRunLengths()){
			int j1=rl.getX()-left,j2=j1+rl.getCount();
			for(int j=j1;j<=j2;j++)
				++pro[j];
		}
		pro[pro.length-1]=(byte)ele.getHeight();
		return pro;
	}
	/**
	 * Get vertical projection to be matched
	 * @param ele known glyph
	 * @return vertical projection
	 */
	public byte[] getCharacteristic(Glyph ele){
		return getCharacteristic(ele.getConnectedComponent());
	}
}