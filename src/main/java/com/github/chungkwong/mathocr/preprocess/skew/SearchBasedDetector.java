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
import java.awt.image.*;
/**
 * Search based skew detector
 *
 * @author Chan Chung Kwong
 */
public abstract class SearchBasedDetector implements SkewDetector{
	private final SearchStrategy strategy;
	/**
	 * Create a skew detector search strategy
	 *
	 * @param strategy
	 */
	public SearchBasedDetector(SearchStrategy strategy){
		this.strategy=strategy;
	}
	@Override
	public double detect(BufferedImage image){
		int width=image.getWidth(), height=image.getHeight();
		int[] pixels=image.getRGB(0,0,width,height,null,0,width);
		return strategy.search((angle)->getCost(pixels,width,height,angle));
	}
	/**
	 * Calculate cost for a angle(lower is better)
	 *
	 * @param pixels pixel array
	 * @param width width of the bitmap
	 * @param height height of the bitmap
	 * @param theta angle
	 * @return the cost
	 */
	protected abstract double getCost(int[] pixels,int width,int height,double theta);
}
