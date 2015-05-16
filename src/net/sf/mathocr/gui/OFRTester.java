/* OFRTester.java
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
import java.awt.image.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.imageio.*;
import javax.swing.*;
import net.sf.mathocr.layout.*;
import net.sf.mathocr.ocr.*;
import net.sf.mathocr.ofr.*;
import static net.sf.mathocr.Environment.env;
/**
 * A GUI component being used to test optical formula recognition result using image in a directory
 */
public final class OFRTester extends Box implements ActionListener,FileFilter{
	private static final int NUMBER_OF_TYPES=8;
	JLabel pic=new JLabel(),cnt=new JLabel();
	String[] extensions=new String[]{".png",".jpg",".jpeg",".bmp",".gif",".PNG",".JPG",".JPEG",".BMP",".GIF",".ppm",".pgm",".pbm"};
	String[] tName=new String[]{env.getTranslation("TOTAL"),env.getTranslation("SIMPLE_EXPR"),env.getTranslation("HAT_EXPR")
	,env.getTranslation("RADICAL_EXPR"),env.getTranslation("FRACTION_EXPR"),env.getTranslation("MATRIX_EXPR")
	,env.getTranslation("OPERATOR_EXPR"),env.getTranslation("MULTILINE_EXPR")};
	String timeString=env.getTranslation("TIME_USED"),perString=env.getTranslation("FORMULA_PER_SECOND");
	JCheckBox[] type=new JCheckBox[]{new JCheckBox(tName[2]),new JCheckBox(tName[3]),new JCheckBox(tName[4]),new JCheckBox(tName[5])
	,new JCheckBox(tName[6]),new JCheckBox(tName[7])};
	JTextArea recognized=new JTextArea(),expected=new JTextArea(),unexpected=new JTextArea();
	JButton correct=new JButton(env.getTranslation("CORRECT")),wrong=new JButton(env.getTranslation("WRONG")),exit=new JButton(env.getTranslation("EXIT"));
	JLabel[] info=new JLabel[NUMBER_OF_TYPES];
	JLabel timeinfo=new JLabel();
	File olddatafile,newdatafile,logfile;
	DataInputStream in;
	DataOutputStream out;
	PrintWriter log;
	DataBase db=new DataBase();
	int index=0;
	int[] count=new int[NUMBER_OF_TYPES],corr=new int[NUMBER_OF_TYPES];
	long timeUsed=0;
	String nextFile,result;
	HashSet<String> nextPosEx=new HashSet<String>(),nextNegEx=new HashSet<String>();
	boolean[] nextType=new boolean[NUMBER_OF_TYPES-2];
	File[] pics;
	public OFRTester(File dir){
		super(BoxLayout.Y_AXIS);
		correct.setAlignmentX(0);
		correct.setActionCommand("CORRECT");
		correct.addActionListener(this);
		add(correct);
		wrong.setAlignmentX(0);
		wrong.setActionCommand("WRONG");
		wrong.addActionListener(this);
		add(wrong);
		exit.setAlignmentX(0);
		exit.setActionCommand("EXIT");
		exit.addActionListener(this);
		add(exit);
		pic.setAlignmentX(0);
		add(cnt);
		add(pic);
		JLabel lReg=new JLabel(env.getTranslation("RECOGNITION_RESULT"));
		lReg.setAlignmentX(0);
		add(lReg);
		recognized.setEditable(false);
		recognized.setAlignmentX(0);
		add(recognized);
		JLabel lExp=new JLabel(env.getTranslation("EXPECTED_RESULT"));
		lExp.setAlignmentX(0);
		add(lExp);
		expected.setEditable(false);
		expected.setAlignmentX(0);
		add(expected);
		JLabel lUnExp=new JLabel(env.getTranslation("UNEXPECTED_RESULT"));
		lUnExp.setAlignmentX(0);
		add(lUnExp);
		unexpected.setEditable(false);
		unexpected.setAlignmentX(0);
		add(unexpected);
		JLabel lType=new JLabel(env.getTranslation("TYPE"));
		lType.setAlignmentX(0);
		add(lType);
		for(JCheckBox box:type){
			box.setAlignmentX(0);
			add(box);
		}
		for(int i=0;i<NUMBER_OF_TYPES;i++){
			info[i]=new JLabel();
			info[i].setAlignmentX(0);
			add(info[i]);
		}
		add(timeinfo);
		pics=dir.listFiles(this);
		Arrays.sort(pics);
		olddatafile=new File(dir.getPath()+System.getProperty("file.separator")+"index.dat");
		try{
			if(olddatafile.exists())
				in=new DataInputStream(new FileInputStream(olddatafile));
			newdatafile=new File(dir.getPath()+System.getProperty("file.separator")+"index2.dat");
			newdatafile.createNewFile();
			out=new DataOutputStream(new FileOutputStream(newdatafile));
			log=new PrintWriter(new FileWriter(dir.getPath()+System.getProperty("file.separator")+"result.log",true));
			loadNext();
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	void updateInfo(){
		for(int i=0;i<NUMBER_OF_TYPES;i++)
			info[i].setText(tName[i]+"\t"+corr[i]+"/"+count[i]+"("+(corr[i]/(count[i]+0.00001))+"%)");
		timeinfo.setText(timeString+timeUsed+"ms ("+(index*1000/(timeUsed+0.001))+perString+")");
	}
	void loadNext(){
		updateInfo();
		for(int i=index;i<pics.length;i++){
			File file=pics[i];
			BufferedImage img=null;
			try{
				img=ImageIO.read(file);
			}catch(Exception ex){
				ex.printStackTrace();
				continue;
			}
			if(in!=null&&nextFile==null)
				try{nextFile=in.readUTF();}catch(Exception ex){nextFile=null;}
			while(nextFile!=null&&nextFile.compareTo(file.getName())<0){
				readEntry();
				try{nextFile=in.readUTF();}catch(Exception ex){nextFile=null;}
			}
			long t=System.currentTimeMillis();
			Page page=new Page(img,null);
			page.load();
			page.preprocessByDefault();
			page.componentAnalysis();
			CharactersLine line=new CharactersLine(page.getComponentPool().getComponents());
			result=new LogicalLine(line,env.getBoolean("DISPLAY_EQUATION")).recognize();
			timeUsed+=(System.currentTimeMillis()-t);
			if(file.getName().equals(nextFile)){
				readEntry();
				if(nextPosEx.contains(result)){
					writeEntry(true);
					continue;
				}else if(nextNegEx.contains(result)){
					writeEntry(false);
					continue;
				}
			}else{
				nextPosEx.clear();
				nextNegEx.clear();
				for(int j=0;j<nextType.length;j++)
					type[j].setSelected(false);
			}
			pic.setIcon(new ImageIcon(img));
			recognized.setText(result);
			expected.setText("");
			for(String eg:nextPosEx)
				expected.append(eg+"\n");
			unexpected.setText("");
			for(String eg:nextNegEx)
				unexpected.append(eg+"\n");
			cnt.setText(file.getName());
			return;
		}
		correct.setEnabled(false);
		wrong.setEnabled(false);
		exit.setEnabled(false);
		try{
			if(in!=null){
				in.close();
				olddatafile.delete();
			}
			out.close();
			log.close();
			newdatafile.renameTo(olddatafile);
			updateInfo();
			JOptionPane.showMessageDialog(null,env.getTranslation("FINISHED"));
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	void pause(){
		correct.setEnabled(false);
		wrong.setEnabled(false);
		exit.setEnabled(false);
		try{
			if(in!=null){
				try{
					out.writeUTF(pics[index].getName());
					for(int i=0;i<nextType.length;i++)
						out.writeBoolean(type[i].isSelected());
					for(String str:nextPosEx)
						out.writeUTF(str);
					out.writeUTF("$");
					for(String str:nextNegEx)
						out.writeUTF(str);
					out.writeUTF("$");
					while(true){
						out.writeUTF(in.readUTF());
						for(int i=0;i<nextType.length;i++)
							out.writeBoolean(in.readBoolean());
						String str;
						while(!(str=in.readUTF()).equals("$"))
							out.writeUTF(str);
						out.writeUTF("$");
						while(!(str=in.readUTF()).equals("$"))
							out.writeUTF(str);
						out.writeUTF("$");
					}
				}catch(Exception ex){}
				in.close();
				olddatafile.delete();
			}
			out.close();
			log.close();
			newdatafile.renameTo(olddatafile);
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	void readEntry(){
		nextPosEx.clear();
		nextNegEx.clear();
		try{
			for(int i=0;i<nextType.length;i++)
				type[i].setSelected(in.readBoolean());
			String str;
			while(!(str=in.readUTF()).equals("$"))
				nextPosEx.add(str);
			while(!(str=in.readUTF()).equals("$"))
				nextNegEx.add(str);
		}catch(Exception ex){
			ex.printStackTrace();
		}
		nextFile=null;
	}
	void writeEntry(boolean diag){
		try{
			boolean simple=true;
			out.writeUTF(pics[index].getName());
			log.println(pics[index].getName());
			++count[0];
			for(int i=0;i<type.length;i++){
				out.writeBoolean(type[i].isSelected());
				if(type[i].isSelected()){
					++count[i+2];
					simple=false;
				}
			}
			if(simple)
				++count[1];
			if(diag){
				++corr[0];
				for(int i=0;i<type.length;i++)
					if(type[i].isSelected())
						++corr[i+2];
				if(simple)
					++corr[1];
				nextPosEx.add(result);
				log.println("Correct");
			}else{
				nextNegEx.add(result);
				log.println("Wrong");
			}
			log.println(result);
			for(String eg:nextPosEx)
				out.writeUTF(eg);
			out.writeUTF("$");
			for(String eg:nextNegEx)
				out.writeUTF(eg);
			out.writeUTF("$");
			++index;
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	public void actionPerformed(ActionEvent e){
		if(e.getActionCommand().equals("EXIT")){
			pause();
		}else{
			writeEntry(e.getActionCommand().equals("CORRECT"));
			loadNext();
		}
	}
	public boolean accept(File pathname){
		String name=pathname.getName();
		for(String extension:extensions)
			if(name.endsWith(extension))
				return true;
		return false;
	}
}