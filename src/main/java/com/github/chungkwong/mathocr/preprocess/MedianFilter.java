/* MedianFilter.java
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
package com.github.chungkwong.mathocr.preprocess;
import java.awt.image.*;
/**
 * Median filter
 */
public final class MedianFilter extends SimplePreprocessor{
	/**
	 * Construct a MedianFilter
	 */
	public MedianFilter(){
	}
	@Override
	public boolean isApplicable(BufferedImage image){
		return image.getType()==BufferedImage.TYPE_BYTE_GRAY
				||image.getType()==BufferedImage.TYPE_USHORT_GRAY;
	}
	@Override
	public int[] preprocess(int[] pixels,int width,int height){
		int[] prev=new int[width], sort=new int[9];
		for(int i=0;i<width;i++){
			prev[i]=pixels[i];
		}
		for(int i=1, start=width;i<height-1;i++){
			int tmp=pixels[start], tmp2, tmp3;
			pixels[start++]=tmp;
			for(int j=1;j<width-1;j++,start++){
				sort[0]=prev[j-1];
				sort[1]=prev[j];
				sort[2]=prev[j+1];
				sort[3]=tmp;
				sort[4]=pixels[start];
				sort[5]=pixels[start+1];
				sort[6]=pixels[start+width-1];
				sort[7]=pixels[start+width];
				sort[8]=pixels[start+width+1];
				prev[j-1]=tmp;
				for(int k=0;k<5;k++){
					for(int l=k+1;l<9;l++){
						if(sort[l]<sort[k]){
							tmp3=sort[k];
							sort[k]=sort[l];
							sort[l]=tmp3;
						}
					}
				}
				tmp2=sort[4];
				prev[j-1]=tmp;
				tmp=pixels[start];
				pixels[start]=tmp2;
			}
			prev[width-2]=tmp;
			tmp=prev[width-1]=pixels[start];
			pixels[start++]=tmp;
		}
		return pixels;
	}
}
