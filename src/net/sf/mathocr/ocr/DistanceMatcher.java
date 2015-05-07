/* DistanceMatcher.java
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
 * Abstract matcher based on distance
 */
public abstract class DistanceMatcher<T> implements Matcher{
	/**
	 * Get a characteristic to be matched
	 * @param ele to be matched
	 * @return the characteristic
	 */
	public abstract T getCharacteristic(ConnectedComponent ele);
	/**
	 * Get a characteristic to be matched
	 * @param ele known glyph
	 * @return the characteristic
	 */
	public abstract T getCharacteristic(Glyph ele);
	/**
	 * Calculate the distance between characteristic
	 * @param ele characteristic to be matched
	 * @param glyph another characteristic to be matched
	 * @return distance between the two characteristic
	 */
	public abstract double getDistance(T ele,T glyph);
	/**
	 * Remain only the glyphs that are likely match ele
	 * @param ele to be matched
	 * @param cand known glyphs
	 * @return remained glyphs
	 */
	public Map<Glyph,Double> gauss(ConnectedComponent ele,Map<Glyph,Double> cand){
		//if(cand.size()<=1)
		//	return cand;
		int i=0;
		T sample=getCharacteristic(ele);
		double max=0;
		for(Map.Entry<Glyph,Double> entry:cand.entrySet()){
			double cert=1.0-getDistance(sample,getCharacteristic(entry.getKey()));
			entry.setValue(cert);
			if(cert>max)
				max=cert;
		}
		Iterator<Map.Entry<Glyph,Double>> iter=cand.entrySet().iterator();
		while(iter.hasNext()){
			Map.Entry<Glyph,Double> dist=iter.next();
			if(dist.getValue()<max*0.9)
				iter.remove();
		}
		return cand;
	}
}