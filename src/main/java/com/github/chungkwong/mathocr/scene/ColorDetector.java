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
import java.util.List;
import java.util.stream.*;
/**
 *
 * @author Chan Chung Kwong
 */
public class ColorDetector implements TextDetector{
	public static final String NAME="COLOR";
	@Override
	public List<LineCandidate> detect(BufferedImage image){
		int width=image.getWidth();
		int height=image.getHeight();
		int[] rgb=image.getRGB(0,0,width,height,null,0,width);
		List<Stroke> colorCluster=clusterByColor(rgb,width,height);
		filterConponent(colorCluster);
		List<LineCandidate> lines=roughCharacterCluster(colorCluster);
		filterLine(lines);
		return lines;
	}
	private List<Stroke> clusterByColor(int[] pixels,int width,int height){
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
					components.add(new Stroke(new ConnectedComponent(new RunLength(i,j,k-j-1)),pixels[ind-1]));
					partition.makeSet();
					id=curr++;
				}else{
					id=partition.findRoot(id);
					components.get(id).getComponent().addRunLengthToLast(new RunLength(i,j,k-j-1));
				}
				lastlt=last[k-1];
				for(int l=j;l<k;l++){
					last[l]=id;
				}
				j=k-1;
				--ind;
			}
		}
		//System.out.println(components.size());
		return components;
	}
	private static boolean isConnected(int i,int j){
		int dr=Math.abs((i&0xFF0000)>>16-(j&0xFF0000)>>16);
		int dg=Math.abs((i&0xFF00)>>8-(j&0xFF00)>>8);
		int db=Math.abs(i&0xFF-j&0xFF);
		return dr+dg+db<45;
	}
	private void filterConponent(List<Stroke> colorCluster){
		colorCluster.removeIf((c)->c==null||c.getComponent().getBox().getWidth()<=4||c.getComponent().getBox().getHeight()<=4||c.getComponent().getNumberOfHoles()>=10);
	}
	private List<LineCandidate> roughCharacterCluster(List<Stroke> strokes){
		List<LineCandidate> lines=strokes.stream().map((s)->new LineCandidate(s.getComponent())).collect(Collectors.toCollection(ArrayList::new));
		Partition partition=new Partition((int m,int n)->{
			lines.get(n).merge(lines.get(m));
			lines.set(m,null);
		},strokes.size());
		for(ListIterator<Stroke> iterator=strokes.listIterator();iterator.hasNext();){
			Stroke from=iterator.next();
			for(ListIterator<Stroke> iter=strokes.listIterator(iterator.previousIndex()+1);iter.hasNext();){
				Stroke to=iter.next();
				if(isLikelySameCharacter(from,to)){
					partition.union(iterator.previousIndex(),iter.previousIndex());
				}
			}
		}
		//System.out.println(lines.size());
		lines.removeIf((c)->c==null||c.getComponents().size()<=1||c.getBox().getArea()<=256);
		//System.out.println(lines.size());
		//System.out.println();
		return lines;
	}
	private boolean isLikelySameCharacter(Stroke from,Stroke to){
		int fromSize=Math.max(from.getComponent().getWidth(),from.getComponent().getHeight());
		int toSize=Math.max(to.getComponent().getWidth(),to.getComponent().getHeight());
		if(fromSize>=5*toSize||toSize>=5*fromSize){
			return false;
		}
		if(!isConnected(from.getRGB(),to.getRGB())){
			return false;
		}
		int distance=calculateDistance(from.getComponent(),to.getComponent());
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
		return Math.max(dx,dy);
	}
	private void filterLine(List<LineCandidate> lines){
		lines.removeIf((line)->line.getComponents().size()<=1||line.getBox().getArea()<=512);
	}
	private static class Stroke{
		private final ConnectedComponent component;
		private int r, g, b, w;
		public Stroke(ConnectedComponent component,int rgb){
			this.component=component;
			w=component.getWeight();
			r=((rgb&0xFF0000)>>16)*w;
			g=((rgb&0xFF00)>>8)*w;
			b=(rgb&0xFF)*w;
		}
		public int getRGB(){
			int red=r/w;
			int green=g/w;
			int blue=b/w;
			return (red<<16)|(green<<8)|blue;
		}
		public ConnectedComponent getComponent(){
			return component;
		}
		public void combineWith(Stroke stroke){
			component.combineWith(stroke.component);
			r+=stroke.r;
			g+=stroke.g;
			b+=stroke.b;
		}
	}
}
