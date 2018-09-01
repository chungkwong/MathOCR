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
import com.github.chungkwong.mathocr.character.Features;
import com.github.chungkwong.mathocr.character.ModelTypes;
import java.awt.*;
import java.util.List;
import java.util.stream.*;
import javax.swing.*;
import static com.github.chungkwong.mathocr.Environment.ENVIRONMENT;
import com.github.chungkwong.mathocr.character.*;
import java.awt.geom.*;
import java.io.*;
import java.util.logging.*;
/**
 *
 * @author Chan Chung Kwong
 */
public class FontTrainingPane extends JPanel{
	private TrainSet trainSet=new TrainSet();
	private final List<JCheckBox> featureChooser;
	private final List<JCheckBox> modelChooser;
	public FontTrainingPane(){
		super(new BorderLayout());
		JTextArea info=new JTextArea();
		info.setEditable(false);
		add(new JScrollPane(info),BorderLayout.CENTER);
		JPanel sample=new JPanel(new BorderLayout());
		FontChooser fontChooser=new FontChooser();
		sample.add(fontChooser,BorderLayout.WEST);
		JTextField regex=new JTextField();
		sample.add(regex,BorderLayout.CENTER);
		JButton add=new JButton(ENVIRONMENT.getTranslation("ADD"));
		add.addActionListener((e)->{
			Font font=fontChooser.getFont();
			TrainSet.CodeSet codeSet=new TrainSet.CodeSet(0,0x10FFFF,0,regex.getText());
			trainSet.getData().add(new TrainSet.CombinedSet(new Font[]{font},new float[]{font.getSize()},
					new int[]{font.getStyle()},new AffineTransform[]{font.getTransform()},new TrainSet.CodeSet[]{codeSet}));
			info.append("\n"+font.toString()+"\t"+regex.getText());
		});
		sample.add(add,BorderLayout.EAST);
		add(sample,BorderLayout.NORTH);
		Box types=Box.createVerticalBox();
		types.add(new JLabel(ENVIRONMENT.getTranslation("FEATURES")));
		featureChooser=Features.REGISTRY.names().stream().map((name)->new JCheckBox(name,true)).collect(Collectors.toList());
		featureChooser.forEach((checkBox)->types.add(checkBox));
		types.add(new JLabel(ENVIRONMENT.getTranslation("MODEL")));
		modelChooser=ModelTypes.REGISTRY.names().stream().map((name)->new JCheckBox(name,true)).collect(Collectors.toList());
		modelChooser.forEach((checkBox)->types.add(checkBox));
		JButton save=new JButton(ENVIRONMENT.getTranslation("SAVE"));
		save.addActionListener((e)->{
			JFileChooser fileChooser=new JFileChooser();
			if(fileChooser.showSaveDialog(FontTrainingPane.this)==JFileChooser.APPROVE_OPTION){
				try{
					trainSet.save(new FileOutputStream(fileChooser.getSelectedFile()));
				}catch(Exception ex){
					Logger.getLogger(FontTrainingPane.class.getName()).log(Level.SEVERE,null,ex);
				}
			}
		});
		types.add(save);
		JButton load=new JButton(ENVIRONMENT.getTranslation("LOAD"));
		load.addActionListener((e)->{
			JFileChooser fileChooser=new JFileChooser();
			if(fileChooser.showOpenDialog(FontTrainingPane.this)==JFileChooser.APPROVE_OPTION){
				try{
					trainSet=TrainSet.load(fileChooser.getSelectedFile());
					info.setText(fileChooser.getSelectedFile().getAbsolutePath());
				}catch(Exception ex){
					Logger.getLogger(FontTrainingPane.class.getName()).log(Level.SEVERE,null,ex);
				}
			}
		});
		types.add(load);
		add(types,BorderLayout.WEST);
		JButton begin=new JButton(ENVIRONMENT.getTranslation("FONT_TRAINING"));
		begin.addActionListener((e)->train());
		add(begin,BorderLayout.SOUTH);
	}
	private void train(){
		JFileChooser fileChooser=new JFileChooser();
		fileChooser.setFileHidingEnabled(false);
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		if(fileChooser.showOpenDialog(this)==JFileChooser.APPROVE_OPTION){
			trainSet.getFeatures().clear();
			trainSet.getFeatures().addAll(featureChooser.stream().filter((checkBox)->checkBox.isSelected()).
					map((checkBox)->checkBox.getText()).collect(Collectors.toList()));
			trainSet.getClassifier().clear();
			trainSet.getClassifier().addAll(modelChooser.stream().filter((checkBox)->checkBox.isSelected()).
					map((checkBox)->checkBox.getText()).collect(Collectors.toList()));
			trainSet.train(fileChooser.getSelectedFile());
		}
	}
}
