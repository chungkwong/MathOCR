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
import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;
/**
 *
 * @author Chan Chung Kwong
 */
public class FontChooser extends Box{
	private final JComboBox<Font> font=new JComboBox<>(GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts());
	private final JSpinner scale=new JSpinner(new SpinnerNumberModel(1.0,0.0,100.0,1.0));
	private final JSpinner size=new JSpinner(new SpinnerNumberModel(24,1,1024,1.0));
	private final JToggleButton italic=new JToggleButton("I");
	private final JToggleButton bold=new JToggleButton("B");
	public FontChooser(){
		super(BoxLayout.X_AXIS);
		add(font);
		add(size);
		add(scale);
		add(italic);
		add(bold);
	}
	@Override
	public Font getFont(){
		Font f=(Font)font.getSelectedItem();
		float fontsize=((Number)size.getValue()).floatValue();
		double scale=((Number)this.scale.getValue()).doubleValue();
		int style=Font.PLAIN;
		if(italic.isSelected()){
			style|=Font.ITALIC;
		}
		if(bold.isSelected()){
			style|=Font.BOLD;
		}
		f=f.deriveFont(style,fontsize);
		if(Math.abs(scale-1.0)>1e-6){
			f=f.deriveFont(AffineTransform.getScaleInstance(scale,scale));
		}
		return f;
	}
}
