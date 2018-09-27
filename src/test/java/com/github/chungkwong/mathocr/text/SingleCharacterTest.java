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
import java.util.List;
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
	//ErrataList erratas=ModelManager.getErrataList();
	public void addSample(ConnectedComponent ele,int codePoint){
		NavigableSet<CharacterCandidate> geuss=recognizer.recognize(ele,model,list);
		if(!geuss.isEmpty()){
			/*NavigableSet<Pair<Integer,Double>> errata=erratas.getErrata(geuss.first().getCodePoint());
			if(!errata.isEmpty()&&errata.first().getValue()>0.5){
				geuss.clear();
				geuss.add(new CharacterCandidate(errata.first().getKey(),1,null,0,"",0,0));
			}*/
			if(geuss.stream().mapToInt((CharacterCandidate c)->c.getCodePoint()).anyMatch((c)->c==codePoint)){//||isEquivent(c,codePoint))){
				matrix.advanceFrequency(codePoint,codePoint);
			}else{
				matrix.advanceFrequency(codePoint,geuss.first().getCodePoint());
			}
		}
	}
	private boolean isEquivent(int c0,int c1){
		return isEquivent2(c0,c1)||isEquivent2(c1,c0);
	}
	private boolean isEquivent2(int c0,int c1){
		switch(c0){
			case 'c':
				return c1=='C';
			case 'o':
				return c1=='O'||c1=='0'||c1=='º';
			case '0':
				return c1=='O'||c1=='º';
			case 'O':
				return c1=='º';
			case 'p':
				return c1=='P';
			case 's':
				return c1=='S';
			case 'v':
				return c1=='V';
			case 'w':
				return c1=='W';
			case 'x':
				return c1=='X';
			case 'z':
				return c1=='Z';
			case '∑':
				return c1=='Σ';
			case '∏':
				return c1=='Π';
			case '⋃':
				return c1=='U';
			case '⋯':
				return c1=='…';
			case ',':
				return c1=='’';
			case '˙':
				return c1=='·'||c1=='•'||c1=='.';
			case '·':
				return c1=='•'||c1=='.';
			case '•':
				return c1=='.';
			case '−':
				return c1=='¯'||c1=='–'||c1=='‐'||c1=='—'||c1=='_';
			case '¯':
				return c1=='–'||c1=='‐'||c1=='—'||c1=='_';
			case '–':
				return c1=='‐'||c1=='—'||c1=='_';
			case '‐':
				return c1=='—'||c1=='_';
			case '—':
				return c1=='_';
			case '1':
				return c1=='l'||c1=='I'||c1=='|';
			case 'l':
				return c1=='I'||c1=='|';
			case 'I':
				return c1=='|';
			default:
				return false;
		}
	}
	public void printResult(){
		System.out.println(matrix);
	}
	public ConfusionMatrix getConfusionMatrix(){
		return matrix;
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
		return buildModel(TrainSet.load(discriptor).train(false),recognizer);
	}
	public static SingleCharacterTest buildModel(Font[] fonts,int[] codePoints,CharacterRecognizer recognizer,List<String> features){
		System.out.println("Training:");
		DataSet dataSet=new DataSet(features);
		for(Font font:fonts){
			System.out.println(font.toString());
			dataSet.addSample(font,codePoints);
		}
		return buildModel(dataSet,recognizer);
	}
	public static SingleCharacterTest buildModel(DataSet dataSet,CharacterRecognizer recognizer){
		System.out.println("Building:");
		//System.out.println(dataSet.getCharacterList().getCharacters());
		return new SingleCharacterTest(ModelTypes.REGISTRY.get(recognizer.getModelType()).build(dataSet),
				dataSet.getCharacterList(),recognizer);
	}
	public static SingleCharacterTest loadModel(CharacterRecognizer classifier,File data){
		return new SingleCharacterTest(ModelManager.getModel(classifier.getModelType(),data),ModelManager.getCharacterList(data),classifier);
	}
}
