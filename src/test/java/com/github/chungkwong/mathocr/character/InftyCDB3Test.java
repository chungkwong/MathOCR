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
package com.github.chungkwong.mathocr.character;
import com.github.chungkwong.mathocr.character.*;
import com.github.chungkwong.mathocr.character.classifier.*;
import com.github.chungkwong.mathocr.character.feature.*;
import com.github.chungkwong.mathocr.common.*;
import com.github.chungkwong.mathocr.preprocess.*;
import java.awt.image.*;
import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;
import java.util.logging.*;
import javax.imageio.*;
/**
 *
 * @author Chan Chung Kwong
 */
public class InftyCDB3Test{
	private static final File DATASETS=new File("../datasets");
	public static void main(String[] args) throws IOException{
		CombinedPreprocessor preprocessor=CombinedPreprocessor.getDefaultCombinedPreprocessor();
		DataSet dataSet=new DataSet(Arrays.asList(AspectRatio.NAME,Gradient.NAME,Grid.NAME,Moments.NAME,CrossNumber.NAME));
		System.out.println("training");
		int[] counter=new int[]{0,-1};
		BufferedImage[] sheet=new BufferedImage[]{null};
		Files.lines(new File(DATASETS,"InftyCDB-3/InftyCDB-3_new/InftyCDB-3-A/CharInfoDB-3-A_Info.csv").toPath()).skip(1).forEach((line)->{
			String[] row=line.split(",");
			try{
				++counter[0];
				if(counter[0]%1000==0){
					System.out.println(counter[0]);
				}
				int s=Integer.parseInt(row[2]);
				if(s!=counter[1]){
					sheet[0]=preprocessor.apply(ImageIO.read(new File(DATASETS,"InftyCDB-3/InftyCDB-3_new/InftyCDB-3-A/images/"+s+".png")),true);
					counter[1]=s;
				}
				ConnectedComponent image=new ConnectedComponent(sheet[0].getSubimage(Integer.parseInt(row[3]),Integer.parseInt(row[4]),Integer.parseInt(row[5]),Integer.parseInt(row[6])));
				int codePoint=Integer.parseInt(row[1],16);
				CharacterPrototype prototype=new CharacterPrototype(codePoint,image.getBox(),"",32,0);
				dataSet.addSample(prototype,image);
			}catch(IOException ex){
				Logger.getLogger(SingleCharacterTest.class.getName()).log(Level.SEVERE,line,ex);
			}
		});
		System.out.println("building");
		int[] result=new int[]{0,0};
		CharacterRecognizer recognizer=new LinearClassifier(1);
		//CharacterRecognizer recognizer=new SvmClassifier();
		Object model=ModelTypes.REGISTRY.get(recognizer.getModelType()).build(dataSet);
		CharacterList list=dataSet.getCharacterList();
		SingleCharacterTest tester=new SingleCharacterTest(model,list,recognizer);
		String trainFile="InftyCDB-1-6152/InftyCDB-1/InftyCDB-1/InftyCDB-1.csv";
		System.out.println("testing");
		CsvParser parser=new CsvParser(Files.lines(new File(DATASETS,trainFile).toPath(),StandardCharsets.ISO_8859_1),false);
		class Record{
			int codePoint, width, height, left, top;
		}
		Map<String,List<Record>> records=new HashMap<>();
		while(parser.hasNext()){
			java.util.List<String> row=parser.next();
			Record record=new Record();
			String path=row.get(15);
			record.codePoint=Integer.parseInt(row.get(4),16);
			record.width=Integer.parseInt(row.get(11));
			record.height=Integer.parseInt(row.get(12));
			record.left=Integer.parseInt(row.get(16));
			record.top=Integer.parseInt(row.get(17));
			if(!records.containsKey(path)){
				records.put(path,new ArrayList<>());
			}
			records.get(path).add(record);
		}
		int testedImage=0;
		for(Map.Entry<String,List<Record>> entry:records.entrySet()){
			String key=entry.getKey();
			List<Record> value=entry.getValue();
			System.out.println((++testedImage)+":"+key);
			BufferedImage image=ImageIO.read(new File(DATASETS,"InftyCDB-1-6152/InftyCDB-1/InftyCDB-1/Images/"+key));
			image=CombinedPreprocessor.getDefaultCombinedPreprocessor().apply(image,true);
			for(Record record:value){
				tester.addSample(image.getSubimage(record.left,record.top,record.width,record.height),record.codePoint);
			}
		}
		tester.printResult();
	}
}
