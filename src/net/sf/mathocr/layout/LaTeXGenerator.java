/* LaTeXGenerator.java
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
/**
 * Generator of output in the format LaTeX
 */
public final class LaTeXGenerator{
	Document doc;
	/**
	 * Construct a HTMLGenerator for a Document
	 * @param doc the Document
	 */
	public LaTeXGenerator(Document doc){
		this.doc=doc;
	}
	/**
	 * Get the LaTeX code
	 */
	public String toString(){
		StringBuilder str=new StringBuilder();
		TreeSet<String> pkg=new TreeSet<String>();
		pkg.add("amsmath");
		pkg.add("amssymb");
		pkg.add("mathrsfs");
		List<LogicalBlock> blocks=doc.getLogicalBlocks();
		String cls="\\documentclass{"+Document.getTypeName(doc.getType())+"}\n";
		str.append(cls);
		if(doc.getTitle()!=null){
			str.append("\\title{"+doc.getTitle()+"}\n");
			if(doc.getAuthor()!=null)
				str.append("\\author{"+doc.getAuthor()+"}\n");
			str.append("\\begin{document}\n\\maketitle\n");
		}else
			str.append("\\begin{document}\n");
		Stack<Listing> lstlv=new Stack<Listing>();
		Stack<Heading> headlv=new Stack<Heading>();
		int hlv=doc.getType()==Document.TYPE_ARTICLE?1:0;
		for(LogicalBlock block:blocks){
			if(block instanceof Paragraph||block instanceof Caption){
				//if(((Paragraph)block).isNoStart())
				//	str.append(((Paragraph)block).getContent()+"\n");
				//else{
					while(!lstlv.isEmpty())
						str.append(lstlv.pop().isNumbered()?"\\end{enumerate}\n":"\\end{itemize}\n");
					str.append("\\paragraph{}"+((TextLike)block).getContent()+"\n");
				//}
			}else if(block instanceof Listing){
				int cmp=0;
				while((cmp=lstlv.isEmpty()?1:((Listing)block).compareLevel(lstlv.peek()))<0)
					str.append(lstlv.pop().isNumbered()?"\\end{enumerate}\n":"\\end{itemize}\n");
				if(cmp>0){
					lstlv.push((Listing)block);
					str.append(((Listing)block).isNumbered()?"\\begin{enumerate}\n":"\\begin{itemize}\n");
				}
				str.append("\\item "+((Listing)block).getContentNoPrefix()+"\n");
			}else if(block instanceof Heading){
				while(!lstlv.isEmpty())
					str.append(lstlv.pop().isNumbered()?"\\end{enumerate}\n":"\\end{itemize}\n");
				int cmp=0;
				while((cmp=headlv.isEmpty()?1:((Heading)block).compareLevel(headlv.peek()))<=0){
					headlv.pop();
					--hlv;
				}
				headlv.push((Heading)block);
				++hlv;
				str.append("\\"+((Heading)block).getLevelName(hlv)+"{"+((Heading)block).getContentNoPrefix()+"}\n");
			}else if(block instanceof Table){
				boolean hascaption=!((Table)block).getCaption().isEmpty();
				if(hascaption){
					str.append("\\begin{table}\n");
					str.append("\\caption{"+Caption.removePrefix(((Table)block).getCaption())+"}\n");
				}
				str.append("\\includegraphics{"+((Table)block).getPath()+"}\n");
				if(hascaption){
					str.append("\\end{table}\n");
				}
				pkg.add("graphicx");
			}else if(block instanceof Image){
				boolean hascaption=!((Image)block).getCaption().isEmpty();
				if(hascaption){
					str.append("\\begin{figure}\n");
				}
				str.append("\\includegraphics{"+((Image)block).getPath()+"}\n");
				if(hascaption){
					str.append("\\caption{"+Caption.removePrefix(((Image)block).getCaption())+"}\n");
					str.append("\\end{figure}\n");
				}
				pkg.add("graphicx");
			}
		}
		while(!lstlv.isEmpty())
			str.append(lstlv.pop().isNumbered()?"\\end{enumerate}\n":"\\end{itemize}\n");
		str.append("\\end{document}\n");
		for(String p:pkg)
			str.insert(cls.length(),"\\usepackage{"+p+"}\n");
		return str.toString();
	}
}