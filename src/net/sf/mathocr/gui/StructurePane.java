/* CharacterPane.java
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
package net.sf.mathocr.gui;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import net.sf.mathocr.*;
import net.sf.mathocr.common.*;
import net.sf.mathocr.ocr.*;
import net.sf.mathocr.ofr.*;
import static net.sf.mathocr.Environment.env;
/**
 * A GUI component being used to display intermediate result of structural analysis stage in mathematical expression recognition
 */
public final class StructurePane extends JToolBar implements ActionListener,MouseMotionListener,IconPaint{
	FormulaPane pageEdit;
	JButton stepwise,next;
	LogicalLine line;
	public StructurePane(FormulaPane pageEdit){
		super(JToolBar.VERTICAL);
		this.pageEdit=pageEdit;
		add(new JLabel(env.getTranslation("CHAR_RECOGNITION")));
		stepwise=addButton("STEPWISE");
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
	public void activate(java.util.List<Char> characters){
		line=new LogicalLine(new CharactersLine(characters),true);
		pageEdit.getPageIcon().removeMouseMotionListener(pageEdit.getPageIcon());
		pageEdit.getPageIcon().addMouseMotionListener(this);
		pageEdit.getPageIcon().setPainter(this);
	}
	public void actionPerformed(ActionEvent e){
		switch(e.getActionCommand()){
			case "STEPWISE":
				if(!line.getAdjointGraph().stepwise()){
					pageEdit.getPageIcon().removeMouseMotionListener(this);
					pageEdit.getPageIcon().addMouseMotionListener(pageEdit.getPageIcon());
					pageEdit.getPageIcon().setPainter(null);
					pageEdit.showResult(line.recognize());
					line=null;
				}else{
					line.getAdjointGraph().output();
				}
				break;
			case "NEXT":
				pageEdit.getPageIcon().removeMouseMotionListener(this);
				pageEdit.getPageIcon().addMouseMotionListener(pageEdit.getPageIcon());
				pageEdit.getPageIcon().setPainter(null);
				pageEdit.showResult(line.recognize());
				line=null;
				break;
		}
		pageEdit.getPageIcon().repaint();
	}
	/**
	 * Not used
	 */
	public void mouseReleased(MouseEvent e){}
	public void paintOn(Graphics2D g2d){
		g2d.setColor(Color.RED);
		for(Expression expr:line.getExpressions()){
			g2d.drawLine(expr.getLogicalLeft(),expr.getY(),expr.getLogicalRight(),expr.getY());
			g2d.drawLine(expr.getX(),expr.getLogicalTop(),expr.getX(),expr.getLogicalBottom());
		}
		g2d.setColor(Color.GREEN);
		line.getAdjointGraph().paintOn(g2d);
	}
	/**
	 * Not used
	 */
	public void mouseDragged(MouseEvent e){}
	/**
	 *
	 * @param e the event
	 */
	public void mouseMoved(MouseEvent e){
		double scale=pageEdit.getPageIcon().getScale();
		int x=(int)(e.getX()/scale+0.5),y=(int)(e.getY()/scale+0.5);
		Expression cand=null;
		for(Expression ele:line.getExpressions())
			if(ele!=null&&x>=ele.getLogicalLeft()&&x<=ele.getLogicalRight()&&y<=ele.getLogicalBottom()&&y>=ele.getLogicalTop())
				if(cand==null||ele.getPhysicalHeight()*ele.getLogicalWidth()<cand.getLogicalHeight()*cand.getLogicalWidth())
					cand=ele;
		if(cand!=null){
			pageEdit.getPageIcon().setToolTipText(cand.toString());
		}else
			pageEdit.getPageIcon().setToolTipText("");
	}
}