/* SkewDetectTest.java
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
package net.sf.mathocr.test;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import net.sf.mathocr.preprocess.*;
/**
 * Test for skew detection methods
 */
public final class SkewDetectTest implements Runnable{
	BufferedImage image;
	int method;
	static Grayscale gray=new Grayscale();
	static Threhold threhold=new Threhold(128);
	static Thread curr;
	/**
	 * Test a method using a given image
	 *	@param image sample
	 * @param method the method id
	 */
	public SkewDetectTest(BufferedImage image,int method){
		this.image=image;
		this.method=method;
	}
	public void run(){
		try{
			long time=System.currentTimeMillis();
			double detected=0;
			switch(method){
				case 0:
					detected=SkewCorrect.detectSkewPP(image);
					break;
				case 1:
					detected=SkewCorrect.detectSkewTC(image);
					break;
				case 2:
					detected=SkewCorrect.detectSkewHT(image);
					break;
				case 3:
					detected=SkewCorrect.detectSkewPCP(image);
					break;
				case 4:
					detected=SkewCorrect.detectSkewPPA(image);
					break;
				case 5:
					detected=SkewCorrect.detectSkewNN(image);
					break;
				case 6:
					detected=SkewCorrect.detectSkewCC(image);
					break;
			}
			time=System.currentTimeMillis()-time;
			System.out.print(detected+"\t"+time+"\t");
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	/**
	 * Start the test
	 */
	public static void main(String[] args) throws Exception{
		File dir=new File("/home/kwong/projects/MathOCR/datasets/icdar2013_benchmarking_disec_grayvalue_bin");//the directory which contain the test images
		File[] files=dir.listFiles();
		java.util.Arrays.sort(files);
		int count=0;
		for(File file:files){
			//System.out.println(Runtime.getRuntime().maxMemory()/1024/1024+","+Runtime.getRuntime().totalMemory()/1024/1024);
			BufferedImage image=null;
			double truth=0;
			String name=null;
			try{
				image=threhold.preprocess(gray.preprocess(ImageIO.read(file)));
				truth=-Double.parseDouble(file.getName().substring(file.getName().indexOf("[")+1,file.getName().indexOf("]")))*Math.PI/180;
				name=file.getName().substring(4,8);
			}catch(Exception ex){
				ex.printStackTrace();
				continue;
			}
			System.out.print(name+"\t"+truth+"\t");
			for(int i=0;i<7;i++){
				curr=new Thread(new SkewDetectTest(image,i));
				curr.start();
				//curr.join();
				curr.join(10000);
				if(curr.isAlive())
					System.out.print("0.0\t5000\t");
				curr.stop();
			}
			System.out.println();
			System.err.println(name);
		}
	}
}