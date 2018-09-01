/* AspectRatioMatcher.java
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
 * Matcher using aspect ratio
 */
public final class AspectRatioMatcher implements Matcher{
	/**
	 * Remain only the glyphs that match with ele
	 * @param ele the ConnectedElement to be matched
	 * @param cand known glyph
	 * @return glyphs which match
	 */
	public Map<Glyph,Double> gauss(ConnectedComponent ele,Map<Glyph,Double> cand){
		Iterator<Map.Entry<Glyph,Double>> iter=cand.entrySet().iterator();
		while(iter.hasNext()){
			Glyph g=iter.next().getKey();
			double ratio=(ele.getWidth()+0.0)*g.getHeight()/ele.getHeight()/g.getWidth();
			if(ratio>1.25||ratio<0.8)
				iter.remove();
		}
		return cand;
	}
}