/* Preprocessor.java
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
 * Interface for image preprocesor
 */
public interface Preprocessor{
	/**
	 * Perform preprocess operation
	 * @param image input image
	 * @return processed image
	 */
	public BufferedImage preprocess(BufferedImage image);
}