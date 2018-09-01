/* ColorInvert.java
 * =========================================================================
 * This file is originally part of the MathOCR Project
 *
 * Copyright (C) 2014 Chan Chung Kwong
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
/**
 * A preprocessor used to invert color of the image(i.e. black to white and
 * white to black)
 */
public final class ColorInvert extends SimplePreprocessor{
	private final boolean autoDetect;
	/**
	 * Construct a ColorInvert
	 *
	 * @param autoDetect color will only be inverted when the image seem to be
	 * white on black if autoDetect is true, or else color will always be
	 * inverted
	 */
	public ColorInvert(boolean autoDetect){
		this.autoDetect=autoDetect;
	}
	/**
	 * Check if auto detection of white on black is enabled
	 *
	 * @return enabled or not
	 */
	public boolean isAutoDetect(){
		return autoDetect;
	}
	/**
	 * Check if a image seem to be white on black
	 *
	 * @param pixels the pixels array of the image
	 * @return test result
	 */
	public boolean checkWhiteOnBlack(int[] pixels){
		int count=0;
		for(int pix:pixels){
			if((pix&0xFF)<=0x80){
				++count;
			}
		}
		return count>pixels.length/2;
	}
	/**
	 * Perform preprocess operation
	 *
	 * @param pixels pixel array of the input image
	 * @param width width of the input image
	 * @param height height of the input image
	 * @return pixel array of processed image
	 */
	@Override
	public int[] preprocess(int[] pixels,int width,int height){
		if(!autoDetect||checkWhiteOnBlack(pixels)){
			for(int i=0;i<pixels.length;i++){
				pixels[i]=(~pixels[i])|0xff000000;
			}
		}
		return pixels;
	}
}
