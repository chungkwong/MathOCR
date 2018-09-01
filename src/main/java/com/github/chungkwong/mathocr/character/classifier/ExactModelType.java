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
import com.github.chungkwong.mathocr.character.ModelType;
import com.github.chungkwong.mathocr.character.CharacterPrototype;
import com.github.chungkwong.mathocr.character.DataSet;
import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.*;
import java.util.regex.*;
import java.util.stream.*;
/**
 *
 * @author Chan Chung Kwong
 */
public class ExactModelType<T> implements ModelType<Map<T,List<Integer>>>{
	public static final String NAME="EXACT";
	private final String modelName;
	private final Function<T,String> encoder;
	private final Function<String,T> decoder;
	public ExactModelType(String modelName,Function<T,String> encoder,Function<String,T> decoder){
		this.modelName=modelName;
		this.encoder=encoder;
		this.decoder=decoder;
	}
	@Override
	public Map<T,List<Integer>> build(DataSet set){
		Map<T,List<Integer>> model=new HashMap<>();
		int featureIndex=set.getFeatureIndex(modelName);
		for(Map.Entry<CharacterPrototype,List<Object[]>> entry:set.getSamples().entrySet()){
			CharacterPrototype prototype=entry.getKey();
			entry.getValue().stream().map((features)->(T)features[featureIndex]).distinct().
					forEach((feature)->{
						List<Integer> list=model.get(feature);
						if(list==null){
							list=new LinkedList<>();
							model.put(feature,list);
						}
						list.add(set.getCharacterIndex(prototype));
					});
		}
		return model;
	}
	@Override
	public void write(Map<T,List<Integer>> model,String fileName) throws IOException{
		try(BufferedWriter out=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName),StandardCharsets.UTF_8))){
			for(Map.Entry<T,List<Integer>> entry:model.entrySet()){
				T key=entry.getKey();
				List<Integer> value=entry.getValue();
				out.write(encoder.apply(key));
				for(Integer integer:value){
					out.write('\t');
					out.write(Integer.toString(integer));
				}
				out.write('\n');
			}
		}
	}
	@Override
	public Map<T,List<Integer>> read(String fileName) throws IOException{
		return Files.lines(new File(fileName).toPath()).filter((line)->line.indexOf('\t')!=-1).collect((Collectors.toMap((line)->{
			return decoder.apply(line.substring(0,line.indexOf('\t')));
		},(line)->{
			return Pattern.compile("\\t").splitAsStream(line).skip(1).map((str)->Integer.parseInt(str)).collect(Collectors.toList());
		})));
	}
}
