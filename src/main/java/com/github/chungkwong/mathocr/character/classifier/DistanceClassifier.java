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
import com.github.chungkwong.mathocr.common.ConnectedComponent;
import com.github.chungkwong.mathocr.character.VectorFeature;
import com.github.chungkwong.mathocr.character.Features;
import com.github.chungkwong.mathocr.character.CharacterCandidate;
import com.github.chungkwong.mathocr.character.CharacterRecognizer;
import com.github.chungkwong.mathocr.character.CharacterList;
import java.util.*;
import java.util.function.*;
/**
 *
 * @author Chan Chung Kwong
 */
public class DistanceClassifier implements CharacterRecognizer{
	public static final String NAME="DISTANCE";
	private final ToDoubleBiFunction<double[],double[]> distance;
	public DistanceClassifier(ToDoubleBiFunction<double[],double[]> distance){
		this.distance=distance;
	}
	@Override
	public NavigableSet<CharacterCandidate> recognize(ConnectedComponent component,Object modelRaw,CharacterList list){
		DistanceModel model=(DistanceModel)modelRaw;
		int variables=model.getFeatures().stream().mapToInt((name)->((VectorFeature)Features.REGISTRY.get(name)).getDimension()).sum();
		double[] unknown=new double[variables];
		int j=0;
		for(String name:model.getFeatures()){
			double[] sub=(double[])Features.REGISTRY.get(name).extract(component);
			for(double d:sub){
				unknown[j++]=d;
			}
		}
		int i=0;
		double minDist=Double.MAX_VALUE;
		int bestCandidate=0;
		for(Iterator<List<double[]>> iterator=model.getModel().iterator();iterator.hasNext();++i){
			List<double[]> samples=iterator.next();
			for(double[] sample:samples){
				double dist=distance.applyAsDouble(sample,unknown);
				if(dist<minDist){
					minDist=dist;
					bestCandidate=i;
				}
			}
		}
		TreeSet<CharacterCandidate> treeSet=new TreeSet<>();
		treeSet.add(list.getCharacters().get(bestCandidate).toCandidate(component.getBox(),-minDist));
		return treeSet;
	}
	@Override
	public String getModelType(){
		return DistanceModelType.NAME;
	}
	public static final ToDoubleBiFunction<double[],double[]> EUCLID_DISTANCE=(x,y)->{
		double sum=0;
		for(int i=0;i<x.length;i++){
			double diff=x[i]-y[i];
			sum+=diff*diff;
		}
		return sum;
	};
}
