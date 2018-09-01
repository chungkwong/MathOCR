/* SubstackExpression.java
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
import java.util.*;
/**
 * A data structure representing a substack
 */
public final class SubstackExpression extends Expression{
	/**
	 * Construct a SubstackExpression
	 * @param lines the lines
	 */
	public SubstackExpression(List<Expression> lines){
		x=pleft=ptop=lleft=ltop=Integer.MAX_VALUE;
		y=pright=pbottom=lright=lbottom=0;
		notcustomized=false;
		baseLineFixed=false;
		scale=0;
		LaTeXString="";
		for(Expression expr:lines){
			pleft=Math.min(pleft,expr.getPhysicalLeft());
			ptop=Math.min(ptop,expr.getPhysicalTop());
			lleft=Math.min(lleft,expr.getLogicalLeft());
			ltop=Math.min(ltop,expr.getLogicalTop());
			pright=Math.max(pright,expr.getPhysicalRight());
			pbottom=Math.max(pbottom,expr.getPhysicalBottom());
			lright=Math.max(lright,expr.getLogicalRight());
			lbottom=Math.max(lbottom,expr.getLogicalBottom());
			x=Math.min(x,expr.getX());
			y=expr.getY();
			notcustomized|=expr.isNotCustomized();
			scale=Math.max(scale,expr.getScale());
			LaTeXString+=expr.toString()+"\\\\";
		}
		LaTeXString="\\substack{"+LaTeXString.substring(0,LaTeXString.length()-2)+"}";
	}
}