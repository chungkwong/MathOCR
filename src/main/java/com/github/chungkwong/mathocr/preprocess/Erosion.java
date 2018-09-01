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
package com.github.chungkwong.mathocr.preprocess;
import java.awt.image.*;
import java.util.*;
/**
 *
 * @author Chan Chung Kwong
 */
public class Erosion extends SimplePreprocessor{
	private final int windowSize;
	private final BitSet template;
	public Erosion(BitSet template){
		this.template=template;
		int size=1;
		while(size*size<=template.size()){
			size+=2;
		}
		this.windowSize=size-2;
	}
	@Override
	public boolean isApplicable(BufferedImage image){
		return image.getType()==BufferedImage.TYPE_BYTE_BINARY;
	}
	@Override
	public int[] preprocess(int[] pixels,int width,int height){
		int[] result=new int[height*width];
		Arrays.fill(result,0xFFFFFFFF);
		int r=windowSize/2;
		for(int i=r;i<height-r;i++){
			outer:
			for(int j=r, ind=i*width+j;j<width-r;j++,ind++){
				for(int k=-r, tind=0;k<=r;k++){
					for(int l=-r;l<=r;l++,tind++){
						if(template.get(tind)&&(pixels[ind+k*width+l]&0xFF)!=0){
							continue outer;
						}
					}
				}
				result[ind]=0xFF000000;
			}
		}
		return result;
	}
}
