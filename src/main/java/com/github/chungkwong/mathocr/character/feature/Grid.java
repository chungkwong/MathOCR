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
import java.util.*;
/**
 * Density
 *
 * @author Chan Chung Kwong
 */
public class Grid implements VectorFeature{
	public static final String NAME="GRID";
	private final int m, n;
	/**
	 * Create density feature
	 *
	 * @param m rows of grid
	 * @param n columns of grid
	 */
	public Grid(int m,int n){
		this.m=m;
		this.n=n;
	}
	@Override
	public double[] extract(ConnectedComponent component){
		int h=component.getHeight(), w=component.getWidth();
		double scale=Math.max(h*1.0/m,w*1.0/n);
		double a=m*0.5-(component.getBottom()+component.getTop()+1)*0.5/scale;
		double b=n*0.5-(component.getLeft()+component.getRight()+1)*0.5/scale;
		double[] ptv=new double[m+1], pth=new double[n+1];
		for(int i=0;i<=m;i++){
			ptv[i]=(scale*(i-a));
		}
		for(int j=0;j<=n;j++){
			pth[j]=(scale*(j-b));
		}
		double[] vec=new double[m*n];
		Iterator<RunLength> iter=component.getRunLengths().iterator();
		RunLength curr=iter.hasNext()?iter.next():null;
		for(int i=0;i<m;i++){
			while(curr!=null&&curr.getY()<ptv[i+1]){
				double j1=curr.getX(), j2=j1+curr.getCount()+1;
				for(int j=0, ind=n*i;j<n;j++,ind++){
					if(j1<pth[j+1]){
						if(j2<pth[j+1]){
							vec[ind]+=(j2-j1);
							break;
						}else{
							vec[ind]+=(pth[j+1]-j1);
							j1=pth[j+1];
						}
					}
				}
				curr=iter.hasNext()?iter.next():null;
			}
		}
		for(int i=0, ind=0;i<m;i++){
			for(int j=0;j<n;j++,ind++){
				if(vec[ind]!=0){
					vec[ind]/=((ptv[i+1]-ptv[i])*(pth[j+1]-pth[j]));
				}
			}
		}
		return vec;
	}
	@Override
	public int getDimension(){
		return m*n;
	}
}
