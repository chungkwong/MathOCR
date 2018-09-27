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
package com.github.chungkwong.mathocr.character.feature;
import com.github.chungkwong.mathocr.character.*;
import com.github.chungkwong.mathocr.common.ConnectedComponent;
/**
 * Crossing number
 *
 * @author Chan Chung Kwong
 */
public class CrossNumber implements VectorFeature{
	public static final String NAME="CROSSING";
	@Override
	public double[] extract(ConnectedComponent component){
		double[] verticalCrossing=getMeanAndVar(component.getVerticalCrossing());
		double[] horizontalCrossing=getMeanAndVar(component.getHorizontalCrossing());
		return new double[]{verticalCrossing[0],verticalCrossing[1],horizontalCrossing[0],horizontalCrossing[1]};
	}
	private static double[] getMeanAndVar(byte[] data){
		int sum=0;
		int sqSum=0;
		for(byte b:data){
			sum+=b;
			sqSum+=b*b;
		}
		double mean=((double)sum)/data.length;
		double var=((double)sqSum)/data.length-mean*mean;
		return new double[]{mean/10,var/100};
	}
	@Override
	public int getDimension(){
		return 4;
	}
}
