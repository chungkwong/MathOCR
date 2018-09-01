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
 * Linear search for angle
 *
 * @author Chan Chung Kwong
 */
public class LinearStrategy implements SearchStrategy{
	private final double lower, upper, step;
	/**
	 * Define a linear search
	 *
	 * @param lower lower bound of search space
	 * @param upper upper bound of search space
	 * @param step gap between two angles to be searched
	 */
	public LinearStrategy(double lower,double upper,double step){
		this.lower=lower;
		this.upper=upper;
		this.step=step;
	}
	@Override
	public double search(DoubleFunction<Double> cost){
		double bestCost=Double.MAX_VALUE;
		double bestAngle=0;
		for(double angle=lower;angle<upper;angle+=step){
			double c=cost.apply(angle);
			if(c<bestCost){
				bestCost=c;
				bestAngle=angle;
			}
		}
		return bestAngle;
	}
}
