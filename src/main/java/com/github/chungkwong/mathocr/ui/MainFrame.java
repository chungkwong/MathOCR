/* MainFrame.java
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
import com.github.chungkwong.mathocr.ui.StringViewer;
import com.github.chungkwong.mathocr.ui.FontTrainingPane;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import static com.github.chungkwong.mathocr.Environment.ENVIRONMENT;
/**
 * Factory to construct the main frame of the GUI interface
 */
public final class MainFrame implements ActionListener,WindowListener,Runnable{
	private final JFileChooser fileChooser=new JFileChooser();
	private final ErrorConsole errConsole;
	private final JTabbedPane pane=new JTabbedPane();
	private final JLabel mem=new JLabel();
	private final JFrame f=new JFrame("MathOCR");
	public MainFrame(){
		errConsole=new ErrorConsole();
		f.setTitle("MathOCR");
		f.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("icon.png")));
		fileChooser.setFileHidingEnabled(false);
		fileChooser.setMultiSelectionEnabled(true);
		JMenuBar jmb=new JMenuBar();
		JMenu newmenu=new JMenu(ENVIRONMENT.getTranslation("FILE"));
		addMenuItem("DOCUMENT_RECOGNITION",newmenu);
		addMenuItem("FORMULA_RECOGNITION",newmenu);
		newmenu.addSeparator();
		addMenuItem("PREFERENCE",newmenu);
		newmenu.addSeparator();
		addMenuItem("CLOSE_TAB",newmenu);
		addMenuItem("CLOSE_ALL_TAB",newmenu);
		newmenu.addSeparator();
		addMenuItem("EXIT",newmenu);
		jmb.add(newmenu);
		JMenu devmenu=new JMenu(ENVIRONMENT.getTranslation("DEV_TOOL"));
		addMenuItem("FONT_TRAINING",devmenu);
		addMenuItem("SINGLE_CHARACTER_RECOGNITION",devmenu);
		devmenu.addSeparator();
		addMenuItem("FONT_VIEWER",devmenu);
		addMenuItem("REGISTER_FONT",devmenu);
		jmb.add(devmenu);
		JMenu helpmenu=new JMenu(ENVIRONMENT.getTranslation("HELP"));
		addMenuItem("DOCUMENT",helpmenu);
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
		JMenuItem item=new JMenuItem(ENVIRONMENT.getTranslation(str));
		item.setActionCommand(str);
		item.addActionListener(this);
		menu.add(item);
	}
	public void actionPerformed(ActionEvent e){
		String command=e.getActionCommand();
		switch(command){
			case "DOCUMENT_RECOGNITION":
				JPanel master=new JPanel(new BorderLayout());
				master.add(new SourceInspector(master));
				pane.addTab(ENVIRONMENT.getTranslation("DOCUMENT_RECOGNITION"),master);
				pane.setSelectedIndex(pane.getComponentCount()-1);
				break;
			case "FORMULA_RECOGNITION":
				pane.addTab(ENVIRONMENT.getTranslation("FORMULA_RECOGNITION"),new MathInspector());
				pane.setSelectedIndex(pane.getComponentCount()-1);
				break;
			case "FONT_TRAINING":
				//pane.addTab(env.getTranslation("FONT_TRAINING"),new TrainPane());
				pane.addTab(ENVIRONMENT.getTranslation("FONT_TRAINING"),new FontTrainingPane());
				pane.setSelectedIndex(pane.getComponentCount()-1);
				break;
			case "CLOSE_TAB":
				pane.removeTabAt(pane.getSelectedIndex());
				break;
			case "CLOSE_ALL_TAB":
				while(pane.getTabCount()>0){
					pane.removeTabAt(pane.getSelectedIndex());
				}
				break;
			case "EXIT":
				if(JOptionPane.showConfirmDialog(null,ENVIRONMENT.getTranslation("EXIT_CONFIRM"),ENVIRONMENT.getTranslation("EXIT"),JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION){
					Runtime.getRuntime().exit(0);
				}
				break;
			case "DOCUMENT":
				pane.addTab(ENVIRONMENT.getTranslation("DOCUMENT"),new Helper());
				pane.setSelectedIndex(pane.getComponentCount()-1);
				break;
			case "ERR_MSG":
				errConsole.show();
				break;
			case "PREFERENCE":
				pane.addTab(ENVIRONMENT.getTranslation("PREFERENCE"),new JScrollPane(new PreferenceEditor()));
				pane.setSelectedIndex(pane.getComponentCount()-1);
				break;
			case "FONT_VIEWER":
				pane.addTab(ENVIRONMENT.getTranslation("FONT_VIEWER"),new StringViewer());
				pane.setSelectedIndex(pane.getComponentCount()-1);
				break;
			case "REGISTER_FONT":
				fileChooser.setMultiSelectionEnabled(true);
				if(fileChooser.showOpenDialog(null)==JFileChooser.APPROVE_OPTION){
					Arrays.stream(fileChooser.getSelectedFiles()).forEach((file)->{
						try{
							GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(
									Font.createFont(file.getName().endsWith(".ttf")||file.getName().endsWith(".otf")?Font.TRUETYPE_FONT:Font.TYPE1_FONT,file));
						}catch(Exception ex){
							ex.printStackTrace();
						}
					});
				}
				break;
			case "SINGLE_CHARACTER_RECOGNITION":
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				fileChooser.setMultiSelectionEnabled(false);
				if(fileChooser.showOpenDialog(null)==JFileChooser.APPROVE_OPTION){
					pane.addTab(ENVIRONMENT.getTranslation("SINGLE_CHARACTER_RECOGNITION"),new JScrollPane(new SingleCharacterPane(fileChooser.getSelectedFile())));
					pane.setSelectedIndex(pane.getComponentCount()-1);
				}
				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				fileChooser.setMultiSelectionEnabled(true);
				break;
			default:
				break;
		}
	}
	public void windowActivated(WindowEvent e){
	}
	public void windowClosed(WindowEvent e){
	}
	public void windowClosing(WindowEvent e){
		ENVIRONMENT.saveAsPreference();
	}
	public void windowDeactivated(WindowEvent e){
	}
	public void windowDeiconified(WindowEvent e){
	}
	public void windowIconified(WindowEvent e){
	}
	public void windowOpened(WindowEvent e){
	}
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
