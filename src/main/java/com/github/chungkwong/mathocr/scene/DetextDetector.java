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
import com.github.chungkwong.mathocr.*;
import com.github.chungkwong.mathocr.common.*;
import java.awt.image.*;
import java.util.*;
import java.util.stream.*;
/**
 *
 * @author Chan Chung Kwong
 */
public class DetextDetector extends BinaryDetector{
	public static final String NAME="DETEXT";
	public DetextDetector(){
		Environment.ENVIRONMENT.setInteger("SAUVOLA_WINDOW",16);
		Environment.ENVIRONMENT.setFloat("SAUVOLA_WEIGHT",0.3f);
	}
	@Override
	public void detect(BufferedImage image,List<LineCandidate> candidates){
		//System.out.println(componentPool.getComponents().size());
		candidates.addAll(roughCharacterCluster(getCharacterCandidate(image)));
	}
	private ArrayList<ConnectedComponent> getCharacterCandidate(BufferedImage image){
		int width=image.getWidth();
		int height=image.getHeight();
		ComponentPool componentPool=new ComponentPool(image);
		ArrayList<ConnectedComponent> components=componentPool.getComponents();
		int minWidth=3;
		int minHeight=3;
		int maxWidth=width/10;
		int maxHeight=height/10;
		int maxslope=10;
		int maxContain=2;
		componentPool.getComponents().removeIf((ConnectedComponent c)->c.getWidth()<minWidth||c.getHeight()<minHeight||c.getWidth()>maxWidth||c.getHeight()>maxHeight||c.getWidth()>maxslope*c.getHeight()||c.getHeight()>maxslope*c.getWidth());
		Collections.sort(components);
		BitSet toDelete=new BitSet(components.size());
		for(int i=0;i<components.size();i++){
			ConnectedComponent curr=components.get(i);
			int count=0;
			for(int j=i+1;j<components.size();j++){
				ConnectedComponent tmp=components.get(j);
				if(BoundBox.isContaining(curr.getBox(),tmp.getBox())){
					if(++count>maxContain){
						toDelete.set(i);
						break;
					}
				}else if(tmp.getBox().getTop()>curr.getBox().getBottom()){
					break;
				}
			}
		}
		for(ListIterator<ConnectedComponent> iterator=components.listIterator(components.size());iterator.hasPrevious();){
			ConnectedComponent next=iterator.previous();
			if(toDelete.get(iterator.nextIndex())){
				iterator.remove();
			}
		}
		return components;
	}
	private List<LineCandidate> roughCharacterCluster(List<ConnectedComponent> strokes){
		List<LineCandidate> lines=strokes.stream().map((ConnectedComponent s)->new LineCandidate(s)).collect(Collectors.toCollection(ArrayList::new));
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
		lines.removeIf((LineCandidate c)->c==null||c.getComponents().size()<=2);
		for(int i=0;i<lines.size();i++){
			LineCandidate line1=lines.get(i);
			if(line1==null){
				continue;
			}
			for(int j=i+1;j<lines.size();j++){
				LineCandidate line2=lines.get(j);
				if(line2==null){
					continue;
				}
				if(isLikelySameRow(line1,line2)){
					line1.merge(line2);
					lines.set(j,null);
				}
			}
		}
		for(int i=0;i<lines.size();i++){
			LineCandidate line1=lines.get(i);
			if(line1==null){
				continue;
			}
			for(int j=i+1;j<lines.size();j++){
				LineCandidate line2=lines.get(j);
				if(line2==null){
					continue;
				}
				if(isLikelySameColumn(line1,line2)){
					line1.merge(line2);
					lines.set(j,null);
				}
			}
		}
		lines.removeIf((LineCandidate c)->c==null);
		return lines;
	}
	private boolean isLikelySameColumn(LineCandidate from,LineCandidate to){
		if(isLikelySameColumn(from.getBox(),to.getBox())){
			if(from.getBox().getWidth()>to.getBox().getWidth()*2||to.getBox().getWidth()>from.getBox().getWidth()*2){
				return false;
			}
			int fontSize=Arrays.asList(from,to).stream().flatMap((LineCandidate l)->l.getComponents().stream()).mapToInt((ConnectedComponent c)->c.getWidth()).max().orElse(1)*2;
			return from.getBox().getWidth()<=fontSize&&to.getBox().getWidth()<=fontSize
					&&to.getBox().getTop()<=from.getBox().getBottom()+fontSize&&to.getBox().getBottom()>=from.getBox().getTop()-fontSize;
		}else{
			return false;
		}
	}
	private boolean isLikelySameRow(LineCandidate from,LineCandidate to){
		if(isLikelySameRow(from.getBox(),to.getBox())){
			if(from.getBox().getHeight()>to.getBox().getHeight()*2||to.getBox().getHeight()>from.getBox().getHeight()*2){
				return false;
			}
			int fontSize=Arrays.asList(from,to).stream().flatMap((LineCandidate l)->l.getComponents().stream()).mapToInt((ConnectedComponent c)->c.getHeight()).max().orElse(1)*2;
			return from.getBox().getHeight()<=fontSize&&to.getBox().getHeight()<=fontSize
					&&to.getBox().getLeft()<=from.getBox().getRight()+fontSize&&to.getBox().getRight()>=from.getBox().getLeft()-fontSize;
		}else{
			return false;
		}
	}
	private boolean isLikelySameCharacter(ConnectedComponent from,ConnectedComponent to){
		if(isLikelySameRow(from.getBox(),to.getBox())){
			int fontSize=Math.max(from.getHeight(),to.getHeight());
			return to.getLeft()<=from.getRight()+fontSize/2&&to.getRight()>=from.getLeft()-fontSize/2;
		}else if(isLikelySameColumn(from.getBox(),to.getBox())){
			int fontSize=Math.max(from.getWidth(),to.getWidth());
			return to.getTop()<=from.getBottom()+fontSize/2&&to.getBottom()>=from.getTop()-fontSize/2;
		}else{
			return false;
		}
	}
	private boolean isLikelySameRow(BoundBox box1,BoundBox box2){
		return box1.getTop()<=box2.getBottom()&&box2.getTop()<=box1.getBottom()&&Math.min(box1.getBottom(),box2.getBottom())-Math.max(box1.getTop(),box2.getTop())>(Math.max(box1.getBottom(),box2.getBottom())-Math.min(box1.getTop(),box2.getTop()))/3;
	}
	private boolean isLikelySameColumn(BoundBox box1,BoundBox box2){
		return box1.getLeft()<=box2.getRight()&&box2.getLeft()<=box1.getRight()&&Math.min(box1.getRight(),box2.getRight())-Math.max(box1.getLeft(),box2.getLeft())>(Math.max(box1.getRight(),box2.getRight())-Math.min(box1.getLeft(),box2.getLeft()))/3;
	}
}
