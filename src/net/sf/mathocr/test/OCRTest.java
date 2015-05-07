/* OCRTest.java
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
import java.awt.*;
import java.awt.image.*;
import java.awt.font.*;
import java.io.*;
import java.text.*;
import java.util.*;
import net.sf.mathocr.common.*;
import net.sf.mathocr.ocr.*;
import net.sf.mathocr.preprocess.*;
/**
 * A auto-tester of the character recognition system
 */
public final class OCRTest{
	static String[] list=new String[]{"CMBSY10.ttf","CMB10.ttf","CMMI10.ttf","CMMIB10.ttf","CMR10.ttf"
	,"CMEX10.ttf","EUFB10.ttf","MSAM10.ttf","MSBM10.ttf","RSFS10.ttf","CMSY10.ttf"
	/*"FandolSong-Regular.otf","FandolHei-Regular.otf","FandolKai-Regular.otf","FandolFang-Regular.otf"*/};//specified the fonts to be tested
	/**
	 * Test recognition rate and show failure samples
	 * @param db the DataBase used
	 * @param name name of the font
	 * @param size the size of sample to be generated
	 */
	public static int[] test(DataBase db,String name,int size){
		try{
			FontRenderContext context=new FontRenderContext(null,true,true);
			Preprocessor preprocessor=CombinedPreprocessor.getDefaultCombinedPreprocessor();
			BufferedReader in=new BufferedReader(new InputStreamReader(new FileInputStream("/home/kwong/projects/TypeLess/fonts/"
			+name.substring(0,name.length()-4)+".nam")));//specified locatation of the list of characters
			Font font=Font.createFont(Font.TRUETYPE_FONT,new File("/home/kwong/projects/TypeLess/fonts/"+name));//specified location of the fonts
			GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);
			font=font.deriveFont(Float.valueOf(size));
			int sum=0,correct=0;
			String line;
			while((line=in.readLine())!=null){
				String line2=line.substring(7,line.indexOf("\t",7));
				if(line.endsWith("\tP"))
					continue;
				int codepoint=Integer.valueOf(line.substring(2,6),16);
				line=line.substring(line.indexOf("\t",7)+1);
				if(line.endsWith("\tV")||line.endsWith("\tS"))
					line=line.substring(0,line.length()-2);
				if(line.endsWith("\tH"))
					line=line.substring(0,line.length()-2)+"{}";
				if(line.endsWith("\tVH"))
					line=line.substring(0,line.length()-3)+"{}";
				if(line.isEmpty())
					line=line2;
				String ch=new String(new int[]{codepoint},0,1);
				Rectangle rect=font.createGlyphVector(context,ch).getPixelBounds(context,0,0);
				int width=(int)rect.getWidth()+14;
				int height=(int)rect.getHeight()+14;
				BufferedImage bi=new BufferedImage(width,height,BufferedImage.TYPE_BYTE_GRAY);
				Graphics2D g2d=bi.createGraphics();
				g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
				g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,RenderingHints.VALUE_FRACTIONALMETRICS_ON);
				g2d.setFont(font);
				g2d.setColor(Color.white);
				g2d.fillRect(0,0,width,height);
				g2d.setColor(Color.black);
				g2d.drawString(ch,-(int)rect.getX()+7,-(int)rect.getY()+7);
				g2d.dispose();
				bi=preprocessor.preprocess(bi);
				ComponentPool pool=new ComponentPool(bi);
				CharactersLine cline=new CharactersLine(pool.getComponents(),db);
				java.util.List<Char> re=cline.getCharacters();
				if(!re.isEmpty()&&re.get(0).getCandidates().first().getSymbol().toString().equals(line)){
					//System.out.println(sum);
					++correct;
				}else{
					//System.out.println(sum+":"+line+"\t"+(re.isEmpty()?"":re.get(0).getCandidates().first().getSymbol().toString()));
					//System.out.println(sum+":"+line+"\t"+(re.isEmpty()?"":re.get(0).toString()));
					//System.out.println(line+";"+(re.isEmpty()?"":re)+";"+sum);
				}
				++sum;
			}
			//System.out.println(name+" "+size+":"+correct+"/"+sum);
			return new int[]{correct,sum};
		}catch(Exception e){
			e.printStackTrace();
		}
		return new int[]{0,0};
	}
	static StringBuilder acc=new StringBuilder(),tim=new StringBuilder();
	/**
	 * Test OCR using sample characters in specified font size
	 * @param db the DataBase used
	 * @param size the font size
	 */
	public static void test(DataBase db,int size){
		int correct=0,sum=0;
		long time1=System.currentTimeMillis();
		for(String name:list){
			int[] re=test(db,name,size);
			System.out.println(name+re[0]+"/"+re[1]);
			correct+=re[0];
			sum+=re[1];
		}
		long time=System.currentTimeMillis()-time1;
		System.out.println(size+":"+correct+"/"+sum+"("+((correct+0.0)/sum)+"%)"+(time+0.0)/sum);
		acc.append(new DecimalFormat("##.00").format(correct*100.0/sum));
		tim.append(new DecimalFormat("##.00").format((time+0.0)/sum));
		acc.append("\\% & ");
		tim.append("ms & ");
	}
	/**
	 * Entrance of the test program
	 * @param args not used
	 */
	public static void main(String[] args) throws Exception{
		//String[] list=new String[]{"CMR10"};
		//File[] fontfile=new File[list.length];
		//for(int i=0;i<list.length;i++)
		//	fontfile[i]=new File("/home/kwong/projects/TypeLess/fonts/"+list[i]+".ttf.lg");
		net.sf.mathocr.Environment.env.setBoolean("USE_HOLE_MATCHER",false);
		net.sf.mathocr.Environment.env.setBoolean("USE_ASPECT_RATIO_MATCHER",true);
		net.sf.mathocr.Environment.env.setBoolean("USE_CHOP_MATCHER",false);
		net.sf.mathocr.Environment.env.setBoolean("USE_GRID_MATCHER",true);
		net.sf.mathocr.Environment.env.setBoolean("USE_MOMENTS_MATCHER",true);
		net.sf.mathocr.Environment.env.setBoolean("USE_PROJECTION_MATCHER",true);
		net.sf.mathocr.Environment.env.setBoolean("USE_PIXEL_MATCHER",false);
		//DataBase db=DataBase.DEFAULT_DATABASE;
		File[] datafile=new File[list.length];
		for(int i=0;i<list.length;i++)
			datafile[i]=new File("/home/kwong/projects/TypeLess/fonts/"+list[i]+".lg");
		DataBase db=new DataBase(datafile);
		test(db,10);
		test(db,20);
		test(db,30);
		test(db,40);
		test(db,50);
		acc.replace(acc.length()-2,acc.length(),"\\\\");
		tim.replace(tim.length()-2,tim.length(),"\\\\");
		System.out.println(acc.toString());
		System.out.println(tim.toString());
	}
}