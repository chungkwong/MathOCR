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
import com.github.chungkwong.mathocr.common.Linkable;
import com.github.chungkwong.mathocr.common.Partition;
import com.github.chungkwong.mathocr.common.BoundBox;
import com.github.chungkwong.mathocr.common.ConnectedComponent;
import com.github.chungkwong.mathocr.common.RunLength;
import static com.github.chungkwong.mathocr.scene.ContrastDetector.calculateDistance;
import java.awt.image.*;
import java.util.*;
import java.util.List;
import java.util.stream.*;
/**
 *
 * @author Chan Chung Kwong
 */
public class SwtDetector extends BinaryDetector{
	@Override
	public void detect(BufferedImage image,List<LineCandidate> candidates){
		int width=image.getWidth(), height=image.getHeight();
		int[] bitmap=image.getRGB(0,0,width,height,null,0,width);
		strokeWidthTransform(bitmap,width,height);
		candidates.addAll(cluster(bitmap,width,height));
	}
	private void strokeWidthTransform(int[] bitmap,int width,int height){
		int[] vrl=new int[width];
		for(int i=0, ind=0;i<height;i++){
			int hrl=0;
			boolean lastBlack=false;
			for(int j=0;j<width;j++,ind++){
				boolean black=(bitmap[ind]&0xFFFFFF)==0;
				if(black){
					++hrl;
					++vrl[j];
				}else{
					bitmap[ind]=0x0;
					if(lastBlack){
						for(int k=ind-hrl;k<ind;k++){
							bitmap[k]=hrl;
						}
					}
					if(vrl[j]>0){
						for(int k=ind-vrl[j]*width;k<ind;k+=width){
							if(vrl[j]<bitmap[k]){
								bitmap[k]=vrl[j];
							}
						}
						vrl[j]=0;
					}
					hrl=0;
				}
				lastBlack=black;
			}
			if(lastBlack){
				for(int k=ind-hrl;k<ind;k++){
					bitmap[k]=hrl;
				}
			}
		}
		for(int j=0;j<width;j++){
			if(vrl[j]>0){
				for(int k=bitmap.length+j-vrl[j]*width;k<bitmap.length;k+=width){
					if(vrl[j]<bitmap[k]){
						bitmap[k]=vrl[j];
					}
				}
			}
		}
	}
	private static final int RATIO_THREHOLD=3;
	private List<LineCandidate> cluster(int[] bitmap,int width,int height){
		List<Stroke> strokeCluster=strokeCluster(bitmap,width,height);
		strokeCluster.removeIf((s)->s.getComponent().getWidth()>8*s.getComponent().getHeight()||s.getComponent().getHeight()>8*s.getComponent().getWidth());
		/*Collections.sort(strokeCluster,Comparator.comparing((s)->s.getComponent().getWidth()*s.getComponent().getHeight()));
		int old=-1, count=0;
		for(Stroke s:strokeCluster){
		int w=s.getComponent().getWidth()*s.getComponent().getHeight();
		if(w!=old){
		System.out.println(old+":"+count);
		old=w;
		count=0;
		}
		++count;
		}
		System.out.println(old+":"+count);*/
		List<LineCandidate> lines=roughClusterLine(strokeCluster);
		refineLines(lines);
		//lines.removeIf((line)->line.getBox().getWidth()>width/2||line.getBox().getHeight()>height/2);
		return lines;
	}
	private List<LineCandidate> roughClusterLine(List<Stroke> strokes){
		List<LineCandidate> lines=strokes.stream().map((s)->new LineCandidate(((Stroke)s).getComponent())).collect(Collectors.toCollection(ArrayList::new));
		Partition partition=new Partition((int m,int n)->{
			lines.get(n).merge(lines.get(m));
			lines.set(m,null);
		},strokes.size());
		for(ListIterator<Stroke> iterator=strokes.listIterator();iterator.hasNext();){
			Stroke from=iterator.next();
			for(ListIterator<Stroke> iter=strokes.listIterator(iterator.previousIndex()+1);iter.hasNext();){
				Stroke to=iter.next();
				if(isSurelySameLine(from,to)){
					partition.union(iterator.previousIndex(),iter.previousIndex());
				}
			}
		}
		lines.removeIf((c)->c==null||c.getComponents().size()<=1);
		/*Partition partition=new Partition((int m,int n)->{
			lines.get(n).merge(lines.get(m));
			lines.set(m,null);
		},strokes.size());
		for(ListIterator<Stroke> iterator=strokes.listIterator();iterator.hasNext();){
			Stroke from=iterator.next();
			for(ListIterator<Stroke> iter=strokes.listIterator(iterator.previousIndex()+1);iter.hasNext();){
				Stroke to=iter.next();
				if(isLikelySameCharacter(from.getComponent(),to.getComponent())
						&&(from.getAverageWidth()<2*to.getAverageWidth()&&to.getAverageWidth()<2*from.getAverageWidth())){
					partition.union(iterator.previousIndex(),iter.previousIndex());
				}
			}
		}*/
		//lines.removeIf((c)->c==null||c.getComponents().size()<=1||c.getBox().getArea()<=256);
		lines.removeIf((c)->c==null||c.getBox().getArea()<=256);
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
	private void refineLines(List<LineCandidate> componentPool){
		/*int characterThrehold=16;
		componentPool.removeIf((line)->{
			BoundBox box=line.getBox();
			if(Math.max(box.getWidth(),box.getHeight())<=characterThrehold){
				return true;
			}
			return false;
		});*/
		for(int i=0;i<componentPool.size();i++){
			LineCandidate from=componentPool.get(i);
			if(from==null){
				continue;
			}
			BoundBox fromBox=from.getBox();
			for(int j=i+1;j<componentPool.size();j++){
				LineCandidate to=componentPool.get(i);
				if(to==null){
					continue;
				}
				BoundBox toBox=to.getBox();
				if(BoundBox.isIntersect(fromBox,toBox)){
					if(BoundBox.isContaining(fromBox,toBox)){
						componentPool.set(i,null);
						break;
					}else if(BoundBox.isContaining(toBox,fromBox)){
						componentPool.set(j,null);
					}else{
						from.merge(to);
						componentPool.set(j,null);
					}
				}
			}
		}
		componentPool.removeIf((line)->line==null);
	}
	private boolean isSurelySameLine(Stroke from,Stroke to){
		if(!isConnected(from.getAverageWidth(),to.getAverageWidth())){
			return false;
		}
		int distance=Edge.calculateDistance(from,to);
		if(distance>Math.max(
				Math.max(from.getComponent().getWidth(),to.getComponent().getWidth()),
				Math.max(from.getComponent().getHeight(),to.getComponent().getHeight()))/3){
			return false;
		}
		return true;
	}
	private List<Stroke> strokeCluster(int[] pixels,int width,int height){
		List<Stroke> components=new ArrayList<>();
		int curr=0;
		//Integer tmp;
		Partition partition=new Partition(new Linkable(){
			public void link(int m,int n){
				components.get(n).combineWith(components.get(m));
				components.set(m,null);
			}
		});
		int[] last=new int[width];
		int lastlt=-1;
		for(int j=0;j<width;j++){
			last[j]=-1;
		}
		for(int i=0, ind=0;i<height;i++){
			for(int j=0;j<width;j++,ind++){
				if(pixels[ind]>0){
					int id=(i!=0&&j!=0&&isConnected(pixels[ind],pixels[ind-width-1]))?lastlt:-1;
					int k=j+1;
					int sum=pixels[ind];
					++ind;
					for(;k<width&&isConnected(pixels[ind],pixels[ind-1]);k++,ind++){
						sum+=pixels[ind];
						if(i!=0&&isConnected(pixels[ind],pixels[ind-width])){
							if(id==-1){
								id=last[k];
							}else{
								partition.union(id,last[k]);
							}
						}
					}
					if(i!=0&&k!=width&&isConnected(pixels[ind-1],pixels[ind-width])){
						if(id==-1){
							id=last[k];
						}else{
							partition.union(id,last[k]);
						}
					}
					if(id==-1){
						components.add(new Stroke(new RunLength(i,j,k-j-1),sum));
						partition.makeSet();
						id=curr++;
					}else{
						id=partition.findRoot(id);
						components.get(id).addRunLengthToLast(new RunLength(i,j,k-j-1),sum);
					}
					lastlt=last[k-1];
					for(int l=j;l<k;l++){
						last[l]=id;
					}
					j=k-1;
					--ind;
				}else{
					lastlt=last[j];
					last[j]=-1;
				}
			}
		}
		components.removeIf((c)->c==null||c.getComponent().getWidth()*c.getComponent().getHeight()<=2);
		return components;
	}
	private static boolean isConnected(int i,int j){
		return i<=j*RATIO_THREHOLD&&j<=i*RATIO_THREHOLD;
	}
	private static class Stroke{
		private ConnectedComponent component;
		private int pixelCount;
		private int widthSum;
		public Stroke(RunLength rl,int widthSum){
			this.component=new ConnectedComponent(rl);
			this.pixelCount=rl.getCount()+1;
			this.widthSum=widthSum;
		}
		public ConnectedComponent getComponent(){
			return component;
		}
		public void combineWith(Stroke s){
			pixelCount+=s.pixelCount;
			widthSum+=s.widthSum;
			component.combineWith(s.component);
		}
		public void addRunLengthToLast(RunLength runLength,int sum){
			pixelCount+=runLength.getCount()+1;
			widthSum+=sum;
			component.addRunLengthToLast(runLength);
		}
		public int getAverageWidth(){
			return widthSum/pixelCount;
		}
	}
	private static final int WIDTH_PENALITY=10;
	private static class Edge implements Comparable<Edge>{
		private final Stroke form, to;
		private final int formIndex, toIndex;
		private final int distance;
		public Edge(Stroke from,Stroke to,int fromIndex,int toIndex){
			this.form=from;
			this.to=to;
			this.formIndex=fromIndex;
			this.toIndex=toIndex;
			this.distance=calculateDistance(from,to)+WIDTH_PENALITY*Math.abs(from.getAverageWidth()-to.getAverageWidth());
		}
		public int getDistance(){
			return distance;
		}
		public Stroke getForm(){
			return form;
		}
		public Stroke getTo(){
			return to;
		}
		public static int calculateDistance(Stroke from,Stroke to){
			BoundBox fromBox=from.getComponent().getBox();
			BoundBox toBox=to.getComponent().getBox();
			int dx=fromBox.getLeft()<=toBox.getRight()&&toBox.getLeft()<=fromBox.getRight()?0
					:Math.max(fromBox.getLeft()-toBox.getRight(),toBox.getLeft()-fromBox.getRight());
			int dy=fromBox.getTop()<=toBox.getBottom()&&toBox.getTop()<=fromBox.getBottom()?0
					:Math.max(fromBox.getTop()-toBox.getBottom(),toBox.getTop()-fromBox.getBottom());
			return dx+dy;
		}
		@Override
		public int compareTo(Edge o){
			int compare=Integer.compare(distance,o.distance);
			if(compare!=0){
				return compare;
			}
			compare=Integer.compare(formIndex,o.formIndex);
			if(compare!=0){
				return compare;
			}
			return Integer.compare(toIndex,o.toIndex);
		}
	}
}
