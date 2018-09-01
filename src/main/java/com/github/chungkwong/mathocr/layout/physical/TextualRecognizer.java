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
package com.github.chungkwong.mathocr.layout.physical;
import com.github.chungkwong.mathocr.text.structure.Line;
import com.github.chungkwong.mathocr.text.LineSegmenters;
import com.github.chungkwong.mathocr.text.LineRecognizers;
import com.github.chungkwong.mathocr.text.TextLine;
import com.github.chungkwong.mathocr.layout.logical.LogicalBlock;
import com.github.chungkwong.mathocr.layout.logical.HorizontalRule;
import com.github.chungkwong.mathocr.layout.logical.TextBlock;
import com.github.chungkwong.mathocr.layout.logical.Listing;
import com.github.chungkwong.mathocr.common.ConnectedComponent;
import com.github.chungkwong.mathocr.common.BoundBox;
import java.awt.image.*;
import java.util.*;
/**
 * Textual block recognizer
 *
 * @author Chan Chung Kwong
 */
public class TextualRecognizer implements BlockRecognizer{
	private static final boolean[][] possCont=new boolean[][]{
		{true,false,false,false,false},
		{false,true,false,false,false},
		{false,false,false,false,false},
		{false,false,true,false,true},
		{false,false,true,false,true}
	};
	@Override
	public List<LogicalBlock> recognize(PhysicalBlock block,BufferedImage input){
		List<TextLine> lineBlocks=LineSegmenters.REGISTRY.get().segment(block);
		ListIterator<TextLine> lines=lineBlocks.listIterator();
		List<LogicalBlock> logicalBlocks=new LinkedList<>();
		TextLine line=lines.hasNext()?lines.next():null;
		while(line!=null){
			if(line.getComponents().size()==1&&isHorizontalLine(line.getComponents().get(0))){
				logicalBlocks.add(new HorizontalRule(line.getBox()));
			}
			List<Line> content=new ArrayList<>();
			content.add(LineRecognizers.REGISTRY.get().recognize(line,input));
			//System.out.println(content+"\t"+line.getAlignment());
			int lstType=Listing.testItem(content.toString());
			int align=line.getAlignment();
			int indent=line.getBox().getLeft()-block.getBox().getLeft();
			int fontsize=line.getBox().getHeight();
			boolean noStart=!(line.getBox().getLeft()-block.getBox().getLeft()>line.getBox().getHeight());
			boolean noEnd=!(block.getBox().getRight()-line.getBox().getRight()>line.getBox().getHeight());
			BoundBox bound=line.getBox();
			line=lines.hasNext()?lines.next():null;
			while(line!=null){
				//System.out.println(line.getText()+"\t"+line.getAlignment());
				if(!possCont[align][line.getAlignment()]&&!content.toString().endsWith("$$")){
					break;
				}
				bound=BoundBox.union(bound,line.getBox());
				content.add(LineRecognizers.REGISTRY.get().recognize(line,input));
				align=line.getAlignment();
				line=lines.hasNext()?lines.next():null;
			}
			logicalBlocks.add(new TextBlock(content,indent,fontsize,noStart,noEnd,bound,false));
		}
		return logicalBlocks;
	}
	private static boolean isHorizontalLine(ConnectedComponent ele){
		return ele.getHeight()>5*ele.getWidth()&&ele.getDensity()>0.8;
	}
}
