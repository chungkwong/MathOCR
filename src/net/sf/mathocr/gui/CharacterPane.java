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
 * A GUI component being used to display result of optical character recongition and allow user to make changes
 */
public final class CharacterPane extends JToolBar implements ActionListener,MouseListener,MouseMotionListener,IconPaint{
	FormulaPane pageEdit;
	JButton del,modify,add,next;
	java.util.List<Char> characters;
	char status='N';
	int lastx=-1,lasty;
	public CharacterPane(FormulaPane pageEdit){
		super(JToolBar.VERTICAL);
		this.pageEdit=pageEdit;
		add(new JLabel(env.getTranslation("CHAR_RECOGNITION")));
		del=addButton("DELETE");
		modify=addButton("MODIFY");
		add=addButton("ADD");
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
		lastx=-1;
		pageEdit.getPageIcon().addMouseListener(this);
		pageEdit.getPageIcon().removeMouseMotionListener(pageEdit.getPageIcon());
		pageEdit.getPageIcon().addMouseMotionListener(this);
		pageEdit.getPageIcon().setPainter(this);
		characters=new CharactersLine(pageEdit.getPage().getComponentPool().getComponents()).getCharacters();
	}
	public Symbol selectSymbol(){
		final JDialog dia=new JDialog();
		dia.setLayout(new BorderLayout());
		JTabbedPane pane=new JTabbedPane();
		JList<Symbol> lst=new JList<Symbol>(DataBase.DEFAULT_DATABASE.getSymbols());
		pane.add(env.getTranslation("OLD_SYMBOL"),new JScrollPane(lst));
		Box box=Box.createVerticalBox();
		JTextField code=new JTextField();
		JFormattedTextField ph=new JFormattedTextField((Integer)30),pw=new JFormattedTextField((Integer)20),lh=new JFormattedTextField((Integer)40)
		,lw=new JFormattedTextField((Integer)20),xa=new JFormattedTextField((Integer)0),ya=new JFormattedTextField((Integer)25),la=new JFormattedTextField((Integer)28);
		box.add(new JLabel(env.getTranslation("LATEX_CODE")));
		box.add(code);
		box.add(new JLabel(env.getTranslation("PIXEL_WIDTH")));
		box.add(pw);
		box.add(new JLabel(env.getTranslation("PIXEL_HEIGHT")));
		box.add(ph);
		box.add(new JLabel(env.getTranslation("LOGICAL_WIDTH")));
		box.add(lw);
		box.add(new JLabel(env.getTranslation("LOGICAL_HEIGHT")));
		box.add(lh);
		box.add(new JLabel(env.getTranslation("X_OFFSET")));
		box.add(xa);
		box.add(new JLabel(env.getTranslation("PIXEL_ASCENT")));
		box.add(ya);
		box.add(new JLabel(env.getTranslation("LOGICAL_ASCENT")));
		box.add(la);
		pane.add(env.getTranslation("NEW_SYMBOL"),box);
		dia.add(pane,BorderLayout.CENTER);
		JButton confirm=new JButton(env.getTranslation("CONFIRM"));
		confirm.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				dia.setVisible(false);
			}
		});
		dia.add(confirm,BorderLayout.SOUTH);
		dia.pack();
		dia.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		dia.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		dia.setVisible(true);
		if(pane.getSelectedIndex()==0){
			return lst.getSelectedValue();
		}else{
			return new Symbol("",code.getText(),(Integer)pw.getValue(),(Integer)ph.getValue(),(Integer)lw.getValue(),(Integer)lh.getValue()
			,(Integer)xa.getValue(),(Integer)ya.getValue(),(Integer)la.getValue());
		}
	}
	public void actionPerformed(ActionEvent e){
		lastx=-1;
		status='N';
		switch(e.getActionCommand()){
			case "DELETE":
				status='D';
				break;
			case "MODIFY":
				status='M';
				break;
			case "ADD":
				status='A';
				break;
			case "NEXT":
				pageEdit.getPageIcon().removeMouseListener(this);
				pageEdit.getPageIcon().removeMouseMotionListener(this);
				pageEdit.getPageIcon().addMouseMotionListener(pageEdit.getPageIcon());
				pageEdit.getPageIcon().setPainter(null);
				pageEdit.showRecognizePane(characters);
				characters=null;
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
		if(status=='A'){
			if(lastx==-1){
				lastx=x;
				lasty=y;
			}else{
				Char ch=new Char(selectSymbol(),Math.min(lastx,x),Math.max(lastx,x),Math.min(lasty,y),Math.max(lasty,y));
				characters.add(ch);
				lastx=-1;
				pageEdit.getPageIcon().repaint();
			}
			return;
		}
		Char cand=null;
		for(Char ele:characters)
			if(x>=ele.getLeft()&&x<=ele.getRight()&&y<=ele.getBottom()&&y>=ele.getTop())
				if(cand==null||ele.getHeight()*ele.getWidth()<cand.getHeight()*cand.getWidth())
					cand=ele;
		if(cand==null){
			return;
		}
		if(status=='D'){
			characters.remove(cand);
		}else if(status=='M'){
			Candidate c=new Candidate(selectSymbol(),1.0);
			cand.getCandidates().clear();
			cand.getCandidates().add(c);
		}
		pageEdit.getPageIcon().repaint();
	}
	/**
	 * Not used
	 */
	public void mouseReleased(MouseEvent e){}
	public void paintOn(Graphics2D g2d){
		for(Char ele:characters){
			Candidate c=ele.getCandidates().first();
			SymbolExpression expr=new SymbolExpression(c.getSymbol(),ele.getLeft(),ele.getRight(),ele.getTop(),ele.getBottom());
			if(c.getCertainty()>0.9)
				g2d.setColor(Color.BLUE);
			else
				g2d.setColor(Color.GREEN);
			g2d.drawLine(expr.getLogicalLeft(),expr.getY(),expr.getLogicalRight(),expr.getY());
			g2d.drawLine(expr.getX(),expr.getLogicalTop(),expr.getX(),expr.getLogicalBottom());
		}
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
		Char cand=null;
		for(Char ele:characters)
			if(x>=ele.getLeft()&&x<=ele.getRight()&&y<=ele.getBottom()&&y>=ele.getTop())
				if(cand==null||ele.getHeight()*ele.getWidth()<cand.getHeight()*cand.getWidth())
					cand=ele;
		if(cand!=null){
			pageEdit.getPageIcon().setToolTipText(cand.toString());
		}else
			pageEdit.getPageIcon().setToolTipText("");
	}
}