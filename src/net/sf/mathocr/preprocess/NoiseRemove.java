/* NoiseRemove.java
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
import net.sf.mathocr.common.*;
/**
 * A preprocessor that apply post-processing step of binarized image described in
 * Adaptive degraded document image binarization by B. Gatos , I. Pratikakis, S.J. Perantonis
 */
public final class NoiseRemove extends SimplePreprocessor{
	/**
	 * Construct a NoiseRemove
	 */
	public NoiseRemove(){

	}
	/**
	 * Perform preprocess operation
	 * @param pixels pixel array of the input image
	 * @param width width of the input image
	 * @param height height of the input image
	 */
	public int[] preprocess(int[] pixels,int width,int height){
		int lh=new ComponentPool(pixels,width,height).getAverageHeight();
		//System.out.println(lh);
		int n=lh*3/20,ksh=n*n/10,ksw=n*n/20,dx=n/4,dy=n/4,ksw1=n*n*7/20;
		int dl=(n+1)/2,dr=n/2,len=width*height;
		for(int i=0;i<len;i++)
			pixels[i]=1-(pixels[i]&0x1);
		long[][] intImg=ImageUtil.getIntegralImage(pixels,width,height);
		for(int i=0,ind=0;i<height;i++)
			for(int j=0;j<width;j++,ind++){
				if(pixels[ind]==1&&ImageUtil.windowValue(intImg,width,height,i,j,dl,dr)<ksh)
					pixels[ind]=0;
			}
		intImg=ImageUtil.getIntegralImage(pixels,width,height);
		for(int i=0,ind=0;i<height;i++)
			for(int j=0;j<width;j++,ind++)
				if(pixels[ind]==0&&ImageUtil.windowValue(intImg,width,height,i,j,dl,dr)>ksw
				&&Math.abs(ImageUtil.averageX(intImg,width,height,i,j,dl,dr)-j)<dx&&Math.abs(ImageUtil.averageY(intImg,width,height,i,j,dl,dr)-i)<dy)
					pixels[ind]=1;
		intImg=ImageUtil.getIntegralImage(pixels,width,height);
		for(int i=0,ind=0;i<height;i++)
			for(int j=0;j<width;j++,ind++){
				if(pixels[ind]==1||ImageUtil.windowValue(intImg,width,height,i,j,dl,dr)>ksw1)
					pixels[ind]=0xff000000;
				else
					pixels[ind]=0xffffffff;
			}
		return pixels;
	}
}