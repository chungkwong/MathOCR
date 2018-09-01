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
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;
import libsvm.*;
/**
 *
 * @author Chan Chung Kwong
 */
public class SvmModelType implements ModelType<SvmModel>{
	public static final String NAME="SVM";
	private final svm_parameter parameter;
	public SvmModelType(){
		parameter=new svm_parameter();
		parameter.svm_type=svm_parameter.C_SVC;
		parameter.kernel_type=svm_parameter.RBF;
		parameter.degree=3;
		parameter.gamma=0;
		parameter.coef0=0;
		parameter.nu=0.5;
		parameter.cache_size=100;
		parameter.C=10000;
		parameter.eps=1e-3;
		parameter.p=0.1;
		parameter.shrinking=1;
		parameter.probability=0;
		parameter.nr_weight=0;
		parameter.weight_label=new int[0];
		parameter.weight=new double[0];
		svm.svm_set_print_string_function((str)->{
		});
	}
	public SvmModelType(svm_parameter parameter){
		this.parameter=parameter;
	}
	public svm_parameter getParameter(){
		return parameter;
	}
	@Override
	public SvmModel build(DataSet set){
		svm_problem problem=new svm_problem();
		int count=set.getCount();
		List<String> names=set.getSelectedFeatureNames().stream().
				filter((name)->Features.REGISTRY.get(name) instanceof VectorFeature).collect(Collectors.toList());
		int variables=names.stream().mapToInt((name)->((VectorFeature)Features.REGISTRY.get(name)).getDimension()).sum();
		int[] indices=names.stream().mapToInt((name)->set.getFeatureIndex(name)).toArray();
		problem.l=count;
		problem.x=new svm_node[count][variables];
		problem.y=new double[count];
		int i=0;
		for(Map.Entry<CharacterPrototype,List<Object[]>> entry:set.getSamples().entrySet()){
			for(Object[] vector:entry.getValue()){
				int j=0;
				for(int column:indices){
					for(double value:(double[])vector[column]){
						svm_node node=new svm_node();
						node.index=j;
						node.value=value;
						problem.x[i][j++]=node;
					}
				}
				problem.y[i++]=set.getCharacterIndex(entry.getKey());
			}
		}
		if(parameter.gamma==0){
			svm_parameter modified=(svm_parameter)parameter.clone();
			modified.gamma=1.0/variables;
			SvmModel m=new SvmModel(svm.svm_train(problem,modified),names);
			return m;
		}else{
			return new SvmModel(svm.svm_train(problem,parameter),names);
		}
	}
	@Override
	public void write(SvmModel model,String fileName) throws IOException{
		saveFeatureList(model.getFeatures(),fileName);
		svm.svm_save_model(fileName,model.getModel());
	}
	@Override
	public SvmModel read(String fileName) throws IOException{
		return new SvmModel(svm.svm_load_model(fileName),readFeatureList(fileName));
	}
	private void saveFeatureList(List<String> features,String fileName) throws IOException{
		Files.write(new File(fileName+"_f").toPath(),features.stream().collect(Collectors.joining("\n")).getBytes());
	}
	private List<String> readFeatureList(String fileName) throws IOException{
		return Files.lines(new File(fileName+"_f").toPath()).collect(Collectors.toList());
	}
	public static void main(String[] args){
		svm_parameter parameter=new SvmModelType().parameter;
		parameter.gamma=0.5;
		svm_problem problem=new svm_problem();
		problem.l=10;
		problem.y=new double[]{0,0,0,0,0,1,1,1,1,1};
		problem.x=new svm_node[10][2];
		for(int i=0;i<5;i++){
			problem.x[i][0]=new svm_node();
			problem.x[i][0].index=1;
			problem.x[i][0].value=i;
			problem.x[i][1]=new svm_node();
			problem.x[i][1].index=2;
			problem.x[i][1].value=i+1;
		}
		for(int i=5;i<10;i++){
			problem.x[i][0]=new svm_node();
			problem.x[i][0].index=1;
			problem.x[i][0].value=i-5;
			problem.x[i][1]=new svm_node();
			problem.x[i][1].index=2;
			problem.x[i][1].value=i-6;
		}
		svm_model model=svm.svm_train(problem,parameter);
		svm_node[] unknown=new svm_node[]{
			new svm_node(),
			new svm_node()
		};
		unknown[0].index=1;
		unknown[1].index=2;
		unknown[0].value=0;
		unknown[1].value=2;
		System.out.println(svm.svm_predict(model,unknown));
	}
}
