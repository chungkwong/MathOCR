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
import de.bwaldvogel.liblinear.*;
import java.util.*;
/**
 *
 * @author Chan Chung Kwong
 */
public class LinearClassifier implements CharacterRecognizer{
	public static final String NAME="LINEAR";
	private static final NavigableSet<CharacterCandidate> EMPTY=new TreeSet<>();
	private final int limit;
	public LinearClassifier(){
		this.limit=5;
	}
	public LinearClassifier(int limit){
		this.limit=limit;
	}
	@Override
	public NavigableSet<CharacterCandidate> recognize(ConnectedComponent component,Object model,CharacterList list){
		LinearModel svmModel=(LinearModel)model;
		int variables=svmModel.getFeatures().stream().mapToInt((name)->((VectorFeature)Features.REGISTRY.get(name)).getDimension()).sum();
		de.bwaldvogel.liblinear.Feature[] vector=new de.bwaldvogel.liblinear.Feature[variables];
		int j=0;
		for(String name:svmModel.getFeatures()){
			double[] sub=(double[])Features.REGISTRY.get(name).extract(component);
			for(double d:sub){
				vector[j++]=new FeatureNode(j,d);
			}
		}
		double[] prob=new double[svmModel.getModel().getNrClass()];
		int best=(int)(Linear.predictValues(svmModel.getModel(),vector,prob)+0.5);
		int[] candidates=new int[limit];
		double[] candidatesValue=new double[limit];
		Arrays.fill(candidates,-1);
		Arrays.fill(candidatesValue,Double.NEGATIVE_INFINITY);
		for(int i=0;i<prob.length;i++){
			int k=limit-1;
			if(prob[i]>candidatesValue[k]){
				for(;k>=1&&prob[i]>candidatesValue[k-1];k--){
					candidates[k]=candidates[k-1];
					candidatesValue[k]=candidatesValue[k-1];
				}
				candidates[k]=i;
				candidatesValue[k]=prob[i];
			}
		}
		//int candidate=(int)(Linear.predict(svmModel.getModel(),vector)+0.5);
		TreeSet<CharacterCandidate> treeSet=new TreeSet<>();
		for(int i=0;i<limit;i++){
			if(candidates[i]!=-1){
				treeSet.add(list.getCharacters().get(svmModel.getModel().getLabels()[candidates[i]]).toCandidate(component.getBox(),candidatesValue[i]));
			}
		}
		return treeSet;
	}
	@Override
	public String getModelType(){
		return LinearModelType.NAME;
	}
}
