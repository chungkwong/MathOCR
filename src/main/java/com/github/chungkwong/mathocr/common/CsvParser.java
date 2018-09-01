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
package com.github.chungkwong.mathocr.common;
import java.util.*;
import java.util.stream.*;
/**
 *
 * @author Chan Chung Kwong
 */
public class CsvParser implements Iterator<List<String>>{
	private final Iterator<String> lines;
	private final char separator;
	private final char quotationMark;
	/**
	 * Create a parser, the first line is skiped
	 *
	 * @param lines the input lines
	 */
	public CsvParser(Stream<String> lines){
		this(lines,true);
	}
	/**
	 * Create a parser, `,` will be the separator `"` will be the quotation mark
	 *
	 * @param lines the input lines
	 * @param skipHeader if the first line should be skiped
	 */
	public CsvParser(Stream<String> lines,boolean skipHeader){
		this(lines,skipHeader,',','"');
	}
	/**
	 * Create a parser
	 *
	 * @param lines the input lines
	 * @param skipHeader if the first line should be skiped
	 * @param separator the separator between fields
	 * @param quotationMark the quotationMark
	 */
	public CsvParser(Stream<String> lines,boolean skipHeader,char separator,char quotationMark){
		this.lines=lines.iterator();
		this.quotationMark=quotationMark;
		this.separator=separator;
		this.lines.next();
	}
	@Override
	public boolean hasNext(){
		return lines.hasNext();
	}
	@Override
	public List<String> next(){
		List<String> row=new ArrayList<>();
		String line=lines.next();
		int last=0;
		for(int i=0;i<line.length();i++){
			char c=line.charAt(i);
			if(c==separator){
				row.add(line.substring(last,i));
				last=i+1;
			}else if(c==quotationMark){
				StringBuilder field=new StringBuilder();
				quote:
				while(true){
					for(i=i+1;i<line.length();i++){
						c=line.charAt(i);
						if(c==quotationMark){
							if(i+1==line.length()){
								row.add(field.toString());
								return row;
							}else if(line.charAt(i+1)==quotationMark){
								field.append(quotationMark);
								++i;
							}else{
								row.add(field.toString());
								last=i+2;
								++i;
								break quote;
							}
						}else{
							field.append(c);
						}
					}
					line=lines.next();
					field.append("\n");
				}
			}
		}
		row.add(line.substring(last));
		return row;
	}
	/**
	 * Parse a CSV file
	 *
	 * @param lines the input lines
	 * @return the records
	 */
	public static Stream<List<String>> parse(Stream<String> lines){
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(new CsvParser(lines),0),true);
	}
	/**
	 * Parse a CSV file
	 *
	 * @param lines the input lines
	 * @param skipHeader if the first line should be skiped
	 * @return the records
	 */
	public static Stream<List<String>> parse(Stream<String> lines,boolean skipHeader){
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(new CsvParser(lines,skipHeader),0),true);
	}
	/**
	 * Parse a CSV file
	 *
	 * @param lines the input lines
	 * @param skipHeader if the first line should be skiped
	 * @param separator the separator between fields
	 * @param quotationMark the quotationMark
	 * @return the records
	 */
	public static Stream<List<String>> parse(Stream<String> lines,boolean skipHeader,char separator,char quotationMark){
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(new CsvParser(lines,skipHeader,separator,quotationMark),0),true);
	}
}
