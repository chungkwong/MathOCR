/* Grayscale.java
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
 * A preprocessor that turn image into grayscale image
 */
public final class Grayscale implements Preprocessor{
	/**
	 * Construct a Grayscale
	 */
	public Grayscale(){
	}
	/**
    * Perform preprocess operation
    * @param image input image
    * @return processed image
    */
	public BufferedImage preprocess(BufferedImage image){
		if(image.getType()==BufferedImage.TYPE_BYTE_GRAY||image.getType()==BufferedImage.TYPE_BYTE_BINARY)
			return image;
		int width=image.getWidth(),height=image.getHeight();
		BufferedImage result=new BufferedImage(width,height,BufferedImage.TYPE_BYTE_GRAY);
		int[] pixels=image.getRGB(0,0,width,height,null,0,width);
		/*for(int i=0;i<pixels.length;i++){
			int alpha=(pixels[i]>>>24)&0xff,red=(pixels[i]>>>16)&0xff,green=(pixels[i]>>>8)&0xff,blue=pixels[i]&0xff;
			int tmp=255-(255-(red*316+green*624+blue*84)/1024)*alpha/255;
			pixels[i]=0xff000000|(tmp<<16)|(tmp<<8)|tmp;
		}
		result.setRGB(0,0,width,height,pixels,0,width);*/
		byte[] buf=((DataBufferByte)result.getRaster().getDataBuffer()).getData();
		for(int i=0;i<pixels.length;i++){
			int alpha=(pixels[i]>>>24)&0xff,red=(pixels[i]>>>16)&0xff,green=(pixels[i]>>>8)&0xff,blue=pixels[i]&0xff;
			buf[i]=(byte)(255-(255-(red*316+green*624+blue*84)/1024)*alpha/255);
		}
		return result;
	}
}