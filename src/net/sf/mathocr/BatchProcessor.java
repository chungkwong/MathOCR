/* BatchProcessor.java
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
package net.sf.mathocr;
import net.sf.mathocr.layout.*;
import net.sf.mathocr.ocr.*;
import net.sf.mathocr.ofr.*;
/**
 * Simple API of the optical document recognition and mathematical expression recognition functionality
 */
public final class BatchProcessor{
	public static final int LATEX=0,HTML=1;
	/**
	 * Recognize a Document
	 * @param doc the input document
	 * @param form output format, can be BatchProcessor.LATEX or BatchProcessor.HTML
	 * @return the recognition result
	 */
	public static String recognize(Document doc,int form){
		for(Page page:doc.getPages()){
			page.load();
			page.preprocessByDefault();
			page.componentAnalysis();
			page.segment(new XYCut());
			page.regionClassify();
			page.readingOrderSort();
			page.produceLogicalBlock();
			page.cleanImage();
		}
		if(form==HTML)
			return new HTMLGenerator(doc).toString();
		return new LaTeXGenerator(doc).toString();
	}
	/**
	 * Recognize a formula
	 * @param image a formula image
	 * @return LaTeX code
	 */
	public static String recognizeFormula(java.awt.image.BufferedImage image){
		Page page=new Page(image,null);
		page.load();
		page.preprocessByDefault();
		page.componentAnalysis();
		CharactersLine line=new CharactersLine(page.getComponentPool().getComponents());
		return new LogicalLine(line,true).recognize();
	}
}