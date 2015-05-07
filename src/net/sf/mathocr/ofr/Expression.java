/* Expression.java
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
 * A data structure representing a mathematical expression
 */
public class Expression implements Comparable<Expression>{
	protected int pleft,pright,ptop,pbottom,lleft,lright,ltop,lbottom,x,y,sub,sup;
	protected double scale;
	protected boolean baseLineFixed,notcustomized;
	protected String LaTeXString,textString;
	/**
	 * Get the physical left bound of the expression
	 * @return the physical left bound
	 */
	public int getPhysicalLeft(){
		return pleft;
	}
	/**
	 * Get the physical right bound of the expression
	 * @return the physical right bound
	 */
	public int getPhysicalRight(){
		return pright;
	}
	/**
	 * Get the physical upper bound of the expression
	 * @return the physical upper bound
	 */
	public int getPhysicalTop(){
		return ptop;
	}
	/**
	 * Get the physical lower bound of the expression
	 * @return the physical lower bound
	 */
	public int getPhysicalBottom(){
		return pbottom;
	}
	/**
	 * Get the physical width of the expression
	 * @return the physical width
	 */
	public int getPhysicalWidth(){
		return pright-pleft+1;
	}
	/**
	 * Get the physical height of the expression
	 * @return the physical height
	 */
	public int getPhysicalHeight(){
		return pbottom-ptop+1;
	}
	/**
	 * Get the logical left bound of the expression
	 * @return the logical left bound
	 */
	public int getLogicalLeft(){
		return lleft;
	}
	/**
	 * Get the logical right bound of the expression
	 * @return the logical right bound
	 */
	public int getLogicalRight(){
		return lright;
	}
	/**
	 * Get the logical upper bound of the expression
	 * @return the logical upper bound
	 */
	public int getLogicalTop(){
		return ltop;
	}
	/**
	 * Get the logical lower bound of the expression
	 * @return the logical lower bound
	 */
	public int getLogicalBottom(){
		return lbottom;
	}
	/**
	 * Get the logical width of the expression
	 * @return the logical width bound
	 */
	public int getLogicalWidth(){
		return lright-lleft+1;
	}
	/**
	 * Get the logical height of the expression
	 * @return the logical height
	 */
	public int getLogicalHeight(){
		return lbottom-ltop+1;
	}
	/**
	 * Get the x coordinate of the reference point
	 * @return x coordinate
	 */
	public int getX(){
		return x;
	}
	/**
	 * Get the y coordinate of the reference point
	 * @return y coordinate
	 */
	public int getY(){
		return y;
	}
	/**
	 * Get the y coordinate of the subscript line
	 * @return y coordinate
	 */
	public int getSubscriptBound(){
		return sub;
	}
	/**
	 * Get the y coordinate of the superscript line
	 * @return y coordinate
	 */
	public int getSuperscriptBound(){
		return sup;
	}
	/**
	 * Currently the same as getLogicalTop
	 */
	public int getUpper(){
		return ltop;
	}
	/**
	 * Currently the same as min(getPhysicalBottom,getY)
	 */
	public int getLower(){
		return Math.max(pbottom,y);
	}
	/**
	 * Check if the expression have a fixed base line
	 * @return resut
	 */
	public double getScale(){
		return scale;
	}
	/**
	 * Check if the expression have a fixed base line
	 * @return result
	 */
	public boolean isBaseLineFixed(){
		return baseLineFixed;
	}
	/**
	 * Check if the scale is reliable
	 * @return result
	 */
	public boolean isNotCustomized(){
		return notcustomized;
	}
	/**
	 * Check if the expression is a actually normal text
	 * @return result
	 */
	public boolean isText(){
		return textString!=null;
	}
	/**
	 * Check if the expression is not actually normal text
	 * @return result
	 */
	public boolean isMath(){
		return !LaTeXString.isEmpty();
	}
	/**
	 * Get text string
	 * @return the string
	 */
	public String getText(){
		return textString;
	}
	/**
	 * Get LaTeX string
	 * @return the string
	 */
	public String toString(){
		if(LaTeXString.isEmpty())
			return "\\text{"+textString+"}";
		return LaTeXString;
	}
	public int compareTo(Expression e){
		return lleft-e.lleft;
	}
}