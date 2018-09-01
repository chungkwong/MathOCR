/* InftyCDB2.java
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
package net.sf.mathocr.test;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.text.*;
import java.util.*;
import javax.imageio.*;
import javax.swing.*;
import net.sf.mathocr.common.*;
import net.sf.mathocr.ocr.*;
import net.sf.mathocr.ofr.*;
import static net.sf.mathocr.Environment.env;
/**
 * Test structural analysis of mathematical expression
 */
public final class InftyCDB2 extends Box implements ActionListener{
	private static final int NUMBER_OF_TYPES=8;
	JLabel pic=new JLabel(),cnt=new JLabel();
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
	Scanner scan;
	File olddatafile,newdatafile,logfile;
	DataInputStream in;
	DataOutputStream out;
	PrintWriter log;
	DataBase db=new DataBase();
	DecimalFormat format=new DecimalFormat("0000");
	int index=1;
	int[] count=new int[NUMBER_OF_TYPES],corr=new int[NUMBER_OF_TYPES];
	long timeUsed=0;
	String result;
	HashSet<String> nextPosEx=new HashSet<String>(),nextNegEx=new HashSet<String>();
	boolean[] nextType=new boolean[NUMBER_OF_TYPES-2];
	public InftyCDB2(){
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
		olddatafile=new File("/home/kwong/projects/MathOCR/datasets/database/index.dat");
		try{
			if(olddatafile.exists())
				in=new DataInputStream(new FileInputStream(olddatafile));
			newdatafile=new File("/home/kwong/projects/MathOCR/datasets/database/index2.dat");
			newdatafile.createNewFile();
			out=new DataOutputStream(new FileOutputStream(newdatafile));
			log=new PrintWriter(new FileWriter("/home/kwong/projects/MathOCR/datasets/database/result.log",true));
			scan=new Scanner(new File("/home/kwong/projects/MathOCR/datasets/database/result3.csv"));
			scan.useDelimiter("[\t\n]");
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
		while(index<=4400){
			File file=new File("/home/kwong/projects/MathOCR/datasets/database/img/img"+format.format(index)+".gif");
			BufferedImage img=null;
			scan.next();
			if(scan.nextInt()!=index){
				System.err.println(index);
				throw new RuntimeException("Input error");
			}
			ArrayList<Char> characters=new ArrayList<Char>();
			try{
				while(scan.hasNextInt()){
					int left=scan.nextInt(),top=scan.nextInt(),right=scan.nextInt(),bottom=scan.nextInt();
					String name=scan.next();
					Symbol sym=db.getSymbol(name);
					if(sym==null){
						if(name.equals("\\varlimsup ")){
							sym=new Symbol(name,name+"\tC",right-left+1,bottom-top+1,right-left+1,(bottom-top+1)*3/2,0,bottom-top+1,bottom-top+1);
						}else
							sym=new Symbol(name,name+"\tC",right-left+1,bottom-top+1,right-left+1,bottom-top+1,0,bottom-top+1,bottom-top+1);
					}
					characters.add(new Char(sym,left,right,top,bottom));
				}
			}catch(Exception ex){
				ex.printStackTrace();
				continue;
			}
			boolean history=false;
			if(in!=null)
				try{history=(in.readInt()==index);}catch(Exception ex){}
			long t=System.currentTimeMillis();
			result=new LogicalLine(new CharactersLine(characters),true).recognize();
			timeUsed+=(System.currentTimeMillis()-t);
			if(history){
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
			try{
				img=ImageIO.read(file);
			}catch(Exception ex){
				ex.printStackTrace();
				continue;
			}
			pic.setIcon(new ImageIcon(img));
			recognized.setText(result);
			expected.setText("");
			for(String eg:nextPosEx)
				expected.append(eg+"\n");
			unexpected.setText("");
			for(String eg:nextNegEx)
				unexpected.append(eg+"\n");
			cnt.setText(Integer.toString(index));
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
					out.writeInt(index);
					for(int i=0;i<nextType.length;i++)
						out.writeBoolean(type[i].isSelected());
					for(String str:nextPosEx)
						out.writeUTF(str);
					out.writeUTF("$");
					for(String str:nextNegEx)
						out.writeUTF(str);
					out.writeUTF("$");
					while(true){
						out.writeInt(in.readInt());
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
	}
	void writeEntry(boolean diag){
		try{
			boolean simple=true;
			out.writeInt(index);
			log.println(index);
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
	public static void main(String[] args){
		JFrame f=new JFrame("MathOCR test");
		f.add(new JScrollPane(new InftyCDB2()),BorderLayout.CENTER);
		f.setSize(800,600);
		f.setExtendedState(JFrame.MAXIMIZED_BOTH);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);
	}
}