/* SkewDetectTest.java
 * =========================================================================
 * This file is originally part of the MathOCR Project
 *
 * Copyright (C) 2014,2015 Chan Chung Kwong
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
package com.github.chungkwong.mathocr.preprocess.skew;
import com.github.chungkwong.mathocr.preprocess.*;
import com.github.chungkwong.mathocr.preprocess.skew.*;
import java.awt.image.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import javax.imageio.*;
/**
 * Test for skew detection methods Assume that the
 * dataset(http://www.iit.demokritos.gr/%7Ealexpap/DISEC13/icdar2013_benchmarking_dataset.rar)
 * is unpacked at <code>../datasets</code>
 */
public final class SkewDetectTest implements Runnable{
	private static final int METHOD_COUNT=7;
	private static final int SAMPLE_COUNT=1550;
	private static final double MARGIN=Math.PI/1800;
	private static final Grayscale gray=new Grayscale();
	private static final Threhold threhold=new ThreholdFixed(128);
	private static int curr;
	private static double[][] errors=new double[METHOD_COUNT][SAMPLE_COUNT];
	private final BufferedImage image;
	private final int method;
	private final int index;
	private final double truth;
	/**
	 * Test a method using a given image
	 *
	 * @param image sample
	 * @param method the method id
	 */
	public SkewDetectTest(BufferedImage image,int method,int index,double truth){
		this.image=image;
		this.method=method;
		this.index=index;
		this.truth=truth;
	}
	public void run(){
		try{
			//long time=System.currentTimeMillis();
			double detected=0;
			switch(method){
				case 0:
					//detected=new SDetector().detect(image);
					detected=new PPDetector(new HierarchyStrategy()).detect(image);
					break;
				case 1:
					detected=new TCDetector(new HierarchyStrategy()).detect(image);
					break;
				case 2:
					detected=new HTDetector(new HierarchyStrategy()).detect(image);
					break;
				case 3:
					detected=new PCPDetector(new HierarchyStrategy()).detect(image);
					break;
				case 4:
					detected=new PPADetector().detect(image);
					break;
				case 5:
					detected=new NNDetector().detect(image);
					break;
				case 6:
					detected=new SDetector().detect(image);
					break;
				case 7:
					detected=new CCDetector().detect(image);
					break;
			}
			//time=System.currentTimeMillis()-time;
			//System.out.print(detected+"\t"+time+"\t");
			errors[method][index]=Math.abs(detected-truth);
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	/**
	 * Start the test
	 */
	public static void main(String[] args) throws Exception{
		File dir=new File("../datasets/icdar2013_benchmarking_dataset");//the directory which contain the test images
		Files.lines(new File(dir,"Ground_Truth.txt").toPath()).forEach((line)->{
			String[] fields=line.split("\t");
			if(fields.length==3){
				try{
					System.err.println(new File(dir,fields[0]+".tif"));
					BufferedImage image=threhold.apply(gray.apply(ImageIO.read(new File(dir,fields[0]+".tif")),true),true);
					double truth=-Double.parseDouble(fields[2])*Math.PI/180;
					Thread[] threads=new Thread[METHOD_COUNT];
					long timeLimit=System.currentTimeMillis()+10000;
					for(int i=0;i<METHOD_COUNT;i++){
						threads[i]=new Thread(new SkewDetectTest(image,i,curr,truth));
						threads[i].start();
						//curr.join();
					}
					for(int i=0;i<METHOD_COUNT;i++){
						threads[i].join(timeLimit-System.currentTimeMillis());
						threads[i].stop();
					}
					System.err.println(curr++);
				}catch(Exception ex){
					ex.printStackTrace();
				}
			}
		});
		printReport();
	}
	private static void printReport(){
		double[] aed=getAed();
		System.out.println("AED:"+Arrays.toString(aed));
		double[] top80=getTop80();
		System.out.println("Top80:"+Arrays.toString(top80));
		int[] ce=getCe();
		System.out.println("CE:"+Arrays.toString(ce));
		int[] s=getS(aed,top80,ce);
		System.out.println("S:"+Arrays.toString(s));
	}
	private static double[] getAed(){
		double[] aed=new double[METHOD_COUNT];
		for(int i=0;i<METHOD_COUNT;i++){
			for(int j=0;j<SAMPLE_COUNT;j++){
				aed[i]+=errors[i][j];
			}
			aed[i]/=SAMPLE_COUNT;
		}
		return aed;
	}
	private static double[] getTop80(){
		double[] top80=new double[METHOD_COUNT];
		for(int i=0;i<METHOD_COUNT;i++){
			Arrays.sort(errors[i]);
			for(int j=0;j<1240;j++){
				top80[i]+=errors[i][j];
			}
			top80[i]/=1240;
		}
		return top80;
	}
	private static int[] getCe(){
		int[] ce=new int[METHOD_COUNT];
		for(int i=0;i<METHOD_COUNT;i++){
			for(int j=0;j<SAMPLE_COUNT;j++){
				if(errors[i][j]>MARGIN){
					++ce[i];
				}
			}
		}
		return ce;
	}
	private static int[] getS(double[] aed,double[] top80,int[] ce){
		int[] s=new int[METHOD_COUNT];
		double[][] sorted=new double[3][METHOD_COUNT];
		for(int i=0;i<METHOD_COUNT;i++){
			sorted[0][i]=aed[i];
			sorted[1][i]=top80[i];
			sorted[2][i]=ce[i];
		}
		Arrays.sort(sorted[0]);
		Arrays.sort(sorted[1]);
		Arrays.sort(sorted[2]);
		for(int i=0;i<METHOD_COUNT;i++){
			for(int k=0;k<METHOD_COUNT;k++){
				if(aed[i]==sorted[0][k]){
					s[i]+=k;
					break;
				}
			}
			for(int k=0;k<METHOD_COUNT;k++){
				if(top80[i]==sorted[1][k]){
					s[i]+=k;
					break;
				}
			}
			for(int k=0;k<METHOD_COUNT;k++){
				if(ce[i]==sorted[2][k]){
					s[i]+=k;
					break;
				}
			}
		}
		return s;
	}
}
