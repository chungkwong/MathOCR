/* RadicalExpression.java
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
 * A data structure representing a radical expression
 */
public final class RadicalExpression extends Expression{
	/**
	 * Construct a RadicalExpression
	 * @param sign the root sign
	 * @param ord the order(can be null)
	 * @param cont the content
	 */
	public RadicalExpression(Expression sign,Expression ord,Expression cont){
		pleft=ord==null?sign.getPhysicalLeft():Math.min(sign.getPhysicalLeft(),ord.getPhysicalLeft());
		pright=sign.getPhysicalRight();
		ptop=sign.getPhysicalTop();
		pbottom=sign.getPhysicalBottom();
		lleft=ord==null?sign.getPhysicalLeft():Math.min(sign.getPhysicalLeft(),ord.getLogicalLeft());
		lright=pright;
		ltop=ptop;
		lbottom=pbottom;
		x=cont.getX();
		y=cont.getY();
		sub=cont.getSubscriptBound();
		sup=cont.getSuperscriptBound();
		notcustomized=cont.isNotCustomized();
		baseLineFixed=cont.isBaseLineFixed();
		scale=cont.getScale();
		LaTeXString="\\sqrt"+(ord==null?"":"["+ord.toString()+"]")+"{"+cont.toString()+"}";
	}
}