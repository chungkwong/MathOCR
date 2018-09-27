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
import java.util.*;
/**
 *
 * @author Chan Chung Kwong
 */
public class LanguageModel{
	public static final int NONE=-1;
	public static final int ROOT=0x200000;
	private int lastId=0x200000;
	private final HashMap<Integer,Integer> parents=new HashMap<>();
	private final HashMap<String,Integer> name2code=new HashMap<>();
	private final HashMap<Integer,String> code2name=new HashMap<>();
	private final Frequencies<Integer> symbolFreq=new Frequencies<>();
	private final Frequencies<Pair<Integer,Integer>> biGramFreq=new Frequencies<>();
	public int createCategory(String name,int parent){
		++lastId;
		parents.put(lastId,parent);
		name2code.put(name,lastId);
		code2name.put(lastId,name);
		return lastId;
	}
	public String getCategoryName(int code){
		return code2name.get(code);
	}
	public int getCategoryCode(String name){
		return name2code.get(name);
	}
	public boolean isContaining(String name,int codePoint){
		if(name2code.containsKey(name)){
			return isContaining(getCategoryCode(name),codePoint);
		}else{
			return false;
		}
	}
	public boolean isContaining(int code,int codePoint){
		while(true){
			if(codePoint==code){
				return true;
			}
			if(parents.containsKey(codePoint)){
				codePoint=parents.get(codePoint);
			}else{
				return code==ROOT;
			}
		}
	}
	private int getParentCategory(int code){
		return parents.getOrDefault(code,ROOT);
	}
	public Frequencies<Integer> getSymbolFreqency(){
		return symbolFreq;
	}
	public Frequencies<Pair<Integer,Integer>> getBiGramFreqency(){
		return biGramFreq;
	}
}
