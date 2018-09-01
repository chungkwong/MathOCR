/* Preprocessor.java
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
import java.awt.image.*;
/**
 * Interface for image preprocesor
 */
public interface Preprocessor{
	/**
	 * Check if the preprocessor can be applied to a image
	 *
	 * @param image input image
	 * @return can handle or not
	 */
	public boolean isApplicable(BufferedImage image);
	/**
	 * Perform preprocess operation
	 *
	 * @param image input image
	 * @param inplace attempt to reuse input image or not
	 * @return processed image
	 */
	public BufferedImage apply(BufferedImage image,boolean inplace);
}
