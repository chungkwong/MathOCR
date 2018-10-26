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
import com.github.chungkwong.mathocr.preprocess.*;
import com.github.chungkwong.mathocr.scene.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import java.util.logging.*;
import javax.imageio.*;
import javax.swing.*;
/**
 *
 * @author Chan Chung Kwong
 */
public class SceneTextDetector extends JSplitPane implements IconPaint,MouseMotionListener{
	private final java.util.List<Shape> textRegions=new ArrayList<>();
	private final JFileChooser fileChooser=new JFileChooser();
	private final JCheckBox invert=new JCheckBox("Invert",false);
	private final JComboBox<TextDetector> detectors=new JComboBox<>(new TextDetector[]{
		new ContrastDetector(),new SwtDetector(),new BackgroundDetector(),new ColorDetector(),new DetextDetector()});
	private final JLabel icon=new JLabel();
	public SceneTextDetector(){
		super();
		JPanel chooser=new JPanel(new BorderLayout());
		detectors.setSelectedIndex(0);
		chooser.add(detectors,BorderLayout.NORTH);
		chooser.add(fileChooser,BorderLayout.CENTER);
		chooser.add(invert,BorderLayout.SOUTH);
		detectors.addActionListener((e)->refresh());
		invert.addActionListener((e)->refresh());
		fileChooser.addActionListener((e)->refresh());
		setLeftComponent(chooser);
		icon.setVerticalAlignment(SwingConstants.TOP);
		icon.setHorizontalAlignment(SwingConstants.LEFT);
		icon.addMouseMotionListener(this);
		setRightComponent(new JScrollPane(icon));
	}
	private void refresh(){
		try{
			BufferedImage image=ImageIO.read(fileChooser.getSelectedFile());
			if(invert.isSelected()){
				image=new ColorInvert(false).apply(image,true);
			}
			textRegions.clear();
			((TextDetector)detectors.getSelectedItem()).detect(image).forEach((line)->textRegions.add(line.getBound()));
			image=CombinedPreprocessor.getDefaultCombinedPreprocessor().apply(image,true);
			icon.setIcon(new PageIcon(image,this));
		}catch(IOException ex){
			Logger.getLogger(SceneTextDetector.class.getName()).log(Level.SEVERE,null,ex);
		}
	}
	public static void main(String[] args) throws IOException{
		JFrame f=new JFrame();
		SceneTextDetector detector=new SceneTextDetector();
		f.add(detector);
		f.setExtendedState(JFrame.MAXIMIZED_BOTH);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);
		detector.setDividerLocation(500);
	}
	@Override
	public void paintOn(Graphics2D g2d){
		g2d.setColor(Color.GREEN);
		for(Shape textRegion:textRegions){
			g2d.draw(textRegion);
		}
	}
	@Override
	public void mouseDragged(MouseEvent e){
	}
	@Override
	public void mouseMoved(MouseEvent e){
		((JLabel)e.getSource()).setToolTipText(e.getX()+","+e.getY());
	}
}
