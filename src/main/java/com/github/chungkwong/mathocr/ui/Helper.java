/* Helper.java
 * =========================================================================
 * This file is originally part of the MathOCR Project
 *
 * Copyright (C) 2014,2015 Chan Chung Kwong
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 */
package com.github.chungkwong.mathocr.ui;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;
import static com.github.chungkwong.mathocr.Environment.ENVIRONMENT;
/**
 * A GUI component being used to display manuals
 */
public final class Helper extends JSplitPane implements ListSelectionListener{
	JTextArea helparea=new JTextArea();
	String[] path;
	public Helper(){
		super(JSplitPane.HORIZONTAL_SPLIT);
		path=new String[]{ENVIRONMENT.getTranslation("ABOUT_PATH"),"LICENSE",ENVIRONMENT.getTranslation("HELP_PATH")};
		JList<String> lst=new JList<String>(new String[]{ENVIRONMENT.getTranslation("ABOUT"),ENVIRONMENT.getTranslation("GPL"),ENVIRONMENT.getTranslation("HELP")});
		lst.addListSelectionListener(this);
		helparea.setEditable(false);
		setLeftComponent(new JScrollPane(lst));
		setRightComponent(new JScrollPane(helparea));
		lst.setSelectedIndex(0);
	}
	public void valueChanged(ListSelectionEvent e){
		if(e.getValueIsAdjusting()){
			return;
		}
		try{
			helparea.read(new InputStreamReader(getClass().getResourceAsStream(path[((JList)e.getSource()).getSelectedIndex()]),"UTF-8"),"MathOCR.jar");
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
}
