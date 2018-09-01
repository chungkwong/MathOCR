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
/**
 *
 * @author Chan Chung Kwong
 */
public class BoundBox{
	private final int left, right, top, bottom;
	public BoundBox(int left,int right,int top,int bottom){
		this.left=left;
		this.right=right;
		this.top=top;
		this.bottom=bottom;
	}
	/**
	 * Get minimum x coordinate of bounding box of this block
	 *
	 * @return the coordinate
	 */
	public int getLeft(){
		return left;
	}
	/**
	 * Get maximum x coordinate of bounding box of this block
	 *
	 * @return the coordinate
	 */
	public int getRight(){
		return right;
	}
	/**
	 * Get minimum y coordinate of bounding box of this block
	 *
	 * @return the coordinate
	 */
	public int getTop(){
		return top;
	}
	/**
	 * Get maximum y coordinate of bounding box of this block
	 *
	 * @return the coordinate
	 */
	public int getBottom(){
		return bottom;
	}
	/**
	 * Get the width of the block
	 *
	 * @return the width
	 */
	public int getWidth(){
		return right-left+1;
	}
	/**
	 * Get the height of the block
	 *
	 * @return the height
	 */
	public int getHeight(){
		return bottom-top+1;
	}
	/**
	 *
	 * @return area of the box
	 */
	public int getArea(){
		return getWidth()*getHeight();
	}
	public static BoundBox union(BoundBox box1,BoundBox box2){
		return new BoundBox(Math.min(box1.getLeft(),box2.getLeft()),
				Math.max(box1.getRight(),box2.getRight()),
				Math.min(box1.getTop(),box2.getTop()),
				Math.max(box1.getBottom(),box2.getBottom()));
	}
	public static BoundBox intersect(BoundBox box1,BoundBox box2){
		return new BoundBox(Math.max(box1.getLeft(),box2.getLeft()),
				Math.min(box1.getRight(),box2.getRight()),
				Math.max(box1.getTop(),box2.getTop()),
				Math.min(box1.getBottom(),box2.getBottom()));
	}
	public static BoundBox union(BoundBox... boxs){
		int left=Integer.MAX_VALUE, right=0, top=Integer.MAX_VALUE, bottom=0;
		for(BoundBox box:boxs){
			if(box.getLeft()<left){
				left=box.getLeft();
			}
			if(box.getTop()<top){
				top=box.getTop();
			}
			if(box.getRight()>right){
				right=box.getRight();
			}
			if(box.getBottom()>bottom){
				bottom=box.getBottom();
			}
		}
		return new BoundBox(left,right,top,bottom);
	}
	public static boolean isIntersect(BoundBox box1,BoundBox box2){
		return box1.getLeft()<=box2.getRight()&&box2.getLeft()<=box1.getRight()
				&&box1.getTop()<=box2.getBottom()&&box2.getTop()<=box1.getBottom();
	}
	public static boolean isContaining(BoundBox box1,BoundBox box2){
		return box1.getLeft()<=box2.getLeft()&&box2.getRight()<=box1.getRight()
				&&box1.getTop()<=box2.getTop()&&box2.getBottom()<=box1.getBottom();
	}
	@Override
	public String toString(){
		return "["+left+","+right+"]x["+top+","+bottom+"]";
	}
}
