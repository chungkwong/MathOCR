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
package com.github.chungkwong.mathocr.preprocess.skew;
import com.github.chungkwong.mathocr.common.ComponentPool;
import java.awt.image.*;
import java.util.*;
/**
 * Detect skew by piecewise painting algorithm
 *
 * @author Chan Chung Kwong
 */
public class PPADetector implements SkewDetector{
	private final int dx, dy;
	/**
	 * Create a skew detector
	 */
	public PPADetector(){
		this.dx=0;
		this.dy=0;
	}
	/**
	 *
	 * @param dx the width of a normal vertical stripe
	 * @param dy the height of a normal horizontal stripe
	 */
	public PPADetector(int dx,int dy){
		this.dx=dx;
		this.dy=dy;
	}
	@Override
	public double detect(BufferedImage image){
		int width=image.getWidth();
		int height=image.getHeight();
		int[] pixels=image.getRGB(0,0,width,height,null,0,width);
		int dx=this.dx;
		int dy=this.dy;
		if(dx<=0||dy<=0){
			ComponentPool pool=new ComponentPool(pixels,width,height);
			dx=pool.getAverageWidth();
			dy=pool.getAverageHeight();
		}
		//for(int i=0;i<pixels.length;i++)
		//pixels[i]&=0x1;
		int columns=(width-1)/dx+1, rows=(height-1)/dy+1, len=width*height;
		int[][] ppaHor=new int[5][rows];
		int[] lacc=new int[Math.max(columns,rows)];
		int nonblanklines=-1;
		for(int row=0;row<rows;row++){
			boolean notfound=true;
			for(int x=0;x<width;x++){
				int count=0, j=0;
				for(int ind=width*row*dy+x;j<dy&&ind<len;j++,ind+=width){
					count+=pixels[ind];
				}
				if(count*2<j){
					if(notfound){
						++nonblanklines;
						ppaHor[0][nonblanklines]=row*dy;
						ppaHor[1][nonblanklines]=x;
						notfound=false;
					}
					ppaHor[3][nonblanklines]=x;
				}
			}
			if(!notfound){
				ppaHor[2][nonblanklines]=(ppaHor[1][nonblanklines]+ppaHor[3][nonblanklines])/2;
				ppaHor[4][nonblanklines]=(ppaHor[3][nonblanklines]-ppaHor[1][nonblanklines])/dx;
				++lacc[ppaHor[4][nonblanklines]];
			}
		}
		int max=0, maxind=0;
		for(int i=0;i<columns;i++){
			if(lacc[i]>max){
				max=lacc[i];
				maxind=i;
			}
		}
		int selected=-1;
		for(int i=0;i<=nonblanklines;i++){
			if(ppaHor[4][i]==maxind){
				++selected;
				ppaHor[0][selected]=ppaHor[0][i];
				ppaHor[1][selected]=ppaHor[1][i];
				ppaHor[2][selected]=ppaHor[2][i];
			}
		}
		if(selected<3){
			return Double.NaN;
		}
		double HLR=Math.atan(-linearRegression(ppaHor[0],ppaHor[1],selected+1));
		double HLD=Math.atan(-(ppaHor[1][selected]-ppaHor[1][0]+0.0)/(ppaHor[0][selected]-ppaHor[0][0]));
		double HMR=Math.atan(-linearRegression(ppaHor[0],ppaHor[2],selected+1));
		ppaHor=null;
		for(int i=0;i<lacc.length;i++){
			lacc[i]=0;
		}
		int[][] ppaVert=new int[5][columns];
		nonblanklines=-1;
		for(int col=0;col<60;col++){
			boolean notfound=true;
			for(int y=0;y<height;y++){
				int count=0, j=0;
				for(int x=col*dx, ind=y*width+x;j<dx&&x<width;j++,ind++,x++){
					count+=pixels[ind];
				}
				if(count*2<j){
					if(notfound){
						++nonblanklines;
						ppaVert[0][nonblanklines]=col*dx;
						ppaVert[1][nonblanklines]=y;
						notfound=false;
					}
					ppaVert[3][nonblanklines]=y;
				}
			}
			if(!notfound){
				ppaVert[2][nonblanklines]=(ppaVert[1][nonblanklines]+ppaVert[3][nonblanklines])/2;
				ppaVert[4][nonblanklines]=(ppaVert[3][nonblanklines]-ppaVert[1][nonblanklines])/dy;
				++lacc[ppaVert[4][nonblanklines]];
			}
		}
		max=0;
		maxind=0;
		for(int i=0;i<rows;i++){
			if(lacc[i]>max){
				max=lacc[i];
				maxind=i;
			}
		}
		selected=-1;
		for(int i=0;i<=nonblanklines;i++){
			if(ppaVert[4][i]==maxind){
				++selected;
				ppaVert[0][selected]=ppaVert[0][i];
				ppaVert[1][selected]=ppaVert[1][i];
				ppaVert[2][selected]=ppaVert[2][i];
			}
		}
		if(selected<3){
			return Double.NaN;
		}
		double VTR=Math.atan(linearRegression(ppaVert[0],ppaVert[1],selected+1));
		double VTD=Math.atan((ppaVert[1][selected]-ppaVert[1][0]+0.0)/(ppaVert[0][selected]-ppaVert[0][0]));
		double VMR=Math.atan(linearRegression(ppaVert[0],ppaVert[2],selected+1));
		double VMD=Math.atan((ppaVert[2][selected]-ppaVert[2][0]+0.0)/(ppaVert[0][selected]-ppaVert[0][0]));
		double[] angles=new double[]{HLR,HLD,HMR,VTR,VTD,VMR,VMD};
		Arrays.sort(angles);
		double SM=angles[3];
		return SM;
	}
	/**
	 * Calculate the slope of the linear regression line of point
	 * (ptx[0],pty[0]),...,(ptx[len-1],pty[len-1]).
	 *
	 * @param ptx x coordinate of the points
	 * @param pty y coordinate of the points
	 * @param len the number of points
	 * @return the slope
	 */
	private static double linearRegression(int[] ptx,int[] pty,int len){
		double sumX=0, sumY=0, sumXX=0, sumXY=0;
		for(int i=0;i<len;i++){
			sumX+=ptx[i];
			sumXX+=ptx[i]*ptx[i];
			sumXY+=ptx[i]*pty[i];
			sumY+=pty[i];
		}
		return (len*sumXY-sumX*sumY)/(len*sumXX-sumX*sumX);
	}
}
