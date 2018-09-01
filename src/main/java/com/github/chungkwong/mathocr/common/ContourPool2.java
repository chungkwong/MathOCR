/* ContourPool2.java
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
package com.github.chungkwong.mathocr.common;
import java.util.*;
public final class ContourPool2{
	List<Contour2> contours=new ArrayList<Contour2>();
	public ContourPool2(int[] pixels,int width,int height){
		ContourAnalysis(pixels,width,height);
	}
	public ContourPool2(java.awt.image.BufferedImage image){
		int width=image.getWidth(),height=image.getHeight();
		ContourAnalysis(image.getRGB(0,0,width,height,null,0,width),width,height);
	}
	private void ContourAnalysis(int[] pixels,int width,int height){
		byte[][] visited=new byte[height+1][width+1];
		for(int i=0;i<pixels.length;i++)
			pixels[i]&=0x1;
		for(int i=0;i<=height;i++){
			for(int j=0;j<=width;j++){
				if(visited[i][j]==1)
					continue;
				int k=i,l=j,dir=-1;
				Contour2 c=null;
				do{
					boolean tl=k!=0&&l!=0&&pixels[(k-1)*width+(l-1)]==0;
					boolean tr=k!=0&&l!=width&&pixels[(k-1)*width+l]==0;
					boolean bl=k!=height&&l!=0&&pixels[k*width+(l-1)]==0;
					boolean br=k!=height&&l!=width&&pixels[k*width+l]==0;
					//if((tl!=tr||tr!=bl||bl!=br))
					//	System.out.println(tl+";"+tr+";"+bl+";"+br+":"+k+","+l+c);
					if(tl){
						if(tr){
							if(bl){
								if(br){
									/* bb
									   bb */
									if(dir!=-1)
										System.out.println("error");
									break;
								}else{
									/* bb
									   bw */
									if(dir==-1){
										c=new Contour2(false);
										contours.add(c);
									}else if(i==k&&j==l)
										break;
									dir=(dir==0?3:2);
								}
							}else{
								if(br){
									/* bb
									   wb */
									dir=(dir==0?1:2);
								}else{
									/* bb
									   ww */
								}
							}
						}else{
							if(bl){
								if(br){
									/* bw
									   bb */
									dir=(dir==1?0:3);
								}else{
									/* bw
									   bw */
								}
							}else{
								if(br){
									/* bw
									   wb */
									if(dir==-1){
										c=new Contour2(false);
										contours.add(c);
										if(visited[k][l]==2)
											dir=2;
										else
											dir=0;
									}else if(i==k&&j==l)
										break;
									else if(dir==0)
										dir=1;
									else if(dir==1)
										dir=0;
									else if(dir==2)
										dir=3;
									else
										dir=2;
									if(visited[k][l]==0)
										visited[k][l]=dir==0||dir==3?(byte)2:(byte)3;
									else
										visited[k][l]=1;
								}else{
									/* bw
									   ww */
									dir=(dir==2?1:0);
								}
							}
						}
					}else{
						if(tr){
							if(bl){
								if(br){
									/* wb
									   bb */
									dir=(dir==2?1:0);
								}else{
									/* wb
									   bw */
									if(dir==-1){
										c=new Contour2(false);
										contours.add(c);
										if(visited[k][l]==2)
											dir=2;
										else
											dir=0;
									}else if(i==k&&j==l)
										break;
									else if(dir==0)
										dir=3;
									else if(dir==1)
										dir=2;
									else if(dir==2)
										dir=1;
									else
										dir=0;
									if(visited[k][l]==0)
										visited[k][l]=dir==0||dir==1?(byte)2:(byte)0;
									else
										visited[k][l]=1;
								}
							}else{
								if(br){
									/* wb
									   wb */

								}else{
									/* wb
									   ww */
									dir=(dir==1?0:3);
								}
							}
						}else{
							if(bl){
								if(br){
									/* ww
									   bb */

								}else{
									/* ww
									   bw */
									dir=(dir==0?1:2);
								}
							}else{
								if(br){
									/* ww
									   wb */
									if(dir==-1){
										c=new Contour2(true);
										contours.add(c);
									}else if(i==k&&j==l)
										break;
									dir=(dir==1?2:3);
								}else{
									/* ww
									   ww */
									if(dir!=-1)
										System.out.println("error");
									break;
								}
							}
						}
					}
					c.add(k,l);
					if(visited[k][l]==0)
						visited[k][l]=1;
					if(dir==0)
						--k;
					else if(dir==1)
						--l;
					else if(dir==2)
						++k;
					else
						++l;
				}while(true);
			}
		}
	}
	public List<Contour2> getContours(){
		return contours;
	}
	public static void main(String[] args)throws Exception{
		java.awt.image.BufferedImage image=javax.imageio.ImageIO.read(new java.io.File("/home/kwong/projects/thesis/image/gray2.png"));
		image=new com.github.chungkwong.mathocr.preprocess.Grayscale().apply(image,true);
		image=new com.github.chungkwong.mathocr.preprocess.ThreholdSauvola(0.2,15).apply(image,true);
		image=new com.github.chungkwong.mathocr.preprocess.MedianFilter().apply(image,true);
		javax.imageio.ImageIO.write(image,"png",new java.io.File("/home/kwong/projects/thesis/image/gray2_median.png"));
		/*java.awt.image.BufferedImage image=javax.imageio.ImageIO.read(new java.io.File("/home/kwong/图片/sss3.png"));
		image=new net.sf.mathocr.preprocess.Grayscale().preprocess(image);
		image=new net.sf.mathocr.preprocess.ThreholdSauvola(0.2,15).preprocess(image);
		java.awt.image.BufferedImage output=new java.awt.image.BufferedImage(image.getWidth()+1,image.getHeight()+1,java.awt.image.BufferedImage.TYPE_INT_ARGB);
		java.awt.Graphics2D g2d=output.createGraphics();
		//int sum=0,n=0;
		for(Contour2 c:new ContourPool2(image).getContours()){
			c.compress();
			c.compress2();
			ListIterator<Integer> iteri=c.getI().listIterator(),iterj=c.getJ().listIterator();
			int previ=iteri.next(),prevj=iterj.next(),lasti=previ,lastj=prevj;
			if(c.isOuter())
				g2d.setColor(java.awt.Color.BLACK);
			else
				g2d.setColor(java.awt.Color.BLUE);
			while(iteri.hasNext()){
				int tmpi=iteri.next(),tmpj=iterj.next();
				g2d.drawRect(tmpj,tmpi,1,1);
				//g2d.drawLine(prevj,previ,tmpj,tmpi);
				previ=tmpi;
				prevj=tmpj;
			}
			g2d.drawRect(lastj,lasti,1,1);
			//g2d.drawLine(prevj,previ,lastj,lasti);
			//System.out.println(c);
			//sum+=c.getSize();
			//++n;
		}
		//System.out.println(sum/n);
		javax.imageio.ImageIO.write(output,"png",new java.io.File("/home/kwong/图片/sss4.png"));*/
	}
}