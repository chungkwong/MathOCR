/* HatExpression.java
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
public final class HatExpression extends Expression{
	/**
	 * Construct a HatExpression
	 * @param hat the hat
	 * @param cont the content
	 */
	public HatExpression(Expression hat,Expression cont){
		pleft=Math.min(hat.getPhysicalLeft(),cont.getPhysicalLeft());
		pright=Math.max(hat.getPhysicalRight(),cont.getPhysicalRight());
		ptop=Math.min(hat.getPhysicalTop(),cont.getPhysicalTop());
		pbottom=Math.max(hat.getPhysicalBottom(),cont.getPhysicalBottom());
		lleft=cont.getLogicalLeft();
		lright=cont.getLogicalRight();
		ltop=cont.getLogicalTop();
		lbottom=cont.getLogicalBottom();
		x=cont.getX();
		y=cont.getY();
		sub=cont.getSubscriptBound();
		sup=cont.getSuperscriptBound();
		notcustomized=cont.isNotCustomized();
		baseLineFixed=cont.isBaseLineFixed();
		scale=cont.getScale();
		String nam=hat.toString();
		if(cont.toString().equals("\\underrightarrow{}"))
			LaTeXString="\\stackrel{"+nam+"}{\\rightarrow}";
		else
			LaTeXString=nam.substring(0,nam.length()-1)+cont.toString()+"}";
	}
}