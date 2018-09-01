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
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;
/**
 *
 * @author Chan Chung Kwong
 */
public class DistanceModelType implements ModelType<DistanceModel>{
	public static final String NAME="DISTANCE";
	@Override
	public DistanceModel build(DataSet set){
		int count=set.getCount();
		List<String> names=set.getSelectedFeatureNames().stream().
				filter((name)->Features.REGISTRY.get(name) instanceof VectorFeature).collect(Collectors.toList());
		int variables=names.stream().mapToInt((name)->((VectorFeature)Features.REGISTRY.get(name)).getDimension()).sum();
		int[] indices=names.stream().mapToInt((name)->set.getFeatureIndex(name)).toArray();
		List<CharacterPrototype> characterList=set.getCharacterList().getCharacters();
		List<List<double[]>> model=new ArrayList<>(characterList.size());
		for(CharacterPrototype prototype:characterList){
			List<Object[]> samples=set.getSamples().get(prototype);
			if(samples!=null){
				ArrayList<double[]> vectors=new ArrayList<>(samples.size());
				for(Object[] sample:samples){
					double[] vector=new double[variables];
					int j=0;
					for(int column:indices){
						for(double value:(double[])sample[column]){
							vector[j++]=value;
						}
					}
					vectors.add(vector);
				}
				model.add(vectors);
			}else{
				model.add(Collections.emptyList());
			}
		}
		return new DistanceModel(model,names);
	}
	@Override
	public void write(DistanceModel model,String fileName) throws IOException{
		try(BufferedWriter out=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName),StandardCharsets.UTF_8))){
			int i=0;
			for(Iterator<List<double[]>> iterator=model.getModel().iterator();iterator.hasNext();++i){
				List<double[]> samples=iterator.next();
				for(double[] sample:samples){
					out.write(Integer.toString(i));
					for(double scalar:sample){
						out.write('\t');
						out.write(Double.toString(scalar));
					}
					out.write('\n');
				}
			}
		}
		saveFeatureList(model.getFeatures(),fileName);
	}
	@Override
	public DistanceModel read(String fileName) throws IOException{
		return new DistanceModel(readVector(fileName),readFeatureList(fileName));
	}
	private List<List<double[]>> readVector(String fileName) throws IOException{
		List<List<double[]>> list=new ArrayList<>();
		Files.lines(new File(fileName).toPath()).forEach((line)->{
			if(line.isEmpty()){
				return;
			}
			String[] split=line.split("\t");
			int index=Integer.parseInt(split[0]);
			while(index>=list.size()){
				list.add(new ArrayList<>());
			}
			list.get(index).add(Arrays.stream(split).skip(1).mapToDouble((s)->Double.parseDouble(s)).toArray());
		});
		return list;
	}
	private void saveFeatureList(List<String> features,String fileName) throws IOException{
		Files.write(new File(fileName+"_f").toPath(),features.stream().collect(Collectors.joining("\n")).getBytes());
	}
	private List<String> readFeatureList(String fileName) throws IOException{
		return Files.lines(new File(fileName+"_f").toPath()).collect(Collectors.toList());
	}
}
