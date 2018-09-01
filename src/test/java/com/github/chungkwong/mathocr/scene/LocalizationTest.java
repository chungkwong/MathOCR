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
import com.github.chungkwong.mathocr.scene.*;
import java.io.*;
import java.util.*;
import java.util.logging.*;
import java.util.stream.*;
import javax.imageio.*;
/**
 *
 * @author Chan Chung Kwong
 */
public abstract class LocalizationTest{
	public LocalizationTest(){
	}
	public void test(TextDetector detector){
		double precision=0;
		double recall=0;
		double hmean=0;
		int count=0;
		for(Iterator<File> iterator=getSamples();iterator.hasNext();){
			System.out.println(++count);
			try{
				File file=iterator.next();
				//System.out.println(file);
				List<BoundBox> detected=detector.detect(ImageIO.read(file)).stream().map((line)->line.getBox()).collect(Collectors.toList());
				List<BoundBox> given=getTruthBoxes(file);
				filter(detected,getIgnoredBoxes(file));
				double m=getMatched(detected,given);
				double r=given.isEmpty()?0:m/given.size();
				double p=detected.isEmpty()?0:m/detected.size();
				recall+=r;
				precision+=p;
				hmean+=r>0||p>0?2*r*p/(r+p):0;
				System.out.println("precision:"+precision/count+"recall:"+recall/count+"hmean:"+hmean/count);
			}catch(IOException ex){
				Logger.getLogger(LocalizationTest.class.getName()).log(Level.SEVERE,null,ex);
			}
		}
		Logger.getGlobal().log(Level.INFO,"precision:{0}",precision/count);
		Logger.getGlobal().log(Level.INFO,"recall:{0}",recall/count);
		Logger.getGlobal().log(Level.INFO,"hmean:{0}",hmean/count);
	}
	private static int getMatched(List<BoundBox> detected,List<BoundBox> given){
		int count=0;
		for(BoundBox d:detected){
			for(BoundBox g:given){
				if(BoundBox.isIntersect(d,g)&&getIntersectionArea(d,g)>getUnionArea(d,g)/2){
					count++;
					break;
				}
			}
		}
		return count;
	}
	private static void filter(List<BoundBox> detected,List<BoundBox> ignored){
		detected.removeIf((d)->ignored.stream().anyMatch((i)->{
			return BoundBox.isIntersect(d,i)&&BoundBox.intersect(d,i).getArea()>i.getArea()/2;
		}));
	}
	private static double getIntersectionArea(BoundBox a,BoundBox b){
		int w=Math.min(a.getRight(),b.getRight())-Math.max(a.getLeft(),b.getLeft());
		if(w<=0){
			return 0;
		}
		int h=Math.min(a.getBottom(),b.getBottom())-Math.max(a.getTop(),b.getTop());
		if(h<=0){
			return 0;
		}
		return h*w;
	}
	private static double getUnionArea(BoundBox a,BoundBox b){
		int w=Math.max(a.getRight(),b.getRight())-Math.min(a.getLeft(),b.getLeft());
		int h=Math.max(a.getBottom(),b.getBottom())-Math.min(a.getTop(),b.getTop());
		return h*w;
	}
	protected abstract Iterator<File> getSamples();
	protected abstract List<BoundBox> getTruthBoxes(File file);
	protected List<BoundBox> getIgnoredBoxes(File file){
		return Collections.emptyList();
	}
	private static int getArea(int x1,int y1,int x2,int y2,int x3,int y3,int x4,int y4){
		return Math.abs(x1*y2+x2*y3+x3*y4+x4*y1-y1*x2-y2*x3-y3*x4-y4*x1)/2;
	}
}
