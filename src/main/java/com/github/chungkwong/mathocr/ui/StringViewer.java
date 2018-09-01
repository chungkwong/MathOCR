/*
 * Copyright (C) 2018 Chan Chung Kwong
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.chungkwong.mathocr.ui;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
/**
 *
 * @author Chan Chung Kwong
 */
public class StringViewer extends JPanel implements UndoableEditListener,ItemListener,ChangeListener{
	private static final ResourceBundle BUNDLE=ResourceBundle.getBundle("com.github.chungkwong.mathocr.resources.gui");
	private final JTextField in=new JTextField();
	private final JSpinner size=new JSpinner(new SpinnerNumberModel(24,1,1024,1.0));
	private final JToggleButton italic=new JToggleButton("I");
	private final JToggleButton bold=new JToggleButton("B");
	private final Box out=Box.createVerticalBox();
	public StringViewer(){
		super(new BorderLayout());
		JPanel input=new JPanel(new BorderLayout());
		in.getDocument().addUndoableEditListener(this);
		input.add(in,BorderLayout.CENTER);
		Box params=Box.createHorizontalBox();
		italic.setFont(italic.getFont().deriveFont(Font.ITALIC));
		italic.addItemListener(this);
		params.add(italic);
		bold.setFont(bold.getFont().deriveFont(Font.BOLD));
		bold.addItemListener(this);
		params.add(bold);
		size.addChangeListener(this);
		params.add(size);
		input.add(params,BorderLayout.EAST);
		add(input,BorderLayout.NORTH);
		add(new JScrollPane(out),BorderLayout.CENTER);
		in.setText(BUNDLE.getString("TEST_STRING"));
	}
	public static void main(String[] args){
		JFrame f=new JFrame();
		f.add(new StringViewer());
		f.setExtendedState(JFrame.MAXIMIZED_BOTH);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);
	}
	@Override
	public void undoableEditHappened(UndoableEditEvent e){
		preview();
	}
	@Override
	public void itemStateChanged(ItemEvent e){
		preview();
	}
	@Override
	public void stateChanged(ChangeEvent e){
		preview();
	}
	private void preview(){
		String text=in.getText();
		float fontsize=((Number)size.getValue()).floatValue();
		int style=Font.PLAIN;
		if(italic.isSelected()){
			style|=Font.ITALIC;
		}
		if(bold.isSelected()){
			style|=Font.BOLD;
		}
		out.removeAll();
		for(Font font:GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts()){
			if(font.canDisplayUpTo(text)==-1){
				out.add(new JLabel(font.getFontName()));
				JLabel sample=new JLabel(text);
				sample.setFont(font.deriveFont(style,fontsize));
				out.add(sample);
			}
		}
		validate();
	}
}
