/* Document.java
 * =========================================================================
 * This file is originally part of the MathOCR Project
 *
 * Copyright (C) 2014,2015 Chan Chung Kwong
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
 * A data structure representing document
 */
public final class Document{
	static final String[] typeName=new String[]{"book","report","article"};
	public static int TYPE_BOOK=0,TYPE_REPORT=1,TYPE_ARTICLE=2;
	ArrayList<Page> pages=new ArrayList<Page>();
	LinkedList<LogicalBlock> blocks;
	String title,author;
	int type=TYPE_ARTICLE;
	/**
	 * Add a page the document
	 * @param page the page
	 */
	public void addPage(Page page){
		pages.add(page);
	}
	/**
	 * Get a page in the Document
	 * @param pageno page number of the page
	 * @return the page
	 */
	public Page getPage(int pageno){
		return pages.get(pageno);
	}
	/**
	 * Get all the page in the Document
	 * @return the pages
	 */
	public List<Page> getPages(){
		return pages;
	}
	/**
	 * Get the number of pages in the document
	 * @return the number of pages
	 */
	public int getNumberOfPage(){
		return pages.size();
	}
	/**
	 * Get the type of the document
	 * @return TYPE_BOOK, TYPE_REPORT or TYPE_ARTICLE
	 */
	public int getType(){
		return type;
	}
	/**
	 * Get the title of the Document
	 * @return the title
	 */
	public String getTitle(){
		return title;
	}
	/**
	 * Get the author of the Document
	 * @return the author
	 */
	public String getAuthor(){
		return author;
	}
	/**
	 * Set the type of the document
	 * @param type TYPE_BOOK, TYPE_REPORT or TYPE_ARTICLE
	 */
	public void setType(int type){
		this.type=type;
	}
	/**
	 * Set the title of the Document
	 * @param title the title
	 */
	public void setTitle(String title){
		this.title=title;
	}
	/**
	 * Set the author of the Document
	 * @param author the title
	 */
	public void setAuthor(String author){
		this.author=author;
	}
	/**
	 * Get all logical blocks in the document
	 * @return the logical blocks
	 */
	public LinkedList<LogicalBlock> getLogicalBlocks(){
		if(blocks!=null)
			return blocks;
		blocks=new LinkedList<LogicalBlock>();
		LogicalBlock lastNonFloat=null;
		for(Page page:pages){
			//boolean footnote=false;
			for(LogicalBlock block:page.getLogicalBlocks()){
				if(!block.isFloating()){
					if(block instanceof Title){
						if(title==null)
							title=((Title)block).getContent();
						else
							title+=" "+((Title)block).getContent();
						continue;
					}
					if(block instanceof Author){
						if(author==null)
							author=((Author)block).getContent();
						else
							author+="\\\\"+((Author)block).getContent();
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
					if(block instanceof Heading&&((Heading)block).getLevel()==1)
						type=TYPE_BOOK;
					if(lastNonFloat!=null&&block instanceof TextLike&&((TextLike)block).isNoStart()&&((TextLike)lastNonFloat).isNoEnd()){
						((TextLike)lastNonFloat).setContent(((TextLike)lastNonFloat).getContent()+" "+((TextLike)block).getContent());
						((TextLike)lastNonFloat).setNoEnd(((TextLike)block).isNoEnd());
						continue;
					}
					lastNonFloat=block;
				}else if(!blocks.isEmpty()&&blocks.getLast() instanceof Caption){
					if(block instanceof Image)
						((Image)block).setCaption(((Caption)blocks.pollLast()).getContentNoPrefix());
					else if(block instanceof Table)
						((Table)block).setCaption(((Caption)blocks.pollLast()).getContentNoPrefix());
				}
				blocks.addLast(block);
			}
			//blocks.addAll(blocks);
		}
		title=net.sf.mathocr.ocr.Convertor.removeDisplayedFormula(title);
		author=net.sf.mathocr.ocr.Convertor.removeDisplayedFormula(author);
		return blocks;
	}
	/**
	 * Get LaTeX type name corresponding to a code
	 * @param t TYPE_BOOK, TYPE_REPORT or TYPE_ARTICLE
	 */
	public static String getTypeName(int t){
		return typeName[t];
	}
}