/* Table.java
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
package com.github.chungkwong.mathocr.layout.logical;
import com.github.chungkwong.mathocr.text.structure.Line;
import com.github.chungkwong.mathocr.common.BoundBox;
import java.util.*;
/**
 * A data structure representing table
 */
public final class Table extends LogicalBlock{
	private final String path;
	private List<Line> caption;
	/**
	 * Construct a Table
	 *
	 * @param path the path to the image file
	 * @param caption the caption
	 * @param box the bounding box
	 */
	public Table(String path,List<Line> caption,BoundBox box){
		super(box,true);
		this.path=path;
		this.caption=caption;
	}
	/**
	 * Get the path to the image file
	 *
	 * @return the path
	 */
	public String getPath(){
		return path;
	}
	/**
	 * Get the caption of the image
	 *
	 * @return the caption
	 */
	public List<Line> getCaption(){
		return caption;
	}
	/**
	 * Set the path to the image
	 *
	 * @param caption the caption
	 */
	public void setCaption(List<Line> caption){
		this.caption=caption;
	}
}
/*import net.sf.mathocr.common.*;
import java.util.*;
public final class Table implements LogicalBlock{
	public static final int ALIGN_LEFT=0,ALIGN_RIGHT=1,ALIGN_CENTER=2;
	int left,right,top,bottom;
	String caption;
	int m,n;
	boolean[][] hline,vline;
	String[][] content;
	LinkedList<Integer> vs=new LinkedList<Integer>(),hs=new LinkedList<Integer>();
		public Table(Block block){
		ListIterator<ConnectedComponent> iter=block.getComponents().listIterator();
		ConnectedComponent frame=iter.next();
		constructGrid(frame);
		int i=0,j=0;
		while(iter.hasNext()){

		}
	}
	void constructGrid(ConnectedComponent frame){
		List<RunLength> rls=frame.getRunLengths();
		int sum=0,count=0,local=0,prev=-1;
		for(RunLength rl:rls)
			if(prev==rl.getY())
				local+=rl.getCount()+1;
			else{
				sum+=local;
				prev=rl.getY();
				local=rl.getCount()+1;
				++count;
			}
		sum=(sum+local)/count;
		vs.addLast(frame.getTop());
		prev=-1;
		local=-1;
		int l=frame.getLeft(),r=frame.getRight(),len=r-l+1,last=frame.getTop();
		int[] projx=new int[len];
		for(RunLength rl:rls){
			if(prev==rl.getY())
				local+=rl.getCount()+1;
			else{
				if(local>sum){
					if(prev-last>sum)
						vs.addLast(prev);
					last=prev;
				}
				prev=rl.getY();
				local=rl.getCount()+1;
			}
			int j0=rl.getX()-l+rl.getCount();
			for(int j=rl.getX()-l;j<=j0;j++)
				++projx[j];
		}
		if(local>sum&&prev-last>sum){
			vs.addLast(prev);
			last=prev;
		}
		if(frame.getBottom()-last>sum)
			vs.addLast(frame.getBottom());
		hs.addLast(l);
		last=l;
		for(int j=0;j<len;j++)
			if(projx[j]>sum){
				if(j-last>sum)
					hs.addLast(j+l);
				last=j;
			}
		if(r-last>sum)
			vs.addLast(r);
		m=vs.size()-1;
		n=hs.size()-1;
		hline=new boolean[m+1][n];
		vline=new boolean[m][n+1];

	}
	public int getNumberOfRows(){
		return m;
	}
	public int getNumberOfColumns(){
		return n;
	}
	public boolean isFloating(){
		return true;
	}
	public String getCaption(){
		return caption;
	}
	public void setCaption(String caption){
		this.caption=caption;
	}
	public int getLeft(){
		return left;
	}
	public int getRight(){
		return right;
	}
	public int getTop(){
		return top;
	}
	public int getBottom(){
		return bottom;
	}
	public String toHTMLForm(){
		StringBuilder str=new StringBuilder();
		str.append("<table>\n");

		str.append("</table>\n");
		return str.toString();
	}
	public String toLaTeXForm(){
		StringBuilder str=new StringBuilder();
		str.append("\\begin{table}\n");
		str.append("\\begin{tabular}{");

		str.append("}");

		str.append("\\end{tabular}\n");
		if(caption!=null)
			str.append("\\caption{"+caption+"}");
		str.append("\\end{table}\n");
		return str.toString();
	}
}*/
