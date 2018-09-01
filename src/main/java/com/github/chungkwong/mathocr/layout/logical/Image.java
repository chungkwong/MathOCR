/* Image.java
 * =========================================================================
 * This file is originally part of the MathOCR Project
 *
 * Copyright (C) 2015 Chan Chung Kwong
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
package com.github.chungkwong.mathocr.layout.logical;
import com.github.chungkwong.mathocr.text.structure.Line;
import com.github.chungkwong.mathocr.common.BoundBox;
import java.util.*;
/**
 * A data structure representing image
 */
public final class Image extends LogicalBlock{
	private final String path;
	private List<Line> caption;
	/**
	 * Construct a Image
	 *
	 * @param path the path to the image file
	 * @param caption the caption
	 * @param box the bounding box
	 */
	public Image(String path,List<Line> caption,BoundBox box){
		super(box,true);
		this.path=path;
		this.caption=caption;
	}
	/**
	 * Get the path to the image file
	 *
	 * @return the path
	 */
	public String getPath(){
		return path;
	}
	/**
	 * Get the caption of the image
	 *
	 * @return the caption
	 */
	public List<Line> getCaption(){
		return caption;
	}
	/**
	 * Set the path to the image
	 *
	 * @param caption the caption
	 */
	public void setCaption(List<Line> caption){
		this.caption=caption;
	}
}
