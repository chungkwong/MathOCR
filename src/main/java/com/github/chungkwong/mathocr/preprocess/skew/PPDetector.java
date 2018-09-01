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
/**
 * Detect skew by projection profile
 *
 * @author Chan Chung Kwong
 */
public class PPDetector extends SearchBasedDetector{
	/**
	 * Create a skew detector
	 *
	 * @param strategy
	 */
	public PPDetector(SearchStrategy strategy){
		super(strategy);
	}
	@Override
	protected double getCost(int[] pixels,int width,int height,double theta){
		int len=width*height;
		double k=Math.tan(theta), sum=0, sqsum=0;
		int[] offset=new int[width];
		for(int j=0;j<width;j++){
			offset[j]=(int)(j*k+0.5);
		}
		for(int i=0;i<height;i++){
			int count=0;
			for(int j=0;j<width;j++){
				int ind=(i+offset[j])*width+j;
				if(ind>=0&&ind<len&&pixels[ind]==0xff000000){
					++count;
				}
			}
			sum+=count;
			sqsum+=count*count;
		}
		return -(sqsum-sum*sum/height)/height;
	}
}
