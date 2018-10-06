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
package com.github.chungkwong.mathocr.scene;
import com.github.chungkwong.mathocr.common.*;
import java.io.*;
import java.util.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
/**
 *
 * @author Chan Chung Kwong
 */
public class Icdar2003Test extends LocalizationTest{
	private static final File DIR=new File("../datasets/scene/scene/SceneTrialTest");
	private final Document document;
	public Icdar2003Test() throws Exception{
		DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
		factory.setIgnoringElementContentWhitespace(true);
		document=factory.newDocumentBuilder().parse(new File(DIR,"locations.xml"));
	}
	@Override
	public Iterator<File> getSamples(){
		List<File> files=new ArrayList<>();
		NodeList images=document.getElementsByTagName("image");
		for(int i=0;i<images.getLength();i++){
			NodeList image=images.item(i).getChildNodes();
			for(int j=0;j<image.getLength();j++){
				if(image.item(j).getNodeName().equals("imageName")){
					String path=image.item(j).getTextContent();
					files.add(new File(DIR,path));
					break;
				}
			}
		}
		return files.iterator();
	}
	@Override
	public List<BoundBox> getTruthBoxes(File file){
		NodeList images=document.getElementsByTagName("image");
		for(int i=0;i<images.getLength();i++){
			String path=" ";
			NodeList image=images.item(i).getChildNodes();
			for(int j=0;j<image.getLength();j++){
				if(image.item(j).getNodeName().equals("imageName")){
					path=image.item(j).getTextContent();
					break;
				}
			}
			if(file.getAbsolutePath().endsWith(path)){
				NodeList rects=null;
				for(int j=0;j<image.getLength();j++){
					if(image.item(j).getNodeName().equals("taggedRectangles")){
						rects=(NodeList)image.item(j);
						break;
					}
				}
				List<BoundBox> frames=new ArrayList<>(rects.getLength());
				for(int j=0;j<rects.getLength();j++){
					if(!(rects.item(j) instanceof Element)){
						continue;
					}
					Element item=(Element)rects.item(j);
					int x=(int)Double.parseDouble(item.getAttribute("x"));
					int y=(int)Double.parseDouble(item.getAttribute("y"));
					int width=(int)Double.parseDouble(item.getAttribute("width"));
					int height=(int)Double.parseDouble(item.getAttribute("height"));
					frames.add(new BoundBox(x,y,x+width-1,y+height-1));
				}
				return frames;
			}
		}
		return Collections.emptyList();
	}
	public static void main(String[] args) throws Exception{
		//new Icdar2003Test().test(new ContrastDetector());
		//new Icdar2003Test().test(new ContrastDetector());
		new Icdar2003Test().test(new ColorDetector());
	}
}
