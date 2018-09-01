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
import com.github.chungkwong.mathocr.preprocess.Grayscale;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import java.util.List;
import javax.imageio.*;
import javax.swing.*;
/**
 *
 * @author Chan Chung Kwong
 */
public class MserExtractor implements Iterator<List<Integer>>{
	private final int[] bitmap;
	private final int width, height;
	private final BitSet mask;
	private final LinkedList<ArrayList<Integer>> components;
	private final LinkedList<Integer> componentsLevel;
	private Pixel currentPixel;
	private PriorityQueue<Pixel> boundary;
	public MserExtractor(BufferedImage image){
		image=new Grayscale().apply(image,true);
		width=image.getWidth();
		height=image.getHeight();
		bitmap=new int[width*height];
		int[] rgb=image.getRGB(0,0,width,height,null,0,width);
		for(int i=0;i<rgb.length;i++){
			bitmap[i]=rgb[i]&0xFF;
		}
		mask=new BitSet(bitmap.length);
		components=new LinkedList<>();
		components.push(new ArrayList<>());
		componentsLevel=new LinkedList<>();
		componentsLevel.push(256);
		currentPixel=new Pixel(0,bitmap[0],-1);
		boundary=new PriorityQueue<>();
		search();
	}
	private void search(){
		while(++currentPixel.direction<8){
			int location=currentPixel.location;
			int i=location/width;
			int j=location%width;
			switch(currentPixel.direction){
				case 0:
					if(j+1<width){
						search(location+1);
					}
					break;
				case 1:
					if(j+1<width&&i+1<height){
						search(location+width+1);
					}
					break;
				case 2:
					if(i+1<height){
						search(location+width);
					}
					break;
				case 3:
					if(j>0&&i+1<height){
						search(location+width-1);
					}
					break;
				case 4:
					if(j>0){
						search(location-1);
					}
					break;
				case 5:
					if(i>0&&j>0){
						search(location-width-1);
					}
					break;
				case 6:
					if(i>0){
						search(location-width);
					}
					break;
				case 7:
					if(i>0&&j<width-1){
						search(location-width+1);
					}
					break;
			}
		}
		components.peek().add(currentPixel.location);
	}
	private void search(int pos){
		if(!mask.get(pos)){
			mask.set(pos);
			int level=bitmap[pos];
			if(level>=currentPixel.level){
				boundary.add(new Pixel(pos,level,-1));
			}else{
				boundary.add(currentPixel);
				currentPixel=new Pixel(pos,level,-1);
				componentsLevel.push(currentPixel.level);
				components.push(new ArrayList<>());
			}
		}
	}
	private List<Integer> next;
	private int currentLevel;
	private boolean inner=false;
	@Override
	public boolean hasNext(){
		if(next!=null){
			return true;
		}
		if(inner&&currentPixel.level>componentsLevel.peek()){
			processStack();
			return true;
		}else{
			inner=false;
		}
		while(!boundary.isEmpty()){
			Pixel prev=currentPixel;
			currentPixel=boundary.poll();
			int newLevel=currentPixel.level;
			if(currentPixel.level>prev.level){
				processStack();
				return true;
			}
			search();
		}
		return false;
	}
	private void processStack(){
		next=components.peek();
		currentLevel=componentsLevel.peek();
		if(componentsLevel.size()<2){
			inner=false;
			return;
		}
		int secondLevel=componentsLevel.get(1);
		int nextLevel=Math.min(secondLevel,currentPixel.level);
		if(currentPixel.level<secondLevel){
			componentsLevel.set(0,secondLevel);
			inner=false;
		}else{
			componentsLevel.pop();
			components.pop();
			components.peek().addAll(next);
			inner=true;
		}
	}
	@Override
	public List<Integer> next(){
		if(hasNext()){
			List<Integer> tmp=next;
			next=null;
			return tmp;
		}else{
			throw new NoSuchElementException();
		}
	}
	public int getCurrentLevel(){
		return currentLevel;
	}
	private static class Pixel implements Comparable<Pixel>{
		private int location;
		private int level;
		private int direction;
		public Pixel(int location,int level,int direction){
			this.location=location;
			this.level=level;
			this.direction=direction;
		}
		@Override
		public int compareTo(Pixel o){
			int compare=Integer.compare(level,o.level);
			if(compare!=0){
				return compare;
			}else{
				return Integer.compare(location,o.location);
			}
		}
	}
	public static void main(String[] args) throws IOException{
		//BufferedImage image=ImageIO.read(new File("/home/kwong/图片/0705_16.jpg"));
		BufferedImage image=new Grayscale().apply(ImageIO.read(new File("/home/kwong/projects/SciOCR-data/icdar2017/ch8_training_images_8/img_7007.jpg")),true);
		MserExtractor extractor=new MserExtractor(image);
		JFrame f=new JFrame();
		JLabel icon=new JLabel(new ImageIcon(image));
		f.add(new JScrollPane(icon),BorderLayout.CENTER);
		JButton next=new JButton("next");
		next.addActionListener((e)->{
			if(extractor.hasNext()){
				int[] rgb=image.getRGB(0,0,image.getWidth(),image.getHeight(),null,0,image.getWidth());
				for(int i=0;i<rgb.length;i++){
					rgb[i]=0xFFFFFF;
				}
				for(int i:extractor.next()){
					rgb[i]=0x000000;
				}
				image.setRGB(0,0,image.getWidth(),image.getHeight(),rgb,0,image.getWidth());
				icon.repaint();
				System.out.println(extractor.getCurrentLevel());
			}else{
				next.setEnabled(false);
			}
		});
		f.add(next,BorderLayout.SOUTH);
		f.setExtendedState(JFrame.MAXIMIZED_BOTH);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);
	}
}
