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
import com.github.chungkwong.mathocr.character.feature.*;
import com.github.chungkwong.mathocr.common.*;
import com.github.chungkwong.mathocr.preprocess.*;
import java.awt.image.*;
import java.util.*;
/**
 *
 * @author Chan Chung Kwong
 */
public class SDetector implements SkewDetector{
	@Override
	public double detect(BufferedImage image){
		image=CombinedPreprocessor.getDefaultCombinedPreprocessor().apply(image,false);
		Gradient.StrokeSpace strokes=Gradient.getStrokeDirection(new ConnectedComponent(image));
		int width=strokes.getWidth();
		int height=strokes.getHeight();
		byte[] direction=strokes.getDirection();
		int[] thickness=strokes.getThickness();
		int[] dx={1,1,0,-1,-1,-1,0,1};
		int[] dy={0,1,1,1,0,-1,-1,-1};
		int n=0;
		double sumxy=0.0, sumxx=0.0;
		double threhold=Math.PI/8;
		double aspectSum=0;
		for(int i=0, ind=0;i<height;i++){
			for(int j=0;j<width;j++,ind++){
				if((direction[ind]&Gradient.VERTICAL)!=0){
					LinkedList<Pair<Pair<Integer,Integer>,Integer>> stack=new LinkedList<>();
					direction[ind]&=~Gradient.VERTICAL;
					stack.push(new Pair<>(new Pair<>(i,j),0));
					int count=0;
					long sumx2=0, sumxx2=0, sumy2=0, sumyy2=0, sumxy2=0, sumt2=0;
					while(!stack.isEmpty()){
						Pair<Pair<Integer,Integer>,Integer> curr=stack.peek();
						Pair<Integer,Integer> pos=curr.getKey();
						int state=curr.getValue();
						if(state==7){
							stack.pop();
						}else{
							int k=pos.getKey();
							int l=pos.getValue();
							if(state==0){
								sumx2+=l;
								sumxx2+=l*l;
								sumy2+=k;
								sumyy2+=k*k;
								sumxy2+=k*l;
								sumt2+=thickness[k*width+l];
								++count;
							}
							curr.setValue(state+1);
						}
						int k=pos.getKey()+dy[state];
						int l=pos.getValue()+dx[state];
						if(k>=0&&l>=0&&k<height&&l<width){
							int next=k*width+l;
							if((direction[next]&Gradient.VERTICAL)!=0){
								direction[next]&=~Gradient.VERTICAL;
								stack.push(new Pair<>(new Pair<>(k,l),0));
							}
						}
					}
					double meanx=((double)sumx2)/count;
					double meany=((double)sumy2)/count;
					double a=(sumxy2-count*meanx*meany)/(sumxx2-count*meanx*meanx);
					double b=meany-a*meanx;
					double aspect=(sumxx2-count*meanx*meanx)*(1+a*a)*(1+a*a)/(sumyy2+a*a*sumxx2-2*a*sumxy2-count*b*b);
					//if(Double.isFinite(aspect)){
					//	aspectSum+=aspect;
					if(count>=2&&(sumyy2+a*a*sumxx2-2*a*sumxy2-count*b*b)<2*sumt2){
						sumxx+=(sumxx2-count*meanx*meanx)*count*count;
						sumxy+=(sumxy2-count*meanx*meany)*count*count;
						n+=count;
					}
					//}
				}
				if((direction[ind]&Gradient.HORIZONTAL)!=0){
					LinkedList<Pair<Pair<Integer,Integer>,Integer>> stack=new LinkedList<>();
					direction[ind]&=~Gradient.HORIZONTAL;
					stack.push(new Pair<>(new Pair<>(i,j),0));
					int count=0;
					long sumx2=0, sumxx2=0, sumy2=0, sumyy2=0, sumxy2=0, sumt2=0;
					while(!stack.isEmpty()){
						Pair<Pair<Integer,Integer>,Integer> curr=stack.peek();
						Pair<Integer,Integer> pos=curr.getKey();
						int state=curr.getValue();
						if(state==7){
							stack.pop();
						}else{
							int k=pos.getKey();
							int l=pos.getValue();
							if(state==0){
								sumx2+=k;
								sumxx2+=k*k;
								sumy2+=-l;
								sumyy2+=l*l;
								sumxy2+=-k*l;
								sumt2+=thickness[k*width+l];
								++count;
							}
							curr.setValue(state+1);
						}
						int k=pos.getKey()+dy[state];
						int l=pos.getValue()+dx[state];
						if(k>=0&&l>=0&&k<height&&l<width){
							int next=k*width+l;
							if((direction[next]&Gradient.HORIZONTAL)!=0){
								direction[next]&=~Gradient.HORIZONTAL;
								stack.push(new Pair<>(new Pair<>(k,l),0));
							}
						}
					}
					double meanx=((double)sumx2)/count;
					double meany=((double)sumy2)/count;
					double a=(sumxy2-count*meanx*meany)/(sumxx2-count*meanx*meanx);
					double b=meany-a*meanx;
					double aspect=(sumxx2-count*meanx*meanx)*(1+a*a)*(1+a*a)/(sumyy2+a*a*sumxx2-2*a*sumxy2-count*b*b);
					if(count>2&&(sumyy2+a*a*sumxx2-2*a*sumxy2-count*b*b)<2*sumt2){
						sumxx+=(sumxx2-count*meanx*meanx)*count*count;
						sumxy+=(sumxy2-count*meanx*meany)*count*count;
						n+=count;
					}
				}
			}
		}
		double angle=Math.atan2(sumxy,sumxx);
		if(angle>threhold){
			System.err.println("bad:"+angle);
			angle=threhold;
		}else if(angle<-threhold){
			System.err.println("bad:"+angle);
			angle=-threhold;
		}
		return angle;
	}
}
