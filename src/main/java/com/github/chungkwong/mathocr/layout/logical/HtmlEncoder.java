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
import com.github.chungkwong.mathocr.text.structure.Under;
import com.github.chungkwong.mathocr.text.structure.Fraction;
import com.github.chungkwong.mathocr.text.structure.Matrix;
import com.github.chungkwong.mathocr.text.structure.Superscript;
import com.github.chungkwong.mathocr.text.structure.Line;
import com.github.chungkwong.mathocr.text.structure.UnderOver;
import com.github.chungkwong.mathocr.text.structure.Symbol;
import com.github.chungkwong.mathocr.text.structure.Over;
import com.github.chungkwong.mathocr.text.structure.Span;
import com.github.chungkwong.mathocr.text.structure.Radical;
import com.github.chungkwong.mathocr.text.structure.Subscript;
import java.util.*;
/**
 *
 * @author Chan Chung Kwong
 */
public class HtmlEncoder implements DocumentEncoder{
	@Override
	public String encode(Document doc){
		StringBuilder str=new StringBuilder();
		List<LogicalBlock> blocks=doc.getBlocks();
		str.append("<!DOCTYPE html>\n<html>\n");
		str.append("<head>\n");
		if(doc.getTitle()!=null){
			str.append("<title>").append(doc.getTitle()).append("</title>\n");
		}
		str.append("</head>\n");
		str.append("<body>\n");
		if(doc.getTitle()!=null){
			str.append("<h1>");
			encode(doc.getTitle(),str);
			str.append("</h1>\n");
		}
		if(doc.getAuthor()!=null){
			str.append("<address>");
			encode(doc.getAuthor(),str);
			str.append("</address>\n");
		}
		Stack<Listing> lstlv=new Stack<>();
		Stack<Heading> headlv=new Stack<>();
		int hlv=1;
		for(LogicalBlock block:blocks){
			if(block instanceof Paragraph||block instanceof Caption){
				//if(((Paragraph)block).isNoStart())
				//	str.append(((Paragraph)block).getContent());
				//else{
				while(!lstlv.isEmpty()){
					str.append(lstlv.pop().isNumbered()?"</ol>\n":"</ul>\n");
				}
				str.append("<p>");
				encode(((TextBlock)block).getLines(),str);
				str.append("</p>\n");
				//}
			}else if(block instanceof Listing){
				int cmp=0;
				while((cmp=lstlv.isEmpty()?1:((Listing)block).compareLevel(lstlv.peek()))<0){
					str.append(lstlv.pop().isNumbered()?"</ol>\n":"</ul>\n");
				}
				if(cmp>0){
					lstlv.push((Listing)block);
					str.append(((Listing)block).isNumbered()?"<ol>\n":"<ul>\n");
				}
				str.append("<li>");
				encode(((Listing)block).getContentNoPrefix(),str);
				str.append("</li>\n");
			}else if(block instanceof Heading){
				while(!lstlv.isEmpty()){
					str.append(lstlv.pop().isNumbered()?"</ol>\n":"</ul>\n");
				}
				int cmp=0;
				while((cmp=headlv.isEmpty()?1:((Heading)block).compareLevel(headlv.peek()))<=0){
					headlv.pop();
					--hlv;
				}
				headlv.push((Heading)block);
				++hlv;
				str.append("<h").append(hlv).append(">");
				encode(((Heading)block).getLines(),str);
				str.append("</h").append(hlv).append(">\n");
			}else if(block instanceof Table){
				str.append("<table>\n");
				if(((Table)block).getCaption()!=null){
					str.append("<caption>");
					encode(((Table)block).getCaption(),str);
					str.append("</caption>\n");
				}
				str.append("<img src=\"file://").append(((Table)block).getPath()).append("\" >\n");
				str.append("</table>\n");
			}else if(block instanceof Image){
				str.append("<figure>\n");
				str.append("<img src=\"file://").append(((Image)block).getPath()).append("\" >\n");
				if(((Image)block).getCaption()!=null){
					str.append("<figcaption>");
					encode(((Image)block).getCaption(),str);
					str.append("</figcaption>\n");
				}
				str.append("</figure>\n");
			}else if(block instanceof HorizontalRule){
				str.append("<hr>");
			}else if(block instanceof TextBlock){
				encode(((TextBlock)block).getLines(),str);
			}
		}
		while(!lstlv.isEmpty()){
			str.append(lstlv.pop().isNumbered()?"</ol>\n":"</ul>\n");
		}
		str.append("</body>\n");
		str.append("</html>\n");
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
			if(math){
				buf.append("<mi>");
				escape(((Symbol)span).getCodePoint(),buf);
				buf.append("</mi>");
			}else{
				escape(((Symbol)span).getCodePoint(),buf);
			}
		}else if(span instanceof Line){
			if(math){
				buf.append("<mrow>");
				for(Span comp:((Line)span).getSpans()){
					encode(comp,math,buf);
				}
				buf.append("</mrow>");
			}else{
				boolean lastMath=math;
				for(Span comp:((Line)span).getSpans()){
					boolean thisMath=isMathMode(comp);
					if((!lastMath)&&thisMath){
						buf.append("<math xmlns=\"http://www.w3.org/1998/Math/MathML\">");
					}else if(lastMath&&!thisMath){
						buf.append("</math>");
					}
					encode(comp,thisMath,buf);
					lastMath=thisMath;
				}
				if(lastMath){
					buf.append("</math>");
				}
			}
		}else if(span instanceof Subscript){
			if(math){
				buf.append("<msub><mspace />");
				encode(((Subscript)span).getContent(),math,buf);
				buf.append("</msub>");
			}else{
				buf.append("<sub>");
				encode(((Subscript)span).getContent(),math,buf);
				buf.append("</sub>");
			}
		}else if(span instanceof Superscript){
			if(math){
				buf.append("<msup><mspace />");
				encode(((Superscript)span).getContent(),math,buf);
				buf.append("</msup>");
			}else{
				buf.append("<sup>");
				encode(((Superscript)span).getContent(),math,buf);
				buf.append("</sup>");
			}
		}else if(span instanceof Fraction){
			buf.append("<mfrac>");
			encode(((Fraction)span).getNumerator(),true,buf);
			encode(((Fraction)span).getDenominator(),true,buf);
			buf.append("</mfrac>");
		}else if(span instanceof Radical){
			if(((Radical)span).getPower()!=null){
				buf.append("<mroot>");
				encode(((Radical)span).getRadicand(),true,buf);
				encode(((Radical)span).getPower(),true,buf);
				buf.append("</mroot>");
			}else{
				buf.append("<msqrt>");
				encode(((Radical)span).getRadicand(),true,buf);
				buf.append("</msqrt>");
			}
		}else if(span instanceof Matrix){
			buf.append("<mtable>");
			for(List<Span> row:((Matrix)span).getMatrix()){
				buf.append("<mtr>");
				for(Span cell:row){
					buf.append("<mtd>");
					encode(cell,true,buf);
					buf.append("</mtd>");
				}
				buf.append("</mtr>");
			}
			buf.append("</mtable>");
		}else if(span instanceof Over){
			buf.append("<mover>");
			encode(((Over)span).getContent(),true,buf);
			encode(((Over)span).getOver(),true,buf);
			buf.append("</mover>");
		}else if(span instanceof Under){
			buf.append("<munder>");
			encode(((Under)span).getContent(),true,buf);
			encode(((Under)span).getUnder(),true,buf);
			buf.append("</munder>");
		}else if(span instanceof UnderOver){
			buf.append("<munderover>");
			encode(((UnderOver)span).getContent(),true,buf);
			encode(((UnderOver)span).getUnder(),true,buf);
			encode(((UnderOver)span).getOver(),true,buf);
			buf.append("</munderover>");
		}else{
			if(math){
				buf.append("<mi>");
				escape(span.toString(),buf);
				buf.append("</mi>");
			}else{
				escape(span.toString(),buf);
			}
		}
	}
	private static boolean isMathMode(Span span){
		return span instanceof Fraction||span instanceof Radical||span instanceof Matrix;
	}
	private static void escape(String str,StringBuilder latex){
		int len=str.length();
		for(int i=0;i<len;i++){
			char c=str.charAt(i);
			switch(c){
				case '<':
					latex.append("&lt;");
					break;
				case '&':
					latex.append("&amp;");
					break;
				default:
					latex.append(c);
					break;
			}
		}
	}
	private static void escape(int codePoint,StringBuilder latex){
		switch(codePoint){
			case '<':
				latex.append("&lt;");
				break;
			case '&':
				latex.append("&amp;");
				break;
			default:
				latex.appendCodePoint(codePoint);
				break;
		}
	}
	public static void main(String[] args){
		System.out.println(Integer.toHexString(3138));
	}
}
