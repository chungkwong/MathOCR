/* RunLength.java
 * =========================================================================
 * This file is originally part of the MathOCR Project
 *
 * Copyright (C) 2014,2015 Chan Chung Kwong
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 */
package com.github.chungkwong.mathocr.common;
/**
 * A data structure represent runlength of a image
 */
public final class RunLength implements Comparable<RunLength>{
	int x, y, count;
	/**
	 * Construct a RunLength
	 *
	 * @param y the y coordinate
	 * @param x the x coordinate
	 * @param count the length of the RunLength - 1
	 */
	public RunLength(int y,int x,int count){
		this.y=y;
		this.x=x;
		this.count=count;
	}
	/**
	 * Get the x coordinate
	 *
	 * @return the x coordinate
	 */
	public int getX(){
		return x;
	}
	/**
	 * Get the y coordinate
	 *
	 * @return the y coordinate
	 */
	public int getY(){
		return y;
	}
	/**
	 * Get the length - 1
	 *
	 * @return length - 1
	 */
	public int getCount(){
		return count;
	}
	/**
	 * Reset a RunLength
	 *
	 * @param y the y coordinate
	 * @param x the x coordinate
	 * @param count the length of the RunLength - 1
	 */
	public void reset(int y,int x,int count){
		this.y=y;
		this.x=x;
		this.count=count;
	}
	/**
	 * Compare RunLength by y and x coordinate
	 *
	 * @param opd the RunLength to compare
	 * @return -1,0 or 1
	 */
	@Override
	public int compareTo(RunLength opd){
		if(y<opd.y||(y==opd.y&&x<opd.x)){
			return -1;
		}
		if(y==opd.y&&x==opd.x){
			return 0;
		}
		return 1;
	}
	@Override
	public String toString(){
		return y+":"+x+":"+count;
	}
}
