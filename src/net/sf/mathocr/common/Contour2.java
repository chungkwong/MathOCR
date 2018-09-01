/* Contour2.java
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
package net.sf.mathocr.common;
import java.util.*;
public final class Contour2{
	boolean outer;
	LinkedList<Integer> is,js,split;
	static final double TOLERANCE_DISTANCE=1.0,TOLERANCE_ANGLE=Math.atan(TOLERANCE_DISTANCE);
	public Contour2(boolean outer){
		is=new LinkedList<Integer>();
		js=new LinkedList<Integer>();
		this.outer=outer;
	}
	public Contour2(LinkedList<Integer> is,LinkedList<Integer> js){
		this.is=is;
		this.js=js;
		this.outer=outer;
	}
	public void add(int i,int j){
		is.addLast(i);
		js.addLast(j);
	}
	static final boolean isAdjoint(int i1,int j1,int i2,int j2){
		return (i1==i2&&Math.abs(j1-j2)==1)||(j1==j2&&Math.abs(i1-i2)==1);
	}
	public static final int LEFT=0,RIGHT=1,TOP=2,BOTTOM=3;
	static final int getDirection(int i1,int j1,int i2,int j2){
		if(j2>j1)
			return RIGHT;
		if(j2<j1)
			return LEFT;
		if(i2>i1)
			return BOTTOM;
		return TOP;
	}
	public boolean isClosed(){
		return isAdjoint(is.getFirst(),js.getFirst(),is.getLast(),js.getLast());
	}
	public int getSize(){
		return is.size();
	}
	public void compress(){
		is.addLast(is.getFirst());
		js.addLast(js.getFirst());
		ListIterator<Integer> iteri=is.listIterator(),iterj=js.listIterator();
		int previ=iteri.next(),prevj=iterj.next(),a=0,b=0;
		switch(getDirection(previ,prevj,iteri.next(),iterj.next())){
			case LEFT:
				a=-1;
				b=0;
				break;
			case RIGHT:
				a=1;
				b=0;
				break;
			case TOP:
				a=0;
				b=-1;
				break;
			case BOTTOM:
				a=0;
				b=1;
				break;
		}
		iteri.remove();
		iterj.remove();
		double angleMin=-TOLERANCE_ANGLE,angleMax=TOLERANCE_ANGLE;
		while(iteri.hasNext()){
			int i=iteri.next(),j=iterj.next();
			double d=Math.hypot(i-previ,j-prevj),t=Math.atan2(-(j-prevj)*b+(i-previ)*a,(j-prevj)*a+(i-previ)*b),dt=Math.asin(TOLERANCE_DISTANCE/d);
			if((i!=previ||j!=prevj)&&angleMin<=t&&angleMax>=t){
				if(t+dt<angleMax)
					angleMax=t+dt;
				if(t-dt>angleMin)
					angleMin=t-dt;
				iteri.remove();
				iterj.remove();
			}else{
				previ=i;
				prevj=j;
				if(iteri.hasNext()){
					switch(getDirection(previ,prevj,iteri.next(),iterj.next())){
						case LEFT:
							a=-1;
							b=0;
							break;
						case RIGHT:
							a=1;
							b=0;
							break;
						case TOP:
							a=0;
							b=-1;
							break;
						case BOTTOM:
							a=0;
							b=1;
							break;
					}
					angleMin=-TOLERANCE_ANGLE;
					angleMax=TOLERANCE_ANGLE;
					iteri.remove();
					iterj.remove();
				}else{
					iteri.remove();
					iterj.remove();
				}
			}
		}
	}
	public List<Integer> getI(){
		return is;
	}
	public List<Integer> getJ(){
		return js;
	}
	public void compress2(){
		if(is.size()<=2)
			return;
		int lastdi=is.getFirst()-is.getLast(),lastdj=is.getFirst()-is.getLast();
		ListIterator<Integer> iteri=is.listIterator(),iterj=js.listIterator();
		int previ=iteri.next(),prevj=iterj.next();
		while(iteri.hasNext()){
			int i=iteri.next(),j=iterj.next();
			int di=i-previ,dj=j-prevj;
			double t=Math.acos((lastdi*di+lastdj*dj)/(Math.hypot(di,dj)*Math.hypot(lastdi,lastdj)));
			if(t<Math.PI/3){
				iteri.previous();
				iteri.previous();
				iteri.remove();
				iteri.next();
				iterj.previous();
				iterj.previous();
				iterj.remove();
				iterj.next();
			}
			previ=i;
			prevj=j;
		}

	}
	public boolean isOuter(){
		return outer;
	}
	public double getLength(){
		double s=0;
		ListIterator<Integer> iteri=is.listIterator(),iterj=js.listIterator();
		int previ=iteri.next(),prevj=iterj.next(),lasti=previ,lastj=prevj;
		while(iteri.hasNext()){
			int i=iteri.next(),j=iterj.next();
			s+=Math.hypot(i-previ,j-prevj);
			previ=i;
			prevj=j;
		}
		s+=Math.hypot(lasti-previ,lastj-prevj);
		return s;
	}
	public double getArea(){
		double a=0;
		ListIterator<Integer> iteri=is.listIterator(),iterj=js.listIterator();
		int previ=iteri.next(),prevj=iterj.next(),lasti=previ,lastj=prevj;
		while(iteri.hasNext()){
			int i=iteri.next(),j=iterj.next();
			a+=previ*j-i*prevj;
			previ=i;
			prevj=j;
		}
		a+=previ*lastj-lasti*prevj;
		return 0.5*Math.abs(a);
	}
	private static final double square(double x){
		return x*x;
	}
	public double getRound(){
		return square(getLength())/getArea();
	}
	public String toString(){
		StringBuilder str=new StringBuilder();
		ListIterator<Integer> iteri=is.listIterator(),iterj=js.listIterator();
		while(iteri.hasNext()){
			str.append(iteri.next());
			str.append(",");
			str.append(iterj.next());
			str.append(";");
		}
		return str.toString();
	}
}