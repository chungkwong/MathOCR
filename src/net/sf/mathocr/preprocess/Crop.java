/* Crop.java
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
import java.awt.image.*;
/**
 * A preprocessor being use to crop image
 */
public final class Crop implements Preprocessor{
	int left,right,top,bottom;
	/**
	 * Construct a Crop
	 * @param left minimum x-coordinate of bounding box of the area to be kept
	 * @param right maximum x-coordinate of bounding box of the area to be kept
	 * @param top minimum y-coordinate of bounding box of the area to be kept
	 * @param bottom maximum y-coordinate of bounding box of the area to be kept
	 */
	public Crop(int left,int right,int top,int bottom){
		this.left=Math.max(left,0);
		this.right=right;
		this.top=Math.max(top,0);
		this.bottom=bottom;
	}
	/**
    * Perform preprocess operation
    * @param image input image
    * @return processed image
    */
	public BufferedImage preprocess(BufferedImage image){
		return image.getSubimage(left,top,Math.min(right,image.getWidth()-1)-left,Math.min(bottom,image.getHeight()-1)-top);
	}
}