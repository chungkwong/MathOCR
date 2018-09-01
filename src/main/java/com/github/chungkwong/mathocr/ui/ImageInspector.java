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
import com.github.chungkwong.mathocr.preprocess.skew.SkewDetectors;
import com.github.chungkwong.mathocr.preprocess.Preprocessor;
import com.github.chungkwong.mathocr.preprocess.Crop;
import com.github.chungkwong.mathocr.preprocess.Rotate;
import com.github.chungkwong.mathocr.preprocess.MedianFilter;
import com.github.chungkwong.mathocr.preprocess.ThreholdFixed;
import com.github.chungkwong.mathocr.preprocess.ThreholdSauvola;
import com.github.chungkwong.mathocr.preprocess.MeanFilter;
import com.github.chungkwong.mathocr.preprocess.KFill;
import com.github.chungkwong.mathocr.preprocess.Grayscale;
import com.github.chungkwong.mathocr.preprocess.NoiseRemove;
import com.github.chungkwong.mathocr.preprocess.Dilation;
import com.github.chungkwong.mathocr.preprocess.Erosion;
import com.github.chungkwong.mathocr.preprocess.ThreholdOtsu;
import com.github.chungkwong.mathocr.preprocess.ColorInvert;
import com.github.chungkwong.mathocr.layout.logical.Page;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;
import static com.github.chungkwong.mathocr.Environment.ENVIRONMENT;
import java.util.*;
/**
 *
 * @author Chan Chung Kwong
 */
public class ImageInspector extends Inspector<BufferedImage,Page,Page> implements ActionListener,MouseListener{
	private BufferedImage input, modified;
	private PageIcon icon;
	private JToolBar bar=new JToolBar(JToolBar.VERTICAL);
	private JButton crop, meanfilter, medianfilter, noise, kfill, erosion, dilation, skewDetect, skewCorrect, manual, otsu, sauvola, invert, next, undo;
	private JSpinner kIn=new JSpinner(new SpinnerNumberModel((int)ENVIRONMENT.getInteger("KFILL_WINDOW"),3,255,2));
	private JFormattedTextField angleIn=new JFormattedTextField(0.0);
	private JSpinner winIn=new JSpinner(new SpinnerNumberModel((int)ENVIRONMENT.getInteger("SAUVOLA_WINDOW"),3,255,2));
	private JSpinner weightIn=new JSpinner(new SpinnerNumberModel((double)ENVIRONMENT.getFloat("SAUVOLA_WEIGHT"),-1.0,1.0,0.01));
	private JSpinner limIn=new JSpinner(new SpinnerNumberModel((int)ENVIRONMENT.getInteger("MANUAL_THREHOLD_LIMIT"),0,255,1));
	private JCheckBox checkWhiteOnBlack=new JCheckBox(ENVIRONMENT.getTranslation("CHECK_INVERT"),ENVIRONMENT.getBoolean("DETECT_INVERT"));
	private double angle=0;
	private int leftcrop=0, rightcrop=0, topcrop=0, bottomcrop=0;
	private int x=-1, y;
	private char status='N';
	@Override
	protected void onCreated(BufferedImage src){
		input=src;
		modified=new Grayscale().apply(input,false);
		icon=new PageIcon(modified,null);
		icon.getContent().addMouseListener(this);
		add(new JScrollPane(icon),BorderLayout.CENTER);
		crop=addButton("CROP");
		bar.addSeparator();
		bar.add(new JLabel(ENVIRONMENT.getTranslation("FILTER")));
		meanfilter=addButton("MEAN_FILTER");
		medianfilter=addButton("MEDIAN_FILTER");
		kfill=addButton("KFILL");
		erosion=addButton("EROSION");
		dilation=addButton("DILATION");
		kIn.setAlignmentX(0);
		kIn.setMaximumSize(kIn.getPreferredSize());
		bar.add(kIn);
		noise=addButton("NOISE_REMOVE");
		bar.addSeparator();
		JLabel labthre=new JLabel(ENVIRONMENT.getTranslation("THREHOLD_METHOD"));
		labthre.setAlignmentX(0);
		bar.add(labthre);
		sauvola=addButton("SAUVOLA_METHOD");
		winIn.setAlignmentX(0);
		winIn.setMaximumSize(winIn.getPreferredSize());
		bar.add(winIn);
		weightIn.setAlignmentX(0);
		weightIn.setMaximumSize(weightIn.getPreferredSize());
		bar.add(weightIn);
		otsu=addButton("OTSU_METHOD");
		manual=addButton("MANUAL");
		limIn.setAlignmentX(0);
		limIn.setMaximumSize(limIn.getPreferredSize());
		bar.add(limIn);
		invert=addButton("INVERT_COLOR");
		bar.add(checkWhiteOnBlack);
		bar.addSeparator();
		bar.add(new JLabel(ENVIRONMENT.getTranslation("SKEW_CORRECTION")));
		skewDetect=addButton("SKEW_DETECTION");
		angleIn.setAlignmentX(0);
		//angleIn.setMaximumSize(angleIn.getPreferredSize());
		bar.add(angleIn);
		skewCorrect=addButton("SKEW_CORRECTION");
		bar.addSeparator();
		next=addButton("NEXT");
		undo=addButton("UNDO");
		meanfilter.setEnabled(true);
		medianfilter.setEnabled(true);
		manual.setEnabled(true);
		otsu.setEnabled(true);
		sauvola.setEnabled(true);
		undo.setEnabled(true);
		setEditible(false);
		add(new JScrollPane(bar),BorderLayout.EAST);
	}
	private final JButton addButton(String com){
		JButton button=new JButton(ENVIRONMENT.getTranslation(com));
		button.setActionCommand(com);
		button.addActionListener(this);
		button.setAlignmentX(0);
		button.setMaximumSize(button.getPreferredSize());
		bar.add(button);
		return button;
	}
	private final void setEditible(boolean flag){
		//grayscale.setEnabled(!flag);
		kfill.setEnabled(flag);
		erosion.setEnabled(flag);
		dilation.setEnabled(flag);
		noise.setEnabled(flag);
		skewDetect.setEnabled(flag);
		skewCorrect.setEnabled(flag);
		next.setEnabled(flag);
	}
	@Override
	protected void onReturned(Page val){
		ret(val);
	}
	@Override
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
				preprocess(new MeanFilter());
				break;
			case "MEDIAN_FILTER":
				preprocess(new MedianFilter());
				break;
			case "KFILL":
				preprocess(new KFill((Integer)kIn.getValue()));
				break;
			case "EROSION": {
				BitSet template=new BitSet(9);
				template.set(0,9);
				preprocess(new Erosion(template));
			}
			break;
			case "DILATION": {
				BitSet template=new BitSet(9);
				template.set(0,9);
				preprocess(new Dilation(template));
			}
			break;
			case "NOISE_REMOVE":
				//pageEdit.getPage().preprocess(new NoiseRemove(pageEdit.getPage().getModifiedImage().getHeight()/50));//param
				preprocess(new NoiseRemove());
				break;
			case "SKEW_DETECTION":
				double detectedSkew=SkewDetectors.REGISTRY.get().detect(modified);
				if(Double.isNaN(detectedSkew)){
					JOptionPane.showMessageDialog(null,ENVIRONMENT.getTranslation("DETECTION_FAILED"));
				}else{
					angleIn.setValue(detectedSkew);
				}
				break;
			case "SKEW_CORRECTION":
				preprocess(new Rotate((Double)angleIn.getValue()));
				angle+=(Double)angleIn.getValue();
				break;
			case "SAUVOLA_METHOD":
				preprocess(new ThreholdSauvola((Double)weightIn.getValue(),(Integer)winIn.getValue()));
				setEditible(true);
				break;
			case "OTSU_METHOD":
				preprocess(new ThreholdOtsu());
				setEditible(true);
				break;
			case "MANUAL":
				preprocess(new ThreholdFixed((Integer)limIn.getValue()));
				setEditible(true);
				break;
			case "INVERT_COLOR":
				preprocess(new ColorInvert(checkWhiteOnBlack.isSelected()));
				break;
			case "UNDO":
				angle=0;
				leftcrop=rightcrop=topcrop=0;
				setEditible(false);
				modified=input;
				preprocess(new Grayscale());
				break;
			case "NEXT":
				if(rightcrop!=0){
					preprocessInput(new Crop(leftcrop,rightcrop,topcrop,bottomcrop));
				}
				if(angle!=0){
					preprocessInput(new Rotate(angle));
				}
				setEditible(false);
				call(new ComponentInspector(),modified);
				return;
		}
	}
	@Override
	public void mouseClicked(MouseEvent e){
		if(x==-1){
			x=e.getX();
			y=e.getY();
		}else if(status=='C'){
			double scale=icon.getScale();
			x/=scale;
			y/=scale;
			int x1=(int)(e.getX()/scale), y1=(int)(e.getY()/scale);
			int xmin=Math.min(x,x1), xmax=Math.max(x,x1), ymin=Math.min(y,y1), ymax=Math.max(y,y1);
			preprocess(new Crop(xmin,xmax,ymin,ymax));
			rightcrop=leftcrop+xmax;
			bottomcrop=topcrop+ymax;
			leftcrop+=xmin;
			topcrop+=ymin;
			x=-1;
		}else{
			angleIn.setValue(Math.atan((e.getY()-y+0.0)/(e.getX()-x)));
			x=-1;
		}
	}
	private void preprocess(Preprocessor preprocessor){
		modified=preprocessor.apply(modified,true);
		icon.setPage(modified);
	}
	private void preprocessInput(Preprocessor preprocessor){
		input=preprocessor.apply(input,true);
	}
	@Override
	public void mousePressed(MouseEvent e){
	}
	@Override
	public void mouseReleased(MouseEvent e){
	}
	@Override
	public void mouseEntered(MouseEvent e){
	}
	@Override
	public void mouseExited(MouseEvent e){
	}
}
