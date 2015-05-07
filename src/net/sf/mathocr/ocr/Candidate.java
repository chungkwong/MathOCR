/* Candidate.java
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
 * A data structure represent a candidate
 */
public final class Candidate implements Comparable<Candidate>{
	Symbol sym;
	double cert,score;
	List<Integer> otherIndex;
	/**
	 * Construct a candidate
	 * @param sym recognition result
	 * @param cert corresponsing certainty,should between 0 and 1
	 */
	public Candidate(Symbol sym,double cert){
		this.sym=sym;
		this.cert=cert;
		renewScore();
	}
	/**
	 * Construct a candidate
	 * @param sym recognition result
	 * @param cert corresponsing certainty,should between 0 and 1
	 * @param otherIndex the indices belong to the same character
	 */
	public Candidate(Symbol sym,double cert,List<Integer> otherIndex){
		this.sym=sym;
		this.cert=cert;
		this.otherIndex=otherIndex;
		renewScore();
	}
	void renewScore(){
		score=cert*0.895+sym.getGlyphs().size()*0.02;
		//score=cert*0.995;
		if(!sym.isVarSize())
			score+=0.005;
	}
	/**
	 * Get the indices belong to the same character
	 * @return indices
	 */
	public List<Integer> getOtherIndex(){
		return otherIndex;
	}
	/**
	 * Set the recognition result
	 * @return the new recognition result
	 */
	public void setSymbol(Symbol sym){
		this.sym=sym;
	}
	/**
	 * Get the recognition result
	 * @return the result
	 */
	public Symbol getSymbol(){
		return sym;
	}
	/**
	 * Get the certainty
	 * @return certainty
	 */
	public double getCertainty(){
		return cert;
	}
	/**
	 * Get the score being used to sort Candidate
	 * @return the score
	 */
	public double getScore(){
		return score;
	}
	public int compareTo(Candidate cand){
		double cmp=cand.getScore()-getScore();
		if(cmp<0)
			return -1;
		else if(cmp>0)
			return 1;
		return 0;
	}
}