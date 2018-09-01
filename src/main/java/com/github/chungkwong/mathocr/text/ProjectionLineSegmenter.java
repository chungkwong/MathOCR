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
package com.github.chungkwong.mathocr.text;
import com.github.chungkwong.mathocr.layout.physical.PhysicalBlock;
import com.github.chungkwong.mathocr.common.BoundBox;
import com.github.chungkwong.mathocr.common.ConnectedComponent;
import java.util.*;
/**
 * Extract text lines from a box using projection profile
 *
 * @author Chan Chung Kwong
 */
public class ProjectionLineSegmenter implements LineSegmenter{
	public static final String NAME="PROJECTION";
	@Override
	public List<TextLine> segment(PhysicalBlock block){
		List<ConnectedComponent> pool=block.getComponents();
		Collections.sort(pool);
		List<Integer> breakpoints=new ArrayList<>();
		int b=-1, sum=0, count=0;
		for(ConnectedComponent ele:pool){
			if(ele.getTop()<=b){
				if(ele.getBottom()>=b){
					b=ele.getBottom()+1;//should +1 be drop?
				}
			}else{
				if(b!=-1){
					breakpoints.add(b);
					sum+=ele.getTop()-b;
				}
				breakpoints.add(ele.getTop());
				b=ele.getBottom()+1;//should +1 be drop?
				++count;
			}
		}
		breakpoints.add(b);
		ArrayList<TextLine> lines=new ArrayList<>(count);
		if(count<=1){
			lines.add(new TextLine(pool,block.getBox(),checkAlignment(block.getBox(),block.getBox())));
		}else{
			ListIterator<ConnectedComponent> it=pool.listIterator();
			int thre=0;//sum/(count-1)/4;
			int start=breakpoints.get(0);
			ConnectedComponent ele=it.next();
			for(int i=1;i<=count;i++){
				int end=breakpoints.get((i<<1)-1);
				if(i==count||breakpoints.get(i<<1)-end>=thre){
					ArrayList<ConnectedComponent> lst=new ArrayList<>();
					int left=Integer.MAX_VALUE, right=0;
					while(ele!=null&&ele.getBottom()<=end){
						lst.add(ele);
						if(ele.getLeft()<left){
							left=ele.getLeft();
						}
						if(ele.getRight()>right){
							right=ele.getRight();
						}
						if(it.hasNext()){
							ele=it.next();
						}else{
							ele=null;
						}
					}
					BoundBox lb=new BoundBox(left,right,start,end-1);
					lines.add(new TextLine(lst,lb,checkAlignment(lb,block.getBox())));//-1 should be droped if +1 above is droped?
					if(i!=count){
						start=breakpoints.get(i<<1);
					}
				}
			}
		}
		return lines;
	}
	private static int checkAlignment(BoundBox inner,BoundBox outer){
		int fontsize=inner.getHeight();
		int leftmargin=inner.getLeft()-outer.getLeft(), rightmargin=outer.getRight()-inner.getRight();
		if(leftmargin>rightmargin+fontsize){
			return TextLine.ALIGN_RIGHT;
		}else if(rightmargin>leftmargin+fontsize){
			return TextLine.ALIGN_LEFT;
		}else if(leftmargin>fontsize&&rightmargin>fontsize){
			return TextLine.ALIGN_CENTER;
		}else{
			return TextLine.ALIGN_FULL;
		}
	}
}
