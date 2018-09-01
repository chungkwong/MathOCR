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
package com.github.chungkwong.mathocr.scene;
import com.github.chungkwong.mathocr.common.BoundBox;
import com.github.chungkwong.mathocr.common.ConnectedComponent;
import com.github.chungkwong.mathocr.common.RunLength;
import java.awt.*;
import java.util.*;
/**
 *
 * @author Chan Chung Kwong
 */
public class LineCandidate{
	private final java.util.List<ConnectedComponent> components;
	private Shape bound;
	private double angle;
	public LineCandidate(java.util.List<ConnectedComponent> components){
		this.components=components;
	}
	public LineCandidate(ConnectedComponent component){
		this.components=new ArrayList<>();
		components.add(component);
	}
	public void merge(LineCandidate candidate){
		components.addAll(candidate.getComponents());
		bound=null;
	}
	public java.util.List<ConnectedComponent> getComponents(){
		return components;
	}
	public double getAngle(){
		if(bound==null){
			updateBound();
		}
		return angle;
	}
	public Shape getBound(){
		BoundBox box=getBox();
		return new Rectangle(box.getLeft(),box.getTop(),box.getWidth(),box.getHeight());
		/*if(bound==null){
		updateBound();
		}
		return bound;*/
	}
	public BoundBox getBox(){
		if(components.isEmpty()){
			return new BoundBox(0,0,0,0);
		}else{
			int xmin=components.stream().mapToInt((c)->c.getLeft()).min().getAsInt();
			int xmax=components.stream().mapToInt((c)->c.getRight()).max().getAsInt();
			int ymin=components.stream().mapToInt((c)->c.getTop()).min().getAsInt();
			int ymax=components.stream().mapToInt((c)->c.getBottom()).max().getAsInt();
			return new BoundBox(xmin,xmax,ymin,ymax);
		}
	}
	private void updateBound(){
		updateSlope();
		double slope=Math.tan(angle);
		double lower=Double.NEGATIVE_INFINITY, upper=Double.POSITIVE_INFINITY;
		int left=Integer.MAX_VALUE, right=0;
		for(ConnectedComponent stroke:components){
			double predict=stroke.getLeft()*slope;
			double l=stroke.getBottom()-predict;
			double u=stroke.getTop()-predict;
			if(l>lower){
				lower=l;
			}
			if(u<upper){
				upper=u;
			}
			predict=stroke.getRight()*slope;
			l=stroke.getBottom()-predict;
			u=stroke.getTop()-predict;
			if(l>lower){
				lower=l;
			}
			if(u<upper){
				upper=u;
			}
			if(stroke.getLeft()<left){
				left=stroke.getLeft();
			}
			if(stroke.getRight()>right){
				right=stroke.getRight();
			}
		}
		bound=new Polygon(new int[]{left,left,right,right},
				new int[]{(int)(upper+left*slope),(int)(lower+left*slope),(int)(lower+right*slope),(int)(upper+right*slope)},4);
	}
	private void updateSlope(){
		double sumX=0, sumY=0, sumXX=0, sumXY=0, sumYY=0, len=0;
		for(ConnectedComponent component:components){
			for(RunLength rl:component.getRunLengths()){
				int y=rl.getY();
				int end=rl.getX()+rl.getCount();
				for(int x=rl.getX();x<=end;x++){
					sumX+=x;
					sumXX+=x*x;
					sumXY+=x*y;
					sumYY+=y*y;
					sumY+=y;
					++len;
				}
			}
		}
		double uxx=len*sumXX-sumX*sumX;
		double uxy=len*sumXY-sumX*sumY;
		double uyy=len*sumYY-sumY*sumY;
		angle=0.5*Math.atan(2*uxy/(uxx-uyy));
	}
}
