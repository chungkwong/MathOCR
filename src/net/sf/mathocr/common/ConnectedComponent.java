/* ConnectedComponent.java
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
package net.sf.mathocr.common;
import java.io.*;
import java.util.*;
/**
 * A data structure represent a connented component
 */
public final class ConnectedComponent implements Externalizable,Comparable<ConnectedComponent>{
	private static final long serialVersionUID=1L;
	public static final Comparator<ConnectedComponent> FROM_LEFT=new ConnectedComponentFromLeft();
	public int cordTop=Integer.MAX_VALUE,cordLeft=Integer.MAX_VALUE,cordBottom=0,cordRight=0;
	List<RunLength> runlengths=new LinkedList<RunLength>();
	/**
	 * Construct a empty ConnectedComponent
	 */
	public ConnectedComponent(){

	}
	/**
	 * Construct a empty ConnectedComponent with given bounds
	 * @param cordLeft left bound
	 * @param cordRight right bound
	 * @param cordTop upper bound
	 * @param cordBottom lower bound
	 */
	public ConnectedComponent(int cordLeft,int cordRight,int cordTop,int cordBottom){
		this.cordLeft=cordLeft;
		this.cordRight=cordRight;
		this.cordTop=cordTop;
		this.cordBottom=cordBottom;
		//addRunLength(new RunLength(cordTop,cordLeft,cordRight-cordLeft));
		//addRunLength(new RunLength(cordBottom,cordLeft,cordRight-cordLeft));
	}
	/**
	 * Construct a ConnectedComponent with a single RunLength
	 * @param runlength the RunLength
	 */
	public ConnectedComponent(RunLength runlength){
		addRunLength(runlength);
	}
	/**
	 * Add a RunLength
	 * @param runlength the RunLength to be added
	 */
	public void addRunLength(RunLength runlength){
		ListIterator<RunLength> iter=runlengths.listIterator();
		while(iter.hasNext())
			if(iter.next().compareTo(runlength)>0){
				iter.previous();
				break;
			}
		iter.add(runlength);
		if(runlength.y<cordTop)
			cordTop=runlength.y;
		if(runlength.x<cordLeft)
			cordLeft=runlength.x;
		if(runlength.y>cordBottom)
			cordBottom=runlength.y;
		if(runlength.x+runlength.count>cordRight)
			cordRight=runlength.x+runlength.count;
	}
	/**
	 * Add a RunLength provided that it is greater than all the RunLength already in the ConnectedComponent
	 * this method is added to enhance performance of connected component analysis
	 * @param runlength the RunLength to be added
	 */
	public void addRunLengthToLast(RunLength runlength){
		runlengths.add(runlength);
		if(runlength.y<cordTop)
			cordTop=runlength.y;
		if(runlength.x<cordLeft)
			cordLeft=runlength.x;
		if(runlength.y>cordBottom)
			cordBottom=runlength.y;
		if(runlength.x+runlength.count>cordRight)
			cordRight=runlength.x+runlength.count;
	}
	/**
	 * Get the runlengths in the component
	 * @return the runlengths
	 */
	public List<RunLength> getRunLengths(){
		return runlengths;
	}
	/**
	 * Merge this component with c
	 * @param c the component to merge
	 */
	public void combineWith(ConnectedComponent c){
		ListIterator<RunLength> iter=runlengths.listIterator(),iterc=c.runlengths.listIterator();
		RunLength currc=iterc.hasNext()?iterc.next():null;
		while(currc!=null){
			if(iter.hasNext()){
				if(iter.next().compareTo(currc)>0){
					iter.previous();
					iter.add(currc);
					currc=iterc.hasNext()?iterc.next():null;
				}
			}else{
				iter.add(currc);
				while(iterc.hasNext())
					iter.add(iterc.next());
				break;
			}
		}
		if(c.cordTop<cordTop)
			cordTop=c.cordTop;
		if(c.cordLeft<cordLeft)
			cordLeft=c.cordLeft;
		if(c.cordBottom>cordBottom)
			cordBottom=c.cordBottom;
		if(c.cordRight>cordRight)
			cordRight=c.cordRight;
	}
	/**
	 * Form a ConnectedComponent by combining a list of ConnectedComponent
	 * @param c the ConnectedComponent to be combined
	 * @return combination result
	 */
	public static ConnectedComponent combine(List<ConnectedComponent> c){
		//can be improved using binary merge
		ConnectedComponent ele=new ConnectedComponent();
		for(ConnectedComponent g:c){
			ListIterator<RunLength> iter=ele.runlengths.listIterator(),iterc=g.runlengths.listIterator();
			RunLength currc=iterc.hasNext()?iterc.next():null;
			while(currc!=null){
				if(iter.hasNext()){
					if(iter.next().compareTo(currc)>0){
						iter.previous();
						iter.add(currc);
						currc=iterc.hasNext()?iterc.next():null;
					}
				}else{
					iter.add(currc);
					while(iterc.hasNext())
						iter.add(iterc.next());
					break;
				}
			}
			if(g.cordTop<ele.cordTop)
				ele.cordTop=g.cordTop;
			if(g.cordLeft<ele.cordLeft)
				ele.cordLeft=g.cordLeft;
			if(g.cordBottom>ele.cordBottom)
				ele.cordBottom=g.cordBottom;
			if(g.cordRight>ele.cordRight)
				ele.cordRight=g.cordRight;
		}
		return ele;
	}
	/**
	 * Split this component horizontally
	 * @param x the coordinate of the line to be used to split
	 * @return the right one
	 */
	public ConnectedComponent splitHorizontally(int x){
		ConnectedComponent ele=new ConnectedComponent();
		Iterator<RunLength> iter=runlengths.iterator();
		cordTop=Integer.MAX_VALUE;
		cordBottom=0;
		while(iter.hasNext()){
			RunLength length=iter.next();
			if(length.getX()>x){
				ele.addRunLengthToLast(length);
				iter.remove();
			}else{
				if(length.getX()+length.getCount()>x){
					ele.addRunLengthToLast(new RunLength(length.getY(),x+1,length.getX()+length.getCount()-x-1));
					length.reset(length.getY(),length.getX(),x-length.getX());
				}
				if(length.y<cordTop)
					cordTop=length.y;
				if(length.y>cordBottom)
					cordBottom=length.y;
			}
		}
		cordRight=x;
		return ele;
	}
	/**
	 * Split this component vertically
	 * @param y the coordinate of the line to be used to split
	 * @return the lower one
	 */
	public ConnectedComponent splitVertically(int y){
		ConnectedComponent ele=new ConnectedComponent();
		Iterator<RunLength> iter=runlengths.iterator();
		cordLeft=Integer.MAX_VALUE;
		cordRight=0;
		while(iter.hasNext()){
			RunLength length=iter.next();
			if(length.getY()>y){
				ele.addRunLengthToLast(length);
				iter.remove();
			}else{
				if(length.x<cordLeft)
					cordLeft=length.x;
				if(length.x+length.count>cordRight)
					cordRight=length.x+length.count;
			}
		}
		cordBottom=y;
		return ele;
	}
	/**
	 * Get the position of the top of the component in the image
	 * @return the coordinate
	 */
	public int getTop(){
		return cordTop;
	}
	/**
	 * Get the position of the bottom of the component in the image
	 * @return the coordinate
	 */
	public int getBottom(){
		return cordBottom;
	}
	/**
	 * Get the position of the left of the component in the image
	 * @return the coordinate
	 */
	public int getLeft(){
		return cordLeft;
	}

	/**
	 * Get the position of the right of the component in the image
	 * @return the coordinate
	 */
	public int getRight(){
		return cordRight;
	}
	/**
	 * Get the width
	 * @return width
	 */
	public int getWidth(){
		return cordRight-cordLeft+1;
	}
	/**
	 * Get the height
	 * @return height
	 */
	public int getHeight(){
		return cordBottom-cordTop+1;
	}
	/**
	 * Compute horizontal crossing numbers
	 * @return horizontal crossing numbers
	 */
	public byte[] getHorizontalCrossing(){
		byte[] cross=new byte[getHeight()];
		for(RunLength rl:runlengths)
			++cross[rl.getY()-cordTop];
		return cross;
	}
	/**
	 * Compute vertical crossing numbers
	 * @return vertical crossing numbers
	 */
	public byte[] getVerticalCrossing(){
		byte[] cross=new byte[getWidth()],prev=new byte[getWidth()];
		Iterator<RunLength> iter=runlengths.iterator();
		RunLength curr=iter.hasNext()?iter.next():null;
		for(int i=cordTop;i<=cordBottom;i++){
			while(curr!=null&&curr.getY()==i){
				for(int j=0,k=curr.getX()-cordLeft;j<=curr.getCount();j++,k++){
					if(prev[k]==0)
						++cross[k];
					prev[k]=-1;
				}
				curr=iter.hasNext()?iter.next():null;
			}
			for(int k=0;k<prev.length;k++)
				prev[k]=(byte)(prev[k]==-1?1:0);
		}
		return cross;
	}
	/**
	 * Compute horizontal crossing characteristic
	 * @return horizontal crossing characteristic
	 */
	public String getHorizontalChar(){
		Iterator<RunLength> iter=runlengths.iterator();
		RunLength curr=iter.hasNext()?iter.next():null;
		byte last=-1;
		String ch="";
		for(int i=cordTop;i<=cordBottom;i++){
			byte count=0;
			while(curr!=null&&curr.getY()==i){
				++count;
				curr=iter.hasNext()?iter.next():null;
			}
			if(i==cordTop||last!=count){
				last=count;
				ch+=Character.forDigit(count,36);
			}
		}
		return ch;
	}
	/**
	 * Compute vertical crossing characteristic
	 * @return vertical crossing characteristic
	 */
	public String getVerticalChar(){
		//byte[] count=new byte[getWidth()],prev=new byte[getWidth()];
		byte[] count=getVerticalCrossing();
		String ch="";
		/*RunLength curr=runlengths.first();
		for(int i=cordTop;i<=cordBottom;i++){
			while(curr!=null&&curr.getY()==i){
				for(int j=0,k=curr.getX()-cordLeft;j<=curr.getCount();j++,k++){
					if(prev[k]==0)
						++count[k];
					prev[k]=-1;
				}
				curr=runlengths.higher(curr);
			}
			for(int k=0;k<prev.length;k++)
				prev[k]=(byte)(prev[k]==-1?1:0);
		}*/
		LinkedList<Byte> parts=new LinkedList<Byte>();
		byte pre=count[0];
		parts.add(pre);
		ch+=Character.forDigit(pre,36);
		for(int j=1;j<count.length;j++){
			byte curr=count[j];
			if(pre!=curr){
				parts.add(curr);
				ch+=Character.forDigit(curr,36);
			}
			pre=curr;
		}
		return ch;
	}
	/**
	 * Compute power
	 * @param base the base
	 * @param exp the exponent
	 * @return the power
	 */
	private float pow(float base,int exp){
		float re=1;
		while(--exp>=0)
			re*=base;
		return re;
	}
	/**
	 * Get the moment
	 * @param p the order of x
	 * @param q the order of y
	 * @return the moment
	 */
	public float getMoment(int p,int q){
		float moment=0;
		int count=0;
		for(RunLength rl:runlengths){
			int y=rl.getY()-cordTop,x=rl.getX()-cordLeft-1;
			for(int j=0;j<=rl.getCount();j++){
				moment+=pow(++x,p)*pow(y,q);
				++count;
			}
		}
		return moment/count;
	}
	/**
	 * Get the standardized moment
	 * @param p the order of x
	 * @param q the order of y
	 * @return the moment
	 */
	public float getCentralMoment(int p,int q){
		float moment=0;
		int count=0;
		float xmean=getMoment(1,0)+cordLeft,ymean=getMoment(0,1)+cordTop;
		for(RunLength rl:runlengths){
			float y=rl.getY()-ymean,x=rl.getX()-xmean-1;
			for(int j=0;j<=rl.getCount();j++){
				moment+=pow(++x,p)*pow(y,q);
				++count;
			}
		}
		return (float)(moment/Math.pow(count,(p+q)*0.5+1));
		//return moment/pow(getWidth(),p)/pow(getHeight(),q)/count;
	}
	/**
	 * Get the horizontal center
	 * @return the horizontal center
	 */
	public float getCenterX(){
		return getMoment(1,0)/getWidth();
	}
	/**
	 * Get the vertical center
	 * @return the vertical center
	 */
	public float getCenterY(){
		return getMoment(0,1)/getHeight();
	}
	/**
	 * Get the direction
	 * @return direction
	 */
	public float getDirection(){
		return 0.5f*(float)Math.atan2(2*getCentralMoment(1,1),getCentralMoment(2,0)-getCentralMoment(0,2));
	}
	/**
	 * Get the density
	 * @return density
	 */
	public float getDensity(){
		float total=0;
		for(RunLength rl:runlengths)
			total+=rl.getCount()+1;
		return total/getWidth()/getHeight();
	}
	/**
	 * Get the number of holes
	 * @return number of holes
	 */
	public int getNumberOfHoles(){
		int width=getWidth(),height=getHeight();
		int holes=-1;
		int curr_id=0;
		int[] label=new int[width+2];
		ArrayList<Integer> parent=new ArrayList<Integer>();
		parent.add(-1);
		Iterator<RunLength> iter=runlengths.iterator();
		RunLength curr=iter.hasNext()?iter.next():null;
		for(int i=0;i<height;i++){
			int[] tmp=new int[width+2];
			while(curr!=null&&curr.getY()-cordTop==i){
				for(int j=curr.getX()-cordLeft+1,k=0;k<=curr.getCount();j++,k++)
					tmp[j]=-1;
				curr=iter.hasNext()?iter.next():null;
			}
			for(int j=1;j<width+2;j++)
				if(tmp[j]==0){
					TreeSet<Integer> neighbour=new TreeSet<Integer>();
					if(label[j-1]>=0)
						neighbour.add(label[j-1]);
					if(label[j]>=0)
						neighbour.add(label[j]);
					if(j<=width&&label[j+1]>=0)
						neighbour.add(label[j+1]);
					if(tmp[j-1]>=0)
						neighbour.add(tmp[j-1]);
					if(neighbour.isEmpty()){
						parent.add(-1);
						tmp[j]=++curr_id;
					}else{
						Integer first=neighbour.first();
						tmp[j]=first;
						Integer next=first;
						while((next=neighbour.higher(next))!=null)
							parent.set(next,first);
					}
				}
			/*for(int j=1;j<width+2;j++)
				if(tmp[j]==0)
					if(label[j]>=0&&tmp[j-1]>=0){
						if(label[j]==tmp[j-1])
							tmp[j]=tmp[j-1];
						else if(label[j]<tmp[j-1]){
							parent.set(tmp[j-1],label[j]);
							tmp[j]=label[j];
						}else{
							parent.set(label[j],tmp[j-1]);
							tmp[j]=tmp[j-1];
						}
					}else if(label[j]>=0&&tmp[j-1]<0)
						tmp[j]=label[j];
					else if(label[j]<0&&tmp[j-1]>=0)
						tmp[j]=tmp[j-1];
					else if(label[j]<0&&tmp[j-1]<0){
						parent.add(-1);
						tmp[j]=++curr_id;
					}*/
			label=tmp;
		}
		for(int j=1;j<width+1;j++)
			if(label[j]>0)
				parent.set(label[j],0);
		for(Integer par:parent)
			if(par==-1)
				++holes;
		return holes;
	}
	/**
	 * Get 3 times 3 grid characteristic
	 * @return the grid characteristic
	 */
	public float[] getGrid(){
		int h=getHeight(),w=getWidth();
		int[] ptv=new int[]{0,h/3,h-h/3,h},pth=new int[]{0,w/3,w-w/3,w};
		float[] vec=new float[9];
		float sum=0;
		Iterator<RunLength> iter=runlengths.iterator();
		RunLength curr=iter.hasNext()?iter.next():null;
		for(int i=0;i<3;i++){
			while(curr!=null&&curr.getY()-cordTop<ptv[i+1]){
				int j1=curr.getX()-cordLeft,j2=j1+curr.getCount();
				for(int j=0,ind=3*i;j<3;j++,ind++)
					if(j1<pth[j+1])
						if(j2<pth[j+1]){
							vec[ind]+=(j2-j1+1);
							break;
						}else{
							vec[ind]+=(pth[j+1]-j1);
							j1=pth[j+1];
						}
				curr=iter.hasNext()?iter.next():null;
			}
		}
		for(int i=0,ind=0;i<3;i++)
			for(int j=0;j<3;j++,ind++){
				if(vec[ind]!=0)
					vec[ind]/=((ptv[i+1]-ptv[i])*(pth[j+1]-pth[j]));
				sum+=vec[ind];
			}
		for(int i=0,ind=0;i<3;i++)
			for(int j=0;j<3;j++,ind++)
				vec[ind]/=sum;
		return vec;
	}
	/**
	 * Get two dimension pixels array representation
	 * @return pixels array
	 */
	public byte[][] toPixelArray(){
		byte[][] pixels=new byte[getHeight()][getWidth()];
		for(RunLength rl:runlengths){
			int i=rl.getY()-cordTop,j=rl.getX()-cordLeft-1;
			for(int k=0;k<=rl.getCount();k++)
				pixels[i][++j]=1;
		}
		return pixels;
	}
	/**
	 * Get one dimension pixels array representation
	 * @return pixels array
	 */
	public byte[] toPixelArray2(){
		byte[] pixels=new byte[getHeight()*getWidth()];
		int width=getWidth();
		for(RunLength rl:runlengths){
			int i=rl.getY()-cordTop,j=rl.getX()-cordLeft-1;
			for(int k=0;k<=rl.getCount();k++)
				pixels[i*width+(++j)]=1;
		}
		return pixels;
	}
	/**
	 * Get String that show the shape
	 * @return the string that show the shape
	 */
	public String toString(){
		byte[][] pixels=toPixelArray();
		int height=pixels.length,width=pixels[0].length;
		StringBuilder str=new StringBuilder((width+1)*height);
		for(int i=0;i<height;i++){
			for(int j=0;j<width;j++)
				str.append(pixels[i][j]);
			str.append('\n');
		}
		return str.toString();
	}
	/**
	 * Write this object to stream
	 * @param out output stream
	 */
	public void writeExternal(ObjectOutput out)throws IOException{
		out.writeByte(cordRight-cordLeft);
		out.writeByte(cordBottom-cordTop);
		for(RunLength rl:runlengths){
			out.writeByte(rl.getY()-cordTop);
			out.writeByte(rl.getX()-cordLeft);
			out.writeByte(rl.getCount());
		}
		out.writeByte(255);
	}
	/**
	 * Load ConnectedComponent from stream
	 * @param in input stream
	 */
	public void readExternal(ObjectInput in)throws IOException{
		cordLeft=0;
		cordTop=0;
		cordRight=in.readUnsignedByte();
		cordBottom=in.readUnsignedByte();
		while(true){
			int first=in.readUnsignedByte();
			if(first==255)
				break;
			runlengths.add(new RunLength(first,in.readUnsignedByte(),in.readUnsignedByte()));
		}
	}
	/**
	 * Compare left bound of ConnectedComponent
	 * @param ele to be compared to
	 * @return a integer
	 */
	public int compareTo(ConnectedComponent ele){
		return cordTop-ele.cordTop;
	}
}
class ConnectedComponentFromLeft implements Comparator<ConnectedComponent>{
	public int compare(ConnectedComponent ele,ConnectedComponent ele2){
		return ele.getLeft()-ele2.getLeft();
	}
	public boolean equals(ConnectedComponent ele,ConnectedComponent ele2){
		return ele.equals(ele2);
	}
}