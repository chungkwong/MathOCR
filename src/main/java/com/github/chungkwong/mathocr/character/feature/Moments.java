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
import com.github.chungkwong.mathocr.common.ConnectedComponent;
import com.github.chungkwong.mathocr.character.VectorFeature;
/**
 * First order and second order moments
 *
 * @author Chan Chung Kwong
 */
public class Moments implements VectorFeature{
	public static final String NAME="MOMENTS";
	@Override
	public double[] extract(ConnectedComponent ele){
		return new double[]{ele.getCenterX(),ele.getCenterY(),ele.getCentralMoment(2,0),ele.getCentralMoment(1,1),ele.getCentralMoment(0,2)};
	}
	@Override
	public int getDimension(){
		return 5;
	}
}
