/* PreprocessPane.java
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
import javax.swing.*;
import net.sf.mathocr.*;
import net.sf.mathocr.layout.*;
import net.sf.mathocr.preprocess.*;
import static net.sf.mathocr.Environment.env;
/**
 * A GUI component being used to display result of preprocess stage and allow user to make changes
 */
public final class PreprocessPane extends JToolBar implements ActionListener,MouseListener{
	PageEditor pageEdit;
	JButton crop,meanfilter,medianfilter,noise,kfill,skewDetect,skewCorrect,manual,otsu,sauvola,invert,next,undo;
	JSpinner kIn=new JSpinner(new SpinnerNumberModel((int)env.getInteger("KFILL_WINDOW"),3,255,2));
	JSpinner angleIn=new JSpinner(new SpinnerNumberModel(0.0,-Math.PI,Math.PI,0.00001));
	JSpinner winIn=new JSpinner(new SpinnerNumberModel((int)env.getInteger("SAUVOLA_WINDOW"),3,255,2));
	JSpinner weightIn=new JSpinner(new SpinnerNumberModel((double)env.getFloat("SAUVOLA_WEIGHT"),-1.0,1.0,0.01));
	JSpinner limIn=new JSpinner(new SpinnerNumberModel((int)env.getInteger("MANUAL_THREHOLD_LIMIT"),0,255,1));
	JCheckBox checkWhiteOnBlack=new JCheckBox(env.getTranslation("CHECK_INVERT"),env.getBoolean("DETECT_INVERT"));
	double angle=0;
	int leftcrop=0,rightcrop=0,topcrop=0,bottomcrop;
	int x=-1,y;
	char status='N';
	PreprocessPane(PageEditor pageEdit){
		super(JToolBar.VERTICAL);
		this.pageEdit=pageEdit;
		//grayscale=addButton("GRAYSCALE");
		//grayscale.setEnabled(true);
		//add(new JSeparator());
		crop=addButton("CROP");
		addSeparator();
		add(new JLabel(env.getTranslation("FILTER")));
		meanfilter=addButton("MEAN_FILTER");
		medianfilter=addButton("MEDIAN_FILTER");
		kfill=addButton("KFILL");
		kIn.setAlignmentX(0);
		kIn.setMaximumSize(kIn.getPreferredSize());
		add(kIn);
		noise=addButton("NOISE_REMOVE");
		addSeparator();
		JLabel labthre=new JLabel(env.getTranslation("THREHOLD_METHOD"));
		labthre.setAlignmentX(0);
		add(labthre);
		sauvola=addButton("SAUVOLA_METHOD");
		winIn.setAlignmentX(0);
		winIn.setMaximumSize(winIn.getPreferredSize());
		add(winIn);
		weightIn.setAlignmentX(0);
		weightIn.setMaximumSize(weightIn.getPreferredSize());
		add(weightIn);
		otsu=addButton("OTSU_METHOD");
		manual=addButton("MANUAL");
		limIn.setAlignmentX(0);
		limIn.setMaximumSize(limIn.getPreferredSize());
		add(limIn);
		invert=addButton("INVERT_COLOR");
		add(checkWhiteOnBlack);
		addSeparator();
		add(new JLabel(env.getTranslation("SKEW_CORRECTION")));
		skewDetect=addButton("SKEW_DETECTION");
		angleIn.setAlignmentX(0);
		angleIn.setMaximumSize(angleIn.getPreferredSize());
		add(angleIn);
		skewCorrect=addButton("SKEW_CORRECTION");
		addSeparator();
		next=addButton("NEXT");
		undo=addButton("UNDO");
		meanfilter.setEnabled(true);
		medianfilter.setEnabled(true);
		manual.setEnabled(true);
		otsu.setEnabled(true);
		sauvola.setEnabled(true);
		undo.setEnabled(true);
		setEditible(false);
	}
	private final JButton addButton(String com){
		JButton button=new JButton(env.getTranslation(com));
		button.setActionCommand(com);
		button.addActionListener(this);
		button.setAlignmentX(0);
		button.setMaximumSize(button.getPreferredSize());
		add(button);
		return button;
	}
	private final void setEditible(boolean flag){
		//grayscale.setEnabled(!flag);
		kfill.setEnabled(flag);noise.setEnabled(flag);skewDetect.setEnabled(flag);skewCorrect.setEnabled(flag);next.setEnabled(flag);
	}
	public void activate(){
		pageEdit.getPageIcon().addMouseListener(this);
		status='N';
		angle=0;
		x=-1;
		leftcrop=rightcrop=topcrop=0;
	}
	public void actionPerformed(ActionEvent e){
		x=-1;
		status='N';
		switch(e.getActionCommand()){
			//case "GRAYSCALE":
			//	pageEdit.getPage().preprocess(new Grayscale());
			//	setEditible(true);
			//	kfill.setEnabled(false);
			//	next.setEnabled(false);
			//	break;
			case "CROP":
				status='C';
				break;
			case "MEAN_FILTER":
				pageEdit.getPage().preprocess(new MeanFilter());
				break;
			case "MEDIAN_FILTER":
				pageEdit.getPage().preprocess(new MedianFilter());
				break;
			case "KFILL":
				pageEdit.getPage().preprocess(new KFill((Integer)kIn.getValue()));
				break;
			case "NOISE_REMOVE":
				//pageEdit.getPage().preprocess(new NoiseRemove(pageEdit.getPage().getModifiedImage().getHeight()/50));//param
				pageEdit.getPage().preprocess(new NoiseRemove());
				break;
			case "SKEW_DETECTION":
				double detectedSkew=SkewCorrect.detectSkew(pageEdit.getPage().getModifiedImage(),env.getString("SKEW_DETECT_METHOD"));
				if(Double.isNaN(detectedSkew))
					JOptionPane.showMessageDialog(null,env.getTranslation("DETECTION_FAILED"));
				else
					angleIn.setValue(detectedSkew);
				break;
			case "SKEW_CORRECTION":
				pageEdit.getPage().preprocess(new SkewCorrect((Double)angleIn.getValue()));
				angle+=(Double)angleIn.getValue();
				break;
			case "SAUVOLA_METHOD":
				pageEdit.getPage().preprocess(new ThreholdSauvola((Double)weightIn.getValue(),(Integer)winIn.getValue()));
				setEditible(true);
				break;
			case "OTSU_METHOD":
				pageEdit.getPage().preprocess(new ThreholdOtsu());
				setEditible(true);
				break;
			case "MANUAL":
				pageEdit.getPage().preprocess(new Threhold((Integer)limIn.getValue()));
				setEditible(true);
				break;
			case "INVERT_COLOR":
				pageEdit.getPage().preprocess(new ColorInvert(checkWhiteOnBlack.isSelected()));
				break;
			case "NEXT":
				if(angle!=0){
					pageEdit.getPage().preprocessInput(new SkewCorrect(angle));
				}
				if(rightcrop!=0){
					pageEdit.getPage().preprocessInput(new Crop(leftcrop,rightcrop,topcrop,bottomcrop));
				}
				setEditible(false);
				pageEdit.getPageIcon().removeMouseListener(this);
				pageEdit.showComponentPane();
				break;
			case "UNDO":
				angle=0;
				leftcrop=rightcrop=topcrop=0;
				setEditible(false);
				pageEdit.getPage().resetModifiedImage();
				break;
		}
		pageEdit.getPageIcon().repaint();
	}
	/**
	 * Not used
	 */
	public void mouseClicked(MouseEvent e){}
	/**
	 * Not used
	 */
	public void mouseEntered(MouseEvent e){}
	/**
	 * Not used
	 */
	public void mouseExited(MouseEvent e){}
	/**
	 * Change recognition result of the symbol that the mouse pointed to
	 * @param e the event
	 */
	public void mousePressed(MouseEvent e){
		if(x==-1){
			x=e.getX();
			y=e.getY();
		}else if(status=='C'){
			double scale=pageEdit.getPageIcon().getScale();
			x/=scale;
			y/=scale;
			int x1=(int)(e.getX()/scale),y1=(int)(e.getY()/scale);
			int xmin=Math.min(x,x1),xmax=Math.max(x,x1),ymin=Math.min(y,y1),ymax=Math.max(y,y1);
			pageEdit.getPage().preprocess(new Crop(xmin,xmax,ymin,ymax));
			rightcrop=leftcrop+xmax;
			bottomcrop=topcrop+ymax;
			leftcrop+=xmin;
			topcrop+=ymin;
			x=-1;
			pageEdit.getPageIcon().repaint();
		}else{
			angleIn.setValue(Math.atan((e.getY()-y+0.0)/(e.getX()-x)));
			x=-1;
		}
	}
	/**
	 * Not used
	 */
	public void mouseReleased(MouseEvent e){}
}