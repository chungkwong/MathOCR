/* MSEMatcher.java
 * =========================================================================
 * This file is originally part of the MathOCR Project
 *
 * Copyright (C) 2014,2015 Chan Chung Kwong
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
package net.sf.mathocr.ocr;
import net.sf.mathocr.common.*;
/**
 * Matcher based on statistics characteristic
 */
public abstract class MSEMatcher extends DistanceMatcher<float[]>{
	int numberOfVariables;
	int sampleCount=0;
	float[] sum,sqsum,var;
	/**
	 * Construct a MSEMatcher
	 */
	public MSEMatcher(){
	}
	/**
	 * Get the number of dimenion used
	 * @return the number
	 */
	public int getNumberOfVariables(){
		return numberOfVariables;
	}
	/**
	 * Add a sample
	 * @param glyph known glyph
	 */
	public void addSample(Glyph glyph){
		if(sum==null){
			sum=new float[numberOfVariables];
			sqsum=new float[numberOfVariables];
		}
		float[] data=getCharacteristic(glyph);
		for(int i=0;i<numberOfVariables;i++){
			sum[i]+=data[i];
			sqsum[i]+=data[i]*data[i];
		}
		++sampleCount;
	}
	/**
	 * Should be called after before any match attempt
	 */
	public void allSampleAdded(){
		var=new float[numberOfVariables];
		for(int i=0;i<numberOfVariables;i++)
			var[i]=(float)Math.sqrt((sqsum[i]-sum[i]*sum[i]/sampleCount)/(sampleCount-1))*3;
			//var[i]=(sqsum[i]-sum[i]*sum[i]/sampleCount)/(sampleCount-1);
		//sum=null;
		//sqsum=null;
	}
	/**
	 * Calculate the distance between characteristic
	 * @param sample characteristic to be matched
	 * @param data another characteristic to be matched
	 * @return distance between the two characteristic
	 */
	public double getDistance(float[] sample,float[] data){
		double score=0;
		for(int j=0;j<numberOfVariables;j++)
			score+=Math.abs((sample[j]-data[j])/var[j]);
		return Math.min(score/numberOfVariables,1.0);
	}
}