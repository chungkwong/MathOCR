/* CharacterViewer.java
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
import javax.swing.event.*;
import javax.swing.filechooser.*;
import net.sf.mathocr.common.*;
import net.sf.mathocr.preprocess.*;
import static net.sf.mathocr.Environment.env;
/**
 * A GUI component being used to display image or character in detail
 */
public final class CharacterViewer extends JSplitPane implements ActionListener,ChangeListener{
	JFileChooser fileChooser=new JFileChooser();
	JCheckBox useMeanFilter=new JCheckBox(env.getTranslation("MEAN_FILTER"));
	JCheckBox useMedianFilter=new JCheckBox(env.getTranslation("MEDIAN_FILTER"));
	JCheckBox useKfillFilter=new JCheckBox("kfill");
	JCheckBox useNoiseFilter=new JCheckBox(env.getTranslation("NOISE_REMOVE"));
	JTextField charIn=new JTextField();
	JSpinner sizeIn=new JSpinner(new SpinnerNumberModel(40,1,255,1)),kIn=new JSpinner(new SpinnerNumberModel(3,3,255,2));
	JRadioButton useOtsu=new JRadioButton(env.getTranslation("OTSU_METHOD"),false);
	JRadioButton useSauvola=new JRadioButton(env.getTranslation("SAUVOLA_METHOD"),true);
	JSpinner winIn=new JSpinner(new SpinnerNumberModel(15,3,255,2)),weightIn=new JSpinner(new SpinnerNumberModel(0.2,-1.0,1.0,0.01));
	JRadioButton useProvided=new JRadioButton(env.getTranslation("MANUAL"),false);
	JSpinner limIn=new JSpinner(new SpinnerNumberModel(192,0,255,1));
	JTextArea rightpane=new JTextArea();
	JScrollPane scrollpane=new JScrollPane(rightpane);
	JComboBox<String> sysfonts=new JComboBox<String>();
	int curr_height=0;
	public CharacterViewer(){
		super(JSplitPane.HORIZONTAL_SPLIT);
		Box ctrl=Box.createVerticalBox();
		charIn.setAlignmentX(0);
		ctrl.add(new JLabel(env.getTranslation("CHARACTER")));
		ctrl.add(charIn);
		ctrl.add(new JLabel(env.getTranslation("FONT_SIZE")));
		sizeIn.setAlignmentX(0);
		sizeIn.setMaximumSize(sizeIn.getPreferredSize());
		ctrl.add(sizeIn);
		ctrl.add(new JLabel(env.getTranslation("CHOOSE_SYS_FONT")));
		//sysfonts=new JComboBox<Font>(GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts());
		for(Font font:GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts())
			sysfonts.addItem(font.getName());
		sysfonts.setActionCommand("SYS_FONT");
		sysfonts.addActionListener(this);
		sysfonts.setAlignmentX(0);
		ctrl.add(sysfonts);
		JButton showFontChooser=new JButton(env.getTranslation("CHOOSE_FONT"));
		showFontChooser.setActionCommand("FONT");
		showFontChooser.addActionListener(this);
		ctrl.add(showFontChooser);
		JButton showPicChooser=new JButton(env.getTranslation("CHOOSE_PICTURE"));
		showPicChooser.setActionCommand("PICTURE");
		showPicChooser.addActionListener(this);
		ctrl.add(showPicChooser);
		ctrl.add(new JSeparator());
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
		setLeftComponent(new JScrollPane(ctrl));
		//rightpane.setFont(Font.getFont(Font.MONOSPACED));
		scrollpane.getVerticalScrollBar().getModel().addChangeListener(this);
		setRightComponent(scrollpane);
	}
	public void display(BufferedImage image){
		int width=image.getWidth(),height=image.getHeight();
		int[] pixels=image.getRGB(0,0,width,height,null,0,width);
		StringBuilder str=new StringBuilder();
		for(int i=0,ind=0;i<height;i++){
			for(int j=0;j<width;j++,ind++){
				str.append((pixels[ind]&0x1)==0?"1":"0");
			}
			str.append("\n");
		}
		ComponentPool pool=new ComponentPool(image);
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
		str.append("\nleft=");
		str.append(left);
		str.append("\nright=");
		str.append(right);
		str.append("\ntop=");
		str.append(top);
		str.append("\nbottom=");
		str.append(bottom);
		str.append("\n");
		for(ConnectedComponent ele:pool.getComponents()){
			int r=ele.getRight(),b=ele.getBottom();
			for(int i=ele.getTop();i<=b;i++){
				for(int j=ele.getLeft();j<=r;j++){
					str.append((pixels[i*width+j]&0x1)==0?"1":"0");
				}
				str.append("\n");
			}
			str.append("\nleft=");
			str.append(ele.getLeft());
			str.append("\nright=");
			str.append(ele.getRight());
			str.append("\ntop=");
			str.append(ele.getTop());
			str.append("\nbottom=");
			str.append(ele.getBottom());
			str.append("\n");
		}
		str.append("\n\n");
		rightpane.append(str.toString());
	}
	public void actionPerformed(ActionEvent e){
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
		CombinedPreprocessor preprocessor=new CombinedPreprocessor(preprocessors);
		if(e.getActionCommand().endsWith("FONT")){
			Font font=null;
			if(e.getActionCommand().startsWith("SYS")){
				//font=(Font)sysfonts.getSelectedItem();
				font=new Font((String)sysfonts.getSelectedItem(),Font.PLAIN,(Integer)sizeIn.getValue());
			}else{
				fileChooser.setMultiSelectionEnabled(false);
				fileChooser.setFileFilter(new FileNameExtensionFilter(env.getTranslation("FONT_FILE"),"ttf","otf"));
				if(fileChooser.showOpenDialog(null)==JFileChooser.APPROVE_OPTION){
					File file=fileChooser.getSelectedFile();
					try{
						font=Font.createFont(file.getName().endsWith(".ttf")||file.getName().endsWith(".otf")?Font.TRUETYPE_FONT:Font.TYPE1_FONT,file);
						font=font.deriveFont(Float.valueOf((Integer)sizeIn.getValue()));
					}catch(Exception ex){
						ex.printStackTrace();
					}
					GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);
					sysfonts.addItem(font.getName());
				}
			}
			if(font!=null){
				String ch=charIn.getText();
				FontRenderContext context=new FontRenderContext(null,true,true);
				GlyphVector glyphs=font.createGlyphVector(context,ch);
				Rectangle rect=glyphs.getPixelBounds(context,0,0);
				//Rectangle2D rect2=glyphs.getLogicalBounds();
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
				display(preprocessor.preprocess(bi));
			}
		}else if(e.getActionCommand().equals("PICTURE")){
			fileChooser.setMultiSelectionEnabled(true);
			fileChooser.setFileFilter(new FileNameExtensionFilter(env.getTranslation("PICTURE_FILE"),"png","jpg","jpeg","bmp","gif","PNG","JPG","JPEG","BMP","GIF","ppm","pgm","pbm"));
			if(fileChooser.showOpenDialog(null)==JFileChooser.APPROVE_OPTION){
				File[] files=fileChooser.getSelectedFiles();
				for(File file:files)
					try{
						display(preprocessor.preprocess(ImageIO.read(file)));
					}catch(Exception ex){
						ex.printStackTrace();
					}
			}
		}
	}
	/**
	 * Scroll to bottom when new component is added
	 * @param e the event
	 */
	public void stateChanged(ChangeEvent e){
		int tmp=scrollpane.getVerticalScrollBar().getMaximum();
		if(curr_height!=tmp){
			scrollpane.getVerticalScrollBar().setValue(tmp);
			curr_height=tmp;
		}
	}
}