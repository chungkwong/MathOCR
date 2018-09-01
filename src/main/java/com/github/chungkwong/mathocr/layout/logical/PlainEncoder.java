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
import com.github.chungkwong.mathocr.text.structure.Line;
import com.github.chungkwong.mathocr.text.structure.Symbol;
import com.github.chungkwong.mathocr.text.structure.Span;
import java.util.*;
/**
 * Encode document into plain text format
 *
 * @author Chan Chung Kwong
 */
public class PlainEncoder implements DocumentEncoder{
	@Override
	public String encode(Document doc){
		StringBuilder str=new StringBuilder();
		List<LogicalBlock> blocks=doc.getBlocks();
		if(doc.getTitle()!=null){
			encode(doc.getTitle(),str);
			str.append('\n');
		}
		if(doc.getAuthor()!=null){
			encode(doc.getAuthor(),str);
			str.append('\n');
		}
		Stack<Listing> lstlv=new Stack<>();
		for(LogicalBlock block:blocks){
			if(block instanceof Paragraph||block instanceof Caption){
				encode(((TextBlock)block).getLines(),str);
				str.append('\n');
			}else if(block instanceof Listing){
				int cmp=0;
				while((cmp=lstlv.isEmpty()?1:((Listing)block).compareLevel(lstlv.peek()))<0){
					lstlv.pop();
				}
				if(cmp>0){
					lstlv.push((Listing)block);
				}
				encode(((Listing)block).getLines(),str);
			}else if(block instanceof Heading){
				while(!lstlv.isEmpty()){
					lstlv.pop();
				}
				encode(((Heading)block).getLines(),str);
				str.append('\n');
			}else if(block instanceof Table){
				str.append('[');
				if(((Table)block).getCaption()!=null){
					encode(((Table)block).getCaption(),str);
				}
				str.append(']').append('(');
				str.append("file://").append(((Table)block).getPath()).append(')');
			}else if(block instanceof Image){
				str.append('[');
				if(((Image)block).getCaption()!=null){
					encode(((Image)block).getCaption(),str);
				}
				str.append(']').append('(');
				str.append("file://").append(((Image)block).getPath()).append(')');
			}else if(block instanceof HorizontalRule){
				str.append("\n---\n\n");
			}else if(block instanceof TextBlock){
				encode(((TextBlock)block).getLines(),str);
			}
		}
		return str.toString();
	}
	private static void encode(List<Line> lines,StringBuilder buf){
		for(Line line:lines){
			encode(line,buf);
			buf.append('\n');
		}
	}
	private static void encode(Span span,StringBuilder buf){
		if(span instanceof Symbol){
			buf.appendCodePoint(((Symbol)span).getCodePoint());
		}else if(span instanceof Line){
			for(Span comp:((Line)span).getSpans()){
				encode(comp,buf);
			}
		}else{
			buf.append(span.toString());
		}
	}
}
