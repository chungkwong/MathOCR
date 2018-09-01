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
package com.github.chungkwong.mathocr.character;
import com.github.chungkwong.mathocr.common.BoundBox;
import com.github.chungkwong.mathocr.common.ConnectedComponent;
import java.awt.*;
import java.awt.font.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.logging.*;
import javax.swing.*;
/**
 *
 * @author Chan Chung Kwong
 */
public class DataSet{
	private final Map<CharacterPrototype,List<Object[]>> samples=new HashMap<>();
	private final Map<CharacterPrototype,Integer> characterIndex=new HashMap<>();
	private final Map<String,Integer> featureIndex;
	private final CharacterList characters=new CharacterList(new ArrayList<>());
	private final Feature[] extracters;
	private int count=0;
	public DataSet(){
		this(Features.REGISTRY.names());
	}
	public DataSet(Collection<String> selectedFeatures){
		int size=selectedFeatures.size();
		featureIndex=new HashMap<>();
		extracters=new Feature[size];
		int i=0;
		for(Iterator<String> iterator=selectedFeatures.iterator();iterator.hasNext();){
			String next=iterator.next();
			extracters[i]=Features.REGISTRY.get(next);
			featureIndex.put(next,i++);
		}
	}
	public void addSample(CharacterPrototype c,ConnectedComponent ele){
		Object[] sample=new Object[extracters.length];
		for(int i=0;i<extracters.length;i++){
			sample[i]=extracters[i].extract(ele);
		}
		List<Object[]> cls=samples.get(c);
		if(cls==null){
			characterIndex.put(c,samples.size());
			characters.getCharacters().add(c);
			cls=new LinkedList<>();
			samples.put(c,cls);
		}
		cls.add(sample);
		++count;
	}
	public void addSample(Font font){
		for(int i=0;i<0x10FFFF;i++){
			if(font.canDisplay(i)){
				addSample(font,i);
			}
		}
	}
	public void addSample(Font font,int[] codePoints){
		Arrays.stream(codePoints).filter((c)->font.canDisplay(c)).forEach((c)->addSample(font,c));
	}
	public void addSample(Font font,int[] codePoints,int diff){
		Arrays.stream(codePoints).filter((c)->font.canDisplay(c)).forEach((c)->addSample(font,c,c+diff));
	}
	public void addSample(Font font,int codePoint){
		addSample(font,codePoint,codePoint);
	}
	public void addSample(Font font,int codePoint,int toCodePoint){
		FontRenderContext context=new FontRenderContext(null,false,true);
		GlyphVector glyphVector=font.createGlyphVector(context,new String(new int[]{codePoint},0,1));
		float x=(float)glyphVector.getVisualBounds().getX();
		float y=(float)glyphVector.getVisualBounds().getY();
		int width=(int)(glyphVector.getVisualBounds().getWidth()+0.5);
		int height=(int)(glyphVector.getVisualBounds().getHeight()+0.5);
		int fontSize=(int)(glyphVector.getLogicalBounds().getHeight()+0.5);
		if(width==0||height==0){
			return;
		}
		BufferedImage bi=new BufferedImage(width,height,BufferedImage.TYPE_BYTE_BINARY);
		Graphics2D g2d=bi.createGraphics();
		g2d.setColor(Color.WHITE);
		g2d.fillRect(0,0,width,height);
		g2d.setColor(Color.BLACK);
		g2d.drawGlyphVector(glyphVector,-x,-y);
		ConnectedComponent ele=new ConnectedComponent(bi);
		BoundBox box=new BoundBox((int)x,(int)x+width,(int)y,(int)y+height);
		CharacterPrototype prototype=new CharacterPrototype(toCodePoint,box,font.getFamily(),fontSize,font.getStyle());
		addSample(prototype,ele);
	}
	public int getCount(){
		return count;
	}
	public Map<CharacterPrototype,List<Object[]>> getSamples(){
		return samples;
	}
	public Set<String> getSelectedFeatureNames(){
		return featureIndex.keySet();
	}
	public int getFeatureIndex(String name){
		return featureIndex.get(name);
	}
	public int getCharacterIndex(CharacterPrototype prototype){
		return characterIndex.get(prototype);
	}
	public CharacterList getCharacterList(){
		return characters;
	}
	public void train(Collection<String> modelNames,File directory){
		try{
			characters.write(new File(directory,"index"));
		}catch(IOException ex){
			Logger.getLogger(DataSet.class.getName()).log(Level.SEVERE,null,ex);
		}
		modelNames.forEach((name)->{
			try{
				ModelType model=ModelTypes.REGISTRY.get(name);
				model.write(model.build(this),new File(directory,name).getAbsolutePath());
			}catch(IOException ex){
				Logger.getLogger(DataSet.class.getName()).log(Level.SEVERE,null,ex);
			}
		});
	}
	public static void main(String[] args) throws IOException,FontFormatException{
		Font font=Font.createFont(Font.TYPE1_FONT,new File("/usr/share/texlive/texmf-dist/fonts/type1/public/amsfonts/symbols/msbm10.pfb"));
		JFrame f=new JFrame();
		//GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);
		JLabel label=new JLabel("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
		label.setFont(font.deriveFont(12.0f));
		f.add(label);
		f.setExtendedState(JFrame.MAXIMIZED_BOTH);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);
		for(int i=0;i<0xFFFF;i++){
			if(font.canDisplay(i)){
				System.out.println(Integer.toHexString(i)+":"+new String(new int[]{i},0,1));
			}
		}
	}
}
