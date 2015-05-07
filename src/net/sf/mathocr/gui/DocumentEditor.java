/* DocumentEditor.java
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
 * A GUI component being used to show a document
 */
public final class DocumentEditor extends JPanel implements ActionListener,MouseListener,Runnable{
	Document doc;
	JLabel pageLabel=new JLabel();
	//JTree tree=new JTree();
	//JPanel pagePane=new JPanel();
	PageEditor pageEdit;
	int currPageNo=0;
	JComboBox<String> format=new JComboBox<String>(new String[]{"LaTeX","HTML"});
	JTextArea output;
	JPopupMenu popup=new JPopupMenu();
	JMenuItem save=new JMenuItem(env.getTranslation("SAVE")),preview=new JMenuItem(env.getTranslation("PREVIEW"));
	public DocumentEditor(Document doc){
		super(new BorderLayout());
		this.doc=doc;
		//setLeftComponent(tree);
		//setLayout(new BorderLayout());
		//setRightComponent(pagePane);
		if(env.getBoolean("SKIP_PREPROCESS_PANE")&&env.getBoolean("SKIP_COMPONENT_PANE")
		&&env.getBoolean("SKIP_LAYOUT_PANE")&&env.getBoolean("SKIP_RECOGNIZE_PANE")){
			add(pageLabel,BorderLayout.NORTH);
			new Thread(this).start();
		}else{
			pageEdit=new PageEditor(this);
			add(pageEdit,BorderLayout.CENTER);
			add(pageLabel,BorderLayout.NORTH);
			nextPage();
		}
	}
	public void nextPage(){
		if(currPageNo<doc.getNumberOfPage()){
			Page page=doc.getPage(currPageNo++);
			pageLabel.setText(env.getTranslation("PAGE")+(currPageNo)+"/"+doc.getNumberOfPage()+":"+page.getSource().toString());
			pageEdit.setPage(page);
		}else{
			this.removeAll();
			//JPanel resultPane=new JPanel();
			//resultPane.setLayout(new BorderLayout());
			add(format,BorderLayout.NORTH);
			save.addActionListener(this);
			save.setActionCommand("SAVE");
			popup.add(save);
			preview.addActionListener(this);
			preview.setActionCommand("PREVIEW");
			popup.add(preview);
			output=new JTextArea();
			output.setLineWrap(true);
			output.setWrapStyleWord(true);
			output.addMouseListener(this);
			add(new JScrollPane(output),BorderLayout.CENTER);
			format.addActionListener(this);
			if(env.getString("OUTPUT_FORMAT").toUpperCase().equals("HTML")){
				format.setSelectedIndex(1);
			}else{
				format.setSelectedIndex(0);
			}
			//setRightComponent(resultPane);
		}
	}
	public Document getDocument(){
		return doc;
	}
	public void actionPerformed(ActionEvent e){
		if(e.getActionCommand().equals("SAVE")){
			JFileChooser jfc=new JFileChooser();
			if(format.getSelectedIndex()==0)
				jfc.setFileFilter(new FileNameExtensionFilter(env.getTranslation("TEX_FILE"),"tex"));
			else
				jfc.setFileFilter(new FileNameExtensionFilter(env.getTranslation("HTML_FILE"),"html"));
			if(jfc.showSaveDialog(this)==JFileChooser.APPROVE_OPTION)
				try{
					output.write(new OutputStreamWriter(new FileOutputStream(jfc.getSelectedFile()),"UTF-8"));
				}catch(Exception ex){
					ex.printStackTrace();
				}
		}else if(e.getActionCommand().equals("PREVIEW")){
			JFrame dia=new JFrame();
			dia.setLayout(new BorderLayout());
			dia.add(new JScrollPane(new JEditorPane("text/html",output.getText())),BorderLayout.CENTER);
			dia.setExtendedState(JFrame.MAXIMIZED_BOTH);
			dia.setVisible(true);
		}else{
			if(format.getSelectedIndex()==0){
				output.setText(new LaTeXGenerator(doc).toString());
				preview.setEnabled(false);
			}else if(format.getSelectedIndex()==1){
				output.setText(new HTMLGenerator(doc).toString());
				preview.setEnabled(true);
			}
			revalidate();
		}
	}
	public void mouseClicked(MouseEvent e){}
	public void mouseEntered(MouseEvent e){}
	public void mouseExited(MouseEvent e){}
	public void mousePressed(MouseEvent e){
		if(e.isPopupTrigger())
			popup.show(e.getComponent(),e.getX(),e.getY());
	}
	public void mouseReleased(MouseEvent e){}
	public void run(){
		while(currPageNo<doc.getNumberOfPage()){
			Page page=doc.getPage(currPageNo++);
			String prefix=env.getTranslation("PAGE")+(currPageNo)+"/"+doc.getNumberOfPage()+":"+page.getSource().toString()+"-";
			pageLabel.setText(prefix+env.getTranslation("PREPROCESS"));
			page.load();
			page.preprocessByDefault();
			pageLabel.setText(prefix+env.getTranslation("COMPONENT_ANALYSIS"));
			page.componentAnalysis();
			pageLabel.setText(prefix+env.getTranslation("PHYSICAL_LAYOUT_ANALYSIS"));
			page.segment(new XYCut());
			page.regionClassify();
			page.readingOrderSort();
			pageLabel.setText(prefix+env.getTranslation("LOGICAL_LAYOUT_ANALYSIS"));
			page.produceLogicalBlock();
		}
		nextPage();
	}
}