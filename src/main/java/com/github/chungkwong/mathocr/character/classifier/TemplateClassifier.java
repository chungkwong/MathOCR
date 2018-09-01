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
package com.github.chungkwong.mathocr.character.classifier;
import com.github.chungkwong.mathocr.common.Pair;
import com.github.chungkwong.mathocr.common.ConnectedComponent;
import com.github.chungkwong.mathocr.common.RunLength;
import com.github.chungkwong.mathocr.common.LimitedSortedList;
import com.github.chungkwong.mathocr.character.CharacterCandidate;
import com.github.chungkwong.mathocr.character.CharacterRecognizer;
import com.github.chungkwong.mathocr.character.CharacterList;
import java.util.*;
/**
 *
 * @author Chan Chung Kwong
 */
public class TemplateClassifier implements CharacterRecognizer{
	private final int limit;
	public TemplateClassifier(int limit){
		this.limit=limit;
	}
	@Override
	public NavigableSet<CharacterCandidate> recognize(ConnectedComponent component,Object modelRaw,CharacterList list){
		List<Pair<Integer,ConnectedComponent>> model=(List<Pair<Integer,ConnectedComponent>>)modelRaw;
		LimitedSortedList<CharacterCandidate> candidates=new LimitedSortedList<CharacterCandidate>(limit,Comparator.naturalOrder());
		model.forEach((e)->candidates.add(list.getCharacters().get(e.getKey()).toCandidate(component.getBox(),-getDistance(component,e.getValue()))));
		return new TreeSet<>(candidates.getElements());
	}
	/**
	 * Calculate the Hausdoff distance between characteristic
	 *
	 * @param ele1 characteristic to be matched
	 * @param ele2 another characteristic to be matched
	 * @return distance between the two characteristic
	 */
	public static double getDistance(ConnectedComponent ele1,ConnectedComponent ele2){
		int height1=ele1.getHeight(), width1=ele1.getWidth(), height2=ele2.getHeight(), width2=ele2.getWidth();
		int ret1=0, ret2=0;
		int dnorm1=height1*height1+width1*width1, dnorm2=height2*height2+width2*width2;
		int top1=ele1.getTop(), top2=ele2.getTop(), left1=ele1.getLeft(), left2=ele2.getLeft();
		int p=height1, q=height2;
		if(width2>height2){
			p=width1;
			q=width2;
		}
		List<RunLength> rls1=ele1.getRunLengths(), rls2=ele2.getRunLengths();
		int i, j, k, l, j11, j12, j21, j22, x, y, dist, tmp;
		for(RunLength rl2:rls2){
			i=rl2.getY()-top2;
			y=i*p/q;
			j21=rl2.getX()-left2;
			j22=j21+rl2.getCount();
			for(j=j21;j<=j22;j++){
				x=j*p/q;
				dist=Integer.MAX_VALUE;
				for(RunLength rl1:rls1){
					k=rl1.getY()-top1-y;
					k*=k;
					if(k>=dist){
						continue;
					}
					j11=rl1.getX()-left1-x;
					j12=j11+rl1.getCount();
					for(l=j11;l<=j12;l++){
						tmp=k+l*l;
						if(tmp<dist){
							dist=tmp;
						}
						//dist=Math.min(dist,k+l*l);
					}
				}
				ret1=Math.max(ret1,dist);
			}
		}
		for(RunLength rl1:rls1){
			i=rl1.getY()-top1;
			y=i*q/p;
			j11=rl1.getX()-left1;
			j12=j11+rl1.getCount();
			for(j=j11;j<=j12;j++){
				x=j*q/p;
				dist=Integer.MAX_VALUE;
				for(RunLength rl2:rls2){
					k=rl2.getY()-top2-y;
					k*=k;
					if(k>=dist){
						continue;
					}
					j21=rl2.getX()-left2-x;
					j22=j21+rl2.getCount();
					for(l=j21;l<=j22;l++){
						tmp=k+l*l;
						if(tmp<dist){
							dist=tmp;
						}
						//dist=Math.min(dist,k+l*l);
					}
				}
				ret2=Math.max(ret2,dist);
			}
		}
		return Math.min(Math.max(Math.sqrt((double)ret1/dnorm1),Math.sqrt((double)ret2/dnorm2)),1);
		//return Math.min(Math.max(Math.sqrt(ret1)/Math.min(height1,width1),Math.sqrt(ret2)/Math.min(height2,width2)),1.0);
	}
	@Override
	public String getModelType(){
		return TemplateModelType.NAME;
	}
	public static void main(String[] args){
		new TemplateClassifier(10).recognize(new ConnectedComponent(),null,null);
	}
}
