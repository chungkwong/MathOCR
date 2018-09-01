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
package com.github.chungkwong.mathocr.common;
import java.util.*;
/**
 *
 * @author Chan Chung Kwong
 */
public class Bitmap implements Fragment{
	private final int x, y, width, height;
	private final BitSet map;
	public Bitmap(int x,int y,int width,int height){
		this.x=x;
		this.y=y;
		this.width=width;
		this.height=height;
		map=new BitSet(width*height);
	}
	public boolean isBlack(int x,int y){
		x-=this.x;
		y-=this.y;
		if(x<0||y<0||x>=width||y>=height){
			return false;
		}else{
			return map.get(y*width+x);
		}
	}
	public static Bitmap from(ConnectedComponent component){
		Bitmap bitmap=new Bitmap(component.getLeft(),component.getTop(),component.getWidth(),component.getHeight());
		for(RunLength rl:component.getRunLengths()){
			int start=(rl.getY()-bitmap.y)*bitmap.width+(rl.getX()-bitmap.x);
			int end=start+rl.count+1;
			bitmap.map.set(start,end);
		}
		return bitmap;
	}
	@Override
	public BoundBox getBoundBox(){
		return new BoundBox(x,x+width-1,y,y+height-1);
	}
}
