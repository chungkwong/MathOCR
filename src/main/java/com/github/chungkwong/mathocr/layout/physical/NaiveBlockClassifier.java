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
import java.awt.image.*;
/**
 * Naive implementation of BlockClassifier
 *
 * @author Chan Chung Kwong
 */
public class NaiveBlockClassifier implements BlockClassifier{
	public static final String NAME="NAIVE";
	/**
	 * Create a block classifier
	 */
	public NaiveBlockClassifier(){
	}
	@Override
	public String classify(PhysicalBlock block,BufferedImage input){
		int thre=input.getWidth()/10;
		ConnectedComponent e=null;
		if(!block.getComponents().isEmpty()){
			boolean isGraphics=false;
			boolean isTable=false;
			int sum=0, max=0, count=0, cw=0;
			for(ConnectedComponent ele:block.getComponents()){
				int h=ele.getHeight();
				sum+=h;
				++count;
				if(h>max){//&&!isPossibleSpecial(ele)
					max=h;
					e=ele;
					cw=ele.getWidth();
				}
			}
			/*if(ele.getWidth()>=thre||ele.getHeight()>=thre)
					if(ele.getLeft()==block.getLeft()&&ele.getRight()==block.getRight()&&ele.getTop()==block.getTop()&&ele.getBottom()==block.getBottom()){
						if(blocks.size()>1||ele.getWidth()<=20*ele.getHeight()||ele.getDensity()<=0.8){
							isTable=true;
							break;
						}
					int count=0;
					for(ConnectedComponent ele2:block.getComponents())
						if(ele.getLeft()<=ele2.getRight()&&ele2.getLeft()<=ele.getRight()&&ele.getTop()<=ele2.getBottom()&&ele2.getTop()<=ele.getBottom())
							++count;
					if(count>=50){
						isGraphics=true;
						break;
					}
				}*/
			if(max>sum*15/count||(max>block.getBox().getHeight()*2/3&&cw>block.getBox().getWidth()*2/3&&cw>thre&&!isPossibleSpecial(e))){
				isGraphics=true;
			}
			if(isTable){
				return "TABLE";
			}else if(isGraphics){
				return "IMAGE";
			}else{
				return "TEXT";
			}
		}else{
			return "TEXT";
		}
	}
	/**
	 * Check if ele is likely a speical character
	 *
	 * @param ele to be checked
	 * @return result
	 */
	private static final boolean isPossibleSpecial(ConnectedComponent ele){
		//if(!SpecialMatcher.usable){
		return false;
		//}
		//if(SpecialMatcher.isPossibleRootSign(ele)){
		//	return true;
		//}
		//return false;
	}
}
