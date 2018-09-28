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
import java.util.*;
/**
 * Naive implementation of PageAnalyzer
 *
 * @author Chan Chung Kwong
 */
public class NaivePageAnalyzer implements PageAnalyzer{
	public static final String NAME="NAIVE";
	@Override
	public List<LogicalBlock> analysis(List<LogicalBlock> in){
		boolean titleFound=false;
		LinkedList<LogicalBlock> out=new LinkedList<>();
		int leftbound=in.stream().mapToInt((b)->b.getBox().getLeft()).min().orElse(0);
		int rightbound=in.stream().mapToInt((b)->b.getBox().getRight()).max().orElse(0);
		/*if(!in.isEmpty()){
			LogicalBlock last=in.get(in.size()-1);
			if(last instanceof TextLike
					&&last.getBox().getLeft()<=block.getBox().getRight()&&block.getLeft()<=last.getRight()){
				((TextLike)logBlocks.getLast()).setNoEnd(false);
			}
		}*/
		for(LogicalBlock block:in){
			if(block instanceof TextBlock){
				TextBlock textLike=(TextBlock)block;
				String beginning=textLike.getBeginning();
				String ending=textLike.getEnding();
				int lstType=Listing.testItem(beginning);
				boolean centered=false;
				TextBlock lb;
				if(centered&&textLike.getLines().size()==1){
					lb=new Paragraph(textLike);
					lb.setNoEnd(true);
					lb.setNoStart(true);
				}else if(Caption.isCaption(beginning)){
					lb=new Caption(textLike);
				}else if(centered&&!titleFound){
					lb=new Title(textLike);
					titleFound=true;
				}else if(centered&&!out.isEmpty()&&out.get(out.size()-1) instanceof Title){
					lb=new Author(textLike);
				}else if(centered||(((textLike.isNoStart())||(textLike.getBox().getWidth()<(rightbound-leftbound)/3))&&!isEndedProperly(ending))){
					lb=new Heading(textLike);
				}else if(lstType!=0){
					lb=new Listing(lstType,textLike);
				}else{
					lb=new Paragraph(textLike);
				}
				out.add(lb);
			}else{
				out.add(block);
			}
		}
		return out;
	}
	private static final char[] endMarks=new char[]{'.','?','!',';',':','。','？','！','；','：'};
	/**
	 * Check if the line is ended with a ending punctuation mark
	 *
	 * @return the result
	 */
	private static boolean isEndedProperly(String text){
		String str=text.trim();
		if(!str.isEmpty()){
			char mark=str.charAt(str.length()-1);
			for(char c:endMarks){
				if(mark==c){
					return true;
				}
			}
		}
		return false;
	}
}
