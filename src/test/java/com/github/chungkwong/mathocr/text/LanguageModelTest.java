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
import com.github.chungkwong.mathocr.common.*;
import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;
/**
 *
 * @author Chan Chung Kwong
 */
public class LanguageModelTest{
	private static final String TRAIN_FILE="../datasets/InftyCDB-1-6152/InftyCDB-1/InftyCDB-1/InftyCDB-1.csv";
	public static void main(String[] args) throws IOException{
		ArrayList<Record> records=loadRecords();
		Frequencies<Integer> frequencies=new Frequencies<>();
		Frequencies<Integer> firstFrequencies=new Frequencies<>();
		for(Record record:records){
			if(record==null){
				continue;
			}
			if(!record.region){
				frequencies.advanceFrequency(record.codepoint);
				if(record.parent<0){
					firstFrequencies.advanceFrequency(record.codepoint);
				}
			}
		}
		frequencies.toMap().forEach((k,v)->{
			System.out.println(new String(new int[]{k},0,1)+":"+firstFrequencies.getFrequency(k)*1.0/v.getCount());
		});
	}
	private static ArrayList<Record> loadRecords() throws IOException{
		Map<String,Integer> name2code=new HashMap<>();
		new BufferedReader(new InputStreamReader(LinkageTest.class.getResourceAsStream("inftyCDB1.map"),StandardCharsets.UTF_8)).
				lines().forEach((line)->{
					int i=line.indexOf('\t');
					if(i!=-1){
						name2code.put(line.substring(0,i),Integer.parseInt(line.substring(i+1),16));
					}
				});
		CsvParser parser=new CsvParser(Files.lines(new File(TRAIN_FILE).toPath(),StandardCharsets.ISO_8859_1),false);
		int recordCount=688569+1;
		ArrayList<Record> records=new ArrayList<>(recordCount);
		for(int i=0;i<recordCount;i++){
			records.add(null);
		}
		while(parser.hasNext()){
			List<String> row=parser.next();
			int index=Integer.parseInt(row.get(0));
			Record record=new Record();
			record.type=row.get(3);
			record.codepoint=name2code.getOrDefault(row.get(5),0);
			record.region=row.get(6).equals("text");
			record.baseline=row.get(7).equals("1");
			record.italic=row.get(8).equals("1");
			record.bold=row.get(9).equals("1");
			record.width=Integer.parseInt(row.get(11));
			record.height=Integer.parseInt(row.get(12));
			record.parent=Integer.parseInt(row.get(13));
			record.link=row.get(14).intern();
			record.left=Integer.parseInt(row.get(16));
			record.top=Integer.parseInt(row.get(17));
			record.right=Integer.parseInt(row.get(18));
			record.bottom=Integer.parseInt(row.get(19));
			records.set(index,record);
		}
		return records;
	}
	private static class Record{
		String type, link;
		boolean region, baseline, italic, bold;
		int codepoint, width, height, parent, left, top, right, bottom;
	}
}
