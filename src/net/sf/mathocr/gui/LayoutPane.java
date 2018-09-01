/* LayoutPane.java
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
package net.sf.mathocr.gui;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import net.sf.mathocr.*;
import net.sf.mathocr.layout.*;
import static net.sf.mathocr.Environment.env;
/**
 * A GUI component being used to display result of physical layout analysis and allow user to make changes
 */
public final class LayoutPane extends JToolBar implements ActionListener,MouseListener,IconPaint{
	PageEditor pageEdit;
	JButton create,del,merge,splitH,splitV,classify,fixtype,autoSort,manualSort,extractLine,splitLine,mergeLine,next;
	JComboBox<String> type;
	java.util.List<Block> blocks;
	char status='N';
	Block curr=null;
	int readOrd=0;
	public LayoutPane(PageEditor pageEdit){
		super(JToolBar.VERTICAL);
		this.pageEdit=pageEdit;
		add(new JLabel(env.getTranslation("LAYOUT_ANALYSIS")));
		//create=addButton("ADD");
		del=addButton("DELETE");
		merge=addButton("MERGE");
		splitH=addButton("HSPLIT");
		splitV=addButton("VSPLIT");
		addSeparator();
		classify=addButton("REGION_CLASSIFY");
		classify=addButton("REGION_MANUAL");
		type=new JComboBox<String>(new String[]{env.getTranslation("TEXT"),env.getTranslation("IMAGE"),env.getTranslation("TABLE")});
		type.setAlignmentX(0);
		type.setMaximumSize(type.getPreferredSize());
		add(type);
		addSeparator();
		autoSort=addButton("READING_ORDER_SORT");
		manualSort=addButton("READING_ORDER_MANUAL");
		addSeparator();
		extractLine=addButton("EXTRACT_LINES");
		splitLine=addButton("SPLIT_LINE");
		mergeLine=addButton("MERGE_LINE");
		addSeparator();
		next=addButton("NEXT");
	}
	private final JButton addButton(String com){
		JButton button=new JButton(env.getTranslation(com));
		button.setActionCommand(com);
		button.addActionListener(this);
		button.setAlignmentX(0);
		button.setMaximumSize(button.getPreferredSize());
		add(button);
		return button;
	}
	public void activate(){
		pageEdit.getPageIcon().addMouseListener(this);
		pageEdit.getPageIcon().setPainter(this);
		//create.setEnabled(true);
		del.setEnabled(true);
		merge.setEnabled(true);
		splitH.setEnabled(true);
		splitV.setEnabled(true);
		extractLine.setEnabled(false);
		splitLine.setEnabled(false);
		mergeLine.setEnabled(false);
		next.setEnabled(false);
		blocks=pageEdit.getPage().getBlocks();
	}
	public void actionPerformed(ActionEvent e){
		curr=null;
		status='N';
		readOrd=0;
		switch(e.getActionCommand()){
			case "ADD":
				status='A';
				break;
			case "DELETE":
				status='D';
				break;
			case "MERGE":
				status='M';
				break;
			case "HSPLIT":
				status='H';
				break;
			case "VSPLIT":
				status='V';
				break;
			case "REGION_MANUAL":
				status='C';
				break;
			case "REGION_CLASSIFY":
				pageEdit.getPage().regionClassify();
				pageEdit.getPageIcon().repaint();
				del.setEnabled(false);
				merge.setEnabled(false);
				splitH.setEnabled(false);
				splitV.setEnabled(false);
				extractLine.setEnabled(true);
				break;
			case "READING_ORDER_SORT":
				pageEdit.getPage().readingOrderSort();
				pageEdit.getPageIcon().repaint();
				blocks=pageEdit.getPage().getBlocks();
				break;
			case "READING_ORDER_MANUAL":
				status='S';
				break;
			case "EXTRACT_LINES":
				for(Block block:blocks)
					block.extractTextLines();
				pageEdit.getPageIcon().repaint();
				splitLine.setEnabled(true);
				mergeLine.setEnabled(true);
				next.setEnabled(true);
				break;
			case "SPLIT_LINE":
				status='s';
				break;
			case "MERGE_LINE":
				status='m';
				break;
			case "NEXT":
				pageEdit.getPageIcon().setPainter(null);
				blocks=null;
				pageEdit.getPageIcon().removeMouseListener(this);
				pageEdit.getPageIcon().repaint();
				pageEdit.showRecognizePane();
				break;
		}
	}
	/**
	 * Not used
	 */
	public void mouseClicked(MouseEvent e){}
	/**
	 * Not used
	 */
	public void mouseEntered(MouseEvent e){}
	/**
	 * Not used
	 */
	public void mouseExited(MouseEvent e){}
	/**
	 * Change recognition result of the symbol that the mouse pointed to
	 * @param e the event
	 */
	public void mousePressed(MouseEvent e){
		if(status=='N')
			return;
		double scale=pageEdit.getPageIcon().getScale();
		int x=(int)(e.getX()/scale+0.5),y=(int)(e.getY()/scale+0.5);
		Block cand=null;
		int candind=-1;
		ListIterator<Block> iter=blocks.listIterator();
		while(iter.hasNext()){
			Block block=iter.next();
			if(x>=block.getLeft()&&x<=block.getRight()&&y<=block.getBottom()&&y>=block.getTop())
				if(cand==null||block.getHeight()*block.getWidth()<cand.getHeight()*cand.getWidth()){
					cand=block;
					candind=iter.previousIndex();
				}
		}
		if(cand==null){
			curr=null;
			return;
		}
		if(status=='D'){
			blocks.remove(cand);
		}else if(status=='M'){
			if(curr==null)
				curr=cand;
			else{
				cand.merge(curr);
				blocks.remove(curr);
				curr=null;
			}
		}else if(status=='H'){
			blocks.add(cand.splitHorizontally(x));
		}else if(status=='V'){
			blocks.add(cand.splitVertically(y));
		}else if(status=='S'){
			blocks.set(candind,blocks.get(readOrd));
			blocks.set(readOrd,cand);
			readOrd=(readOrd==blocks.size()-1)?0:readOrd+1;
		}else if(status=='C'){
			switch(type.getSelectedIndex()){
				case 0:
					cand.setType(TextBlock.DEFAULT_BLOCK);
					break;
				case 1:
					cand.setType(ImageBlock.DEFAULT_BLOCK);
					break;
				case 2:
					cand.setType(TableBlock.DEFAULT_BLOCK);
					break;
			}
		}else if(status=='s'){
			java.util.List<TextLine> lines=cand.getTextLines();
			if(lines!=null){
				ListIterator<TextLine> it=lines.listIterator();
				while(it.hasNext()){
					TextLine line=it.next();
					if(x<=line.getRight()&&line.getLeft()<=x&&y<=line.getBottom()&&line.getTop()<=y)
						it.add(line.splitAt(y));
				}
			}
		}else if(status=='m'){
			java.util.List<TextLine> lines=cand.getTextLines();
			if(lines!=null){
				ListIterator<TextLine> it=lines.listIterator();
				while(it.hasNext()){
					TextLine line=it.next();
					if(x<=line.getRight()&&line.getLeft()<=x&&y<=line.getBottom()&&line.getTop()<=y&&it.hasNext()){
						it.remove();
						it.next().mergeWith(line);
					}
				}
			}
		}
		pageEdit.getPageIcon().repaint();
	}
	/**
	 * Not used
	 */
	public void mouseReleased(MouseEvent e){}
	public void paintOn(Graphics2D g2d){
		ListIterator<Block> iter=blocks.listIterator();
		while(iter.hasNext()){
			Block ele=iter.next();
			BlockType t=ele.getType();
			g2d.setColor(Color.BLUE);
			g2d.drawRect(ele.getLeft(),ele.getTop(),ele.getWidth(),ele.getHeight());
			g2d.drawString(Integer.toString(iter.previousIndex())+(t!=null?env.getTranslation(t.toString()):""),ele.getLeft(),ele.getBottom());
			if(ele.getTextLines()!=null){
				g2d.setColor(Color.GREEN);
				for(TextLine line:ele.getTextLines())
					g2d.drawRect(line.getLeft(),line.getTop(),line.getRight()-line.getLeft()+1,line.getBottom()-line.getTop()+1);
			}
		}
	}
}