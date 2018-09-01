/* SimplePreprocessor.java
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
import java.util.*;
/**
 * Abstract class of Preprocessor that only need pixel array
 */
public abstract class SimplePreprocessor implements Preprocessor{
	/**
	 * Perform preprocess operation
	 * @param pixels pixel array of the input image
	 * @param width width of the input image
	 * @param height height of the input image
	 */
	public abstract int[] preprocess(int[] pixels,int width,int height);
	/**
	 * Perform preprocess operation
	 * @param image input image
	 * @return processed image
	 */
	public BufferedImage preprocess(BufferedImage image){
		int width=image.getWidth(),height=image.getHeight();
		int[] pixels=image.getRGB(0,0,width,height,null,0,width);
		pixels=preprocess(pixels,width,height);
		image.setRGB(0,0,width,height,pixels,0,width);
		return image;
	}
	/*public static BufferedImage preprocess(BufferedImage image,List<SimplePreprocessor> preprocessors){
		int width=image.getWidth(),height=image.getHeight();
		int[] pixels=image.getRGB(0,0,width,height,null,0,width);
		for(SimplePreprocessor preprocessor:preprocessors)
			pixels=preprocessor.preprocess(pixels,width,height);
		image.setRGB(0,0,width,height,pixels,0,width);
		return image;
	}*/
}