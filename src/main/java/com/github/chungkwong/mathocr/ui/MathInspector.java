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
import com.github.chungkwong.mathocr.common.*;
import com.github.chungkwong.mathocr.layout.logical.*;
import com.github.chungkwong.mathocr.preprocess.*;
import com.github.chungkwong.mathocr.text.*;
import java.awt.image.*;
import java.io.*;
import java.util.logging.*;
import javax.imageio.*;
import javax.swing.*;
/**
 *
 * @author Chan Chung Kwong
 */
public class MathInspector extends JSplitPane{
	private final JFileChooser fileChooser=new JFileChooser();
	private final JLabel preview=new JLabel();
	private final JTextArea output=new JTextArea();
	public MathInspector(){
		setLeftComponent(fileChooser);
		Box box=Box.createVerticalBox();
		box.add(preview);
		box.add(output);
		setRightComponent(new JScrollPane(box));
		fileChooser.addActionListener((e)->inspect(fileChooser.getSelectedFile()));
	}
	private void inspect(File file){
		try{
			BufferedImage input=CombinedPreprocessor.getDefaultCombinedPreprocessor().apply(ImageIO.read(file),true);
			BufferedImage preprocessed=CombinedPreprocessor.getDefaultCombinedPreprocessor().apply(ImageIO.read(file),true);
			ComponentPool components=new ComponentPool(preprocessed);
			TextLine line=new TextLine(components.getComponents(),components.getBoundBox(),TextLine.ALIGN_FULL);
			preview.setIcon(new ImageIcon(input));
			output.setText(LatexEncoder.encode(new BuiltinLineRecognizer().recognize(line,input)));
		}catch(IOException ex){
			Logger.getLogger(MathInspector.class.getName()).log(Level.SEVERE,null,ex);
		}
	}
}
