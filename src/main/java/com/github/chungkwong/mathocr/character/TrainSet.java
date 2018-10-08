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
import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.function.*;
import java.util.logging.*;
import java.util.regex.*;
import java.util.stream.*;
import javax.xml.parsers.*;
import javax.xml.stream.*;
import org.w3c.dom.Node;
import org.w3c.dom.*;
/**
 * Allow flexible training set description
 *
 * @author Chan Chung Kwong
 */
public class TrainSet{
	private final List<String> classifier;
	private final List<String> features, smallFeatures;
	private final List<CombinedSet> data;
	public TrainSet(){
		this.classifier=new ArrayList<>();
		this.features=new ArrayList<>();
		this.smallFeatures=new ArrayList<>();
		this.data=new ArrayList<>();
	}
	public TrainSet(List<String> classifier,List<String> features,List<String> smallFeatures,List<CombinedSet> data){
		this.classifier=classifier;
		this.features=features;
		this.smallFeatures=smallFeatures;
		this.data=data;
	}
	public List<String> getClassifier(){
		return classifier;
	}
	public List<CombinedSet> getData(){
		return data;
	}
	public List<String> getFeatures(){
		return features;
	}
	public static TrainSet load(File in) throws Exception{
		return load(new FileInputStream(in));
	}
	public static TrainSet load(InputStream in) throws Exception{
		List<String> classifier=null;
		List<String> features=null;
		List<String> smallFeatures=null;
		List<CombinedSet> data=null;
		Document document=DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
		NodeList roots=document.getDocumentElement().getChildNodes();
		for(int i=0;i<roots.getLength();i++){
			Node item=roots.item(i);
			if(item instanceof Element){
				if(item.getNodeName().equals("models")){
					classifier=loadChildren(item);
				}else if(item.getNodeName().equals("features")){
					features=loadChildren(item);
				}else if(item.getNodeName().equals("small-features")){
					smallFeatures=loadChildren(item);
				}else if(item.getNodeName().equals("data")){
					data=loadData(item);
				}
			}
		}
		return new TrainSet(classifier,features,smallFeatures,data);
	}
	public void train(File directory){
		train(directory,true);
	}
	public void train(File directory,boolean addSimpleSample){
		train(addSimpleSample,false).train(classifier,directory);
		train(false,true).train(classifier,new File(directory.getParentFile(),directory.getName()+"_small"));
	}
	public DataSet train(boolean addSpecialSample,boolean smallOnly){
		DataSet dataSet=new DataSet(smallOnly?smallFeatures:features);
		for(CombinedSet combinedSet:data){
			combinedSet.apply(dataSet,smallOnly);
		}
		if(addSpecialSample){
			new SpecialCharacter(4.0f).addTo(dataSet);
		}
		return dataSet;
	}
	private static List<CombinedSet> loadData(Node element){
		List<CombinedSet> children=new ArrayList<>();
		NodeList childNodes=element.getChildNodes();
		for(int i=0;i<childNodes.getLength();i++){
			Node item=childNodes.item(i);
			if(item instanceof Element&&((Element)item).getNodeName().equals("set")){
				children.add(CombinedSet.loadSet(item));
			}
		}
		return children;
	}
	private static List<String> loadChildren(Node element){
		List<String> children=new ArrayList<>();
		NodeList childNodes=element.getChildNodes();
		for(int i=0;i<childNodes.getLength();i++){
			Node item=childNodes.item(i);
			if(item instanceof Element){
				children.add(item.getTextContent());
			}
		}
		return children;
	}
	public void save(OutputStream out) throws Exception{
		XMLStreamWriter writer=XMLOutputFactory.newFactory().createXMLStreamWriter(out,"UTF-8");
		writer.writeStartDocument();
		writer.writeStartElement("training");
		writer.writeStartElement("models");
		for(String object:classifier){
			writer.writeStartElement("model");
			writer.writeCharacters(object);
			writer.writeEndElement();
		}
		writer.writeEndElement();
		writer.writeStartElement("features");
		for(String object:features){
			writer.writeStartElement("feature");
			writer.writeCharacters(object);
			writer.writeEndElement();
		}
		writer.writeEndElement();
		writer.writeStartElement("data");
		for(CombinedSet object:data){
			writer.writeStartElement("set");
			object.save(writer);
			writer.writeEndElement();
		}
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndDocument();
	}
	public static class CombinedSet{
		private final Font[] base;
		private final float[] size;
		private final int[] style;
		private final AffineTransform[] transform;
		private final CodeSet[] codePoints;
		public CombinedSet(Font[] base,float[] size,int[] style,AffineTransform[] transform,CodeSet[] codePoints){
			this.base=base;
			this.size=size;
			this.style=style;
			this.transform=transform;
			this.codePoints=codePoints;
		}
		public void apply(DataSet data,boolean smallOnly){
			for(Font font:base){
				for(float siz:size){
					for(int sty:style){
						for(AffineTransform affineTransform:transform){
							Font f=font.deriveFont(sty,siz).deriveFont(affineTransform);
							double threhold=getSmallThrehold(f);
							for(CodeSet codePoint:codePoints){
								for(int c:codePoint.codePoints){
									if(!smallOnly||isSmall(c,f,threhold)){
										data.addSample(f,c,c+codePoint.getDiff());
									}
								}
							}
						}
					}
				}
			}
		}
		private final FontRenderContext context=new FontRenderContext(null,false,false);
		private double getSmallThrehold(Font font){
			if(font.canDisplay('A')){
				return font.getLineMetrics("A",context).getAscent()/3;
			}
			return 0;
		}
		private boolean isSmall(int c,Font font,double threhold){
			Rectangle2D box=font.createGlyphVector(context,new String(new int[]{c},0,1)).getPixelBounds(context,0,0);
			return box.getWidth()<threhold&&box.getHeight()<threhold;
		}
		private static CombinedSet loadSet(Node element){
			Font[] base=null;
			float[] size=null;
			int[] style=null;
			AffineTransform[] transform=null;
			CodeSet[] codePoints=null;
			NodeList childNodes=element.getChildNodes();
			for(int i=0;i<childNodes.getLength();i++){
				Node item=childNodes.item(i);
				if(item instanceof Element){
					String name=((Element)item).getNodeName();
					if(name.equals("fonts")){
						base=loadChildren(item).stream().map((f)->{
							try{
								return Font.createFont(f.endsWith(".otf")||f.endsWith(".ttf")||f.endsWith(".ttc")?Font.TRUETYPE_FONT:Font.TYPE1_FONT,new File(f));
							}catch(FontFormatException|IOException ex){
								Logger.getLogger(TrainSet.class.getName()).log(Level.SEVERE,null,ex);
								return Font.decode(f);
							}
						}).toArray(Font[]::new);
					}else if(name.equals("sizes")){
						List<Float> tmp=loadChildren(item).stream().map((s)->Float.parseFloat(s)).collect(Collectors.toList());
						size=new float[tmp.size()];
						for(int j=0;j<size.length;j++){
							size[j]=tmp.get(j);
						}
					}else if(name.equals("scales")){
						transform=loadChildren(item).stream().map((s)->{
							double factor=Double.parseDouble(s);
							return AffineTransform.getScaleInstance(factor,factor);
						}).toArray(AffineTransform[]::new);
					}else if(name.equals("styles")){
						style=loadChildren(item).stream().mapToInt((s)->{
							switch(s){
								case "BOLD":
									return Font.BOLD;
								case "ITALIC":
									return Font.ITALIC;
								case "BOLD_ITALIC":
									return Font.ITALIC|Font.BOLD;
								default:
									return Font.PLAIN;
							}
						}).toArray();
					}else if(name.equals("codepoints")){
						List<CodeSet> points=new ArrayList<>();
						NodeList sub=item.getChildNodes();
						for(int j=0;j<sub.getLength();j++){
							if(sub.item(j) instanceof Element){
								points.add(CodeSet.loadCodeSet((Element)sub.item(j)));
							}
						}
						codePoints=points.toArray(new CodeSet[points.size()]);
					}
				}
			}
			return new CombinedSet(base,size,style,transform,codePoints);
		}
		private void save(XMLStreamWriter writer) throws XMLStreamException{
			writer.writeStartElement("fonts");
			for(Font font:base){
				writer.writeStartElement("font");
				writer.writeCharacters(font.getFontName());
				writer.writeEndElement();
			}
			writer.writeEndElement();
			writer.writeStartElement("sizes");
			for(float s:size){
				writer.writeStartElement("size");
				writer.writeCharacters(Float.toString(s));
				writer.writeEndElement();
			}
			writer.writeEndElement();
			writer.writeStartElement("styles");
			for(int s:style){
				writer.writeStartElement("style");
				if((s&Font.ITALIC)!=0){
					if((s&Font.BOLD)!=0){
						writer.writeCharacters("BOLD_ITALIC");
					}else{
						writer.writeCharacters("ITALIC");
					}
				}else if((s&Font.BOLD)!=0){
					writer.writeCharacters("BOLD");
				}else{
					writer.writeCharacters("PLAIN");
				}
				writer.writeEndElement();
			}
			writer.writeEndElement();
			writer.writeStartElement("scales");
			for(AffineTransform s:transform){
				writer.writeStartElement("scale");
				writer.writeCharacters(Double.toString(s.getScaleX()));
				writer.writeEndElement();
			}
			writer.writeEndElement();
			writer.writeStartElement("codepoints");
			for(CodeSet s:codePoints){
				s.save(writer);
			}
			writer.writeEndElement();
		}
	}
	public static class CodeSet{
		private final int[] codePoints;
		private final int diff;
		public CodeSet(int[] codePoints,int diff){
			this.codePoints=codePoints;
			this.diff=diff;
		}
		public CodeSet(int start,int end,int mappedStart,String regex){
			diff=mappedStart-start;
			if(regex==null){
				codePoints=IntStream.range(start,end+1).toArray();
			}else{
				Predicate<String> pred=Pattern.compile(regex).asPredicate();
				codePoints=IntStream.range(start,end+1).filter((c)->pred.test(new String(new int[]{c},0,1))).toArray();
			}
		}
		public int[] getCodePoints(){
			return codePoints;
		}
		public int getDiff(){
			return diff;
		}
		private static CodeSet loadCodeSet(Element element){
			if(element.getNodeName().equals("range")){
				int start=element.hasAttribute("start")?Integer.parseInt(element.getAttribute("start"),16):1;
				int end=element.hasAttribute("end")?Integer.parseInt(element.getAttribute("end"),16):0x10FFFF;
				int mappedStart=element.hasAttribute("map")?Integer.parseInt(element.getAttribute("map"),16):start;
				String regex=element.hasAttribute("filter")?element.getAttribute("filter"):null;
				return new CodeSet(start,end,mappedStart,regex);
			}else if(element.getNodeName().equals("list")){
				int diff=element.hasAttribute("diff")?Integer.parseInt(element.getAttribute("diff"),16):0;
				int[] codePoints=element.getTextContent().codePoints().filter((c)->!Character.isWhitespace(c)).toArray();
				return new CodeSet(codePoints,diff);
			}else{
				return new CodeSet(new int[0],0);
			}
		}
		private void save(XMLStreamWriter writer) throws XMLStreamException{
			writer.writeStartElement("list");
			if(diff!=0){
				writer.writeAttribute("diff",Integer.toString(diff));
			}
			writer.writeCharacters(new String(codePoints,0,codePoints.length));
			writer.writeEndElement();
		}
	}
}

