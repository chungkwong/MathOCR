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
//import java.io.*;
//import java.nio.file.*;
//import java.util.*;
//import java.util.logging.*;
//import java.util.stream.*;
//import weka.classifiers.trees.*;
//import weka.core.*;
///**
// *
// * @author Chan Chung Kwong
// */
//public class RandomForestModelType implements ModelType<RandomForestModel>{
//	public static final String NAME="RANDOM_FOREST";
//	@Override
//	public RandomForestModel build(DataSet set){
//		int count=set.getCount();
//		List<String> names=set.getSelectedFeatureNames().stream().
//				filter((name)->Features.REGISTRY.get(name) instanceof VectorFeature).collect(Collectors.toList());
//		int variables=names.stream().mapToInt((name)->((VectorFeature)Features.REGISTRY.get(name)).getDimension()).sum();
//		int[] indices=names.stream().mapToInt((name)->set.getFeatureIndex(name)).toArray();
//		ArrayList<Attribute> attInfo=new ArrayList<>(variables+1);
//		for(int i=0;i<variables;i++){
//			attInfo.add(new Attribute(Integer.toString(i)));
//		}
//		int types=set.getCharacterList().getCharacters().size();
//		attInfo.add(new Attribute("class",IntStream.range(0,types).mapToObj(Integer::toString).collect(Collectors.toList())));
//		Instances instances=new Instances(NAME,attInfo,count);
//		instances.setClassIndex(variables);
//		for(Map.Entry<CharacterPrototype,List<Object[]>> entry:set.getSamples().entrySet()){
//			for(Object[] vector:entry.getValue()){
//				int j=0;
//				Instance instance=new DenseInstance(variables+1);
//				instance.setDataset(instances);
//				for(int column:indices){
//					for(double value:(double[])vector[column]){
//						instance.setValue(j++,value);
//					}
//				}
//				instance.setValue(j,Integer.toString(set.getCharacterIndex(entry.getKey())));
//				instances.add(instance);
//			}
//		}
//		//J48 randomForest=new J48();
//		RandomForest randomForest=new RandomForest();
//		//randomForest.setNumFeatures(variables);
//		//randomForest.setNumDecimalPlaces(10);
//		//randomForest.setUnpruned(true);
//		try{
//			long time=System.currentTimeMillis();
//			randomForest.buildClassifier(instances);
//			System.out.println(System.currentTimeMillis()-time);
//			//System.out.println(randomForest.graph());
//		}catch(Exception ex){
//			Logger.getLogger(RandomForestModelType.class.getName()).log(Level.SEVERE,null,ex);
//		}
//		return new RandomForestModel(randomForest,names);
//	}
//	@Override
//	public void write(RandomForestModel model,String fileName) throws IOException{
//		saveFeatureList(model.getFeatures(),fileName);
//		try(ObjectOutputStream out=new ObjectOutputStream(new FileOutputStream(fileName))){
//			out.writeObject(model.getModel());
//		}
//	}
//	@Override
//	public RandomForestModel read(String fileName) throws IOException{
//		try(ObjectInputStream in=new ObjectInputStream(new FileInputStream(fileName))){
//			return new RandomForestModel((RandomForest)in.readObject(),readFeatureList(fileName));
//		}catch(ClassNotFoundException ex){
//			Logger.getLogger(RandomForestModelType.class.getName()).log(Level.SEVERE,null,ex);
//			throw new IOException(ex);
//		}
//	}
//	private void saveFeatureList(List<String> features,String fileName) throws IOException{
//		Files.write(new File(fileName+"_f").toPath(),features.stream().collect(Collectors.joining("\n")).getBytes());
//	}
//	private List<String> readFeatureList(String fileName) throws IOException{
//		return Files.lines(new File(fileName+"_f").toPath()).collect(Collectors.toList());
//	}
//}
