/* PageEditor.java
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
import javax.swing.*;
import javax.swing.event.*;
import net.sf.mathocr.layout.*;
import static net.sf.mathocr.Environment.env;
/**
 * A GUI component being used to show a page
 */
public class PageEditor extends JPanel implements ChangeListener{
	Page page;
	DocumentEditor docEdit;
	protected CardLayout card=new CardLayout();
	protected JPanel rightPane=new JPanel();
	PreprocessPane prePane;
	ComponentPane componentPane;
	LayoutPane layoutPane;
	RecognizePane recognizePane;
	PageIcon icon;
	JSpinner scaleIn=new JSpinner(new SpinnerNumberModel(100,1,1000,25));
	public PageEditor(DocumentEditor docEdit){
		super(new BorderLayout());
		this.page=page;
		this.docEdit=docEdit;
		icon=new PageIcon();
		add(new JScrollPane(icon),BorderLayout.CENTER);
		rightPane.setLayout(card);
		prePane=new PreprocessPane(this);
		rightPane.add(prePane,"PREPROCESS");
		componentPane=new ComponentPane(this);
		rightPane.add(componentPane,"COMPONENT");
		layoutPane=new LayoutPane(this);
		rightPane.add(layoutPane,"LAYOUT");
		recognizePane=new RecognizePane(this);
		rightPane.add(recognizePane,"RECOGNIZE");
		add(new JScrollPane(rightPane),BorderLayout.EAST);
		Box btPane=Box.createHorizontalBox();
		btPane.add(new JLabel(env.getTranslation("SCALE")));
		scaleIn.addChangeListener(this);
		btPane.add(scaleIn);
		btPane.add(new JLabel("%"));
		add(btPane,BorderLayout.SOUTH);
	}
	public void showComponentPane(){
		page.componentAnalysis();
		if(env.getBoolean("SKIP_COMPONENT_PANE")){
			if(env.getBoolean("CLEAN_EDGES"))
				page.cleanPageEdges();
			showLayoutPane();
		}else{
			componentPane.activate();
			card.show(rightPane,"COMPONENT");
		}
	}
	public void showLayoutPane(){
		page.segment(new XYCut());
		if(env.getBoolean("SKIP_LAYOUT_PANE")){
			page.regionClassify();
			page.readingOrderSort();
			showRecognizePane();
		}else{
			layoutPane.activate();
			card.show(rightPane,"LAYOUT");
		}
	}
	public void showRecognizePane(){
		page.produceLogicalBlock();
		if(env.getBoolean("SKIP_RECOGNIZE_PANE")){
			showResult();
		}else{
			recognizePane.activate();
			card.show(rightPane,"RECOGNIZE");
		}
	}
	public void showResult(){
		page.cleanImage();
		docEdit.nextPage();
	}
	public DocumentEditor getDocumentEditor(){
		return docEdit;
	}
	public void setPage(Page page){
		this.page=page;
		page.load();
		icon.setPage(page);
		if(env.getBoolean("SKIP_PREPROCESS_PANE")){
			page.preprocessByDefault();
			showComponentPane();
		}else{
			card.show(rightPane,"PREPROCESS");
			prePane.activate();
		}
	}
	public Page getPage(){
		return page;
	}
	public PageIcon getPageIcon(){
		return icon;
	}
	public void stateChanged(ChangeEvent e){
		icon.setScale(((Integer)scaleIn.getValue())/100.0);
	}
}