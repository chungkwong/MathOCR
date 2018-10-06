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
package com.github.chungkwong.mathocr.text;
import com.github.chungkwong.mathocr.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.*;
import javax.swing.*;
import javax.swing.Box;
/**
 *
 * @author Chan Chung Kwong
 */
public class InteractiveFormulaTest extends Box implements ActionListener{
	private final JTextField in=new JTextField();
	private final JLabel actualImage=new JLabel();
	private final JLabel actual=new JLabel();
	private final JLabel normalizedActual=new JLabel();
	private final JLabel predictedImage=new JLabel();
	private final JLabel predicted=new JLabel();
	private final JLabel normalizedPredicted=new JLabel();
	private final JLabel metric=new JLabel();
	public InteractiveFormulaTest(){
		super(BoxLayout.Y_AXIS);
		in.addActionListener(this);
		add(in);
		add(actualImage);
		add(actual);
		add(normalizedActual);
		add(predictedImage);
		add(predicted);
		add(normalizedPredicted);
		add(metric);
		add(new JPanel());
	}
	@Override
	public void actionPerformed(ActionEvent e){
		String input=in.getText();
		BufferedImage image=Util.render(input);
		String output=PipeLine.recognizeLatexFormula(image);
		String[] normalizedInput=Util.normalize(Util.tokenize(input));
		String[] normalizedOutput=Util.normalize(Util.tokenize(output));
		actual.setText(input);
		predicted.setText(output);
		normalizedActual.setText(Arrays.toString(normalizedInput));
		normalizedPredicted.setText(Arrays.toString(normalizedOutput));
		actualImage.setIcon(new ImageIcon(image));
		predictedImage.setIcon(new ImageIcon(Util.render(output)));
		metric.setText("Edit:"+Util.getLevenshteinDistance(normalizedInput,normalizedOutput));
	}
	public static void main(String[] args){
		JFrame f=new JFrame();
		f.getContentPane().add(new JScrollPane(new InteractiveFormulaTest()));
		f.setSize(800,600);
		f.setExtendedState(JFrame.MAXIMIZED_BOTH);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);
	}
}
