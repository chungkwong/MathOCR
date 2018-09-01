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
 * Detect skew by Piecewise covering by parallelograms
 *
 * @author Chan Chung Kwong
 */
public class PCPDetector extends SearchBasedDetector{
	/**
	 * Create a skew detector
	 *
	 * @param strategy
	 */
	public PCPDetector(SearchStrategy strategy){
		super(strategy);
	}
	@Override
	protected double getCost(int[] pixels,int width,int height,double theta){
		int len=width*height, area=0, dx=width/20;
		double k=Math.tan(theta);
		int[] offset=new int[width];
		for(int j=0;j<width;j++){
			offset[j]=(int)(j*k+0.5);
		}
		for(int i=0;i<height;i++){
			int count=0, next=dx-1;
			for(int j=0;j<width;j++){
				int ind=(i+offset[j])*width+j;
				if(ind>=0&&ind<len&&pixels[ind]!=0xffffffff){
					++count;
				}
				if(j==next){
					if(count>0){
						area+=dx;
						count=0;
					}
					next+=dx;
				}else if(j==width-1&&count>0){
					area+=width%dx;
					count=0;
				}
			}
		}
		int row=k>0?0:height-1;
		for(int i=0;i<width;i++){
			int count=0, next=i+dx-1;
			for(int j=i, l=0;j<width;j++,l++){
				int ind=(row+offset[l])*width+j;
				if(ind>=0&&ind<len&&pixels[ind]!=0xffffffff){
					++count;
				}
				if(j==next){
					if(count>0){
						area+=dx;
						count=0;
					}
					next+=dx;
				}else if(j==width-1&&count>0){
					area+=width%dx;
					count=0;
				}
			}
		}
		return -(len-area);
	}
}
