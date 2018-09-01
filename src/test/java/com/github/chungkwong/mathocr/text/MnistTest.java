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
import com.github.chungkwong.mathocr.character.classifier.*;
import com.github.chungkwong.mathocr.common.*;
import com.github.chungkwong.mathocr.preprocess.*;
import java.awt.image.*;
import java.io.*;
import java.util.logging.*;
import javax.imageio.*;
/**
 *
 * @author Chan Chung Kwong
 */
public class MnistTest{
	private static final File DATASETS=new File("../datasets");
	public static void main(String[] args){
		File root=new File(DATASETS,"mnist_png");
		File train=new File(root,"training");
		CombinedPreprocessor preprocessor=CombinedPreprocessor.getDefaultCombinedPreprocessor();
		DataSet dataSet=new DataSet();
		System.out.println("training");
		int counter=0;
		for(int i=0;i<10;i++){
			int codePoint='0'+i;
			for(File sample:new File(train,Integer.toString(i)).listFiles()){
				try{
					++counter;
					if(counter%1000==0){
						System.out.println(counter);
					}
					ConnectedComponent image=new ConnectedComponent(preprocessor.apply(ImageIO.read(sample),true));
					CharacterPrototype prototype=new CharacterPrototype(codePoint,image.getBox(),"",28,0);
					dataSet.addSample(prototype,image);
				}catch(IOException ex){
					Logger.getLogger(SingleCharacterTest.class.getName()).log(Level.SEVERE,sample.toString(),ex);
				}
			}
		}
		CharacterRecognizer recognizer=new SvmClassifier();
		SingleCharacterTest tester=SingleCharacterTest.buildModel(dataSet,recognizer);
		Object model=tester.getModel();
		CharacterList list=tester.getList();
		File test=new File(root,"testing");
		for(int i=0;i<10;i++){
			int codePoint='0'+i;
			for(File sample:new File(test,Integer.toString(i)).listFiles()){
				try{
					BufferedImage image=preprocessor.apply(ImageIO.read(sample),true);
					tester.addSample(image,codePoint);
				}catch(IOException ex){
					Logger.getLogger(SingleCharacterTest.class.getName()).log(Level.SEVERE,sample.toString(),ex);
				}
			}
		}
		tester.printResult();
	}
}
