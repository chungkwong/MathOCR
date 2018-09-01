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
 * Document
 *
 * @author Chan Chung Kwong
 */
public class Document{
	private static final String[] typeName=new String[]{"book","report","article"};
	public static int TYPE_BOOK=0, TYPE_REPORT=1, TYPE_ARTICLE=2;
	private final LinkedList<LogicalBlock> blocks;
	private final List<Line> title, author;
	private final int type;
	/**
	 * Create a document
	 *
	 * @param blocks
	 * @param title
	 * @param author
	 * @param type
	 */
	public Document(LinkedList<LogicalBlock> blocks,List<Line> title,List<Line> author,int type){
		this.blocks=blocks;
		this.title=title;
		this.author=author;
		this.type=type;
	}
	/**
	 * @return title of the document
	 */
	public List<Line> getTitle(){
		return title;
	}
	/**
	 * @return author of the document
	 */
	public List<Line> getAuthor(){
		return author;
	}
	/**
	 * @return logical blocks
	 */
	public LinkedList<LogicalBlock> getBlocks(){
		return blocks;
	}
	/**
	 * @return document type
	 */
	public int getType(){
		return type;
	}
	/**
	 * @return document type name
	 */
	public String getTypeName(){
		return typeName[type];
	}
}
