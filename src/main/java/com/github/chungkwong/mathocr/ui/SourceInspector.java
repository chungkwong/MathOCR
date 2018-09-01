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
import com.github.chungkwong.mathocr.layout.logical.DocumentAssemblers;
import com.github.chungkwong.mathocr.layout.logical.Page;
import java.awt.*;
import java.awt.font.*;
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
public class SourceInspector extends Inspector<Object,Page,Object>{
	private static File directory;
	private LinkedList<File> files;
	private LinkedList<Page> pages=new LinkedList<>();
	public SourceInspector(JPanel parent){
		super(parent);
		JFileChooser fileChooser=new JFileChooser();
		fileChooser.setCurrentDirectory(directory);
		fileChooser.setMultiSelectionEnabled(true);
		fileChooser.addActionListener((e)->{
			File[] selected=fileChooser.getSelectedFiles();
			directory=fileChooser.getCurrentDirectory();
			files=new LinkedList<>();
			Arrays.stream(selected).forEach((file)->files.add(file));
			processFile();
		});
		add(fileChooser,BorderLayout.CENTER);
		JPanel sample=new JPanel(new BorderLayout());
		FontChooser fontChooser=new FontChooser();
		sample.add(fontChooser,BorderLayout.WEST);
		JTextField text=new JTextField();
		sample.add(text,BorderLayout.SOUTH);
		add(sample,BorderLayout.SOUTH);
		text.addActionListener((e)->{
			call(new ComponentInspector(),drawImage(text.getText(),fontChooser.getFont()));
		});
	}
	private void processFile(){
		File file=files.removeFirst();
		try{
			call(new ImageInspector(),ImageIO.read(file));
		}catch(IOException ex){
			Logger.getLogger(SourceInspector.class.getName()).log(Level.SEVERE,null,ex);
		}
	}
	@Override
	protected void onCreated(Object src){
	}
	@Override
	protected void onReturned(Page val){
		pages.add(val);
		if(files==null||files.isEmpty()){
			call(new OutputInspector(),DocumentAssemblers.REGISTRY.get().assemble(pages));
		}else{
			processFile();
		}
	}
	private BufferedImage drawImage(String string,Font font){
		FontRenderContext context=new FontRenderContext(null,false,true);
		GlyphVector glyphVector=font.createGlyphVector(context,string);
		float x=(float)glyphVector.getVisualBounds().getX();
		float y=(float)glyphVector.getVisualBounds().getY();
		int width=(int)(glyphVector.getVisualBounds().getWidth()+1);
		int height=(int)(glyphVector.getVisualBounds().getHeight()+1);
		BufferedImage bi=new BufferedImage(width,height,BufferedImage.TYPE_BYTE_BINARY);
		Graphics2D g2d=bi.createGraphics();
		g2d.setColor(Color.WHITE);
		g2d.fillRect(0,0,width,height);
		g2d.setColor(Color.BLACK);
		g2d.drawGlyphVector(glyphVector,-x,-y);
		return bi;
	}
	public static void main(String[] args){
		JFrame f=new JFrame();
		JPanel master=new JPanel(new BorderLayout());
		master.add(new SourceInspector(master));
		f.add(master);
		f.setExtendedState(JFrame.MAXIMIZED_BOTH);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);
	}
}
