/* MeanFilter.java
 * =========================================================================
 * This file is originally part of the MathOCR Project
 *
 * Copyright (C) 2014 Chan Chung Kwong
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
 * Mean filter
 */
public final class MeanFilter extends SimplePreprocessor{
	/**
	 * Construct a MeanFilter
	 */
	public MeanFilter(){
	}
	/**
	 * Filter a grayscale image by replacing a pixels with the mean value of pixels in 3 times 3 box centered at the pixels
	 * @param pixels the pixel array of the input grayscale image
	 * @param width the width of the image
	 * @param height the height of the image
	 * @return the pixel array of the filtered image
	 */
	public int[] preprocess(int[] pixels,int width,int height){
		int[] prev=new int[width];
		for(int i=0;i<width;i++)
			prev[i]=pixels[i]&0xff;
		for(int i=width;i<pixels.length;i++)
			pixels[i]&=0xff;
		for(int i=1,start=width;i<height-1;i++){
			int tmp=pixels[start],tmp2;
			pixels[start++]=0xff000000|(tmp<<16)|(tmp<<8)|tmp;
			for(int j=1;j<width-1;j++,start++){
				tmp2=((prev[j-1]+prev[j]+prev[j+1]+tmp+pixels[start]+pixels[start+1]+pixels[start+width-1]+pixels[start+width]+pixels[start+width+1])/9);
				prev[j-1]=tmp;
				tmp=pixels[start];
				pixels[start]=0xff000000|(tmp2<<16)|(tmp2<<8)|tmp2;
			}
			prev[width-2]=tmp;
			tmp=prev[width-1]=pixels[start];
			pixels[start++]=0xff000000|(tmp<<16)|(tmp<<8)|tmp;
		}
		for(int k=width*(height-1);k<pixels.length;k++){
			int tmp=pixels[k];
			pixels[k]=0xff000000|(tmp<<16)|(tmp<<8)|tmp;
		}
		return pixels;
	}
}