/* MainFrame.java
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
import java.io.*;
import javax.swing.*;
import javax.swing.filechooser.*;
import net.sf.mathocr.*;
import net.sf.mathocr.layout.*;
import static net.sf.mathocr.Environment.env;
/**
 * Factory to construct the main frame of the GUI interface
 */
public final class MainFrame implements ActionListener,WindowListener,Runnable{
	JFileChooser fileChooser=new JFileChooser();
	ErrorConsole errConsole;
	JTabbedPane pane=new JTabbedPane();
	JLabel mem=new JLabel();
	JFrame f=new JFrame("MathOCR");
	public MainFrame(){
		errConsole=new ErrorConsole();
		f.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/net/sf/mathocr/resources/icon.png")));
		fileChooser.setMultiSelectionEnabled(true);
		JMenuBar jmb=new JMenuBar();
		JMenu newmenu=new JMenu(env.getTranslation("FILE"));
		addMenuItem("DOCUMENT_RECOGNITION",newmenu);
		addMenuItem("FORMULA_RECOGNITION",newmenu);
		addMenuItem("FORMULA_RECOGNITION_SCREEN",newmenu);
		newmenu.addSeparator();
		addMenuItem("PREFERENCE",newmenu);
		newmenu.addSeparator();
		addMenuItem("CLOSE_TAB",newmenu);
		addMenuItem("CLOSE_ALL_TAB",newmenu);
		newmenu.addSeparator();
		addMenuItem("EXIT",newmenu);
		jmb.add(newmenu);
		JMenu devmenu=new JMenu(env.getTranslation("DEV_TOOL"));
		addMenuItem("FONT_TRAINING",devmenu);
		devmenu.addSeparator();
		addMenuItem("MICROSCOPE",devmenu);
		devmenu.addSeparator();
		//addMenuItem("OCR_TEST",devmenu);
		addMenuItem("OFR_TEST",devmenu);
		jmb.add(devmenu);
		JMenu helpmenu=new JMenu(env.getTranslation("HELP"));
		addMenuItem("DOCUMENT",helpmenu);
		addMenuItem("SOURCE_CODE",helpmenu);
		addMenuItem("ERR_MSG",helpmenu);
		jmb.add(helpmenu);
		jmb.add(Box.createHorizontalGlue());
		jmb.add(mem);
		f.setJMenuBar(jmb);
		//pane.addTab(env.getTranslation("DOCUMENT_RECOGNITION"),recognizePane);
		//
		//
		f.add(pane,BorderLayout.CENTER);
		f.addWindowListener(this);
		f.setSize(800,600);
		f.setExtendedState(JFrame.MAXIMIZED_BOTH);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);
		new Thread(this).start();
	}
	private void addMenuItem(String str,JMenu menu){
		JMenuItem item=new JMenuItem(env.getTranslation(str));
		item.setActionCommand(str);
		item.addActionListener(this);
		menu.add(item);
	}
	public void actionPerformed(ActionEvent e){
		String command=e.getActionCommand();
		if(command.equals("DOCUMENT_RECOGNITION")){
			fileChooser.setFileFilter(new FileNameExtensionFilter(env.getTranslation("PICTURE_FILE"),"png","jpg","jpeg","bmp","gif","PNG","JPG","JPEG","BMP","GIF","ppm","pgm","pbm"));
			if(fileChooser.showOpenDialog(null)==JFileChooser.APPROVE_OPTION){
				Document doc=new Document();
				for(File file:fileChooser.getSelectedFiles())
					doc.addPage(new Page(file,doc));
				pane.addTab(env.getTranslation("DOCUMENT_RECOGNITION"),new DocumentEditor(doc));
				pane.setSelectedIndex(pane.getComponentCount()-1);
			}
		}else if(command.equals("FORMULA_RECOGNITION")){
			fileChooser.setFileFilter(new FileNameExtensionFilter(env.getTranslation("PICTURE_FILE"),"png","jpg","jpeg","bmp","gif","PNG","JPG","JPEG","BMP","GIF","ppm","pgm","pbm"));
			if(fileChooser.showOpenDialog(null)==JFileChooser.APPROVE_OPTION){
				showFormulaPane(fileChooser.getSelectedFiles());
			}
		}else if(command.equals("FORMULA_RECOGNITION_SCREEN")){
			new ScreenCapture(this);
		}else if(command.equals("FONT_TRAINING")){
			pane.addTab(env.getTranslation("FONT_TRAINING"),new TrainPane());
			pane.setSelectedIndex(pane.getComponentCount()-1);
		}else if(command.equals("CLOSE_TAB")){
			pane.removeTabAt(pane.getSelectedIndex());
		}else if(command.equals("CLOSE_ALL_TAB")){
			while(pane.getTabCount()>0)
				pane.removeTabAt(pane.getSelectedIndex());
		}else if(command.equals("EXIT")){
			if(JOptionPane.showConfirmDialog(null,env.getTranslation("EXIT_CONFIRM"),env.getTranslation("EXIT"),JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION)
				Runtime.getRuntime().exit(0);
		}else if(command.equals("DOCUMENT")){
			pane.addTab(env.getTranslation("DOCUMENT"),new Helper());
			pane.setSelectedIndex(pane.getComponentCount()-1);
		}else if(command.equals("SOURCE_CODE")){
			pane.addTab(env.getTranslation("SOURCE_CODE"),new SourceViewer());
			pane.setSelectedIndex(pane.getComponentCount()-1);
		}else if(command.equals("ERR_MSG")){
			errConsole.show();
		}else if(command.equals("PREFERENCE")){
			pane.addTab(env.getTranslation("PREFERENCE"),new JScrollPane(new PreferenceEditor()));
			pane.setSelectedIndex(pane.getComponentCount()-1);
		}else if(command.equals("MICROSCOPE")){
			pane.addTab(env.getTranslation("MICROSCOPE"),new CharacterViewer());
			pane.setSelectedIndex(pane.getComponentCount()-1);
		}else if(command.equals("OFR_TEST")){
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			fileChooser.setMultiSelectionEnabled(false);
			if(fileChooser.showOpenDialog(null)==JFileChooser.APPROVE_OPTION){
				pane.addTab(env.getTranslation("OFR_TEST"),new JScrollPane(new OFRTester(fileChooser.getSelectedFile())));
				pane.setSelectedIndex(pane.getComponentCount()-1);
			}
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fileChooser.setMultiSelectionEnabled(true);
		}
	}
	void showFormulaPane(File[] files){
		pane.addTab(env.getTranslation("FORMULA_RECOGNITION"),new FormulaPane(files));
		pane.setSelectedIndex(pane.getComponentCount()-1);
	}
	public void windowActivated(WindowEvent e){}
	public void windowClosed(WindowEvent e){}
	public void windowClosing(WindowEvent e){
		env.saveAsPreference();
	}
	public void windowDeactivated(WindowEvent e){}
	public void windowDeiconified(WindowEvent e){}
	public void windowIconified(WindowEvent e){}
	public void windowOpened(WindowEvent e){}
	public void run(){
		Runtime runtime=Runtime.getRuntime();
		while(true){
			mem.setText((runtime.freeMemory()>>20)+"MB/"+(runtime.totalMemory()>>20)+"MB");
			try{
				Thread.sleep(1000);
			}catch(Exception ex){
				mem.setText("");
				break;
			}
		}
	}
}