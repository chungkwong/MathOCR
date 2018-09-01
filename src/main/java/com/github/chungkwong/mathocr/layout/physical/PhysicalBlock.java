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
package com.github.chungkwong.mathocr.layout.physical;
import com.github.chungkwong.mathocr.common.ConnectedComponent;
import com.github.chungkwong.mathocr.common.BoundBox;
import java.util.*;
/**
 * Physical block
 *
 * @author Chan Chung Kwong
 */
public class PhysicalBlock{
	private BoundBox box;
	private final List<ConnectedComponent> components;
	/**
	 * Create a block
	 *
	 * @param components the components inside the block
	 * @param box the bounding box of the box
	 */
	public PhysicalBlock(List<ConnectedComponent> components,BoundBox box){
		this.box=box;
		this.components=components;
	}
	/**
	 * @return bounding box
	 */
	public BoundBox getBox(){
		return box;
	}
	/**
	 * Merge with another block
	 *
	 * @param block the other block
	 */
	public void merge(PhysicalBlock block){
		components.addAll(block.getComponents());
		box=BoundBox.union(box,block.getBox());
	}
	/**
	 * Split this element vertically
	 *
	 * @param y the coordinate of the line to be used to split
	 * @return this
	 */
	public PhysicalBlock splitVertically(int y){
		ArrayList<ConnectedComponent> lst=new ArrayList<>(components.size());
		ListIterator<ConnectedComponent> iter=components.listIterator();
		while(iter.hasNext()){
			ConnectedComponent ele=iter.next();
			if(ele.getTop()>=y){
				iter.remove();
				lst.add(ele);
			}
		}
		PhysicalBlock block=new PhysicalBlock(lst,new BoundBox(box.getLeft(),box.getRight(),y,box.getBottom()));
		box=new BoundBox(box.getLeft(),box.getRight(),box.getTop(),y-1);
		return block;
	}
	/**
	 * Split this block horizontally
	 *
	 * @param x the coordinate of the line to be used to split
	 * @return this
	 */
	public PhysicalBlock splitHorizontally(int x){
		ArrayList<ConnectedComponent> lst=new ArrayList<ConnectedComponent>(components.size());
		ListIterator<ConnectedComponent> iter=components.listIterator();
		while(iter.hasNext()){
			ConnectedComponent ele=iter.next();
			if(ele.getLeft()>=x){
				iter.remove();
				lst.add(ele);
			}
		}
		PhysicalBlock block=new PhysicalBlock(lst,new BoundBox(x,box.getRight(),box.getTop(),box.getBottom()));
		box=new BoundBox(box.getLeft(),x-1,box.getTop(),box.getBottom());
		return block;
	}
	/**
	 * Get ConnectedComponent inside the block
	 *
	 * @return the list of ConnectedComponent
	 */
	public List<ConnectedComponent> getComponents(){
		return components;
	}
}
