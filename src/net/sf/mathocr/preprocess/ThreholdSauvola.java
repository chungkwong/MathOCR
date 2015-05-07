/* ThreholdSauvola.java
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
package net.sf.mathocr.preprocess;
import java.awt.image.*;
/**
 * A preprocessor that threhold grayscale image using Sauvola's method
 */
public final class ThreholdSauvola implements Preprocessor{
	double weight;
	int window;
	/**
	 * Construct a ThreholdSauvola
	 * @param weight the weight
	 * @param window the size of each window
	 */
	public ThreholdSauvola(double weight,int window){
		this.weight=weight;
		this.window=window;
	}
	/**
	 * Threhold a picture using Sauvola's method
	 * @param	image the input grayscale picture
	 * @return  threholded image
	 */
	public BufferedImage preprocess(BufferedImage image){
		if(image.getType()==BufferedImage.TYPE_BYTE_BINARY)
			return image;
		int width=image.getWidth(),height=image.getHeight();
		int[] pixels=image.getRGB(0,0,width,height,null,0,width);
		for(int i=0;i<pixels.length;i++)
			pixels[i]&=0xff;
		//byte[] pixels=((DataBufferByte)image.getRaster().getDataBuffer()).getData();
		long[][] intImg=ImageUtil.getIntegralImage(pixels,width,height),sqIntImg=ImageUtil.getSquaredIntegralImage(pixels,width,height);
		int dl=(window+1)/2,dr=window/2;
		//double factor=1.0/(window*window);
		for(int i=0,ind=0;i<height;i++){
			for(int j=0;j<width;j++,ind++){
				long mean=ImageUtil.windowValue(intImg,width,height,i,j,dl,dr);
				//int left=Math.max(-1,j-dl),right=Math.min(width-1,j+dr),top=Math.max(-1,i-dl),bottom=Math.min(height-1,i+dr);
				//long mean=ImageUtil.windowValue2(intImg,width,height,left,right,top,bottom);
				//double factor=1.0/((right-left)*(bottom-top));
				double factor=1.0/((Math.min(height-1,i+dr)-Math.max(-1,i-dl))*(Math.min(width-1,j+dr)-Math.max(-1,j-dl)));
				double s=Math.sqrt((ImageUtil.windowValue(sqIntImg,width,height,i,j,dl,dr)-mean*factor*mean)*factor);
				int lim=(int)(mean*factor*(1+weight*(s/128-1)));
				pixels[ind]=pixels[ind]<=Math.max(lim,0)?0xff000000:0xffffffff;
				//pixels[ind]=(pixels[ind]&0xFF)<=Math.max(lim,0)?(byte)0x00:(byte)0xff;
				//System.out.print(pixels[ind]==0?1:0);
			}
			//System.out.println();
		}
		image=new BufferedImage(width,height,BufferedImage.TYPE_BYTE_BINARY);
		image.setRGB(0,0,width,height,pixels,0,width);
		return image;
	}
}