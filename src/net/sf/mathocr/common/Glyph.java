/* Glyph.java
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
package net.sf.mathocr.common;
import java.util.*;
/**
 * A data structure representing glyph
 */
public final class Glyph{
	Symbol symbol;
	String chop;
	int dx,dy;
	float density;
	float[] moments;
	int numberOfHoles;
	float[] grid;
	ConnectedComponent ele;
	/**
	 * Construct a glyph
	 * @param symbol the symbol that this glyph belong to
	 * @param chop the chops
	 * @param dx horizontal offset from physical left of the symbol
	 * @param dy vertical offset from physical top of the symbol
	 * @param density the density
	 * @param moments some low order moments
	 * @param numberOfHoles the number of holes
	 * @param grid the grid characteristic
	 * @param ele the template
	 */
	public Glyph(Symbol symbol,String chop,int dx,int dy,float density,float[] moments,int numberOfHoles,float[] grid,ConnectedComponent ele){
		this.symbol=symbol;
		this.chop=chop;
		this.dx=dx;
		this.dy=dy;
		this.density=density;
		this.moments=moments;
		this.numberOfHoles=numberOfHoles;
		this.grid=grid;
		this.ele=ele;
	}
	/**
	 * Get the crossing feature
	 * @return the crossing feature
	 */
	public String getChop(){
		return chop;
	}
	/**
	 * Get horizontal offset from left of the symbol
	 */
	public int getXOffset(){
		return dx;
	}
	/**
	 * Get vertical offset from left of the symbol
	 */
	public int getYOffset(){
		return dy;
	}
	/**
	 * Get the width
	 * @return width
	 */
	public int getWidth(){
		return ele.getWidth();
	}
	/**
	 * Get the height
	 * @return height
	 */
	public int getHeight(){
		return ele.getHeight();
	}
	/**
	 * Get some low order moments
	 * @return the moments
	 */
	public float[] getMoments(){
		return moments;
	}
	/**
	 * Get the density
	 * @return the density
	 */
	public float getDensity(){
		return density;
	}
	/**
	 * Get symbol that the glyph belong to
	 *
	 * @return the symbol
	 */
	public Symbol getSymbol(){
		return symbol;
	}
	/**
	 * Get the number of holes
	 * @return the number of holes
	 */
	public int getNumberOfHoles(){
		return numberOfHoles;
	}
	/**
	 * Get the grid characteristic
	 * @return the grid characteristic
	 */
	public float[] getGrid(){
		return grid;
	}
	/**
	 * Get the template
	 * @return template
	 */
	public ConnectedComponent getConnectedComponent(){
		return ele;
	}
	/**
	 * Get the name of the symbol the glyph belong to
	 * @return the name
	 */
	public String toString(){
		return symbol.toString();
	}
}