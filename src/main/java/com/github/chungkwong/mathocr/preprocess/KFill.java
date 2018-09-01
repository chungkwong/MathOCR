/* Kfill.java
 * =========================================================================
 * This file is originally part of the MathOCR Project
 *
 * Copyright (C) 2014 Chan Chung Kwong
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
/**
 * Kfill filter
 */
public final class KFill extends SimplePreprocessor{
	private final int k;
	/**
	 * Construct a Kfill
	 *
	 * @param k length of side of a window, should be a odd number
	 */
	public KFill(int k){
		this.k=k;
	}
	/**
	 * Construct a Kfill with k=3
	 */
	public KFill(){
		this.k=3;
	}
	/**
	 * @return length of size of window
	 */
	public int getK(){
		return k;
	}
	@Override
	public boolean isApplicable(BufferedImage image){
		return image.getType()==BufferedImage.TYPE_BYTE_BINARY;
	}
	@Override
	public int[] preprocess(int[] pixels,int width,int height){
		int hk=k/2, len=pixels.length, k3m4=k*3-4;
		int[] pixels2=new int[len];
		boolean changed=true;
		while(changed){
			changed=false;
			int ind=0;
			for(int i=0;i<hk;i++){
				for(int j=0;j<width;j++,ind++){
					pixels2[ind]=pixels[ind];
				}
			}
			for(int i=hk;i<height-hk;i++){
				for(int j=0;j<hk;j++,ind++){
					pixels2[ind]=pixels[ind];
				}
				for(int j=hk;j<width-hk;j++,ind++){
					if(pixels[ind]==0xff000000){
						int n=0, prev=-1, c=0, r=0;
						for(int k=-hk;k<=hk;k++){
							if(pixels[ind-hk*width+k]!=0xff000000){
								++n;
								if(prev==1){
									++c;
								}
								prev=0;
							}else{
								prev=1;
							}
						}
						for(int k=-hk+1;k<hk;k++){
							if(pixels[ind+k*width+hk]!=0xff000000){
								++n;
								if(prev==1){
									++c;
								}
								prev=0;
							}else{
								prev=1;
							}
						}
						for(int k=hk;k>=-hk;k--){
							if(pixels[ind+hk*width+k]!=0xff000000){
								++n;
								if(prev==1){
									++c;
								}
								prev=0;
							}else{
								prev=1;
							}
						}
						for(int k=hk-1;k>-hk;k--){
							if(pixels[ind+k*width-hk]!=0xff000000){
								++n;
								if(prev==1){
									++c;
								}
								prev=0;
							}else{
								prev=1;
							}
						}
						if(prev==1&&pixels[ind-hk*width-hk]!=0xff000000){
							++c;
						}
						if(pixels[ind-hk*width-hk]!=0xff000000){
							++r;
						}else if(pixels[ind-hk*width+hk]!=0xff000000){
							++r;
						}else if(pixels[ind+hk*width-hk]!=0xff000000){
							++r;
						}else if(pixels[ind+hk*width+hk]!=0xff000000){
							++r;
						}
						if(c==1&&(n>k3m4||(n==k3m4&&r==2))){
							changed=true;
							pixels2[ind]=0xffffffff;
						}else{
							pixels2[ind]=0xff000000;
						}
					}else{
						pixels2[ind]=0xffffffff;
					}
				}
				for(int j=width-hk;j<width;j++,ind++){
					pixels2[ind]=pixels[ind];
				}
			}
			for(;ind<len;ind++){
				pixels2[ind]=pixels[ind];
			}
			ind=0;
			for(int i=0;i<hk;i++){
				for(int j=0;j<width;j++,ind++){
					pixels[ind]=pixels2[ind];
				}
			}
			for(int i=hk;i<height-hk;i++){
				for(int j=0;j<hk;j++,ind++){
					pixels[ind]=pixels2[ind];
				}
				for(int j=hk;j<width-hk;j++,ind++){
					if(pixels2[ind]!=0xff000000){
						int n=0, prev=-1, c=0, r=0;
						for(int k=-hk;k<=hk;k++){
							if(pixels2[ind-hk*width+k]==0xff000000){
								++n;
								if(prev==1){
									++c;
								}
								prev=0;
							}else{
								prev=1;
							}
						}
						for(int k=-hk+1;k<hk;k++){
							if(pixels2[ind+k*width+hk]==0xff000000){
								++n;
								if(prev==1){
									++c;
								}
								prev=0;
							}else{
								prev=1;
							}
						}
						for(int k=hk;k>=-hk;k--){
							if(pixels2[ind+hk*width+k]==0xff000000){
								++n;
								if(prev==1){
									++c;
								}
								prev=0;
							}else{
								prev=1;
							}
						}
						for(int k=hk-1;k>-hk;k--){
							if(pixels2[ind+k*width-hk]==0xff000000){
								++n;
								if(prev==1){
									++c;
								}
								prev=0;
							}else{
								prev=1;
							}
						}
						if(prev==1&&pixels2[ind-hk*width-hk]==0xff000000){
							++c;
						}
						if(pixels2[ind-hk*width-hk]==0xff000000){
							++r;
						}else if(pixels2[ind-hk*width+hk]==0xff000000){
							++r;
						}else if(pixels2[ind+hk*width-hk]==0xff000000){
							++r;
						}else if(pixels2[ind+hk*width+hk]==0xff000000){
							++r;
						}
						if(c==1&&(n>k3m4||(n==k3m4&&r==2))){
							changed=true;
							pixels[ind]=0xff000000;
						}else{
							pixels[ind]=0xffffffff;
						}
					}else{
						pixels[ind]=0xff000000;
					}
				}
				for(int j=width-hk;j<width;j++,ind++){
					pixels[ind]=pixels2[ind];
				}
			}
			for(;ind<len;ind++){
				pixels[ind]=pixels2[ind];
			}
		}
		return pixels;
	}
}
