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
import libsvm.*;
/**
 *
 * @author Chan Chung Kwong
 */
public class SvmClassifier implements CharacterRecognizer{
	public static final String NAME="SVM";
	private static final NavigableSet<CharacterCandidate> EMPTY=new TreeSet<>();
	@Override
	public NavigableSet<CharacterCandidate> recognize(ConnectedComponent component,Object model,CharacterList list){
		SvmModel svmModel=(SvmModel)model;
		int variables=svmModel.getFeatures().stream().mapToInt((name)->((VectorFeature)Features.REGISTRY.get(name)).getDimension()).sum();
		svm_node[] vector=new svm_node[variables];
		int j=0;
		for(String name:svmModel.getFeatures()){
			double[] sub=(double[])Features.REGISTRY.get(name).extract(component);
			for(double d:sub){
				svm_node node=new svm_node();
				node.index=j;
				node.value=d;
				vector[j++]=node;
			}
		}
		int candidate=(int)(svm.svm_predict(svmModel.getModel(),vector)+0.5);
		if(candidate>=0&&candidate<list.getCharacters().size()){
			TreeSet<CharacterCandidate> treeSet=new TreeSet<>();
			treeSet.add(list.getCharacters().get(candidate).toCandidate(component.getBox(),1.0));
			return treeSet;
		}else{
			return Collections.emptyNavigableSet();
		}
	}
	@Override
	public String getModelType(){
		return SvmModelType.NAME;
	}
}
