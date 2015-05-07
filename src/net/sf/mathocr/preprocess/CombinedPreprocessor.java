/* CombinedPreprocessor.java
 * =========================================================================
 * This file is originally part of the MathOCR Project
 *
 * Copyright (C) 2015 Chan Chung Kwong
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
import net.sf.mathocr.*;
import static net.sf.mathocr.Environment.env;
/**
 * A preprocessor preprocess image by applying a sequence of preprocessors one by one
 */
public final class CombinedPreprocessor implements Preprocessor{
	List<Preprocessor> preprocessors;
	/**
	 * Construct a CombinedPreprocessor
	 * @param preprocessors a sequence of preprocessors
	 */
	public CombinedPreprocessor(List<Preprocessor> preprocessors){
		this.preprocessors=preprocessors;
	}
	/**
	 * Perform preprocess operation
	 * @param image input image
	 * @return processed image
	 */
	public BufferedImage preprocess(BufferedImage image){
		for(Preprocessor p:preprocessors)
			image=p.preprocess(image);
		return image;
	}
	/**
	 * Get a CombinedPreprocessor according to global default
	 */
	public static CombinedPreprocessor getDefaultCombinedPreprocessor(){
		List<Preprocessor> preprocessors=new ArrayList<Preprocessor>(6);
		//preprocessors.add(new GrayScale());
		if(env.getBoolean("APPLY_MEAN_FILTER"))
			preprocessors.add(new MeanFilter());
		if(env.getBoolean("APPLY_MEDIAN_FILTER"))
			preprocessors.add(new MedianFilter());
		switch(env.getString("THREHOLD_METHOD").toUpperCase()){
			case "MANUAL":
				preprocessors.add(new Threhold(env.getInteger("MANUAL_THREHOLD_LIMIT")));
				break;
			case "OTSU":
				preprocessors.add(new ThreholdOtsu());
				break;
			default:
				preprocessors.add(new ThreholdSauvola(env.getFloat("SAUVOLA_WEIGHT"),env.getInteger("SAUVOLA_WINDOW")));
		}
		if(env.getBoolean("DETECT_INVERT"))
			preprocessors.add(new ColorInvert(true));
		if(env.getBoolean("APPLY_KFILL_FILTER"))
			preprocessors.add(new KFill(env.getInteger("KFILL_WINDOW")));
		if(env.getBoolean("APPLY_NOISE_FILTER"))
			preprocessors.add(new NoiseRemove());
		return new CombinedPreprocessor(preprocessors);
	}
}