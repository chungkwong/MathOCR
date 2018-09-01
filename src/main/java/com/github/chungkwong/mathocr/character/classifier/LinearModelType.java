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
import com.github.chungkwong.mathocr.character.VectorFeature;
import com.github.chungkwong.mathocr.character.ModelType;
import com.github.chungkwong.mathocr.character.Features;
import com.github.chungkwong.mathocr.character.CharacterPrototype;
import com.github.chungkwong.mathocr.character.DataSet;
import de.bwaldvogel.liblinear.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;
/**
 *
 * @author Chan Chung Kwong
 */
public class LinearModelType implements ModelType<LinearModel>{
	public static final String NAME="LINEAR";
	private final Parameter parameter;
	public LinearModelType(){
		this.parameter=new Parameter(SolverType.L2R_L2LOSS_SVC,5.12,0.1);
	}
	public LinearModelType(Parameter parameter){
		this.parameter=parameter;
	}
	public Parameter getParameter(){
		return parameter;
	}
	@Override
	public LinearModel build(DataSet set){
		Problem problem=new Problem();
		int count=set.getCount();
		List<String> names=set.getSelectedFeatureNames().stream().
				filter((name)->Features.REGISTRY.get(name) instanceof VectorFeature).collect(Collectors.toList());
		int variables=names.stream().mapToInt((name)->((VectorFeature)Features.REGISTRY.get(name)).getDimension()).sum();
		int[] indices=names.stream().mapToInt((name)->set.getFeatureIndex(name)).toArray();
		problem.l=count;
		problem.n=variables;
		problem.x=new de.bwaldvogel.liblinear.Feature[count][variables];
		problem.y=new double[count];
		int i=0;
		for(Map.Entry<CharacterPrototype,List<Object[]>> entry:set.getSamples().entrySet()){
			for(Object[] vector:entry.getValue()){
				int j=0;
				for(int column:indices){
					for(double value:(double[])vector[column]){
						problem.x[i][j++]=new FeatureNode(j,value);
					}
				}
				problem.y[i++]=set.getCharacterIndex(entry.getKey());
			}
		}
		//ParameterSearchResult findParameterC=Linear.findParameterC(problem,parameter,5,0.01,10);
		//System.out.println("C"+findParameterC.getBestC()+"rate"+findParameterC.getBestRate());
		Linear.disableDebugOutput();
		return new LinearModel(Linear.train(problem,parameter),names);
	}
	@Override
	public void write(LinearModel model,String fileName) throws IOException{
		saveFeatureList(model.getFeatures(),fileName);
		Linear.saveModel(new File(fileName),model.getModel());
	}
	@Override
	public LinearModel read(String fileName) throws IOException{
		return new LinearModel(Linear.loadModel(new File(fileName)),readFeatureList(fileName));
	}
	private void saveFeatureList(List<String> features,String fileName) throws IOException{
		Files.write(new File(fileName+"_f").toPath(),features.stream().collect(Collectors.joining("\n")).getBytes());
	}
	private List<String> readFeatureList(String fileName) throws IOException{
		return Files.lines(new File(fileName+"_f").toPath()).collect(Collectors.toList());
	}
}
