/* Convertor.java
 * =========================================================================
 * This file is originally part of the MathOCR Project
 *
 * Copyright (C) 2015 Chan Chung Kwong
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 */
package net.sf.mathocr.ocr;
import java.io.*;
import java.util.*;
/**
 * Utility on text processing
 */
public final class Convertor{
	static ArrayList<String> target=new ArrayList<String>(),replacement=new ArrayList<String>();
	static ArrayList<String> latex=new ArrayList<String>(),html=new ArrayList<String>();
	static HashSet<Character> special=new HashSet<Character>();
	static HashSet<String> fontname=new HashSet<String>();
	static HashMap<Character,String> p2t=new HashMap<Character,String>();
	static{
		try{
			BufferedReader in=new BufferedReader(new InputStreamReader(Convertor.class.getResourceAsStream("/net/sf/mathocr/resources/simplify.map")));
			String str;
			while((str=in.readLine())!=null){
				int ind=str.indexOf("\t");
				target.add(str.substring(0,ind));
				replacement.add(str.substring(ind+1));
			}
			in.close();
			in=new BufferedReader(new InputStreamReader(Convertor.class.getResourceAsStream("/net/sf/mathocr/resources/latex2html.map")));
			while((str=in.readLine())!=null){
				int ind=str.indexOf("\t");
				latex.add(str.substring(0,ind));
				html.add(str.substring(ind+1));
				special.add(str.charAt(0));
			}
			in.close();
		}catch(Exception ex){
			ex.printStackTrace();
		}
		p2t.put('\\',"\\textbackslash");
		p2t.put('&',"\\&");
		p2t.put('$',"\\$");
		p2t.put('%',"\\%");
		p2t.put('#',"\\#");
		p2t.put('_',"\\_");
		p2t.put('^',"\\^");
		p2t.put('{',"\\}");
		p2t.put('}',"\\}");
		fontname.add("\\mathrm");
		fontname.add("\\mathtt");
		fontname.add("\\mathit");
		fontname.add("\\mathfrak");
		fontname.add("\\mathcal");
		fontname.add("\\mathscr");
		fontname.add("\\mathbb");
		fontname.add("\\mathbf");
		fontname.add("\\boldsymbol");
		fontname.add("\\texttt");
		fontname.add("\\textrm");
		fontname.add("\\textbf");
		fontname.add("\\textit");
	}
	private static final boolean regionMatch(String buf,String word,int i){
		return buf.regionMatches(i,word,0,word.length());
	}
	private static final void flushTmp(StringBuilder tmp,int[] count,StringBuilder result){
		int max=0,ind=0;
		for(int i=0;i<4;i++){
			if(count[i]>max){
				max=count[i];
				ind=i;
			}
			count[i]=0;
		}
		if(ind==0)
			result.append(tmp.toString());
		else{
			if(ind==1)
				result.append("\\textbf{");
			else if(ind==2)
				result.append("\\textit{");
			else
				result.append("\\texttt{");
			result.append(tmp.toString());
			result.append('}');
		}
		tmp.setLength(0);
	}
	/**
	 * Group separated superscript and subscript of each box
	 * @param str input code
	 * @param str processed code
	 */
	private static final String fixScript(String str){
		Stack<Sentence> stack=new Stack<Sentence>();
		Sentence curr=new Sentence(0);
		int len=str.length(),lv=0;
		for(int i=0;i<len;i++){
			char c=str.charAt(i);
			switch(c){
				case '\\':
					curr.append(c);
					if(i+1<len&&!Character.isLetter(str.charAt(i+1)))
						curr.append(str.charAt(++i));
					break;
				case '^':
					++lv;
					curr.setStatus(1);
					stack.push(curr);
					curr=new Sentence(lv);
					++i;
					break;
				case '_':
					++lv;
					curr.setStatus(2);
					stack.push(curr);
					curr=new Sentence(lv);
					++i;
					break;
				case '{':
					++lv;
					curr.append(c);
					break;
				case '}':
					--lv;
					if(lv<curr.lv){
						stack.peek().append(curr.toString());
						curr=stack.pop();
						curr.setStatus(0);
					}else
						curr.append(c);
					break;
				default:
					curr.append(c);
			}
		}
		return curr.toString();
	}
	/**
	 * Ensure that every chatacter in the same text mode word have the same font
	 * @param str input code
	 * @param str processed code
	 */
	private static final String fixFont(String str){
		StringBuilder result=new StringBuilder(),tmp=new StringBuilder();
		int[] count=new int[4];//0 for textrm, 1 for textbf, 2 for textit, 3 for texttt
		int len=str.length(),lv=0;
		boolean mathmode=false;
		for(int i=0;i<len;i++){
			char c=str.charAt(i);
			if(Character.isLetter(c)){
				if(mathmode||lv>0)
					result.append(c);
				else{
					tmp.append(c);
					++count[0];
				}
			}else if(c=='\\'){
				int j=i+1;
				if(Character.isLetter(str.charAt(i+1)))
					while(j<len&&Character.isLetter(str.charAt(j)))
						++j;
				else
					++j;
				String token=str.substring(i,j);
				i=j-1;
				if(mathmode||lv>0)
					result.append(token);
				else if(token.equals("\\textbf")){
					++i;
					int l=1;
					while(true){
						char s=str.charAt(++i);
						if(s=='{'){
							++l;
							count[1]-=9;
						}else if(s=='}'){
							if(l==1)
								break;
							--l;
						}
						tmp.append(s);
						++count[1];
					}
				}else if(token.equals("\\textit")){
					++i;
					while(true){
						char s=str.charAt(++i);
						if(s=='}')
							break;
						tmp.append(s);
						++count[2];
					}
				}else if(token.equals("\\texttt")){
					++i;
					while(true){
						char s=str.charAt(++i);
						if(s=='}')
							break;
						tmp.append(s);
						++count[3];
					}
				}else{
					flushTmp(tmp,count,result);
					result.append(token);
				}
			}else if(c=='$'){
				flushTmp(tmp,count,result);
				mathmode=!mathmode;
				result.append(c);
				if(i+1<len&&str.charAt(i+1)==c){
					result.append(c);
					++i;
				}
			}else{
				if(!mathmode)
					flushTmp(tmp,count,result);
				result.append(c);
				if(c=='{')
					++lv;
				else if(c=='}')
					--lv;
			}
		}
		flushTmp(tmp,count,result);
		return result.toString();
	}
	/**
	 * Group content with same style nearby and more
	 * @param str input code
	 * @param str processed code
	 */
	private static final String fixGroup(String str){
		String prev=null;
		boolean changed=false;
		StringBuilder result=new StringBuilder();
		do{
			Stack<String> tokens=new Stack<String>();
			Stack<Boolean> up=new Stack<Boolean>();
			boolean currup=false;
			prev=null;
			changed=false;
			int len=str.length();
			result.setLength(0);
			loop:for(int i=0;i<len;i++){
				char c=str.charAt(i);
				if(c=='\\'){
					int j=i+1;
					if(Character.isLetter(str.charAt(j)))
						while(j<len&&Character.isLetter(str.charAt(j)))
							++j;
					else
						++j;
					String token=str.substring(i,j);
					i=j-1;
					if((token.equals(prev)&&fontname.contains(token))
					||(prev!=null&&(token.equals("\\mathbf")&&prev.equals("\\mathrm")||token.equals("\\mathrm")&&prev.equals("\\mathbf")))){
						result.deleteCharAt(result.length()-1);
						tokens.push(token);
						changed=true;
						up.push(currup);
						currup=false;
						++i;
					}else{
						result.append(token);
						if(j<len&&str.charAt(j)=='{'){
							result.append("{");
							tokens.push(token);
							up.push(currup);
							currup=false;
							++i;
						}
					}
					prev=null;
					continue;
				}else if(c=='{'){
					tokens.push("");
					up.push(currup);
					currup=false;
					prev=null;
				}else if(c=='}'){
					prev=tokens.pop();
					currup=up.pop();
				}else if(c=='^'||c=='_'){
					String token=String.valueOf(c);
					if(token.equals(prev)){
						result.deleteCharAt(result.length()-1);
						changed=true;
						++i;
					}else{
						result.append(token);
						result.append("{");
						++i;
					}
					tokens.push(token);
					up.push(currup);
					currup=(c=='^'&&str.charAt(i+1)=='\'');
					prev=null;
					continue;
				}else{
					prev=null;
					if(c=='\''){
						if(currup)
							result.append("\\prime ");
						else
							result.append("^{\\prime }");
						continue;
					}
				}
				result.append(c);
			}
			str=result.toString();
		}while(changed);
		return str;
	}
	/**
	 * Simplify functions' names and more
	 * @param str input code
	 * @param str processed code
	 */
	private static final String fixName(String str){
		int len=str.length();
		StringBuilder result=new StringBuilder();
		out:for(int i=0;i<len;i++){
			char c=str.charAt(i);
			if(c=='\\'){
				if(i+1<len&&!Character.isLetter(str.charAt(i+1))){
					result.append(c).append(str.charAt(++i));
					continue;
				}
				for(int j=0;j<target.size();j++)
					if(regionMatch(str,target.get(j),i)){
						result.append(replacement.get(j));
						i+=target.get(j).length()-1;
						continue out;
					}
			}else if(c=='_'){
				if(regionMatch(str,"{\\cdot }",i+1)){
					result.append('.');
					i+=8;
					continue;
				}else if(regionMatch(str,"{,}",i+1)){
					result.append(',');
					i+=3;
					continue;
				}
			}else if(c=='^'){
				if(regionMatch(str,"{,}",i+1)){
					result.append("^{\\prime }");
					i+=3;
					continue;
				}
			}else if(c==','){
				if(regionMatch(str,"\\cdots ",i+1)){
					result.append(",\\dots");
					i+=7;
					continue;
				}
			}
			result.append(c);
		}
		return result.toString();
	}
	/**
	 * Simplify LaTeX code, it only work on a subset of LaTeX code since it do not contain a complete TeX parser
	 * @param str LaTeX code
	 * @return simplified code
	 */
	public static String simplifyLaTeX(String str){
		str=fixGroup(str);
		str=fixFont(str);
		str=fixName(str);
		str=fixScript(str);
		return str;
	}
	/**
	 * Convert LaTeX code to HTML code, only a subset of LaTeX is support, illegal input may lead to exception
	 * @param str LaTeX code
	 * @return HTML code
	 */
	public static String LaTeXtoHTML(String str){
		int len=str.length();
		StringBuilder result=new StringBuilder();
		Stack<String> tokens=new Stack<String>();
		boolean mathmode=false,eatspace=false;
		loop:for(int i=0;i<len;i++){
			char c=str.charAt(i);
			if(c=='\\'){
				int j=i+1;
				if(Character.isLetter(str.charAt(j)))
					while(j<len&&Character.isLetter(str.charAt(j)))
						++j;
				else
					++j;
				String token=str.substring(i,j);
				i=j-1;
				eatspace=false;
				if(mathmode){
					if(token.equals("\\end")&&regionMatch(str,"{equation}",j)){
						mathmode=false;
						result.append("$$");
						i+=10;
					}else
						result.append(token);
				}else if(token.equals("\\textbf")){
					++i;
					result.append("<b>");
					tokens.push("</b>");
				}else if(token.equals("\\textit")){
					++i;
					result.append("<i>");
					tokens.push("</i>");
				}else if(token.equals("\\texttt")){
					++i;
					result.append("<code>");
					tokens.push("</code>");
				}else if(latex.contains(token)){
					result.append(html.get(latex.indexOf(token)));
					eatspace=true;
				}else if(token.equals("\\begin")&&regionMatch(str,"{equation}",j)){
					mathmode=true;
					result.append("$$");
					i+=10;
				}else{
					result.append(token);
				}
				continue;
			}else if(c=='$'){
				mathmode=!mathmode;
				if(i+1<len&&str.charAt(i+1)==c){
					result.append(c);
					++i;
				}
			}else if(!mathmode&&c=='}'){
				result.append(tokens.pop());
				eatspace=false;
				continue;
			}else if(!mathmode&&special.contains(c)){
				for(int k=0;k<latex.size();k++)
					if(regionMatch(str,latex.get(k),i)){
						result.append(html.get(k));
						i+=latex.get(k).length()-1;
						eatspace=false;
						continue loop;
					}
			}else if(eatspace&&Character.isSpaceChar(c)){
				continue;
			}
			eatspace=false;
			result.append(c);
		}
		return result.toString();
	}
	/**
	 * Convert plain text to LaTeX code
	 * @param str plain text
	 * @return LaTeX code
	 */
	public static String plainToLaTeX(String str){
		StringBuilder latex=new StringBuilder();
		int len=str.length();
		for(int i=0;i<len;i++){
			char c=str.charAt(i);
			String replace=p2t.get(c);
			if(replace==null)
				latex.append(c);
			else
				latex.append(replace);
		}
		return latex.toString();
	}
	public static String removeDisplayedFormula(String str){
		if(str==null)
			return null;
		int len=str.length();
		StringBuilder result=new StringBuilder();
		for(int i=0;i<len;i++){
			char c=str.charAt(i);
			if(c=='\\'){
				if(i+1<len&&!Character.isLetter(str.charAt(i+1))){
					result.append('\\').append(str.charAt(++i));
				}else if(regionMatch(str,"\\begin{equation}",i)){
					result.append('$');
					i+=15;
				}else if(regionMatch(str,"\\end{equation}",i)){
					result.append('$');
					i+=13;
				}else
					result.append('\\');
			}else if(c=='$'){
				result.append('$');
				if(i+1<len&&str.charAt(i+1)=='$')
					++i;
			}else
				result.append(c);
		}
		return result.toString();
	}
	/*public static void main(String[] args){
		//System.out.println(simplifyLaTeX("$x^{2}^{3}$"));
		System.out.println(fixScript("$_{3dwe}x^{26^{2}}_{4}^{3}$"));
		System.out.println(fixScript("$_{3dwe}x^{2^{4}_{9}^{8}}_{4}^{3}$"));
		System.out.println(fixScript("$x^{\\prime }_{3}^{\\prime }+y^{\\prime }$"));
		System.out.println(simplifyLaTeX("\\mathrm{lim}\\mathbf{sup}"));
	}*/
}
class Sentence{
	StringBuilder tmp=new StringBuilder();
	String normal="",sup=null,sub=null;
	int status=0,lv;
	Sentence(int lv){
		this.lv=lv;
	}
	void setStatus(int newstatus){
		if(newstatus==0){
			if(status==1){
				sup=tmp.toString();
			}else if(status==2){
				sub=tmp.toString();
			}
			tmp.setLength(0);
			tmp.append(normal);
		}else if(newstatus==1){
			normal=tmp.toString();
			tmp.setLength(0);
			if(sup==null)
				tmp.append("^{");
			else
				tmp.append(sup);
		}else if(newstatus==2){
			normal=tmp.toString();
			tmp.setLength(0);
			if(sub==null)
				tmp.append("_{");
			else
				tmp.append(sub);
		}
		status=newstatus;
	}
	void flush(){
		if(sub!=null){
			tmp.append(sub).append('}');
			sub=null;
		}
		if(sup!=null){
			if((sup.length()-2)%7==0){
				int i=2;
				String diff="";
				for(;i<sup.length();i+=7)
					if(!sup.substring(i,i+7).equals("\\prime "))
						break;
					else
						diff+="\'";
				if(i==sup.length())
					tmp.append(diff);
				else
					tmp.append(sup).append('}');
			}else
				tmp.append(sup).append('}');
			sup=null;
		}
	}
	void append(char c){
		if(status==0)
			flush();
		tmp.append(c);
	}
	void append(String s){
		if(status==0)
			flush();
		tmp.append(s);
	}
	public String toString(){
		flush();
		return tmp.toString();
	}
}