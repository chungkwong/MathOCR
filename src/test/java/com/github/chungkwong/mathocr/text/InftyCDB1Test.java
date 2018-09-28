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
import com.github.chungkwong.mathocr.character.classifier.*;
import com.github.chungkwong.mathocr.common.*;
import com.github.chungkwong.mathocr.preprocess.*;
import java.awt.image.*;
import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;
import javax.imageio.*;
/**
 *
 * @author Chan Chung Kwong
 */
public class InftyCDB1Test{
	private static final File DATASETS=new File("../datasets");
	public static void main(String[] args) throws IOException,Exception{
		//SingleCharacterTest tester=SingleCharacterTest.buildModel(SingleCharacterTest.class.getResourceAsStream("/com/github/chungkwong/mathocr/character/infty_train_set.xml"),new LinearClassifier(1));
		//SingleCharacterTest tester=SingleCharacterTest.loadModel(new LinearClassifier(1),new File(System.getProperty("user.home"),".mathocr/default"));
		SingleCharacterTest tester=SingleCharacterTest.loadModel(new SvmClassifier(),new File(System.getProperty("user.home"),".mathocr/infty"));
		Map<String,Integer> name2code=new HashMap<>();
		new BufferedReader(new InputStreamReader(SingleCharacterTest.class.getResourceAsStream("inftyCDB1.map"),StandardCharsets.UTF_8)).
				lines().forEach((line)->{
					int i=line.indexOf('\t');
					if(i!=-1){
						name2code.put(line.substring(0,i),Integer.parseInt(line.substring(i+1),16));
					}
				});
		String trainFile="InftyCDB-1-6152/InftyCDB-1/InftyCDB-1/InftyCDB-1.csv";
		CsvParser parser=new CsvParser(Files.lines(new File(DATASETS,trainFile).toPath(),StandardCharsets.ISO_8859_1),false);
		class Record{
			int codePoint, width, height, left, top;
		}
		Map<String,List<Record>> records=new HashMap<>();
		while(parser.hasNext()){
			java.util.List<String> row=parser.next();
			Record record=new Record();
			String path=row.get(15);
			record.codePoint=name2code.getOrDefault(row.get(5),0);
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
		//ErrataList errata=ErrataList.generate(tester.getConfusionMatrix(),5,0.1);
		//errata.write(new File("/home/kwong/.mathocr/default/erratas.json"));
	}
}
