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
package com.github.chungkwong.mathocr.text;
import com.github.chungkwong.mathocr.character.*;
import com.github.chungkwong.mathocr.common.*;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
/**
 *
 * @author Chan Chung Kwong
 */
class SingleCharacterTest{
	private final Object model;
	private final CharacterList list;
	private final CharacterRecognizer recognizer;
	private final ConfusionMatrix matrix=new ConfusionMatrix();
	public SingleCharacterTest(Object model,CharacterList list,CharacterRecognizer recognizer){
		this.model=model;
		this.list=list;
		this.recognizer=recognizer;
		System.out.println("Testing:");
	}
	public void addSample(BufferedImage image,int codePoint){
		addSample(new ConnectedComponent(image),codePoint);
	}
	public void addSample(ConnectedComponent ele,int codePoint){
		NavigableSet<CharacterCandidate> geuss=recognizer.recognize(ele,model,list);
		if(!geuss.isEmpty()){
			if(geuss.stream().mapToInt((CharacterCandidate c)->c.getCodePoint()).anyMatch((c)->c==codePoint)){
				matrix.advanceFrequency(codePoint,codePoint);
			}else{
				matrix.advanceFrequency(codePoint,geuss.first().getCodePoint());
			}
		}
	}
	public void printResult(){
		System.out.println(matrix);
	}
	public Object getModel(){
		return model;
	}
	public CharacterRecognizer getRecognizer(){
		return recognizer;
	}
	public CharacterList getList(){
		return list;
	}
	public static SingleCharacterTest buildModel(InputStream discriptor,CharacterRecognizer recognizer) throws Exception{
		System.out.println("Training:");
		return buildModel(TrainSet.load(discriptor).train(),recognizer);
	}
	public static SingleCharacterTest buildModel(Font[] fonts,int[] codePoints,CharacterRecognizer recognizer){
		System.out.println("Training:");
		DataSet dataSet=new DataSet();
		for(Font font:fonts){
			System.out.println(font.toString());
			dataSet.addSample(font,codePoints);
		}
		return buildModel(dataSet,recognizer);
	}
	public static SingleCharacterTest buildModel(DataSet dataSet,CharacterRecognizer recognizer){
		System.out.println("Building:");
		return new SingleCharacterTest(ModelTypes.REGISTRY.get(recognizer.getModelType()).build(dataSet),
				dataSet.getCharacterList(),recognizer);
	}
	public static SingleCharacterTest loadModel(CharacterRecognizer classifier,File data){
		return new SingleCharacterTest(ModelManager.getModel(classifier.getModelType(),data),ModelManager.getCharacterList(data),classifier);
	}
}
