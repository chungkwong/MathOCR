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
import java.util.*;
/**
 * Naive implementation of DocumentAssembler
 *
 * @author Chan Chung Kwong
 */
public class NaiveDocumentAssembler implements DocumentAssembler{
	public static final String NAME="NAIVE";
	@Override
	public Document assemble(List<Page> pages){
		LinkedList<LogicalBlock> blocks=new LinkedList<>();
		List<Line> title=null;
		List<Line> author=null;
		int type=Document.TYPE_ARTICLE;
		LogicalBlock lastNonFloat=null;
		for(Page page:pages){
			//boolean footnote=false;
			for(LogicalBlock block:page.getLogicalBlocks()){
				if(!block.isFloating()){
					if(block instanceof Title){
						if(title==null){
							title=new ArrayList<>();
						}
						title.addAll(((Title)block).getLines());
						continue;
					}
					if(block instanceof Author){
						if(author==null){
							author=new ArrayList<>();
						}
						author.addAll(((Author)block).getLines());
						continue;
					}
					if(block instanceof Caption&&!blocks.isEmpty()){
						LogicalBlock b=blocks.getLast();
						if(b instanceof Image&&((Image)b).getCaption().isEmpty()){
							((Image)b).setCaption(((Caption)block).getContentNoPrefix());
							continue;
						}else if(b instanceof Table&&((Table)b).getCaption().isEmpty()){
							((Table)b).setCaption(((Caption)block).getContentNoPrefix());
							continue;
						}
					}
					if(block instanceof Heading&&((Heading)block).getLevel()==1){
						type=Document.TYPE_BOOK;
					}
					if(lastNonFloat!=null&&block instanceof TextBlock&&((TextBlock)block).isNoStart()&&((TextBlock)lastNonFloat).isNoEnd()){
						((TextBlock)lastNonFloat).getLines().addAll(((TextBlock)block).getLines());
						((TextBlock)lastNonFloat).setNoEnd(((TextBlock)block).isNoEnd());
						continue;
					}
					lastNonFloat=block;
				}else if(!blocks.isEmpty()&&blocks.getLast() instanceof Caption){
					if(block instanceof Image){
						((Image)block).setCaption(((Caption)blocks.pollLast()).getContentNoPrefix());
					}else if(block instanceof Table){
						((Table)block).setCaption(((Caption)blocks.pollLast()).getContentNoPrefix());
					}
				}
				blocks.addLast(block);
			}
			//blocks.addAll(blocks);
		}
		return new Document(blocks,title,author,type);
	}
}
