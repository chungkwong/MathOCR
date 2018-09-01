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
import java.awt.image.*;
import java.util.*;
/**
 *
 * @author Chan Chung Kwong
 */
public class BackgroundDetector extends BinaryDetector{
	public static final String NAME="background";
	@Override
	public void detect(BufferedImage image,List<LineCandidate> lines){
		int width=image.getWidth();
		int height=image.getHeight();
		int[] toUp=image.getRGB(0,0,width,height,null,0,width);
		int[] toLeft=new int[width*height];
		for(int i=0, ind=0;i<height;i++){
			for(int j=0;j<width;j++,ind++){
				if((toUp[ind]&0xFF)==0){
					if(i==0){
						toUp[ind]=1;
					}else{
						toUp[ind]=toUp[ind-width]+1;
					}
					if(j==0){
						toLeft[ind]=1;
					}else{
						toLeft[ind]=toLeft[ind-1]+1;
					}
				}else{
					toUp[ind]=0;
				}
			}
		}
		for(int i=height-1;i>=0;i--){
			int base=i*width;
			for(int j2=width-1;j2>=0;j2--){
				if(toUp[base+j2]<3){
					continue;
				}
				int leftLim=j2-toLeft[base+j2];
				boolean bottled=false;
				for(int j1=j2-2;j1>leftLim;j1--){
					if(!bottled){
						if(toUp[base+j1]==1){
							bottled=true;
						}
					}else{
						for(int i1=i-Math.min(toUp[base+j1],toUp[base+j2])+1;i1<i-1;i1++){
							if(toLeft[i1*width+j2]>j2-j1&&toLeft[(i1+1)*width+j2]<=j2-j1
									&&toUp[base+j1+1]<=i-i1&&toUp[base+j2-1]<=i-i1){
								if(j2-j1>=16&&i-i1>=16){
									lines.add(new LineCandidate(new ConnectedComponent(j1,j2,i1,i)));
								}
								break;
							}
						}
					}
				}
			}
		}
		System.out.println("boxs"+lines.size());
		for(int i=0;i<lines.size();i++){
			LineCandidate a=lines.get(i);
			if(a==null){
				continue;
			}
			BoundBox box1=a.getBox();
			for(int j=i+1;j<lines.size();j++){
				LineCandidate b=lines.get(j);
				if(b==null){
					continue;
				}
				BoundBox box2=b.getBox();
				if(BoundBox.isIntersect(box1,box2)){
					if(BoundBox.isContaining(box1,box2)){
						lines.set(i,null);
						break;
					}else if(BoundBox.isContaining(box2,box1)){
						lines.set(j,null);
						continue;
					}else{
						a.merge(b);
						lines.set(j,null);
						continue;
					}
				}
			}
		}
		lines.removeIf((l)->l==null);
		System.out.println("truncated"+lines.size());
	}
}
