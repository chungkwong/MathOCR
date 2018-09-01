/*
 * Copyright (C) 2018 Chan Chung Kwong
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.chungkwong.mathocr.preprocess.skew;
import java.util.function.*;
/**
 * Search for angle using finer scales
 *
 * @author Chan Chung Kwong
 */
public class HierarchyStrategy implements SearchStrategy{
	private final double lower, upper;
	private final double[] steps;
	/**
	 * Search [-15deg,15deg] using step 3deg and 0.2deg
	 */
	public HierarchyStrategy(){
		this(-Math.PI*15/180,Math.PI*15/180,Math.PI*2/180,Math.PI/10/180);
	}
	/**
	 * Define a search
	 *
	 * @param lower lower bound of search space
	 * @param upper upper bound of search space
	 * @param steps gaps between two angles to be searched
	 */
	public HierarchyStrategy(double lower,double upper,double... steps){
		this.lower=lower;
		this.upper=upper;
		this.steps=steps;
	}
	@Override
	public double search(DoubleFunction<Double> cost){
		double l=lower;
		double r=upper;
		double angle=(l+r)/2;
		for(double step:steps){
			angle=new LinearStrategy(l+step/2,r,step).search(cost);
			l=angle-step/2;
			r=angle+step/2;
		}
		return angle;
	}
}
