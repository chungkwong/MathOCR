/* FractionExpression.java
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
package net.sf.mathocr.ofr;
/**
 * A data structure representing a fraction
 */
public final class FractionExpression extends Expression{
	/**
	 * Construct a FractionExpression
	 * @param line the fraction line
	 * @param numerator the numerator of the fraction
	 * @param denominator the denominator
	 */
	public FractionExpression(Expression line,Expression numerator,Expression denominator){
		pleft=line.getPhysicalLeft();
		pright=line.getPhysicalRight();
		ptop=numerator.getPhysicalTop();
		pbottom=denominator.getPhysicalBottom();
		lleft=pleft;
		lright=pright;
		ltop=numerator.getLogicalTop();
		lbottom=denominator.getLogicalBottom();
		x=pleft;
		y=line.getPhysicalBottom();
		sub=denominator.getSubscriptBound();
		sup=numerator.getSuperscriptBound();
		notcustomized=numerator.isNotCustomized()||denominator.isNotCustomized();
		baseLineFixed=false;
		scale=Math.max(numerator.getScale(),denominator.getScale());
		LaTeXString="\\frac{"+numerator.toString()+"}{"+denominator.toString()+"}";
	}
}