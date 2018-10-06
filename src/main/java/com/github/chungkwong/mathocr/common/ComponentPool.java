/* ComponentPool.java
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
package com.github.chungkwong.mathocr.common;
import com.github.chungkwong.mathocr.*;
import java.util.*;
/**
 * A data structure that store connected components
 */
public final class ComponentPool{
	final ArrayList<ConnectedComponent> components=new ArrayList<>();
	/**
	 * Construct a ComponentPool by connected component analysis
	 *
	 * @param pixels the input image
	 * @param width the width of the image
	 * @param height the height of the image
	 */
	public ComponentPool(int[] pixels,int width,int height){
		ConnectedComponentAnalysis(pixels,width,height);
	}
	/**
	 * Construct a ComponentPool by connected component analysis
	 *
	 * @param image the input image
	 */
	public ComponentPool(java.awt.image.BufferedImage image){
		int width=image.getWidth(), height=image.getHeight();
		ConnectedComponentAnalysis(image.getRGB(0,0,width,height,null,0,width),width,height);
	}
	/**
	 * Perform connected component analysis
	 *
	 * @param pixels pixel array of the image
	 * @param width the width of the image
	 * @param height the height of the image
	 */
	private void ConnectedComponentAnalysis(int[] pixels,int width,int height){
		int curr=0;
		//Integer tmp;
		Partition partition=new Partition(new Linkable(){
			public void link(int m,int n){
				components.get(n).combineWith(components.get(m));
				components.set(m,null);
			}
		});
		int[] last=new int[width];
		int lastlt=-1;
		for(int i=0;i<pixels.length;i++){
			pixels[i]&=0x1;
		}
		for(int j=0;j<width;j++){
			last[j]=-1;
		}
		for(int i=0, ind=0;i<height;i++){
			for(int j=0;j<width;j++,ind++){
				if(pixels[ind]==0){
					int id=(i!=0&&j!=0&&lastlt>=0)?lastlt:-1;
					int k=j;
					for(;k<width&&pixels[ind]==0;k++,ind++){
						if(i!=0&&last[k]>=0){
							if(id==-1){
								id=last[k];
							}else{
								partition.union(id,last[k]);
							}
						}
					}
					if(i!=0&&k!=width&&last[k]>=0){
						if(id==-1){
							id=last[k];
						}else{
							partition.union(id,last[k]);
						}
					}
					if(id==-1){
						components.add(new ConnectedComponent(new RunLength(i,j,k-j-1)));
						partition.makeSet();
						id=curr++;
					}else{
						id=partition.findRoot(id);
						components.get(id).addRunLengthToLast(new RunLength(i,j,k-j-1));
					}
					for(int l=j;l<k;l++){
						last[l]=id;
					}
					j=k-1;
					--ind;
				}else{
					lastlt=last[j];
					last[j]=-1;
				}
			}
		}
		trim(components);
		filterNoise();
	}
	/**
	 * Delete null element from a list
	 *
	 * @param components the list of ConnectedComponent
	 */
	private static void trim(List<ConnectedComponent> components){
		ListIterator<ConnectedComponent> iter=components.listIterator();
		while(iter.hasNext()){
			if(iter.next()==null){
				iter.remove();
			}
		}
	}
	/**
	 * Count number of components intersect with a given area
	 *
	 * @param left left bound
	 * @param right right bound
	 * @param top upper bound
	 * @param bottom lower bound
	 * @return the number
	 */
	public int countConnectedComponent(int left,int right,int top,int bottom){
		int c=0;
		for(ConnectedComponent ele:components){
			if(ele.getLeft()<=right&&left<=ele.getRight()&&top<=ele.getBottom()&&ele.getTop()<=bottom){
				++c;
			}
		}
		return c;
	}
	/**
	 * Get all the connected components
	 *
	 * @return a set of connected components
	 */
	public ArrayList<ConnectedComponent> getComponents(){
		return components;
	}
	/**
	 * Get average height of the connected components
	 *
	 * @return the average height
	 */
	public int getAverageHeight(){
		int sum=0;
		for(ConnectedComponent ele:components){
			sum+=ele.getHeight();
		}
		return sum/components.size();
	}
	/**
	 * Get average width of the connected components
	 *
	 * @return the average width
	 */
	public int getAverageWidth(){
		int sum=0;
		for(ConnectedComponent ele:components){
			sum+=ele.getWidth();
		}
		return sum/components.size();
	}
	public void filterNoise(){
		int threhold=Environment.ENVIRONMENT.getInteger("NOISE_THREHOLD");
		if(threhold>0){
			components.removeIf((c)->c.getWidth()<=threhold&&c.getHeight()<=threhold);
		}
	}
	/**
	 * Filter out small components
	 *
	 * @param width the width of the image
	 * @param height the width of the image
	 */
	public void filterNoise(int width,int height){
		ListIterator<ConnectedComponent> iter=components.listIterator();
		while(iter.hasNext()){
			ConnectedComponent ele=iter.next();
			int h=ele.getHeight(), w=ele.getWidth();
			if(h*w<=2||(h/w>20&&h>height/3)){
				iter.remove();
			}
		}
		/*int max=Math.max(width,height);
		int[] acc=new int[max+1];
		for(ConnectedComponent ele:components){
			++acc[ele.getWidth()];
			++acc[ele.getHeight()];
		}*/
	}
	/**
	 * Remove all connected components that touch bound of the image(they are
	 * likely to be noise)
	 */
	public void cleanPageEdges(int width,int height){
		ListIterator<ConnectedComponent> iter=components.listIterator();
		while(iter.hasNext()){
			ConnectedComponent ele=iter.next();
			if(ele.getLeft()==0||ele.getTop()==0||ele.getRight()==width-1||ele.getBottom()==height-1){
				iter.remove();
			}
		}
	}
	public BoundBox getBoundBox(){
		int xmin=Integer.MAX_VALUE, ymin=Integer.MAX_VALUE, xmax=0, ymax=0;
		for(ConnectedComponent component:components){
			if(component.getLeft()<xmin){
				xmin=component.getLeft();
			}
			if(component.getTop()<ymin){
				ymin=component.getTop();
			}
			if(component.getRight()>xmax){
				xmax=component.getRight();
			}
			if(component.getBottom()>ymax){
				ymax=component.getBottom();
			}
		}
		return new BoundBox(xmin,xmax,ymin,ymax);
	}
}
