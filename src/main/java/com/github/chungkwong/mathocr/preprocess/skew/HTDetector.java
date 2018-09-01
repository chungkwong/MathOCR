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
 * Detect skew by Hough transform
 *
 * @author Chan Chung Kwong
 */
public class HTDetector extends SearchBasedDetector{
	/**
	 * Create a skew detector
	 *
	 * @param strategy
	 */
	public HTDetector(SearchStrategy strategy){
		super(strategy);
	}
	@Override
	protected double getCost(int[] pixels,int width,int height,double theta){
		double c=Math.cos(Math.PI/2-theta), s=Math.sin(Math.PI/2-theta);
		int[] acc=new int[((int)Math.hypot(width,height)+1)/20];
		for(int i=0, ind=0;i<height;++i){
			for(int j=0;j<width;++j,++ind){
				if(pixels[ind]!=0xffffffff){
					++acc[(int)(Math.abs(j*c-i*s))/20];
				}
			}
		}
		double sum=0, sqsum=0;
		int count=0;
		for(int i=0;i<acc.length;i++){
			if(acc[i]!=0){
				sum+=acc[i];
				sqsum+=acc[i]*acc[i];
				count=i;
			}
		}
		/*int max=0;
		for(int p=0;p<acc.length;p++)
			if(acc[p]>max)
				max=acc[p];*/
		return -(sqsum-sum*sum/count);
	}
}
