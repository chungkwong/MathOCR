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
	public enum Mask{
		FULL, CONTOUR, SKELETON
	};
	public static final String NAME="GRADIENT";
	public static final String FULL_NAME="FULL_GRADIENT";
	public static final String SKELETON_NAME="SKELETON_GRADIENT";
	private final int m, n;
	private final Mask mask;
	/**
	 * Create a gradient feature
	 *
	 * @param m rows of grid
	 * @param n columns of grid
	 * @param mask only consider contour
	 */
	public Gradient(int m,int n,Mask mask){
		this.m=m;
		this.n=n;
		this.mask=mask;
	}
	@Override
	public int getDimension(){
		return m*n*4;
	}
	public static final byte NONE=0, HORIZONTAL=1, VERTICAL=2, DIAG_DOWN=4, DIAG_UP=8;
	public static final int DD=3;
	public static final int DX=2;
	@Override
	public double[] extract(ConnectedComponent component){
		double[] vec=new double[m*n*4];
		int h=component.getHeight(), w=component.getWidth();
		double scale=Math.max(h*1.0/m,w*1.0/n);
		double a=m*0.5-(component.getBottom()+component.getTop()+1)*0.5/scale;
		double b=n*0.5-(component.getLeft()+component.getRight()+1)*0.5/scale;
		StrokeSpace strokes=getStrokeDirection(component);
		if(mask==Mask.SKELETON){
			remainSkeleton(strokes);
		}
		byte[] direction=strokes.getDirection();
		int[] ptv=new int[m+1], pth=new int[n+1];
		for(int i=0;i<=m;i++){
			ptv[i]=(int)(scale*(i-a));
		}
		for(int j=0;j<=n;j++){
			pth[j]=(int)(scale*(j-b));
		}
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
						if(mask==Mask.CONTOUR&&y!=component.getTop()&&y!=component.getBottom()&&x!=component.getLeft()&&x!=component.getRight()){
							if(direction[pos-1]!=NONE&&direction[pos+1]!=NONE
									&&direction[pos-w]!=NONE&&direction[pos-w-1]!=NONE&&direction[pos-w+1]!=NONE
									&&direction[pos+w]!=NONE&&direction[pos+w-1]!=NONE&&direction[pos+w+1]!=NONE){
								continue;
							}
						}
						if((direction[pos]&HORIZONTAL)!=0){
							++vec[ind];
						}
						if((direction[pos]&VERTICAL)!=0){
							++vec[ind+1];
						}
						if((direction[pos]&DIAG_DOWN)!=0){
							++vec[ind+2];
						}
						if((direction[pos]&DIAG_UP)!=0){
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
	public static StrokeSpace getStrokeDirection(ConnectedComponent component){
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
						if(nL[j]*DX<stroke[ind-k*w]){
							stroke[ind-k*w]=nL[j]*DX;
							direction[ind-k*w]=VERTICAL;
						}else if(nL[j]*DX==stroke[ind-k*w]){
							direction[ind-k*w]|=VERTICAL;
						}
					}
					nL[j]=0;
					for(int k=1;k<=nwL[j];k++){
						int pos=ind-k*w-k;
						if(nwL[j]*DD<stroke[pos]){
							stroke[pos]=nwL[j]*DD;
							direction[pos]=DIAG_DOWN;
						}else if(nwL[j]*DD==stroke[pos]){
							direction[pos]|=DIAG_DOWN;
						}
					}
					nwL[j]=nwNext;
					nwNext=0;
					for(int k=1;k<=neL[j];k++){
						int pos=ind-k*w+k;
						if(neL[j]*DD<stroke[pos]){
							stroke[pos]=neL[j]*DD;
							direction[pos]=DIAG_UP;
						}else if(neL[j]*DD==stroke[pos]){
							direction[pos]|=DIAG_UP;
						}
					}
					if(j>0){
						neL[j-1]=0;
					}
				}
				if(rl.getX()==component.getLeft()){
					for(int k=1;k<=neL[0];k++){
						int pos=ind-k*w+k;
						if((neL[0]+1)*DD<stroke[pos]){
							stroke[pos]=(neL[0]+1)*DD;
							direction[pos]=DIAG_UP;
						}else if((neL[0]+1)*DD==stroke[pos]){
							direction[pos]|=DIAG_UP;
						}
					}
					stroke[ind]=(rl.getCount()+1)*DX;
					if(stroke[ind]<(neL[0]+1)*DD){
						direction[ind]=HORIZONTAL;
					}else if(stroke[ind]==(neL[0]+1)*DD){
						direction[ind]|=HORIZONTAL;
					}else{
						stroke[ind]=(neL[0]+1)*DD;
						direction[ind]=DIAG_UP;
					}
					if(j!=0){
						++nL[j];
					}
					int tmp=nwL[j]+1;
					nwL[j]=nwNext;
					nwNext=tmp;
				}else{
					stroke[ind]=(rl.getCount()+1)*DX;
					direction[ind]=HORIZONTAL;
					++nL[j];
					int tmp=nwL[j]+1;
					nwL[j]=nwNext;
					nwNext=tmp;
					neL[j-1]=neL[j]+1;
				}
				++j;
				++ind;
				for(;j<rl.getX()+rl.getCount()+1-component.getLeft();j++,ind++){
					stroke[ind]=(rl.getCount()+1)*DX;
					direction[ind]=HORIZONTAL;
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
						if(nwNext*DD<stroke[pos]){
							stroke[pos]=nwNext*DD;
							direction[pos]=DIAG_DOWN;
						}else if(nwNext*DD==stroke[pos]){
							direction[pos]|=DIAG_DOWN;
						}
					}
				}
				rl=iterator.hasNext()?iterator.next():null;
			}
			for(;j<w;j++,ind++){
				for(int k=1;k<=nL[j];k++){
					if(nL[j]*DX<stroke[ind-k*w]){
						stroke[ind-k*w]=nL[j]*DX;
						direction[ind-k*w]=VERTICAL;
					}else if(nL[j]*DX==stroke[ind-k*w]){
						direction[ind-k*w]|=VERTICAL;
					}
				}
				nL[j]=0;
				for(int k=1;k<=nwL[j];k++){
					int pos=ind-k*w-k;
					if(nwL[j]*DD<stroke[pos]){
						stroke[pos]=nwL[j]*DD;
						direction[pos]=DIAG_DOWN;
					}else if(nwL[j]*DD==stroke[pos]){
						direction[pos]|=DIAG_DOWN;
					}
				}
				nwL[j]=nwNext;
				nwNext=0;
				for(int k=1;k<=neL[j];k++){
					int pos=ind-k*w+k;
					if(neL[j]*DD<stroke[pos]){
						stroke[pos]=neL[j]*DD;
						direction[pos]=DIAG_UP;
					}else if(neL[j]*DD==stroke[pos]){
						direction[pos]|=DIAG_UP;
					}
				}
				if(j>0){
					neL[j-1]=0;
				}
			}
		}
		return new StrokeSpace(direction,stroke,w,h);
	}
	public static byte[] remainSkeleton(StrokeSpace strokes){
		int h=strokes.getHeight();
		int w=strokes.getWidth();
		byte[] direction=strokes.getDirection();
		int[] thickness=strokes.getThickness();
		int[] constant={HORIZONTAL,VERTICAL,DIAG_DOWN,DIAG_UP};
		int[] dx={-1,0,-1,1};
		int[] dy={0,-1,-1,-1};
		int[] dd={DX,DX,DD,DD};
		for(int i=h-1, ind=direction.length-1;i>=0;i--){
			for(int j=w-1;j>=0;j--,ind--){
				int thick=thickness[ind];
				if(thick>0){
					for(int l=0;l<4;l++){
						if((direction[ind]&constant[l])!=0){
							int t=thick/dd[l];
							int diff=(dy[l]*w+dx[l]);
							int pos=ind+diff*(t/2);
							int i2=i-dy[l];
							int j2=j-dx[l];
							if(i2>=0&&i2<h&&j2>=0&&j2<w&&thickness[i2*w+j2]>0){
								continue;
							}
							for(int k=0, p=ind;k<t;k++,p+=diff){
								if(p==pos){
									direction[p]|=constant[l];
								}else{
									direction[p]&=~constant[l];
								}
							}
						}
					}
				}
			}
		}
		return direction;
	}
	public static void main(String[] args){
		Font font=Font.decode(Font.SERIF).deriveFont(24).deriveFont(AffineTransform.getScaleInstance(10,10));
		ConnectedComponent component=getComponent(font,'永');
		StrokeSpace strokes=getStrokeDirection(component);
		byte[] direction=remainSkeleton(strokes);
		int w=strokes.getWidth();
		int h=strokes.getHeight();
		int[] constant={HORIZONTAL,VERTICAL,DIAG_DOWN,DIAG_UP};
		for(int c:constant){
			for(int i=0, ind=0;i<h;i++){
				for(int j=0;j<w;j++,ind++){
					if((direction[ind]&c)!=0){
						System.out.print("1");
					}else{
						System.out.print("0");
					}
				}
				System.out.println();
			}
			System.out.println();
		}
		System.out.println(Arrays.toString(new Gradient(3,3,Mask.SKELETON).extract(component)));
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
	public static class StrokeSpace{
		private final byte[] direction;
		private final int[] thickness;
		private final int width, height;
		public StrokeSpace(byte[] direction,int[] thickness,int width,int height){
			this.direction=direction;
			this.thickness=thickness;
			this.width=width;
			this.height=height;
		}
		public byte[] getDirection(){
			return direction;
		}
		public int[] getThickness(){
			return thickness;
		}
		public int getWidth(){
			return width;
		}
		public int getHeight(){
			return height;
		}
	}
}
