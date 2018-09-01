/*
 * Copyright (C) 2018 Chan Chung Kwong
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.chungkwong.mathocr.preprocess.skew;
import java.awt.image.*;
/**
 * Detect skew by Cross-corrlation method
 *
 * @author Chan Chung Kwong
 */
public class CCDetector implements SkewDetector{
	/**
	 * Create a skew detector
	 */
	public CCDetector(){
	}
	@Override
	public double detect(BufferedImage image){
		int width=image.getWidth(), height=image.getHeight(), len=width*height;
		int[] pixels=image.getRGB(0,0,width,height,null,0,width);
		for(int i=0;i<pixels.length;i++){
			pixels[i]=(~pixels[i])&0x1;
		}
		int d=width/2;
		double maxR=0;
		int S=0, ds=(int)((width*Math.PI/1800)+1);
		for(int s=-height;s<height;s+=ds){
			int R=0;
			for(int x=0;x<width-d;x++){
				int start=Math.max(0,-s);
				for(int ind1=start*width+x, ind2=(start+s)*width+x+d;ind1<len&&ind2<len;ind1+=width,ind2+=width){
					R+=pixels[ind1]*pixels[ind2];
				}
			}
			if(R>maxR){
				maxR=R;
				S=s;
			}
		}
		return Math.atan((S+0.0)/d);
	}
}
