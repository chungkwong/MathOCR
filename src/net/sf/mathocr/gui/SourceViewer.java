/* SourceViewer.java
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
import java.io.*;
import java.util.*;
import java.util.jar.*;
import javax.swing.*;
import javax.swing.event.*;
/**
 * A GUI component being used to display source code of the program
 */
public final class SourceViewer extends JPanel implements ListSelectionListener{
	JTextArea area=new JTextArea();
	JTextArea line=new JTextArea();
	JScrollPane jsp=new JScrollPane(area);
	JarFile jarfile;
	String sep=" "+System.getProperty("line.separator");
	public SourceViewer(){
		Vector<String> list=new Vector<String>();
		try{
			jarfile=new JarFile(System.getProperty("java.class.path"));
			Enumeration<JarEntry> entries=jarfile.entries();
			for(JarEntry file=entries.nextElement();entries.hasMoreElements();file=entries.nextElement())
				if(!file.isDirectory()&&(file.getName().endsWith(".java")||file.getName().endsWith(".map")))
					list.add(file.getName());
			JList<String> tree=new JList<String>(list);
			area.setFont(new Font("monospaced",0,12));
			tree.addListSelectionListener(this);
			line.setBackground(Color.LIGHT_GRAY);
			line.setEditable(false);
			StringBuilder numbers=new StringBuilder();
			for(int i=1;i<=1000;i++)//suppose line number not greater than 1000
				numbers.append(i+sep);
			line.setText(numbers.toString());
			area.setEditable(false);
			setLayout(new BorderLayout());
			jsp.setRowHeaderView(line);
			add(new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,new JScrollPane(tree),jsp),BorderLayout.CENTER);
		}catch(Exception ex){
			//ex.printStackTrace();
		}
	}
	public void valueChanged(ListSelectionEvent e){
		String path=((JList)e.getSource()).getSelectedValue().toString();
		try{
			area.read(new InputStreamReader(jarfile.getInputStream(jarfile.getEntry(path))),null);
		}catch(Exception ex){
			ex.printStackTrace();
		}
		//StringBuilder numbers=new StringBuilder();
		//int caretPosition=area.getDocument().getLength();
		//int count=area.getDocument().getDefaultRootElement().getElementIndex(caretPosition)+2;
		//for(int i=1;i<count;i++)
		//	numbers.append(i+sep);
		//line.setText(numbers.toString());
	}
}