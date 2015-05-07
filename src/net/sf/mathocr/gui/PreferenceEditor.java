/* PreferenceEditor.java
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
import net.sf.mathocr.*;
import java.awt.*;
import java.awt.event.*;
import java.text.*;
import javax.swing.*;
import static net.sf.mathocr.Environment.env;
/**
 * A GUI component being used to display global preference and allow user to make changes
 */
public final class PreferenceEditor extends Box implements ActionListener{
	public PreferenceEditor(){
		//super(new BorderLayout());
		super(BoxLayout.Y_AXIS);
		for(String com:env.getBooleanKeySet())
			addBooleanProperty(com);
		for(String com:env.getIntegerKeySet())
			addIntegerProperty(com);
		for(String com:env.getFloatKeySet())
			addFloatProperty(com);
		for(String com:env.getStringKeySet())
			addStringProperty(com);
		JLabel remind=new JLabel(env.getTranslation("ENTER_TO_APPLY"));
		remind.setAlignmentX(0);
		add(remind);
	}
	private void addBooleanProperty(String com){
		JCheckBox checkbox=new JCheckBox(env.getTranslation(com),env.getBoolean(com));
		checkbox.setAlignmentX(0);
		checkbox.setActionCommand(com);
		checkbox.addActionListener(this);
		add(checkbox);
	}
	private void addIntegerProperty(String com){
		Box box=Box.createHorizontalBox();
		box.add(new JLabel(env.getTranslation(com)));
		JFormattedTextField field=new JFormattedTextField(env.getInteger(com));
		field.setToolTipText(env.getTranslation(com+"_TIPS"));
		field.setActionCommand(com);
		field.addActionListener(this);
		box.add(field);
		box.setAlignmentX(0);
		add(box);
	}
	private void addFloatProperty(String com){
		Box box=Box.createHorizontalBox();
		box.add(new JLabel(env.getTranslation(com)));
		JFormattedTextField field=new JFormattedTextField(env.getFloat(com));
		field.setToolTipText(env.getTranslation(com+"_TIPS"));
		field.setActionCommand(com);
		field.addActionListener(this);
		box.add(field);
		box.setAlignmentX(0);
		add(box);
	}
	private void addStringProperty(String com){
		Box box=Box.createHorizontalBox();
		box.add(new JLabel(env.getTranslation(com)));
		JTextField field=new JTextField(env.getString(com));
		field.setToolTipText(env.getTranslation(com+"_TIPS"));
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
			env.setBoolean(com,((JCheckBox)src).isSelected());
		}else if(src instanceof JFormattedTextField){
			Object val=((JFormattedTextField)src).getValue();
			if(val instanceof Integer)
				env.setInteger(com,(Integer)val);
			else
				env.setFloat(com,(Float)val);
		}else{
			env.setString(com,((JTextField)src).getText());
		}
	}
}