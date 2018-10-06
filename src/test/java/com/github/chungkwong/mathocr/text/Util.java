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
import java.awt.*;
import java.awt.image.*;
import java.util.*;
import java.util.List;
import java.util.stream.*;
import org.scilab.forge.jlatexmath.*;
/**
 *
 * @author Chan Chung Kwong
 */
public class Util{
	public static int getLevenshteinDistance(String seq1,String seq2){
		return getLevenshteinDistance(seq1.codePoints().toArray(),seq2.codePoints().toArray());
	}
	public static int getLevenshteinDistance(int[] seq1,int[] seq2){
		int l1=seq1.length, l2=seq2.length;
		int[] lastRow=IntStream.range(0,l2+1).toArray();
		int[] currRow=new int[l2+1];
		for(int i=0;i<l1;i++){
			currRow[0]=i+1;
			for(int j=0;j<l2;j++){
				if(seq1[i]==seq2[j]){
					currRow[j+1]=lastRow[j];
				}else{
					currRow[j+1]=Math.min(lastRow[j],Math.min(lastRow[j+1],currRow[j]))+1;
				}
			}
			int[] tmp=currRow;
			currRow=lastRow;
			lastRow=tmp;
		}
		return lastRow[l2];
	}
	public static <T> int getLevenshteinDistance(T[] seq1,T[] seq2){
		int l1=seq1.length, l2=seq2.length;
		int[] lastRow=IntStream.range(0,l2+1).toArray();
		int[] currRow=new int[l2+1];
		for(int i=0;i<l1;i++){
			currRow[0]=i+1;
			for(int j=0;j<l2;j++){
				if(Objects.equals(seq1[i],seq2[j])){
					currRow[j+1]=lastRow[j];
				}else{
					currRow[j+1]=Math.min(lastRow[j],Math.min(lastRow[j+1],currRow[j]))+1;
				}
			}
			int[] tmp=currRow;
			currRow=lastRow;
			lastRow=tmp;
		}
		return lastRow[l2];
	}
	public static List<String> tokenize(String formula){
		ArrayList<String> list=new ArrayList<>();
		for(int i=0;i<formula.length();i++){
			char c=formula.charAt(i);
			if(c=='\\'){
				if(i+1<formula.length()){
					c=formula.charAt(i+1);
					if((c>='a'&&c<='z')||(c>='a'&&c<='z')){
						int j=i+2;
						while(j<formula.length()){
							c=formula.charAt(j);
							if((c>='a'&&c<='z')||(c>='a'&&c<='z')){
								++j;
							}else{
								break;
							}
						}
						if(formula.substring(i,j).equals("\\begin")||formula.substring(i,j).equals("\\end")){
							int k=formula.indexOf('}',j);
							j=(k>=0?k+1:formula.length());
						}
						list.add(formula.substring(i,j));
						i=j-1;
					}else{
						list.add(formula.substring(i,i+2));
						++i;
					}
				}
			}else if(c=='%'){
				int j=formula.indexOf('\n',i);
				if(j>=0){
					i=j;
				}else{
					j=formula.indexOf('\r',i);
					if(j>=0){
						i=j;
					}else{
						break;
					}
				}
			}else if(!Character.isWhitespace(c)){
				list.add(formula.substring(i,i+1));
			}
		}
		return list;
	}
	private static final HashSet<String> DISCARD=new HashSet<>();
	private static final HashMap<String,String> REPLACE=new HashMap<>();
	static{
		DISCARD.add("\\left");
		DISCARD.add("\\right");
		DISCARD.add("\\operatorname");
		DISCARD.add("\\mathrm");
		DISCARD.add("\\mathcal");
		DISCARD.add("\\bf");
		DISCARD.add("\\cal");
		DISCARD.add("\\displaystyle");
		DISCARD.add("~");
		DISCARD.add("\\;");
		DISCARD.add("\\quad");
		DISCARD.add("\\qquad");
		DISCARD.add("\\,");
		DISCARD.add("\\:");
		REPLACE.put("\\dots","\\ldots");
		REPLACE.put("\\lbrace","\\{");
		REPLACE.put("\\rbrace","\\}");
		REPLACE.put("\\lbrack","[");
		REPLACE.put("\\rbrack","]");
		REPLACE.put("\\to","\\rightarrow");
		REPLACE.put("\\mid","\\vert");
		REPLACE.put("|","\\vert");
	}
	public static String[] normalize(List<String> formula){
		for(Iterator<String> iterator=formula.iterator();iterator.hasNext();){
			String next=iterator.next();
		}
		for(int i=0;i<formula.size();i++){
			String curr=formula.get(i);
			if(DISCARD.contains(curr)){
				formula.remove(i);
				--i;
			}else if(REPLACE.containsKey(curr)){
				formula.set(i,REPLACE.get(curr));
			}else if(curr.equals("\\label")){
				int end=findGroupEnd(i+1,formula);
				formula.subList(i,end).clear();
				--i;
			}else if(curr.equals("\\over")){
				int start=findGroupStart(i,formula);
				int end=findGroupEnd(i+1,formula);
				formula.remove(i);
				formula.add(start,"\\frac");
			}else if(curr.equals("{")){
				int end=findGroupEnd(i,formula);
				if(end==i+3){
					formula.remove(i+2);
					formula.remove(i);
					--i;
				}
			}else if(curr.equals("_")||curr.equals("^")){
				List<String> sup=new ArrayList<>();
				List<String> sub=new ArrayList<>();
				int j=i;
				int processed=0;
				while(j<formula.size()){
					int k;
					if(formula.get(j).equals("_")){
						k=findGroupEnd(j+1,formula);
						sub.addAll(formula.subList(j+1,k));
					}else if(formula.get(j).equals("^")){
						k=findGroupEnd(j+1,formula);
						sup.addAll(formula.subList(j+1,k));
					}else{
						break;
					}
					j=k;
					++processed;
				}
				if(processed>=2||(!sup.isEmpty()&&sup.get(0).equals("\\prime"))){
					formula.subList(i,j).clear();
					if(!sub.isEmpty()){
						sub.add(0,"_");
						sub.add(1,"{");
						sub.add("}");
					}
					if(!sup.isEmpty()){
						int p=0;
						while(p<sup.size()&&sup.get(p).equals("\\prime")){
							sup.set(p,"'");
							++p;
						}
						if(p<sup.size()){
							sup.add(p,"^");
							sup.add(p+1,"{");
							sup.add("}");
						}
					}
					sub.addAll(sup);
					formula.addAll(i,sub);
				}
			}
		}
		return formula.toArray(new String[0]);
	}
	private static int findGroupEnd(int start,List<String> formula){
		if(start<formula.size()){
			if(formula.get(start).equals("{")){
				int lv=1;
				while(++start<formula.size()){
					if(formula.get(start).equals("{")){
						++lv;
					}else if(formula.get(start).equals("}")){
						if(--lv==0){
							return start+1;
						}
					}
				}
				return start;
			}else{
				return start+1;
			}
		}else{
			return formula.size();
		}
	}
	private static int findGroupStart(int end,List<String> formula){
		if(end>0){
			if(formula.get(--end).equals("}")){
				int lv=1;
				while(--end>=0){
					if(formula.get(end).equals("}")){
						++lv;
					}else if(formula.get(end).equals("{")){
						if(--lv==0){
							return end;
						}
					}
				}
				return 0;
			}else{
				return end;
			}
		}else{
			return 0;
		}
	}
	public static BufferedImage render(String formula){
		return (BufferedImage)new TeXFormula(formula).createBufferedImage(TeXFormula.SERIF,40.0f,Color.BLACK,Color.WHITE);
	}
}
