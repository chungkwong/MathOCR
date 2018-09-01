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
import com.github.chungkwong.mathocr.common.*;
import java.awt.image.*;
import java.util.*;
import java.util.stream.*;
/**
 *
 * @author Chan Chung Kwong
 */
public class ContrastDetector extends BinaryDetector{
	public static final String NAME="CONTRAST";
	@Override
	public void detect(BufferedImage image,List<LineCandidate> candidates){
		ComponentPool componentPool=new ComponentPool(image);
		filterTextualComponent(componentPool);
		//System.out.println(componentPool.getComponents().size());
		List<LineCandidate> lines=roughCharacterCluster(componentPool.getComponents());
		filterLine(lines);
		candidates.addAll(lines);
	}
	private static final int CONTAINING_COMPONENT_LIMIT=10;
	private void filterTextualComponent(ComponentPool pool){
		ArrayList<ConnectedComponent> components=pool.getComponents();
		Collections.sort(components);
		BitSet toDelete=new BitSet(components.size());
		int boxThrehold=Math.max(4,Math.max(pool.getBoundBox().getWidth(),pool.getBoundBox().getHeight())/1000);
		int slopeThrehold=15;
		for(int i=0;i<components.size();i++){
			ConnectedComponent curr=components.get(i);
			int count=0;
			if(curr.getWidth()<=boxThrehold||curr.getHeight()<boxThrehold){
				toDelete.set(i);
			}else{
				for(int j=i+1;j<components.size();j++){
					ConnectedComponent tmp=components.get(j);
					if(BoundBox.isContaining(curr.getBox(),tmp.getBox())){
						if(++count>CONTAINING_COMPONENT_LIMIT){
							toDelete.set(i);
							break;
						}
					}else if(tmp.getBox().getTop()>curr.getBox().getBottom()){
						break;
					}
				}
			}
		}
		for(ListIterator<ConnectedComponent> iterator=components.listIterator(components.size());iterator.hasPrevious();){
			ConnectedComponent next=iterator.previous();
			if(toDelete.get(iterator.nextIndex())){
				iterator.remove();
			}
		}
	}
	private List<LineCandidate> roughCharacterCluster(List<ConnectedComponent> strokes){
		List<LineCandidate> lines=strokes.stream().map((s)->new LineCandidate(s)).collect(Collectors.toCollection(ArrayList::new));
		Partition partition=new Partition((int m,int n)->{
			lines.get(n).merge(lines.get(m));
			lines.set(m,null);
		},strokes.size());
		for(ListIterator<ConnectedComponent> iterator=strokes.listIterator();iterator.hasNext();){
			ConnectedComponent from=iterator.next();
			for(ListIterator<ConnectedComponent> iter=strokes.listIterator(iterator.previousIndex()+1);iter.hasNext();){
				ConnectedComponent to=iter.next();
				if(isLikelySameCharacter(from,to)){
					partition.union(iterator.previousIndex(),iter.previousIndex());
				}
			}
		}
		lines.removeIf((c)->c==null||c.getComponents().size()<=1||c.getBox().getArea()<=256);
		return lines;
	}
	private boolean isLikelySameCharacter(ConnectedComponent from,ConnectedComponent to){
		int fromSize=from.getWidth()+from.getHeight();
		int toSize=to.getWidth()+to.getHeight();
		if(fromSize>=5*toSize||toSize>=5*fromSize){
			return false;
		}
		int distance=calculateDistance(from,to);
		return distance<Math.min(fromSize,toSize)/2;
	}
	private boolean isLikelySameLine(ConnectedComponent from,ConnectedComponent to){
		int fromSize=from.getWidth()+from.getHeight();
		int toSize=to.getWidth()+to.getHeight();
		if(fromSize>=5*toSize||toSize>=5*fromSize){
			return false;
		}
		int distance=calculateDistance(from,to);
		return distance<Math.min(fromSize,toSize)/2;
	}
	public static int calculateDistance(ConnectedComponent from,ConnectedComponent to){
		BoundBox fromBox=from.getBox();
		BoundBox toBox=to.getBox();
		int dx=fromBox.getLeft()<=toBox.getRight()&&toBox.getLeft()<=fromBox.getRight()?0
				:Math.max(fromBox.getLeft()-toBox.getRight(),toBox.getLeft()-fromBox.getRight());
		//int dy=fromBox.getTop()<=toBox.getBottom()&&toBox.getTop()<=fromBox.getBottom()?0
		//		:Math.max(fromBox.getTop()-toBox.getBottom(),toBox.getTop()-fromBox.getBottom());
		int dy=Math.abs(fromBox.getBottom()-toBox.getBottom());
		return dx+dy;
	}
	private void filterLine(List<LineCandidate> componentPool){
		componentPool.removeIf((line)->line.getBox().getWidth()<2*line.getBox().getHeight()||line.getBox().getArea()<=512);
	}
}
