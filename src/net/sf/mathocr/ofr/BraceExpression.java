/* BraceExpression.java
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
 * A data structure representing a expression with hat
 */
public final class BraceExpression extends Expression{
	/**
	 * Construct a BraceExpression
	 * @param brace the brace
	 * @param cont the content
	 * @param supp note
	 */
	public BraceExpression(Expression brace,Expression cont,Expression supp){
		pleft=Math.min(cont.getPhysicalLeft(),supp.getPhysicalLeft());
		pright=Math.max(cont.getPhysicalRight(),supp.getPhysicalRight());
		ptop=Math.min(cont.getPhysicalTop(),supp.getPhysicalTop());
		pbottom=Math.max(cont.getPhysicalBottom(),supp.getPhysicalBottom());
		lleft=Math.min(cont.getLogicalLeft(),supp.getLogicalLeft());
		lright=Math.max(cont.getLogicalRight(),supp.getLogicalRight());
		ltop=Math.min(cont.getLogicalTop(),supp.getLogicalTop());
		lbottom=Math.max(cont.getLogicalBottom(),supp.getLogicalBottom());
		x=cont.getX();
		y=cont.getY();
		sub=cont.getSubscriptBound();
		sup=cont.getSuperscriptBound();
		notcustomized=cont.isNotCustomized();
		baseLineFixed=cont.isBaseLineFixed();
		scale=cont.getScale();
		String nam=brace.toString();
		LaTeXString=nam.substring(0,nam.length()-2)+"["+supp.toString()+"]{"+cont.toString()+"}";
	}
}