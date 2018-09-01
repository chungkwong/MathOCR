/* Char.java
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
import java.util.*;
import net.sf.mathocr.common.*;
/**
 * A data structure represent a character to be matched and recognition result
 */
public final class Char{
	ConnectedComponent ele;
	TreeSet<Candidate> cand=new TreeSet<Candidate>();
	/**
	 * Construct a Char
	 * @param ele the character to be matched
	 */
	public Char(ConnectedComponent ele){
		this.ele=ele;
	}
	/**
	 * Construct a Char
	 * @param sym recognition result
	 * @param left left bound
	 * @param right right bound
	 * @param top upper bound
	 * @param bottom lower bound
	 */
	public Char(Symbol sym,int left,int right,int top,int bottom){
		cand.add(new Candidate(sym,1.0));
		ele=new ConnectedComponent(left,right,top,bottom);
	}
	/**
	 * Get the character to be matched
	 * @return character
	 */
	public ConnectedComponent getComponent(){
		return ele;
	}
	/**
	 * Get the left bound of the character
	 * @return bound
	 */
	public int getLeft(){
		return ele.getLeft();
	}
	/**
	 * Get the right bound of the character
	 * @return bound
	 */
	public int getRight(){
		return ele.getRight();
	}
	/**
	 * Get the upper bound of the character
	 * @return bound
	 */
	public int getTop(){
		return ele.getTop();
	}
	/**
	 * Get the lower bound of the character
	 * @return bound
	 */
	public int getBottom(){
		return ele.getBottom();
	}
	/**
	 * Get the physical width of the character
	 * @return width
	 */
	public int getWidth(){
		return ele.getWidth();
	}
	/**
	 * Get the physical height of the character
	 * @return height
	 */
	public int getHeight(){
		return ele.getHeight();
	}
	/**
	 * Get all the candidate for the character
	 * @return the candidates
	 */
	public TreeSet<Candidate> getCandidates(){
		return cand;
	}
	/**
	 * Show all candidates in text form
	 * @return text
	 */
	public String toString(){
		StringBuilder str=new StringBuilder();
		for(Candidate c:cand){
			str.append(c.getSymbol().toString());
			str.append('(');
			str.append(c.getCertainty());
			str.append(')');
			str.append(',');
		}
		return str.toString();
	}
}