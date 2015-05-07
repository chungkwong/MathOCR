/* CombinedMatcher.java
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
import java.util.*;
/**
 * A matcher containing some matchers
 */
public final class CombinedMatcher implements Matcher{
	List<Matcher> matchers;
	public CombinedMatcher(List<Matcher> matchers){
		this.matchers=matchers;
	}
   /**
	 * Remain only the glyphs that are likely match ele
	 * @param ele to be matched
	 * @param cand known glyphs
	 * @return remained glyphs
	 */
	public Map<Glyph,Double> gauss(ConnectedComponent ele,Map<Glyph,Double> cand){
		for(Matcher matcher:matchers){
			cand=matcher.gauss(ele,cand);
		}
		return cand;
	}
	/**
	 * Get all the matchers that make up the Combined Matcher
	 * @return the matchers
	 */
	public List<Matcher> getMatchers(){
		return matchers;
	}
}