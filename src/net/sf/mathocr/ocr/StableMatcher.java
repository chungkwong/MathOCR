/* StableMatcher.java
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
 * Abstract matcher based on hash table
 */
public abstract class StableMatcher<T> implements Matcher{
	HashMap<T,Map<Glyph,Double>> table=new HashMap<T,Map<Glyph,Double>>();
	/**
	 * Add a sample
	 * @param key characteristic of glyph
	 * @param glyph known glyph
	 */
	public void addSample(T key,Glyph glyph){
		if(table.containsKey(key)){
			table.get(key).put(glyph,1.0);
		}else{
			Map<Glyph,Double> list=new HashMap<Glyph,Double>();
			list.put(glyph,1.0);
			table.put(key,list);
		}
	}
	/**
	 * Get a characteristic to be matched
	 * @param ele to be matched
	 * @return the characteristic
	 */
	public abstract T getCharacteristic(ConnectedComponent ele);
	/**
	 * Remain only the glyphs that are likely match ele
	 * @param ele to be matched
	 * @param cand known glyphs
	 * @return remained glyphs
	 */
	public Map<Glyph,Double> gauss(ConnectedComponent ele,Map<Glyph,Double> cand){
		T ch=getCharacteristic(ele);
		Map<Glyph,Double> list=table.get(ch);
		if(list==null)
			return new HashMap<Glyph,Double>();
		list=new HashMap<Glyph,Double>(list);
		Iterator<Map.Entry<Glyph,Double>> iter=list.entrySet().iterator();
		while(iter.hasNext()){
			Map.Entry<Glyph,Double> dist=iter.next();
			if(!cand.containsKey(dist.getKey()))
				iter.remove();
		}
		return list;
	}
}