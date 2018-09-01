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
import com.github.chungkwong.mathocr.common.ConnectedComponent;
import com.github.chungkwong.mathocr.common.ComponentPool;
import java.awt.image.*;
/**
 * Detect skew by Nearest Neighbors Clustering
 *
 * @author Chan Chung Kwong
 */
public class NNDetector implements SkewDetector{
	/**
	 * Create a skew detector
	 */
	public NNDetector(){
	}
	@Override
	public double detect(BufferedImage image){
		int width=image.getWidth(), height=image.getHeight();
		int[] pixels=image.getRGB(0,0,width,height,null,0,width);
		ComponentPool pool=new ComponentPool(pixels,width,height);
		int[] hist=new int[450];
		for(ConnectedComponent ele:pool.getComponents()){
			int dx=width, dy=height;
			for(ConnectedComponent ele2:pool.getComponents()){
				if(ele!=ele2){
					int dx1=ele.getLeft()+ele.getRight()-ele2.getLeft()-ele2.getRight();
					int dy1=ele.getBottom()+ele.getTop()-ele2.getBottom()-ele2.getTop();
					if(dx1*dx1+dy1*dy1<dx*dx+dy*dy&&dx1*dx1>dy1*dy1){
						dx=dx1;
						dy=dy1;
						//Math.min(Math.min(ele.getWidth(),ele.getHeight()),Math.min(ele2.getWidth(),ele2.getHeight()))*Math.signum(dx1);
					}
				}
			}
			++hist[(int)(Math.atan((dy+0.0)/dx)*900/Math.PI+225)];
		}
		int max=0, t=225;
		for(int i=0;i<450;i++){
			if(hist[i]>max){
				max=hist[i];
				t=i;
			}
		}
		return (t-225)*Math.PI/900;
	}
}
