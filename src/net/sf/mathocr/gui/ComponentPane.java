/* ComponentPane.java
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
import net.sf.mathocr.common.*;
import static net.sf.mathocr.Environment.env;
/**
 * A GUI component being used to display result of connected component analysis and allow user to make changes
 */
public final class ComponentPane extends JToolBar implements ActionListener,MouseListener,IconPaint{
	PageEditor pageEdit;
	JButton ccdel,ccmerge,ccsplitH,ccsplitV,layoutA,clean,filter;
	java.util.List<ConnectedComponent> components;
	char status='N';
	ConnectedComponent curr=null;
	public ComponentPane(PageEditor pageEdit){
		super(JToolBar.VERTICAL);
		this.pageEdit=pageEdit;
		add(new JLabel(env.getTranslation("COMPONENT_ANALYSIS")));
		ccdel=addButton("DELETE");
		ccmerge=addButton("MERGE");
		ccsplitH=addButton("HSPLIT");
		ccsplitV=addButton("VSPLIT");
		addSeparator();
		clean=addButton("CLEAN_EDGES");
		filter=addButton("NOISE_REMOVE");
		addSeparator();
		layoutA=addButton("NEXT");
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
		ccdel.setEnabled(true);
		ccmerge.setEnabled(true);
		ccsplitH.setEnabled(true);
		ccsplitV.setEnabled(true);
		components=pageEdit.getPage().getComponentPool().getComponents();
	}
	public void actionPerformed(ActionEvent e){
		curr=null;
		status='N';
		switch(e.getActionCommand()){
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
			case "CLEAN_EDGES":
				pageEdit.getPage().cleanPageEdges();
				break;
			case "NOISE_REMOVE":
				pageEdit.getPage().filterNoiseComponent();
				break;
			case "NEXT":
				pageEdit.getPageIcon().removeMouseListener(this);
				pageEdit.getPageIcon().setPainter(null);
				components=null;
				ccdel.setEnabled(false);
				ccmerge.setEnabled(false);
				ccsplitH.setEnabled(false);
				ccsplitV.setEnabled(false);
				pageEdit.showLayoutPane();
				break;
		}
		pageEdit.getPageIcon().repaint();
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
		ConnectedComponent cand=null;
		for(ConnectedComponent ele:components)
			if(x>=ele.getLeft()&&x<=ele.getRight()&&y<=ele.getBottom()&&y>=ele.getTop())
				if(cand==null||ele.getHeight()*ele.getWidth()<cand.getHeight()*cand.getWidth())
					cand=ele;
		if(cand==null){
			curr=null;
			return;
		}
		if(status=='D'){
			components.remove(cand);
		}else if(status=='M'){
			if(curr==null)
				curr=cand;
			else{
				curr.combineWith(cand);
				components.remove(cand);
				curr=null;
			}
		}else if(status=='H'){
			components.add(cand.splitHorizontally(x));
		}else if(status=='V'){
			components.add(cand.splitVertically(y));
		}
		pageEdit.getPageIcon().repaint();
	}
	/**
	 * Not used
	 */
	public void mouseReleased(MouseEvent e){}
	public void paintOn(Graphics2D g2d){
		g2d.setColor(Color.RED);
		for(ConnectedComponent ele:components)
			g2d.drawRect(ele.getLeft(),ele.getTop(),ele.getWidth(),ele.getHeight());
	}
}