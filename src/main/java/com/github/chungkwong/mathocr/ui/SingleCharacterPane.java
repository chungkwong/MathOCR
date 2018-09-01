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
import com.github.chungkwong.mathocr.common.ConnectedComponent;
import com.github.chungkwong.mathocr.character.CharacterRecognizers;
import com.github.chungkwong.mathocr.character.ModelTypes;
import com.github.chungkwong.mathocr.character.CharacterCandidate;
import com.github.chungkwong.mathocr.character.CharacterRecognizer;
import com.github.chungkwong.mathocr.character.CharacterList;
import com.github.chungkwong.mathocr.ui.FontChooser;
import java.awt.*;
import java.awt.font.*;
import java.awt.image.*;
import java.io.*;
import java.util.List;
import java.util.*;
import java.util.logging.*;
import java.util.stream.*;
import javax.swing.*;
/**
 *
 * @author Chan Chung Kwong
 */
public class SingleCharacterPane extends JPanel{
	private final Map<String,Object> models;
	private final List<JCheckBox> recognizerChooser;
	private final CharacterList list;
	public SingleCharacterPane(File directory){
		super(new BorderLayout());
		CharacterList listTmp;
		try{
			listTmp=CharacterList.read(new File(directory,"index"));
		}catch(IOException ex){
			Logger.getLogger(SingleCharacterPane.class.getName()).log(Level.SEVERE,null,ex);
			listTmp=new CharacterList(new ArrayList<>());
		}
		list=listTmp;
		Box recognizerChoosers=Box.createVerticalBox();
		recognizerChooser=CharacterRecognizers.REGISTRY.names().stream().
				filter((name)->new File(directory,name).exists()).
				map((name)->new JCheckBox(name,true)).collect(Collectors.toList());
		recognizerChooser.forEach((checkBox)->recognizerChoosers.add(checkBox));
		models=recognizerChooser.stream().map((checkBox)->checkBox.getText()).
				collect(Collectors.toMap((name)->name,(name)->{
					try{
						String model=CharacterRecognizers.REGISTRY.get(name).getModelType();
						return ModelTypes.REGISTRY.get(model).read(new File(directory,model).getAbsolutePath());
					}catch(IOException ex){
						Logger.getLogger(SingleCharacterPane.class.getName()).log(Level.SEVERE,null,ex);
						return null;
					}
				}));
		add(recognizerChoosers,BorderLayout.WEST);
		JTextArea info=new JTextArea();
		info.setEditable(false);
		add(new JScrollPane(info),BorderLayout.CENTER);
		JPanel sample=new JPanel(new BorderLayout());
		FontChooser fontChooser=new FontChooser();
		sample.add(fontChooser,BorderLayout.WEST);
		JTextField test=new JTextField();
		test.addActionListener((e)->{
			info.append(recognize(test.getText(),fontChooser.getFont()));
		});
		sample.add(test,BorderLayout.CENTER);
		add(sample,BorderLayout.NORTH);
	}
	private String recognize(String text,Font font){
		StringBuilder buf=new StringBuilder();
		List<String> recognizers=recognizerChooser.stream().
				filter((checkBox)->checkBox.isSelected()).
				map((checkBox)->checkBox.getText()).
				collect(Collectors.toList());
		text.codePoints().forEach((c)->{
			buf.appendCodePoint(c).append('\n');
			recognizers.forEach((name)->{
				buf.append(name).append(':');
				CharacterRecognizer recognizer=CharacterRecognizers.REGISTRY.get(name);
				NavigableSet<CharacterCandidate> candidates=recognizer.recognize(getComponent(font,c),models.get(recognizer.getModelType()),list);
				candidates.forEach((cand)->buf.appendCodePoint(cand.getCodePoint()).append('(').append(cand.getScore()).append(')'));
				buf.append('\n');
			});
			buf.append('\n');
		});
		return buf.toString();
	}
	private ConnectedComponent getComponent(Font font,int codePoint){
		FontRenderContext context=new FontRenderContext(null,false,true);
		GlyphVector glyphVector=font.createGlyphVector(context,new String(new int[]{codePoint},0,1));
		float x=(float)glyphVector.getVisualBounds().getX();
		float y=(float)glyphVector.getVisualBounds().getY();
		int width=(int)(glyphVector.getVisualBounds().getWidth()+0.5);
		int height=(int)(glyphVector.getVisualBounds().getHeight()+0.5);
		BufferedImage bi=new BufferedImage(width,height,BufferedImage.TYPE_BYTE_BINARY);
		Graphics2D g2d=bi.createGraphics();
		g2d.setColor(Color.WHITE);
		g2d.fillRect(0,0,width,height);
		g2d.setColor(Color.BLACK);
		g2d.drawGlyphVector(glyphVector,-x,-y);
		return new ConnectedComponent(bi);
	}
}
