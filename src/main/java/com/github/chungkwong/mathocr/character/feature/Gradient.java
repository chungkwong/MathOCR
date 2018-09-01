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
package com.github.chungkwong.mathocr.character.feature;
import com.github.chungkwong.mathocr.common.ConnectedComponent;
import com.github.chungkwong.mathocr.common.RunLength;
import com.github.chungkwong.mathocr.character.VectorFeature;
import com.github.chungkwong.mathocr.common.Bitmap;
import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.util.*;
/**
 * Gradient
 *
 * @author Chan Chung Kwong
 */
public class Gradient implements VectorFeature{
	public static final String NAME="GRADIENT";
	private final int m, n;
	/**
	 * Create a gradient feature
	 *
	 * @param m rows of grid
	 * @param n columns of grid
	 */
	public Gradient(int m,int n){
		this.m=m;
		this.n=n;
	}
	@Override
	public int getDimension(){
		return m*n*4;
	}
	@Override
	public double[] extract(ConnectedComponent component){
		final byte none=0, horizontal=1, vertical=2, diagDown=4, diagUp=8;
		final int dd=3, dx=2;
		double[] vec=new double[m*n*4];
		int h=component.getHeight(), w=component.getWidth();
		int[] nL=new int[w];
		int[] nwL=new int[w];
		int[] neL=new int[w];
		int[] stroke=new int[w*h];
		byte[] direction=new byte[w*h];
		Iterator<RunLength> iterator=component.getRunLengths().iterator();
		Collections.sort(component.getRunLengths());
		RunLength rl=iterator.hasNext()?iterator.next():null;
		for(int i=0, ind=0;i<=h;i++){
			int j=0;
			int nwNext=0;
			while(rl!=null&&rl.getY()-component.getTop()==i){
				for(;j<rl.getX()-component.getLeft();j++,ind++){
					for(int k=1;k<=nL[j];k++){
						if(nL[j]*dx<stroke[ind-k*w]){
							stroke[ind-k*w]=nL[j]*dx;
							direction[ind-k*w]=vertical;
						}else if(nL[j]*dx==stroke[ind-k*w]){
							direction[ind-k*w]|=vertical;
						}
					}
					nL[j]=0;
					for(int k=1;k<=nwL[j];k++){
						int pos=ind-k*w-k;
						if(nwL[j]*dd<stroke[pos]){
							stroke[pos]=nwL[j]*dd;
							direction[pos]=diagDown;
						}else if(nwL[j]*dd==stroke[pos]){
							direction[pos]|=diagDown;
						}
					}
					nwL[j]=nwNext;
					nwNext=0;
					for(int k=1;k<=neL[j];k++){
						int pos=ind-k*w+k;
						if(neL[j]*dd<stroke[pos]){
							stroke[pos]=neL[j]*dd;
							direction[pos]=diagUp;
						}else if(neL[j]*dd==stroke[pos]){
							direction[pos]|=diagUp;
						}
					}
					if(j>0){
						neL[j-1]=0;
					}
				}
				if(rl.getX()==component.getLeft()){
					for(int k=1;k<=neL[0];k++){
						int pos=ind-k*w+k;
						if((neL[0]+1)*dd<stroke[pos]){
							stroke[pos]=(neL[0]+1)*dd;
							direction[pos]=diagUp;
						}else if((neL[0]+1)*dd==stroke[pos]){
							direction[pos]|=diagUp;
						}
					}
					stroke[ind]=(rl.getCount()+1)*dx;
					if(stroke[ind]<(neL[0]+1)*dd){
						direction[ind]=horizontal;
					}else if(stroke[ind]==(neL[0]+1)*dd){
						direction[ind]|=horizontal;
					}else{
						stroke[ind]=(neL[0]+1)*dd;
						direction[ind]=diagUp;
					}
					if(j!=0){
						++nL[j];
					}
					int tmp=nwL[j]+1;
					nwL[j]=nwNext;
					nwNext=tmp;
				}else{
					stroke[ind]=(rl.getCount()+1)*dx;
					direction[ind]=horizontal;
					++nL[j];
					int tmp=nwL[j]+1;
					nwL[j]=nwNext;
					nwNext=tmp;
					neL[j-1]=neL[j]+1;
				}
				++j;
				++ind;
				for(;j<rl.getX()+rl.getCount()+1-component.getLeft();j++,ind++){
					stroke[ind]=(rl.getCount()+1)*dx;
					direction[ind]=horizontal;
					++nL[j];
					int tmp=nwL[j]+1;
					nwL[j]=nwNext;
					nwNext=tmp;
					if(j>0){
						neL[j-1]=neL[j]+1;
					}
				}
				if(rl.getX()+rl.getCount()==component.getRight()){
					for(int k=0;k<=nwNext-1;k++){
						int pos=ind-1-k*w-k;
						if(nwNext*dd<stroke[pos]){
							stroke[pos]=nwNext*dd;
							direction[pos]=diagDown;
						}else if(nwNext*dd==stroke[pos]){
							direction[pos]|=diagDown;
						}
					}
				}
				rl=iterator.hasNext()?iterator.next():null;
			}
			for(;j<w;j++,ind++){
				for(int k=1;k<=nL[j];k++){
					if(nL[j]*dx<stroke[ind-k*w]){
						stroke[ind-k*w]=nL[j]*dx;
						direction[ind-k*w]=vertical;
					}else if(nL[j]*dx==stroke[ind-k*w]){
						direction[ind-k*w]|=vertical;
					}
				}
				nL[j]=0;
				for(int k=1;k<=nwL[j];k++){
					int pos=ind-k*w-k;
					if(nwL[j]*dd<stroke[pos]){
						stroke[pos]=nwL[j]*dd;
						direction[pos]=diagDown;
					}else if(nwL[j]*dd==stroke[pos]){
						direction[pos]|=diagDown;
					}
				}
				nwL[j]=nwNext;
				nwNext=0;
				for(int k=1;k<=neL[j];k++){
					int pos=ind-k*w+k;
					if(neL[j]*dd<stroke[pos]){
						stroke[pos]=neL[j]*dd;
						direction[pos]=diagUp;
					}else if(neL[j]*dd==stroke[pos]){
						direction[pos]|=diagUp;
					}
				}
				if(j>0){
					neL[j-1]=0;
				}
			}
		}
//		for(int i=0, ind=0;i<h;i++){
//			for(int j=0;j<w;j++,ind++){
//				System.out.print(direction[ind]);
//			}
//			System.out.println();
//		}
		double scale=Math.max(h*1.0/m,w*1.0/n);
		double a=m*0.5-(component.getBottom()+component.getTop()+1)*0.5/scale;
		double b=n*0.5-(component.getLeft()+component.getRight()+1)*0.5/scale;
		int[] ptv=new int[m+1], pth=new int[n+1];
		for(int i=0;i<=m;i++){
			ptv[i]=(int)(scale*(i-a));
		}
		for(int j=0;j<=n;j++){
			pth[j]=(int)(scale*(j-b));
		}
		Bitmap map=Bitmap.from(component);
		for(int i=0, ind=0;i<m;i++){
			for(int j=0;j<n;j++,ind+=4){
				for(int y=ptv[i];y<ptv[i+1];y++){
					if(y<component.getTop()||y>component.getBottom()){
						continue;
					}
					for(int x=pth[j];x<pth[j+1];x++){
						if(x<component.getLeft()||x>component.getRight()){
							continue;
						}
						int pos=(y-component.getTop())*w+(x-component.getLeft());
						if((direction[pos]&horizontal)!=0){//FIXME
							++vec[ind];
						}
						if((direction[pos]&vertical)!=0){
							++vec[ind+1];
						}
						if((direction[pos]&diagDown)!=0){
							++vec[ind+2];
						}
						if((direction[pos]&diagUp)!=0){
							++vec[ind+3];
						}
					}
				}
			}
		}
		for(int i=0, ind=0;i<m;i++){
			for(int j=0;j<n;j++,ind+=4){
				double area=(ptv[i+1]-ptv[i])*(pth[j+1]-pth[j]);
				if(area!=0){
					vec[ind]/=area;
					vec[ind+1]/=area;
					vec[ind+2]/=area;
					vec[ind+3]/=area;
				}
			}
		}
		return vec;
	}
	public static void main(String[] args){
		Font font=Font.decode(Font.SERIF).deriveFont(24).deriveFont(AffineTransform.getScaleInstance(10,10));
		ConnectedComponent component=getComponent(font,'+');
		System.out.println(Arrays.toString(new Gradient(3,3).extract(component)));
	}
	private static ConnectedComponent getComponent(Font font,int codePoint){
		FontRenderContext context=new FontRenderContext(null,false,true);
		GlyphVector glyphVector=font.createGlyphVector(context,new String(new int[]{codePoint},0,1));
		float x=(float)glyphVector.getVisualBounds().getX();
		float y=(float)glyphVector.getVisualBounds().getY();
		int width=(int)(glyphVector.getVisualBounds().getWidth()+0.5);
		int height=(int)(glyphVector.getVisualBounds().getHeight()+0.5);
		BufferedImage bi=new BufferedImage(width,height,BufferedImage.TYPE_BYTE_BINARY);
		Graphics2D g2d=bi.createGraphics();
		g2d.setColor(Color.WHITE);
		g2d.fillRect(0,0,width,height);
		g2d.setColor(Color.BLACK);
		g2d.drawGlyphVector(glyphVector,-x,-y);
		return new ConnectedComponent(bi);
	}
}
