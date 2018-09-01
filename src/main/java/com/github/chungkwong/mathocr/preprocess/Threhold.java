/* Threhold.java
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
 * A preprocessor used to threhold image using global threhold value
 */
public abstract class Threhold implements Preprocessor{
	@Override
	public boolean isApplicable(BufferedImage image){
		return image.getType()==BufferedImage.TYPE_BYTE_GRAY
				||image.getType()==BufferedImage.TYPE_USHORT_GRAY;
	}
	/*
	 * Threhold the image
	 * @param image the input image
	 * @return the threholded image
	 */
	@Override
	public BufferedImage apply(BufferedImage image,boolean inplace){
		if(image.getType()==BufferedImage.TYPE_BYTE_BINARY){
			return image;
		}
		int width=image.getWidth(), height=image.getHeight(), len=width*height;
		int[] pixels=image.getRGB(0,0,width,height,null,0,width);
		for(int i=0;i<pixels.length;i++){
			pixels[i]&=0xff;
		}
		int lim=getThrehold(pixels);
		for(int i=0;i<pixels.length;i++){
			pixels[i]=pixels[i]<=lim?0xff000000:0xffffffff;
		}
		image=new BufferedImage(width,height,BufferedImage.TYPE_BYTE_BINARY);
		image.setRGB(0,0,width,height,pixels,0,width);
		return image;
	}
	/*
	 * Get the threhold value
	 * @param pixels pixel array of the input image
	 * @return threhold value
	 */
	protected abstract int getThrehold(int[] pixels);
}
