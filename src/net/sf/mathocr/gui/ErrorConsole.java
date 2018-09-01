/* ErrorConsole.java
 * =========================================================================
 * This file is originally part of the MathOCR Project
 *
 * Copyright (C) 2014 Chan Chung Kwong
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
import javax.swing.*;
import net.sf.mathocr.*;
import static net.sf.mathocr.Environment.env;
/**
 * GUI component showing exception messages
 */
public final class ErrorConsole extends PrintStream{
	JFrame dia;
	JTextArea area=new JTextArea();
	/**
	 * Construct a ErrorReporter
	 * @param title the title of the message dialog
	 */
	public ErrorConsole(){
		super(System.err);
		System.setErr(this);
		dia=new JFrame(env.getTranslation("ERR_MSG"));
		area.setEditable(false);
		dia.add(new JScrollPane(area),BorderLayout.CENTER);
		dia.setSize(600,400);
	}
	/**
	 * Show message in the dialog
	 * @param buf the message
	 * @param off the offset of the message
	 * @param len the length of the message
	 */
	public void write(byte[] buf,int off,int len){
		area.append(new String(buf,off,len));
		area.setCaretPosition(area.getText().length());
		dia.setVisible(true);
		dia.requestFocus();
	}
	/**
	 * Set the console visible
	 */
	public void show(){
		dia.setVisible(true);
		dia.requestFocus();
	}
}