/* MedianFilter.java
 * =========================================================================
 * This file is originally part of the MathOCR Project
 *
 * Copyright (C) 2014,2015 Chan Chung Kwong
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 */
package net.sf.mathocr.preprocess;
/**
 * Median filter
 */
public final class MedianFilter extends SimplePreprocessor{
	/**
	 * Construct a MedianFilter
	 */
	public MedianFilter(){
	}
	/**
	 * Filter a grayscale image by replacing a pixels with the median value of pixels in 3 times 3 box centered at the pixels
	 * @param pixels the pixel array of the input grayscale image
	 * @param width the width of the image
	 * @param height the height of the image
	 * @return the pixel array of the filtered image
	 */
	public int[] preprocess(int[] pixels,int width,int height){
		int[] prev=new int[width],sort=new int[9];
		for(int i=0;i<width;i++)
			prev[i]=pixels[i];
		for(int i=1,start=width;i<height-1;i++){
			int tmp=pixels[start],tmp2,tmp3;
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
				for(int k=0;k<5;k++)
					for(int l=k+1;l<9;l++)
						if(sort[l]<sort[k]){
							tmp3=sort[k];
							sort[k]=sort[l];
							sort[l]=tmp3;
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