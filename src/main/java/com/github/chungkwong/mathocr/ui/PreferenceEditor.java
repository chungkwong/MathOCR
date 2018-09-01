/* PreferenceEditor.java
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
import java.awt.*;
import java.awt.event.*;
import java.text.*;
import javax.swing.*;
import static com.github.chungkwong.mathocr.Environment.ENVIRONMENT;
/**
 * A GUI component being used to display global preference and allow user to make changes
 */
public final class PreferenceEditor extends Box implements ActionListener{
	public PreferenceEditor(){
		//super(new BorderLayout());
		super(BoxLayout.Y_AXIS);
		for(String com:ENVIRONMENT.getBooleanKeySet())
			addBooleanProperty(com);
		for(String com:ENVIRONMENT.getIntegerKeySet())
			addIntegerProperty(com);
		for(String com:ENVIRONMENT.getFloatKeySet())
			addFloatProperty(com);
		for(String com:ENVIRONMENT.getStringKeySet())
			addStringProperty(com);
		JLabel remind=new JLabel(ENVIRONMENT.getTranslation("ENTER_TO_APPLY"));
		remind.setAlignmentX(0);
		add(remind);
	}
	private void addBooleanProperty(String com){
		JCheckBox checkbox=new JCheckBox(ENVIRONMENT.getTranslation(com),ENVIRONMENT.getBoolean(com));
		checkbox.setAlignmentX(0);
		checkbox.setActionCommand(com);
		checkbox.addActionListener(this);
		add(checkbox);
	}
	private void addIntegerProperty(String com){
		Box box=Box.createHorizontalBox();
		box.add(new JLabel(ENVIRONMENT.getTranslation(com)));
		JFormattedTextField field=new JFormattedTextField(ENVIRONMENT.getInteger(com));
		field.setToolTipText(ENVIRONMENT.getTranslation(com+"_TIPS"));
		field.setActionCommand(com);
		field.addActionListener(this);
		box.add(field);
		box.setAlignmentX(0);
		add(box);
	}
	private void addFloatProperty(String com){
		Box box=Box.createHorizontalBox();
		box.add(new JLabel(ENVIRONMENT.getTranslation(com)));
		JFormattedTextField field=new JFormattedTextField(ENVIRONMENT.getFloat(com));
		field.setToolTipText(ENVIRONMENT.getTranslation(com+"_TIPS"));
		field.setActionCommand(com);
		field.addActionListener(this);
		box.add(field);
		box.setAlignmentX(0);
		add(box);
	}
	private void addStringProperty(String com){
		Box box=Box.createHorizontalBox();
		box.add(new JLabel(ENVIRONMENT.getTranslation(com)));
		JTextField field=new JTextField(ENVIRONMENT.getString(com));
		field.setToolTipText(ENVIRONMENT.getTranslation(com+"_TIPS"));
		field.setActionCommand(com);
		field.addActionListener(this);
		box.add(field);
		box.setAlignmentX(0);
		add(box);
	}
	public void actionPerformed(ActionEvent e){
		String com=e.getActionCommand();
		Object src=e.getSource();
		if(src instanceof JCheckBox){
			ENVIRONMENT.setBoolean(com,((JCheckBox)src).isSelected());
		}else if(src instanceof JFormattedTextField){
			Object val=((JFormattedTextField)src).getValue();
			if(val instanceof Integer)
				ENVIRONMENT.setInteger(com,(Integer)val);
			else
				ENVIRONMENT.setFloat(com,(Float)val);
		}else{
			ENVIRONMENT.setString(com,((JTextField)src).getText());
		}
	}
}