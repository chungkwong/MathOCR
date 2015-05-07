/* FormulaPane.java
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
import java.io.*;
import javax.swing.*;
import net.sf.mathocr.preprocess.*;
import net.sf.mathocr.layout.*;
import net.sf.mathocr.ocr.*;
import net.sf.mathocr.ofr.*;
import static net.sf.mathocr.Environment.env;
/**
 * A GUI component being used to show a formula
 */
public final class FormulaPane extends PageEditor{
	File[] files;
	int curr=0;
	CharacterPane charPane;
	StructurePane structPane;
	JTextPane textpane=new JTextPane();
	public FormulaPane(File[] files){
		super(null);
		this.files=files;
		charPane=new CharacterPane(this);
		rightPane.add(charPane,"CHAR");
		structPane=new StructurePane(this);
		rightPane.add(structPane,"STRUCT");
		next();
	}
	void next(){
		if(curr<files.length){
			setPage(new Page(files[curr++],null));
		}else{
			removeAll();
			add(new JScrollPane(textpane),BorderLayout.CENTER);
		}
	}
	public void showLayoutPane(){
		if(env.getBoolean("SKIP_LAYOUT_PANE")){
			showRecognizePane(new CharactersLine(getPage().getComponentPool().getComponents()).getCharacters());
		}else{
			charPane.activate();
			card.show(rightPane,"CHAR");
		}
	}
	public void showRecognizePane(java.util.List<Char> characters){
		if(env.getBoolean("SKIP_RECOGNIZE_PANE")){
			showResult(new LogicalLine(new CharactersLine(characters),true).recognize());
		}else{
			structPane.activate(characters);
			card.show(rightPane,"STRUCT");
		}
	}
	public void showResult(String result){
		textpane.insertIcon(new ImageIcon(getPage().getInputImage()));
		javax.swing.text.Document doc=textpane.getDocument();
		try{
			doc.insertString(doc.getLength(),"\n"+result+"\n",null);
		}catch(Exception ex){
			ex.printStackTrace();
		}
		getPage().cleanImage();
		next();
	}
}