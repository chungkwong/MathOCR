/* XYCut.java
 * =========================================================================
 * This file is originally part of the MathOCR Project
 *
 * Copyright (C) 2015 Chan Chung Kwong
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
package net.sf.mathocr.layout;
import java.util.*;
import net.sf.mathocr.common.*;
/**
 * A segment method using XY cut
 */
public final class XYCut implements SegmentMethod{
	/**
	 * Segment the page using XY cut and also set bound for the page
	 * @param page to be segmented
	 * @return physical blocks
	 */
	public LinkedList<Block> segment(Page page){
		LinkedList<Block> blocks=new LinkedList<Block>();
		ComponentPool pool=page.getComponentPool();
		int thre=3*pool.getAverageHeight()+1;
		int width=page.getModifiedImage().getWidth(),height=page.getModifiedImage().getHeight();
		Stack<ArrayList<ConnectedComponent>> stack=new Stack<ArrayList<ConnectedComponent>>();
		Stack<Bound> bounds=new Stack<Bound>();
		Stack<Boolean> hcut=new Stack<Boolean>();
		stack.push(pool.getComponents());
		bounds.push(new Bound(0,width-1,0,height-1));
		hcut.push(false);
		boolean[] projx=new boolean[width],projy=new boolean[height];
		boolean first=true;
		while(!stack.empty()){
			ArrayList<ConnectedComponent> components=stack.pop();
			Bound bound=bounds.pop();
			boolean hcuted=hcut.pop();
			int left=bound.left,right=bound.right,top=bound.top,bottom=bound.bottom;
			for(int i=top;i<=bottom;i++)
				projy[i]=false;
			for(int j=left;j<=right;j++)
				projx[j]=false;
			for(ConnectedComponent ele:components){
				int left1=ele.getLeft(),right1=ele.getRight(),top1=ele.getTop(),bottom1=ele.getBottom();
				for(int j=left1;j<=right1;j++)
					projx[j]=true;
				for(int i=top1;i<=bottom1;i++)
					projy[i]=true;
			}
			boolean pre=false;
			int start=Integer.MAX_VALUE,maxi=0,ymax=0;
			for(int i=top;i<=bottom;i++){
				boolean curr=projy[i];
				if(curr!=pre){
					if(curr){
						if(i-start>ymax){
							ymax=i-start;
							maxi=start;
						}else if(start==Integer.MAX_VALUE)
							top=i;
					}else{
						start=i;
					}
				}
				pre=curr;
			}
			if(!projy[bottom])
				bottom=start-1;
			pre=false;
			start=Integer.MAX_VALUE;
			int maxj=0,xmax=0;
			for(int j=left;j<=right;j++){
				boolean curr=projx[j];
				if(curr!=pre){
					if(curr){
						if(j-start>xmax){
							xmax=j-start;
							maxj=start;
						}else if(start==Integer.MAX_VALUE)
							left=j;
					}else{
						start=j;
					}
				}
				pre=curr;
			}
			if(!projx[right])
				right=start-1;
			if(xmax<=thre&&ymax<=thre){
				if(!components.isEmpty())
					blocks.add(new Block(page,components,left,right,top,bottom,!hcuted));
			}else if(ymax>=xmax){
				ArrayList<ConnectedComponent> components2=new ArrayList<ConnectedComponent>(components.size());
				ListIterator<ConnectedComponent> iter=components.listIterator();
				while(iter.hasNext()){
					ConnectedComponent ele=iter.next();
					if(ele.getTop()>=maxi){
						iter.remove();
						components2.add(ele);
					}
				}
				stack.push(components);
				bounds.push(new Bound(left,right,top,maxi-1));
				hcut.push(hcuted);
				stack.push(components2);
				bounds.push(new Bound(left,right,maxi+ymax,bottom));
				hcut.push(hcuted);
			}else{
				ArrayList<ConnectedComponent> components2=new ArrayList<ConnectedComponent>(components.size());
				ListIterator<ConnectedComponent> iter=components.listIterator();
				while(iter.hasNext()){
					ConnectedComponent ele=iter.next();
					if(ele.getLeft()>=maxj){
						iter.remove();
						components2.add(ele);
					}
				}
				stack.push(components);
				bounds.push(new Bound(left,maxj-1,top,bottom));
				hcut.push(true);
				stack.push(components2);
				bounds.push(new Bound(maxj+xmax,right,top,bottom));
				hcut.push(true);
			}
			if(first){
				page.setBound(left,right,top,bottom);
				first=false;
			}
		}
		//blocks.add(new Block(page,page.getComponentPool().getComponents(),0,page.getModifiedImage().getWidth(),0,page.getModifiedImage().getHeight()));
		return blocks;
	}
	class Bound{
		int left,right,top,bottom;
		Bound(int left,int right,int top,int bottom){
			this.left=left;this.right=right;this.top=top;this.bottom=bottom;
		}
	}
}