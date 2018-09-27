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
		ExtendedImage extendedImage=new ExtendedImage(rgb,width,height);
		List<Stroke> colorCluster=clusterByColor(extendedImage);
		filterConponent(colorCluster);
		List<LineCandidate> lines=roughCharacterCluster(colorCluster,extendedImage);
		return lines;
	}
	private List<Stroke> clusterByColor(ExtendedImage image){
		int width=image.width;
		int height=image.height;
		int[] pixels=image.pixels;
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
				BoundBox box=new BoundBox(j,k,i,i);
				int w=k-j;
				int r=image.getRed(j,k,i,i);
				int g=image.getGreen(j,k,i,i);
				int b=image.getBlue(j,k,i,i);
				long s=image.getSquaredValue(j,k,i,i);
				if(id==-1){
					components.add(new Stroke(box,w,r,g,b,s));
					partition.makeSet();
					id=curr++;
				}else{
					id=partition.findRoot(id);
					Stroke stroke=components.get(id);
					stroke.box=BoundBox.union(box,stroke.box);
					stroke.w+=w;
					stroke.r+=r;
					stroke.g+=g;
					stroke.b+=b;
					stroke.s+=s;
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
		int dr=Math.abs(((i&0xFF0000)>>16)-((j&0xFF0000)>>16));
		int dg=Math.abs(((i&0xFF00)>>8)-((j&0xFF00)>>8));
		int db=Math.abs((i&0xFF)-(j&0xFF));
		//return 316*dr+624*dg+84*db<45*1024;
		return dr+dg+db<45;
	}
	private void filterConponent(List<Stroke> colorCluster){
		colorCluster.removeIf((c)->c==null||c.box.getWidth()<=4||c.box.getHeight()<=4);
	}
	private List<LineCandidate> roughCharacterCluster(List<Stroke> strokes,ExtendedImage image){
		List<List<Stroke>> lines=strokes.stream().map((s)->{
			ArrayList<Stroke> set=new ArrayList<>(1);
			set.add(s);
			return set;
		}).collect(Collectors.toCollection(ArrayList::new));
		Partition partition=new Partition((int m,int n)->{
			lines.get(n).addAll(lines.get(m));
			lines.set(m,null);
		},strokes.size());
		for(ListIterator<Stroke> iterator=strokes.listIterator();iterator.hasNext();){
			Stroke from=iterator.next();
			for(ListIterator<Stroke> iter=strokes.listIterator(iterator.previousIndex()+1);iter.hasNext();){
				Stroke to=iter.next();
				if(isLikelySameCharacter(from,to,image)){
					partition.union(iterator.previousIndex(),iter.previousIndex());
				}
			}
		}
		//System.out.println(lines.size());
		lines.removeIf((c)->c==null||c.size()<=1||!isLikelyLine(strokes,image));
		//System.out.println(lines.size());
		//System.out.println();
		return lines.stream().map((l)->new LineCandidate(l.stream().map((c)->new ConnectedComponent(c.box)).collect(Collectors.toList()))).collect(Collectors.toList());
	}
	private boolean isLikelySameCharacter(Stroke from,Stroke to,ExtendedImage image){
		if(!isConnected(from.getRGB(),to.getRGB())){
			return false;
		}
		int fromSize=Math.max(from.box.getWidth(),from.box.getHeight());
		int toSize=Math.max(to.box.getWidth(),to.box.getHeight());
		if(fromSize>=5*toSize||toSize>=5*fromSize){
			return false;
		}
		if(!isConnected(from.getRGB(),to.getRGB())){
			return false;
		}
		int distance=calculateDistance(from.box,to.box);
		return distance<Math.min(fromSize,toSize)/2;
	}
	public static int calculateDistance(BoundBox fromBox,BoundBox toBox){
		int dx=fromBox.getLeft()<=toBox.getRight()&&toBox.getLeft()<=fromBox.getRight()?0
				:Math.max(fromBox.getLeft()-toBox.getRight(),toBox.getLeft()-fromBox.getRight());
		//int dy=fromBox.getTop()<=toBox.getBottom()&&toBox.getTop()<=fromBox.getBottom()?0
		//		:Math.max(fromBox.getTop()-toBox.getBottom(),toBox.getTop()-fromBox.getBottom());
		int dy=Math.abs(fromBox.getBottom()-toBox.getBottom());
		return Math.max(dx,dy);
	}
	private boolean isLikelyLine(List<Stroke> strokes,ExtendedImage image){
		BoundBox box=BoundBox.union(strokes.stream().map((s)->s.box).toArray(BoundBox[]::new));
		if(box.getArea()<=512){
			return false;
		}
		int w=strokes.stream().mapToInt((s)->s.w).sum()*3;
		long devIn=strokes.stream().mapToLong((s)->s.s).sum()/w-square(strokes.stream().mapToInt((s)->s.r+s.g+s.b).sum())/w*w;
		int area=box.getArea()*3;
		long devOut=image.getSquaredValue(box.getLeft(),box.getRight(),box.getTop(),box.getBottom())/area
				-(image.getRed(box.getLeft(),box.getRight(),box.getTop(),box.getBottom())+image.getGreen(box.getLeft(),box.getRight(),box.getTop(),box.getBottom())+image.getBlue(box.getLeft(),box.getRight(),box.getTop(),box.getBottom()))/(area*area);
		return devOut>3*devIn;
	}
	private static long square(long l){
		return l*l;
	}
	private static class Stroke{
		private BoundBox box;
		private int r, g, b, w;
		private long s;
		public Stroke(BoundBox box,int w,int r,int g,int b,long s){
			this.box=box;
			this.w=w;
			this.r=r;
			this.g=g;
			this.b=b;
			this.s=s;
		}
		public int getRGB(){
			int red=r/w;
			int green=g/w;
			int blue=b/w;
			return (red<<16)|(green<<8)|blue;
		}
		public void combineWith(Stroke stroke){
			box=BoundBox.union(box,stroke.box);
			w+=stroke.w;
			r+=stroke.r;
			g+=stroke.g;
			b+=stroke.b;
			s+=stroke.s;
		}
	}
	private static class ExtendedImage{
		int[] pixels;
		int width, height;
		int[] rSum, gSum, bSum;
		long[] sqSum;
		public ExtendedImage(int[] pixels,int width,int height){
			this.pixels=pixels;
			this.width=width;
			this.height=height;
			rSum=getIntegralImage(16);
			gSum=getIntegralImage(8);
			bSum=getIntegralImage(0);
			sqSum=getSquaredIntegralImage();
		}
		private int[] getIntegralImage(int shift){
			int[] intImg=new int[(height+1)*(width+1)];
			for(int i=0, ind=width+1, indo=0;i<height;i++){
				++ind;
				for(int j=0;j<width;j++,ind++,indo++){
					intImg[ind]=intImg[ind-width-1]+intImg[ind-1]+((pixels[indo]>>shift)&0xFF)-intImg[ind-width-2];
				}
			}
			return intImg;
		}
		private long[] getSquaredIntegralImage(){
			long[] intImg=new long[(height+1)*(width+1)];
			for(int i=0, ind=width+1, indo=0;i<height;i++){
				++ind;
				for(int j=0;j<width;j++,ind++,indo++){
					intImg[ind]=intImg[ind-width-1]+intImg[ind-1]+getSquaredValue(pixels[indo])-intImg[ind-width-2];
				}
			}
			return intImg;
		}
		public int getRed(int left,int right,int top,int bottom){
			return getValue(left,right,top,bottom,rSum);
		}
		public int getGreen(int left,int right,int top,int bottom){
			return getValue(left,right,top,bottom,gSum);
		}
		public int getBlue(int left,int right,int top,int bottom){
			return getValue(left,right,top,bottom,bSum);
		}
		private int getValue(int left,int right,int top,int bottom,int[] integral){
			++right;
			++bottom;
			return integral[bottom*width+right]-integral[bottom*width+left]-integral[top*width+right]+integral[top*width+left];
		}
		private long getSquaredValue(int left,int right,int top,int bottom){
			++right;
			++bottom;
			return sqSum[bottom*width+right]-sqSum[bottom*width+left]-sqSum[top*width+right]+sqSum[top*width+left];
		}
		private static int getSquaredValue(int pixel){
			int r=(pixel>>16)&0xFF;
			int g=(pixel>>8)&0xFF;
			int b=pixel&0xFF;
			return r*r+g*g+b*b;
		}
	}
}
