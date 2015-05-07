/* HTMLGenerator.java
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
package net.sf.mathocr.layout;
import java.util.*;
import static net.sf.mathocr.ocr.Convertor.*;
/**
 * Generator of output in the format HTML
 */
public final class HTMLGenerator{
	Document doc;
	/**
	 * Construct a HTMLGenerator for a Document
	 * @param doc the Document
	 */
	public HTMLGenerator(Document doc){
		this.doc=doc;
	}
	/**
	 * Get the HTML code
	 */
	public String toString(){
		StringBuilder str=new StringBuilder();
		List<LogicalBlock> blocks=doc.getLogicalBlocks();
		str.append("<!DOCTYPE html>\n<html>\n");
		str.append("<head>\n");
		if(doc.getTitle()!=null)
			str.append("<title>"+doc.getTitle()+"</title>\n");
		str.append("<script type=\"text/javascript\" src=\"https://cdn.mathjax.org/mathjax/latest/MathJax.js?config=TeX-AMS-MML_HTMLorMML\"></script>\n");
		str.append("<script type=\"text/x-mathjax-config\">MathJax.Hub.Config({tex2jax: {inlineMath: [['$','$']]}});</script>\n");
		str.append("</head>\n");
		str.append("<body>\n");
		if(doc.getTitle()!=null){
			str.append("<h1>"+LaTeXtoHTML(doc.getTitle())+"</h1>\n");
			if(doc.getAuthor()!=null)
				str.append("<address>"+LaTeXtoHTML(doc.getAuthor())+"</address>\n");
		}
		Stack<Listing> lstlv=new Stack<Listing>();
		Stack<Heading> headlv=new Stack<Heading>();
		int hlv=1;
		for(LogicalBlock block:blocks){
			if(block instanceof Paragraph||block instanceof Caption){
				//if(((Paragraph)block).isNoStart())
				//	str.append(((Paragraph)block).getContent());
				//else{
					while(!lstlv.isEmpty())
						str.append(lstlv.pop().isNumbered()?"</ol>\n":"\\</ul>\n");
					str.append("<p>"+LaTeXtoHTML(((TextLike)block).getContent())+"</p>\n");
				//}
			}else if(block instanceof Listing){
				int cmp=0;
				while((cmp=lstlv.isEmpty()?1:((Listing)block).compareLevel(lstlv.peek()))<0)
					str.append(lstlv.pop().isNumbered()?"</ol>\n":"</ul>\n");
				if(cmp>0){
					lstlv.push((Listing)block);
					str.append(((Listing)block).isNumbered()?"<ol>\n":"<ul>\n");
				}
				str.append("<li>"+LaTeXtoHTML(((Listing)block).getContent())+"</li>\n");
			}else if(block instanceof Heading){
				while(!lstlv.isEmpty())
					str.append(lstlv.pop().isNumbered()?"</ol>\n":"</ul>\n");
				int cmp=0;
				while((cmp=headlv.isEmpty()?1:((Heading)block).compareLevel(headlv.peek()))<=0){
					headlv.pop();
					--hlv;
				}
				headlv.push((Heading)block);
				++hlv;
				str.append("<h"+hlv+">"+LaTeXtoHTML(((Heading)block).getContent())+"</h"+hlv+">\n");
			}else if(block instanceof Table){
				str.append("<table>\n");
				str.append("<caption>"+LaTeXtoHTML(((Table)block).getCaption())+"</caption>\n");
				str.append("<img src=\"file://"+((Table)block).getPath()+"\" />\n");
				str.append("</table>\n");
			}else if(block instanceof Image){
				str.append("<figure>\n");
				str.append("<img src=\"file://"+((Image)block).getPath()+"\" />\n");
				str.append("<figcaption>"+LaTeXtoHTML(((Image)block).getCaption())+"</figcaption>\n");
				str.append("</figure>\n");
			}
		}
		while(!lstlv.isEmpty())
			str.append(lstlv.pop().isNumbered()?"</ol>\n":"\\</ul>\n");
		str.append("</body>\n");
		str.append("</html>\n");
		return str.toString();
	}
}