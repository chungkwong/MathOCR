/* CombinedPreprocessor.java
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
package com.github.chungkwong.mathocr.preprocess;
import java.awt.image.*;
import java.util.*;
import static com.github.chungkwong.mathocr.Environment.ENVIRONMENT;
/**
 * A preprocessor preprocess image by applying a sequence of preprocessors one
 * by one
 */
public final class CombinedPreprocessor implements Preprocessor{
	private final List<Preprocessor> preprocessors;
	/**
	 * Construct a CombinedPreprocessor
	 *
	 * @param preprocessors a sequence of preprocessors
	 */
	public CombinedPreprocessor(List<Preprocessor> preprocessors){
		this.preprocessors=preprocessors;
	}
	@Override
	public boolean isApplicable(BufferedImage image){
		return preprocessors.isEmpty()||preprocessors.get(0).isApplicable(image);
	}
	/**
	 * Perform preprocess operation
	 *
	 * @param image input image
	 * @return processed image
	 */
	@Override
	public BufferedImage apply(BufferedImage image,boolean inplace){
		for(Preprocessor p:preprocessors){
			image=p.apply(image,inplace);
		}
		return image;
	}
	/**
	 * Get a CombinedPreprocessor according to global default
	 *
	 * @return Preprocessor
	 */
	public static CombinedPreprocessor getDefaultCombinedPreprocessor(){
		List<Preprocessor> preprocessors=new ArrayList<Preprocessor>(6);
		//preprocessors.add(new GrayScale());
		if(ENVIRONMENT.getBoolean("APPLY_MEAN_FILTER")){
			preprocessors.add(new MeanFilter());
		}
		if(ENVIRONMENT.getBoolean("APPLY_MEDIAN_FILTER")){
			preprocessors.add(new MedianFilter());
		}
		switch(ENVIRONMENT.getString("THREHOLD_METHOD").toUpperCase()){
			case "MANUAL":
				preprocessors.add(new ThreholdFixed(ENVIRONMENT.getInteger("MANUAL_THREHOLD_LIMIT")));
				break;
			case "OTSU":
				preprocessors.add(new ThreholdOtsu());
				break;
			default:
				preprocessors.add(new ThreholdSauvola(ENVIRONMENT.getFloat("SAUVOLA_WEIGHT"),ENVIRONMENT.getInteger("SAUVOLA_WINDOW")));
		}
		if(ENVIRONMENT.getBoolean("DETECT_INVERT")){
			preprocessors.add(new ColorInvert(true));
		}
		if(ENVIRONMENT.getBoolean("APPLY_KFILL_FILTER")){
			preprocessors.add(new KFill(ENVIRONMENT.getInteger("KFILL_WINDOW")));
		}
		if(ENVIRONMENT.getBoolean("APPLY_NOISE_FILTER")){
			preprocessors.add(new NoiseRemove());
		}
		return new CombinedPreprocessor(preprocessors);
	}
}
