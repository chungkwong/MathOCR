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
package com.github.chungkwong.mathocr.layout.logical;
import com.github.chungkwong.mathocr.text.structure.Fraction;
import com.github.chungkwong.mathocr.text.structure.Matrix;
import com.github.chungkwong.mathocr.text.structure.Superscript;
import com.github.chungkwong.mathocr.text.structure.Line;
import com.github.chungkwong.mathocr.text.structure.Symbol;
import com.github.chungkwong.mathocr.text.structure.Over;
import com.github.chungkwong.mathocr.text.structure.Span;
import com.github.chungkwong.mathocr.text.structure.Radical;
import com.github.chungkwong.mathocr.text.structure.Subscript;
import java.io.*;
import java.util.*;
import java.util.logging.*;
import java.util.stream.*;
/**
 * LaTeX encoder
 *
 * @author Chan Chung Kwong
 */
public class LatexEncoder implements DocumentEncoder{
	@Override
	public String encode(Document document){
		StringBuilder str=new StringBuilder();
		TreeSet<String> pkg=new TreeSet<>();
		pkg.add("amsmath");
		pkg.add("amssymb");
		pkg.add("mathrsfs");
		List<LogicalBlock> blocks=document.getBlocks();
		String cls="\\documentclass{"+document.getTypeName()+"}\n";
		str.append(cls);
		if(document.getTitle()!=null){
			str.append("\\title{");
			encode(document.getTitle(),str);
			str.append("}\n");
			if(document.getAuthor()!=null){
				str.append("\\author{");
				encode(document.getAuthor(),str);
				str.append("}\n");
			}
			str.append("\\begin{document}\n\\maketitle\n");
		}else{
			str.append("\\begin{document}\n");
		}
		Stack<Listing> lstlv=new Stack<>();
		Stack<Heading> headlv=new Stack<>();
		int hlv=document.getType()==Document.TYPE_ARTICLE?1:0;
		for(LogicalBlock block:blocks){
			if(block instanceof Paragraph||block instanceof Caption){
				//if(((Paragraph)block).isNoStart())
				//	str.append(((Paragraph)block).getContent()+"\n");
				//else{
				while(!lstlv.isEmpty()){
					str.append(lstlv.pop().isNumbered()?"\\end{enumerate}\n":"\\end{itemize}\n");
				}
				str.append("\\paragraph{}");
				encode(((TextBlock)block).getLines(),str);
				str.append("\n");
				//}
			}else if(block instanceof Listing){
				int cmp=0;
				while((cmp=lstlv.isEmpty()?1:((Listing)block).compareLevel(lstlv.peek()))<0){
					str.append(lstlv.pop().isNumbered()?"\\end{enumerate}\n":"\\end{itemize}\n");
				}
				if(cmp>0){
					lstlv.push((Listing)block);
					str.append(((Listing)block).isNumbered()?"\\begin{enumerate}\n":"\\begin{itemize}\n");
				}
				str.append("\\item ");
				encode(((Listing)block).getContentNoPrefix(),str);
				str.append("\n");
			}else if(block instanceof Heading){
				while(!lstlv.isEmpty()){
					str.append(lstlv.pop().isNumbered()?"\\end{enumerate}\n":"\\end{itemize}\n");
				}
				int cmp=0;
				while((cmp=headlv.isEmpty()?1:((Heading)block).compareLevel(headlv.peek()))<=0){
					headlv.pop();
					--hlv;
				}
				headlv.push((Heading)block);
				++hlv;
				str.append("\\").append(((Heading)block).getLevelName(hlv)).append("{");
				encode(((Heading)block).getContentNoPrefix(),str);
				str.append("}\n");
			}else if(block instanceof Table){
				boolean hascaption=((Table)block).getCaption()!=null;
				if(hascaption){
					str.append("\\begin{table}\n");
					str.append("\\centering\n");
					str.append("\\caption{");
					encode(((Table)block).getCaption(),str);
					str.append("}\n");
				}
				str.append("\\includegraphics{").append(((Table)block).getPath()).append("}\n");
				if(hascaption){
					str.append("\\end{table}\n");
				}
				pkg.add("graphicx");
			}else if(block instanceof Image){
				boolean hascaption=((Image)block).getCaption()!=null;
				if(hascaption){
					str.append("\\begin{figure}\n");
					str.append("\\centering\n");
				}
				str.append("\\includegraphics[scale=0.25]{").append(((Image)block).getPath()).append("}\n");
				if(hascaption){
					str.append("\\caption{");
					encode(((Image)block).getCaption(),str);
					str.append("}\n\\end{figure}\n");
				}
				pkg.add("graphicx");
			}else if(block instanceof TextBlock){
				encode(((TextBlock)block).getLines(),str);
			}
		}
		while(!lstlv.isEmpty()){
			str.append(lstlv.pop().isNumbered()?"\\end{enumerate}\n":"\\end{itemize}\n");
		}
		str.append("\\end{document}\n");
		for(String p:pkg){
			str.insert(cls.length(),"\\usepackage{"+p+"}\n");
		}
		return str.toString();
	}
	private static void encode(List<Line> lines,StringBuilder buf){
		for(Line line:lines){
			encode(line,false,buf);
			buf.append('\n');
		}
	}
	private static void encode(Span span,boolean math,StringBuilder buf){
		if(span instanceof Symbol){
			int codePoint=((Symbol)span).getCodePoint();
			String command=getLatexName(codePoint,math);
			if(command!=null){
				buf.append(command);
			}else{
				buf.append(escape(new String(new int[]{codePoint},0,1),math));
			}
		}else if(span instanceof Line){
			boolean lastMath=math;
			for(Span comp:((Line)span).getSpans()){
				boolean thisMath=math||isMathMode(comp);
				if((!lastMath)&&thisMath){
					buf.append('$');
				}else if(!math&&lastMath&&!thisMath){
					buf.append('$');
				}
				encode(comp,thisMath,buf);
				lastMath=thisMath;
			}
			if(lastMath&&!math){
				buf.append('$');
			}
		}else if(span instanceof Subscript){
			buf.append("_{");
			encode(((Subscript)span).getContent(),true,buf);
			buf.append('}');
		}else if(span instanceof Superscript){
			buf.append("^{");
			encode(((Superscript)span).getContent(),true,buf);
			buf.append('}');
		}else if(span instanceof Over){
		}else if(span instanceof Fraction){
			buf.append("\\frac{");
			encode(((Fraction)span).getNumerator(),true,buf);
			buf.append("}{");
			encode(((Fraction)span).getDenominator(),true,buf);
			buf.append('}');
		}else if(span instanceof Radical){
			buf.append("\\sqrt");
			if(((Radical)span).getPower()!=null){
				encode(((Radical)span).getPower(),true,buf);
			}
			buf.append('{');
			encode(((Radical)span).getRadicand(),true,buf);
			buf.append('}');
		}else if(span instanceof Matrix){
			buf.append("\\begin{array}{");
			int columnCount=((Matrix)span).getColumnCount();
			for(int i=0;i<columnCount;i++){
				buf.append('c');//FIXME check alignment
			}
			buf.append("}\n");
			for(List<Span> row:((Matrix)span).getMatrix()){
				int j=columnCount;
				for(Span cell:row){
					encode(cell,true,buf);
					if(--columnCount>0){
						buf.append(" & ");
					}
				}
				buf.append("\\\\\n");
			}
			buf.append("\\end{array}\n");
		}else{
			buf.append(escape(span.toString(),math));
		}
	}
	private static boolean isMathMode(Span span){
		if(span instanceof Symbol){
			int code=((Symbol)span).getCodePoint();
			return MATH_COMMAND.containsKey(code)&&!TEXT_COMMAND.containsKey(code);
		}else if(span instanceof Line){
			return false;
		}else{
			return true;
		}
	}
	private static final Map<Integer,String> TEXT_COMMAND;
	private static final Map<Integer,String> MATH_COMMAND;
	private static String getLatexName(int codePoint,boolean math){
		return math?MATH_COMMAND.get(codePoint):TEXT_COMMAND.get(codePoint);
	}
	private static String escape(String str,boolean math){
		StringBuilder latex=new StringBuilder();
		int len=str.length();
		for(int i=0;i<len;i=str.offsetByCodePoints(i,1)){
			int c=str.codePointAt(i);
			if(math&&MATH_COMMAND.containsKey(c)){
				latex.append(MATH_COMMAND.get(c));
			}else if(!math&&TEXT_COMMAND.containsKey(c)){
				latex.append(TEXT_COMMAND.get(c));
			}else{
				latex.appendCodePoint(c);
			}
		}
		return latex.toString();
	}
	static{
		Properties text=new Properties();
		try{
			text.load(LatexEncoder.class.getResourceAsStream("latex_symbol_text.properties"));
		}catch(IOException ex){
			Logger.getLogger(LatexEncoder.class.getName()).log(Level.SEVERE,null,ex);
		}
		TEXT_COMMAND=text.entrySet().stream().collect(Collectors.toMap((e)->Integer.parseInt(e.getKey().toString(),16),(e)->e.getValue().toString()));
		Properties math=new Properties();
		try{
			math.load(LatexEncoder.class.getResourceAsStream("latex_symbol_math.properties"));
		}catch(IOException ex){
			Logger.getLogger(LatexEncoder.class.getName()).log(Level.SEVERE,null,ex);
		}
		MATH_COMMAND=math.entrySet().stream().collect(Collectors.toMap((e)->Integer.parseInt(e.getKey().toString(),16),(e)->e.getValue().toString()));
	}
}
