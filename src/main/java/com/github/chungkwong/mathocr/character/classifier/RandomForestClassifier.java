///*
// * Copyright (C) 2018 Chan Chung Kwong
// *
// * This program is free software: you can redistribute it and/or modify
// * it under the terms of the GNU Affero General Public License as published by
// * the Free Software Foundation, either version 3 of the License, or
// * (at your option) any later version.
// *
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU Affero General Public License for more details.
// *
// * You should have received a copy of the GNU Affero General Public License
// * along with this program.  If not, see <http://www.gnu.org/licenses/>.
// */
//package com.github.chungkwong.mathocr.character.classifier;
//import com.github.chungkwong.mathocr.character.*;
//import com.github.chungkwong.mathocr.common.*;
//import java.util.*;
//import java.util.logging.*;
//import java.util.stream.*;
//import weka.core.*;
///**
// *
// * @author Chan Chung Kwong
// */
//public class RandomForestClassifier implements CharacterRecognizer{
//	@Override
//	public NavigableSet<CharacterCandidate> recognize(ConnectedComponent component,Object model,CharacterList list){
//		RandomForestModel randomForestModel=(RandomForestModel)model;
//		int variables=randomForestModel.getFeatures().stream().mapToInt((name)->((VectorFeature)Features.REGISTRY.get(name)).getDimension()).sum();
//		double[] vector=new double[variables+1];
//		int j=0;
//		for(String name:randomForestModel.getFeatures()){
//			double[] sub=(double[])Features.REGISTRY.get(name).extract(component);
//			for(double d:sub){
//				vector[j++]=d;
//			}
//		}
//		TreeSet<CharacterCandidate> candidates=new TreeSet<>();
//		ArrayList<Attribute> attInfo=new ArrayList<>(variables+1);
//		for(int i=0;i<variables;i++){
//			attInfo.add(new Attribute(Integer.toString(i)));
//		}
//		int types=list.getCharacters().size();
//		attInfo.add(new Attribute("class",IntStream.range(0,types).mapToObj(Integer::toString).collect(Collectors.toList())));
//		Instances instances=new Instances("",attInfo,1);
//		instances.setClassIndex(variables);
//		Instance instance=new DenseInstance(1.0,vector);
//		instances.add(instance);
//		instance.setDataset(instances);
//		instances.add(instance);
//		try{
//			CharacterPrototype prototype=list.getCharacters().get((int)(randomForestModel.getModel().classifyInstance(instance)+0.5));
//			candidates.add(prototype.toCandidate(component.getBox(),1.0));
//		}catch(Exception ex){
//			Logger.getLogger(RandomForestClassifier.class.getName()).log(Level.SEVERE,null,ex);
//		}
//		return candidates;
//	}
//	@Override
//	public String getModelType(){
//		return RandomForestModelType.NAME;
//	}
//}
