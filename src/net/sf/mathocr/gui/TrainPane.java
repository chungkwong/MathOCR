/* TrainPane.java
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
import java.awt.event.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import javax.imageio.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.filechooser.*;
import net.sf.mathocr.common.*;
import net.sf.mathocr.preprocess.*;
import static net.sf.mathocr.Environment.env;
/**
 * A GUI component being used to train fonts
 */
public final class TrainPane extends JSplitPane implements ActionListener{
	JFileChooser fileChooser=new JFileChooser();
	JCheckBox useMeanFilter=new JCheckBox(env.getTranslation("MEAN_FILTER"));
	JCheckBox useMedianFilter=new JCheckBox(env.getTranslation("MEDIAN_FILTER"));
	JCheckBox useKfillFilter=new JCheckBox("kfill");
	JCheckBox useNoiseFilter=new JCheckBox(env.getTranslation("NOISE_REMOVE"));
	JSpinner sizeIn=new JSpinner(new SpinnerNumberModel(40,1,255,1)),kIn=new JSpinner(new SpinnerNumberModel(3,3,255,2));
	JRadioButton useOtsu=new JRadioButton(env.getTranslation("OTSU_METHOD"),false);
	JRadioButton useSauvola=new JRadioButton(env.getTranslation("SAUVOLA_METHOD"),true);
	JSpinner winIn=new JSpinner(new SpinnerNumberModel(15,3,255,2)),weightIn=new JSpinner(new SpinnerNumberModel(0.2,-1.0,1.0,0.01));
	JRadioButton useProvided=new JRadioButton(env.getTranslation("MANUAL"),false);
	JSpinner limIn=new JSpinner(new SpinnerNumberModel(192,0,255,1));
	JCheckBox showTable=new JCheckBox(env.getTranslation("SHOW_DATA"));
	JCheckBox genPic=new JCheckBox(env.getTranslation("GEN_PICTURE"));
	JTable table;
	DefaultTableModel model;
	public TrainPane(){
		super(JSplitPane.HORIZONTAL_SPLIT);
		fileChooser.setMultiSelectionEnabled(true);
		fileChooser.setFileFilter(new FileNameExtensionFilter(env.getTranslation("FONT_FILE"),"ttf","otf"));
		Box ctrl=Box.createVerticalBox();
		JButton showFontChooser=new JButton(env.getTranslation("CHOOSE_FONT"));
		showFontChooser.addActionListener(this);
		ctrl.add(showFontChooser);
		ctrl.add(new JLabel(env.getTranslation("FONT_SIZE")));
		sizeIn.setAlignmentX(0);
		sizeIn.setMaximumSize(sizeIn.getPreferredSize());
		ctrl.add(sizeIn);
		ctrl.add(new JLabel(env.getTranslation("FILTER")));
		ctrl.add(useMeanFilter);
		ctrl.add(useMedianFilter);
		ctrl.add(useKfillFilter);
		kIn.setMaximumSize(kIn.getPreferredSize());
		kIn.setAlignmentX(0);
		ctrl.add(kIn);
		ctrl.add(useNoiseFilter);
		ctrl.add(new JLabel(env.getTranslation("THREHOLD_METHOD")));
		ButtonGroup threMethods=new ButtonGroup();
		threMethods.add(useOtsu);
		threMethods.add(useSauvola);
		threMethods.add(useProvided);
		ctrl.add(useOtsu);
		ctrl.add(useSauvola);
		winIn.setMaximumSize(limIn.getPreferredSize());
		winIn.setAlignmentX(0);
		ctrl.add(winIn);
		weightIn.setMaximumSize(limIn.getPreferredSize());
		weightIn.setAlignmentX(0);
		ctrl.add(weightIn);
		ctrl.add(useProvided);
		limIn.setMaximumSize(limIn.getPreferredSize());
		limIn.setAlignmentX(0);
		ctrl.add(limIn);
		ctrl.add(showTable);
		ctrl.add(genPic);
		model=new DefaultTableModel(new String[]{env.getTranslation("TYPE"),env.getTranslation("CHARACTER"),env.getTranslation("TEXTNAME"),env.getTranslation("MATHNAME")
		,env.getTranslation("CHOP"),env.getTranslation("HOLES"),env.getTranslation("ASPECT_RATIO"),env.getTranslation("CENTER_X"),env.getTranslation("CENTER_Y")},0){
			public Class<?> getColumnClass(int columnIndex){
				switch(columnIndex){
					case 0:
						return Character.class;
					case 1:
						return ImageIcon.class;
					case 2:
						return String.class;
					case 3:
						return String.class;
					case 4:
						return String.class;
					case 5:
						return Integer.class;
				}
				return Float.class;
			}
			public boolean isCellEditable(int row,int column){
				return false;
			}
		};
		table=new JTable(model);
		table.setFillsViewportHeight(true);
		setLeftComponent(new JScrollPane(ctrl));
		setRightComponent(new JScrollPane(table));
	}
	public void doTrain(Preprocessor preprocessor){
		boolean show=showTable.isSelected(),pic=genPic.isSelected();
		int ind=0;
		model.setRowCount(0);
		File[] files=fileChooser.getSelectedFiles();
		FontRenderContext context=new FontRenderContext(null,true,true);
		int size=(Integer)sizeIn.getValue();
		for(File file:files){
			Font font;
			StringBuilder builder=new StringBuilder();
			int count=0;
			try{
				if(file.getName().endsWith(".ttf")||file.getName().endsWith(".otf"))
					font=Font.createFont(Font.TRUETYPE_FONT,file);
				else
					font=Font.createFont(Font.TYPE1_FONT,file);
				GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);
				font=font.deriveFont(Float.valueOf(size));
				String infofilename=file.getAbsolutePath();
				infofilename=infofilename.substring(0,infofilename.lastIndexOf('.'))+".nam";
				File infofile=new File(infofilename);
				if(!infofile.exists()){
					PrintWriter out=new PrintWriter(new OutputStreamWriter(new FileOutputStream(infofile),"UTF-8"));
					for(int i=0x0021;i<0xFFFF;i++)
						if(font.canDisplay(i))
							out.printf("0x%04X\t%c\t\n",i,Character.toChars(i)[0]);
					out.close();
					if(JOptionPane.showConfirmDialog(null,env.getTranslation("NAM_GENERATED")+"\n"+infofilename)!=JOptionPane.YES_OPTION)
						continue;
				}
				BufferedReader in=new BufferedReader(new InputStreamReader(new FileInputStream(infofile)));
				File logfile=new File(file.getAbsolutePath()+".lg");
				if(logfile.exists())
					logfile.delete();
				logfile.createNewFile();
				ObjectOutputStream log=new ObjectOutputStream(new FileOutputStream(logfile,true));
				String line,textname;
				while((line=in.readLine())!=null){
					int codepoint=Integer.valueOf(line.substring(2,6),16);
					if(!font.canDisplay(codepoint)){
						System.err.println(file.getName()+"do not contain character"+Integer.toHexString(codepoint));
						continue;
					}
					int tab=line.indexOf("\t",7);
					textname=line.substring(7,tab);
					line=line.substring(tab+1);
					String ch=new String(new int[]{codepoint},0,1);
					if(pic){
						builder.append(ch+" ");
					}
					GlyphVector glyphs=font.createGlyphVector(context,ch);
					Rectangle rect=glyphs.getPixelBounds(context,0,0);
					Rectangle2D rect2=glyphs.getLogicalBounds();
					int width=(int)rect.getWidth()+14;
					int height=(int)rect.getHeight()+14;
					BufferedImage bi=new BufferedImage(width,height,BufferedImage.TYPE_BYTE_GRAY);
					Graphics2D g2d=bi.createGraphics();
					g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
					g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,RenderingHints.VALUE_FRACTIONALMETRICS_ON);
					g2d.setFont(font);
					g2d.setColor(Color.white);
					g2d.fillRect(0,0,width,height);
					g2d.setColor(Color.black);
					g2d.drawString(ch,-(int)rect.getX()+7,-(int)rect.getY()+7);
					g2d.dispose();
					if(show){
						model.addRow(new Object[]{'c',new ImageIcon(bi),textname,line,null,null,null,null,null});
						table.setRowHeight(ind++,bi.getHeight());
					}
					log.writeUTF(line);
					log.writeUTF(textname);
					//int[] pixels=bi.getRGB(0,0,width,height,null,0,width);
					bi=preprocessor.preprocess(bi);
					ComponentPool pool=new ComponentPool(bi);
					pool.combineAsGlyph();
					int left=Integer.MAX_VALUE,top=Integer.MAX_VALUE,right=0,bottom=0;
					for(ConnectedComponent ele:pool.getComponents()){
						if(ele.getLeft()<left)
							left=ele.getLeft();
						if(ele.getTop()<top)
							top=ele.getTop();
						if(ele.getRight()>right)
							right=ele.getRight();
						if(ele.getBottom()>bottom)
							bottom=ele.getBottom();
					}
					//log.writeInt((int)rect.getWidth());
					//log.writeInt((int)rect.getHeight());
					log.writeInt(right-left+1);
					log.writeInt(bottom-top+1);
					log.writeInt((int)rect2.getWidth());
					log.writeInt((int)rect2.getHeight());
					log.writeInt(-(int)rect.getX());
					log.writeInt(-(int)rect.getY());
					log.writeInt(-(int)rect2.getY());
					for(ConnectedComponent ele:pool.getComponents()){
						log.writeUTF("GLYPH");
						log.writeUTF(ele.getHorizontalChar()+";"+ele.getVerticalChar());
						log.writeInt(ele.getLeft()-left);
						log.writeInt(ele.getTop()-top);
						log.writeFloat(ele.getDensity());
						log.writeFloat(ele.getCenterX());
						log.writeFloat(ele.getCenterY());
						log.writeFloat(ele.getCentralMoment(2,0));
						log.writeFloat(ele.getCentralMoment(1,1));
						log.writeFloat(ele.getCentralMoment(0,2));
						//log.writeFloat(ele.getDirection());
						log.writeInt(ele.getNumberOfHoles());
						float[] grid=ele.getGrid();
						for(int i=0;i<9;i++)
							log.writeFloat(grid[i]);
						log.writeObject(ele);
						//log.write(ele.toPixelArray2(),0,ele.getWidth()*ele.getHeight());
						if(show){
							model.addRow(new Object[]{'g',null,null,null,ele.getHorizontalChar()+";"+ele.getVerticalChar(),ele.getNumberOfHoles(),
							ele.getWidth()/(ele.getHeight()+0.0f),ele.getCenterX(),ele.getCenterY()});
							++ind;
						}
					}
					log.writeUTF("END");
				}
				log.flush();
				log.close();
				in.close();
				if(pic){
					JLabel lab=new JLabel(builder.toString());
					lab.setFont(font);
					lab.setBackground(Color.white);
					lab.setOpaque(true);
					Dimension dim=lab.getPreferredSize();
					lab.setSize(dim);
					BufferedImage image=new BufferedImage((int)dim.getWidth(),(int)dim.getHeight(),BufferedImage.TYPE_INT_ARGB);
					Graphics2D g=image.createGraphics();
					lab.paint(g);
					g.dispose();
					ImageIO.write(image,"png",new File(file.getAbsolutePath()+".png"));
				}
			}catch(Exception ex){
				ex.printStackTrace();
				break;
			}
		}
	}
	public void actionPerformed(ActionEvent e){
		if(fileChooser.showOpenDialog(null)==JFileChooser.APPROVE_OPTION){
			ArrayList<Preprocessor> preprocessors=new ArrayList<Preprocessor>(10);
			if(useMeanFilter.isSelected())
				preprocessors.add(new MeanFilter());
			if(useOtsu.isSelected())
				preprocessors.add(new ThreholdOtsu());
			else if(useSauvola.isSelected())
				preprocessors.add(new ThreholdSauvola((Double)weightIn.getValue(),(Integer)winIn.getValue()));
			else
				preprocessors.add(new Threhold((Integer)limIn.getValue()));
			if(useMedianFilter.isSelected())
				preprocessors.add(new MedianFilter());
			if(useKfillFilter.isSelected())
				preprocessors.add(new KFill((Integer)kIn.getValue()));
			if(useNoiseFilter.isSelected())
				preprocessors.add(new NoiseRemove());
			doTrain(new CombinedPreprocessor(preprocessors));
		}
	}
}