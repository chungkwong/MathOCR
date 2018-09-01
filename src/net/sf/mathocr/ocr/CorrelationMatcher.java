/* CorrlationMatcher.java
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
import java.util.*;
/**
 * Abstract matcher based on curve matching
 */
public abstract class CorrelationMatcher extends DistanceMatcher<byte[]>{
	/**
	 * Get a sequence to be matched
	 * @param ele to be matched
	 * @return a sequence
	 */
	public abstract byte[] getCharacteristic(ConnectedComponent ele);
	/**
	 * Get a sequence to be matched
	 * @param glyph known glyph
	 * @return a sequence
	 */
	public abstract byte[] getCharacteristic(Glyph glyph);
	/**
	 * Calculate the distance between two sequence
	 * @param tomatch a sequence to be matched
	 * @param glyph another sequence to be matched
	 * @return distance between the two sequence
	 */
	public double getDistance(byte[] tomatch,byte[] glyph){
		double diff=0;
		int len1=tomatch.length-1,len2=glyph.length-1;
		int width1=tomatch[len1],width2=glyph[len2];
		for(int i=0;i<len1;i++){
			int j=i*len2/len1;
			double t=(i+0.0)*len2/len1-j;
			double pt=(j!=len2-1?(1-t)*glyph[j]+t*glyph[j+1]:glyph[j])*width1/width2;
			diff+=Math.abs(tomatch[i]-pt);
		}
		return diff/width1/len1;
	}
}