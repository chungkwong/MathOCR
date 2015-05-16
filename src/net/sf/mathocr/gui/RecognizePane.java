/* RecognizePane.java
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
import java.awt.geom.*;
import java.util.*;
import javax.swing.*;
import net.sf.mathocr.*;
import net.sf.mathocr.layout.*;
import static net.sf.mathocr.Environment.env;
/**
 * A GUI component being used to display result of logical layout analysis and allow user to make changes
 */
public final class RecognizePane extends JToolBar implements ActionListener,MouseListener,IconPaint{
	PageEditor pageEdit;
	JButton del,modify,next;
	JComboBox<String> type=new JComboBox<String>(new String[]{env.getTranslation("IMAGE"),env.getTranslation("TABLE"),env.getTranslation("TITLE")
	,env.getTranslation("AUTHOR"),env.getTranslation("PARAGRAPH"),env.getTranslation("LISTING"),env.getTranslation("HEADING"),env.getTranslation("CAPTION")});
	JCheckBox noStart=new JCheckBox(env.getTranslation("NO_START"),false),noEnd=new JCheckBox(env.getTranslation("NO_END"),false);
	JTextField additional=new JTextField();
	java.util.List<LogicalBlock> blocks;
	LogicalBlock curr;
	JTextArea area=new JTextArea();
	public RecognizePane(PageEditor pageEdit){
		super(JToolBar.VERTICAL);
		this.pageEdit=pageEdit;
		del=addButton("DELETE");
		modify=addButton("MODIFY");
		addSeparator();
		type.setAlignmentX(0);
		add(type);
		area.setAlignmentX(0);
		area.setLineWrap(true);
		add(area);
		noStart.setAlignmentX(0);
		add(noStart);
		noEnd.setAlignmentX(0);
		add(noEnd);
		additional.setAlignmentX(0);
		JLabel lab=new JLabel(env.getTranslation("ADDITIONAL_INFO"));
		lab.setAlignmentX(0);
		add(lab);
		additional.setAlignmentX(0);
		add(additional);
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
		//JDialog dia=new JOptionPane(env.getTranslation("PLEASE_WAIT"),JOptionPane.INFORMATION_MESSAGE).createDialog("");
		blocks=pageEdit.getPage().getLogicalBlocks();
		pageEdit.getPageIcon().addMouseListener(this);
		curr=null;
		pageEdit.getPageIcon().setPainter(this);
	}
	void setCurrent(){
		if(curr==null){
			area.setText("");
			additional.setText("");
		}else{
			if(curr instanceof net.sf.mathocr.layout.Image){
				type.setSelectedIndex(0);
				area.setText(((net.sf.mathocr.layout.Image)curr).getPath());
				additional.setText(((net.sf.mathocr.layout.Image)curr).getCaption());
			}else if(curr instanceof Table){
				type.setSelectedIndex(1);
				area.setText(((Table)curr).toString());
				additional.setText(((Table)curr).getCaption());
			}else if(curr instanceof Title){
				type.setSelectedIndex(2);
				additional.setText("");
			}else if(curr instanceof Author){
				type.setSelectedIndex(3);
				additional.setText("");
			}else if(curr instanceof Paragraph){
				type.setSelectedIndex(4);
				additional.setText("");
			}else if(curr instanceof Listing){
				type.setSelectedIndex(5);
				additional.setText(Integer.toString(((Listing)curr).getIndent()));
			}else if(curr instanceof Heading){
				type.setSelectedIndex(6);
				additional.setText(Integer.toString(((Heading)curr).getFontSize()));
			}else if(curr instanceof Caption){
				type.setSelectedIndex(7);
				additional.setText("");
			}
			if(curr instanceof TextLike){
				area.setText(((TextLike)curr).getContent());
				noStart.setSelected(((TextLike)curr).isNoStart());
				noEnd.setSelected(((TextLike)curr).isNoEnd());
			}
		}
	}
	public void actionPerformed(ActionEvent e){
		switch(e.getActionCommand()){
			case "DELETE":
				blocks.remove(curr);
				curr=null;
				setCurrent();
				pageEdit.getPageIcon().repaint();
				break;
			case "MODIFY":
				if(curr!=null){
					if(curr instanceof TextLike&&type.getSelectedIndex()>=2){
						String content=area.getText();
						TextLike updated=null;
						boolean nostart=noStart.isSelected(),noend=noEnd.isSelected();
						switch(type.getSelectedIndex()){
							case 2:
								updated=new Title(content,nostart,noend,curr.getLeft(),curr.getRight(),curr.getTop(),curr.getBottom());
								break;
							case 3:
								updated=new Author(content,nostart,noend,curr.getLeft(),curr.getRight(),curr.getTop(),curr.getBottom());
								break;
							case 4:
								updated=new Paragraph(content,nostart,noend,curr.getLeft(),curr.getRight(),curr.getTop(),curr.getBottom());
								break;
							case 5:
								updated=new Listing(content,Listing.testItem(content),Integer.parseInt(additional.getText())
								,nostart,noend,curr.getLeft(),curr.getRight(),curr.getTop(),curr.getBottom());
								break;
							case 6:
								updated=new Heading(content,Integer.parseInt(additional.getText()),nostart,noend,curr.getLeft(),curr.getRight(),curr.getTop(),curr.getBottom());
								break;
							case 7:
								updated=new Caption(content,nostart,noend,curr.getLeft(),curr.getRight(),curr.getTop(),curr.getBottom());
								break;
						}
						blocks.set(blocks.indexOf(curr),updated);
						curr=updated;
					}else if(curr instanceof net.sf.mathocr.layout.Image&&type.getSelectedIndex()==0){
						((net.sf.mathocr.layout.Image)curr).setCaption(additional.getText());
					}else if(curr instanceof Table&&type.getSelectedIndex()==1){
						((Table)curr).setCaption(additional.getText());
					}else{
						JOptionPane.showMessageDialog(null,env.getTranslation("NOT_ALLOWED"));
					}
				}
				pageEdit.getPageIcon().repaint();
				break;
			case "NEXT":
				blocks=null;
				curr=null;
				area.setText("");
				additional.setText("");
				pageEdit.getPageIcon().removeMouseListener(this);
				pageEdit.getPageIcon().setPainter(null);
				pageEdit.showResult();
				break;
		}
	}
	static int getTypeNumber(LogicalBlock block){
		if(block instanceof net.sf.mathocr.layout.Image)
			return 0;
		else if(block instanceof Table)
			return 1;
		else if(block instanceof Title)
			return 2;
		else if(block instanceof Author)
			return 3;
		else if(block instanceof Paragraph)
			return 4;
		else if(block instanceof Listing)
			return 5;
		else if(block instanceof Heading)
			return 6;
		else if(block instanceof Caption)
			return 7;
		return -1;
	}
	public void paintOn(Graphics2D g2d){
		ListIterator<LogicalBlock> iter=blocks.listIterator();
		while(iter.hasNext()){
			LogicalBlock block=iter.next();
			Rectangle rect=new Rectangle(block.getLeft(),block.getTop(),block.getRight()-block.getLeft()+1,block.getBottom()-block.getTop()+1);
			if(block==curr){
				g2d.setColor(Color.RED);
				//pageEdit.getPageIcon().scrollRectToVisible(rect);
			}else
				g2d.setColor(Color.BLUE);
			g2d.draw(rect);
			g2d.drawString(Integer.toString(iter.previousIndex())+type.getItemAt(getTypeNumber(block)),block.getLeft(),block.getBottom());
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
		double scale=pageEdit.getPageIcon().getScale();
		int x=(int)(e.getX()/scale+0.5),y=(int)(e.getY()/scale+0.5);
		ListIterator<LogicalBlock> iter=blocks.listIterator();
		while(iter.hasNext()){
			LogicalBlock block=iter.next();
			if(x>=block.getLeft()&&x<=block.getRight()&&y<=block.getBottom()&&y>=block.getTop())
				curr=block;
		}
		setCurrent();
		pageEdit.getPageIcon().repaint();
	}
	/**
	 * Not used
	 */
	public void mouseReleased(MouseEvent e){}
}