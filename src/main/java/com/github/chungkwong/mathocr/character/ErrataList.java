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
import com.github.chungkwong.json.*;
import com.github.chungkwong.mathocr.common.*;
import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;
import java.util.logging.*;
import java.util.stream.*;
/**
 *
 * @author Chan Chung Kwong
 */
public class ErrataList{
	private static final Comparator<Pair<Integer,Double>> COMPARATOR=Comparator.comparingDouble((Pair<Integer,Double> p)->p.getValue()).reversed();
	private final Map<Integer,NavigableSet<Pair<Integer,Double>>> erratas;
	public ErrataList(){
		erratas=new HashMap<>();
	}
	private ErrataList(Map<Integer,NavigableSet<Pair<Integer,Double>>> erratas){
		this.erratas=erratas;
	}
	public NavigableSet<Pair<Integer,Double>> getErrata(int codePoint){
		return erratas.getOrDefault(codePoint,Collections.emptyNavigableSet());
	}
	public static ErrataList generate(Iterator<Pair<Integer,ConnectedComponent>> iterator,CharacterRecognizer recognizer,long support,double threhold){
		ConfusionMatrix matrix=new ConfusionMatrix();
		Object model=ModelManager.getModel(recognizer.getModelType());
		CharacterList characterList=ModelManager.getCharacterList();
		while(iterator.hasNext()){
			Pair<Integer,ConnectedComponent> next=iterator.next();
			NavigableSet<CharacterCandidate> candidates=recognizer.recognize(next.getValue(),model,characterList);
			if(!candidates.isEmpty()){
				matrix.advanceFrequency(next.getKey(),candidates.first().getCodePoint());
			}
		}
		return generate(matrix,support,threhold);
	}
	public static ErrataList generate(ConfusionMatrix matrix,long support,double threhold){
		Frequencies<Integer> predicted=new Frequencies<>();
		matrix.getRaw().toMap().forEach((k,v)->predicted.advanceFrequency(k.getValue(),v.getCount()));
		Map<Integer,NavigableSet<Pair<Integer,Double>>> erratas=new HashMap<>();
		matrix.getRaw().toMap().forEach((k,v)->{
			if(v.getCount()>=support&&!Objects.equals(k.getKey(),k.getValue())){
				double p=((double)v.getCount())/predicted.getFrequency(k.getValue());
				if(p>=threhold){
					if(!erratas.containsKey(k.getValue())){
						erratas.put(k.getValue(),new TreeSet<>(COMPARATOR));
					}
					erratas.get(k.getValue()).add(new Pair<>(k.getKey(),p));
				}
			}
		});
		return new ErrataList(erratas);
	}
	private static Font[] getFontCombination(Font[] base,float[] size,int[] style,AffineTransform[] transform){
		Font[] fonts=new Font[base.length*size.length*style.length*transform.length];
		int i=0;
		for(Font font:base){
			for(float f:size){
				for(int j:style){
					for(AffineTransform trans:transform){
						fonts[i++]=font.deriveFont(j,f).deriveFont(trans);
					}
				}
			}
		}
		return fonts;
	}
	public static Iterator<Pair<Integer,ConnectedComponent>> getSamples(Font[] fonts,int[] codePoints){
		return Arrays.stream(fonts).flatMap((font)->Arrays.stream(codePoints).mapToObj((c)->new Pair<>(c,getComponent(font,c)))).iterator();
	}
	private static ConnectedComponent getComponent(Font font,int codePoint){
		FontRenderContext context=new FontRenderContext(null,false,true);
		GlyphVector glyphVector=font.createGlyphVector(context,new String(new int[]{codePoint},0,1));
		float x=(float)glyphVector.getVisualBounds().getX();
		float y=(float)glyphVector.getVisualBounds().getY();
		int width=(int)(glyphVector.getVisualBounds().getWidth()+0.5);
		int height=(int)(glyphVector.getVisualBounds().getHeight()+0.5);
		if(width==0||height==0){
			return new ConnectedComponent();
		}
		BufferedImage bi=new BufferedImage(width,height,BufferedImage.TYPE_BYTE_BINARY);
		Graphics2D g2d=bi.createGraphics();
		g2d.setColor(Color.WHITE);
		g2d.fillRect(0,0,width,height);
		g2d.setColor(Color.BLACK);
		g2d.drawGlyphVector(glyphVector,-x,-y);
		return new ConnectedComponent(bi);
	}
	public void write(File file) throws IOException{
		try(BufferedWriter out=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file),StandardCharsets.UTF_8))){
			String json=JSONEncoder.encode(erratas,new POJOWalker(){
				@Override
				public boolean isMap(Object o){
					return o instanceof HashMap;
				}
				@Override
				public boolean isList(Object o){
					return o instanceof NavigableSet;
				}
				@Override
				public Iterator<Map.Entry<?,?>> getEntryIterator(Object o){
					return (Iterator)((HashMap<Integer,NavigableSet<Pair<Integer,Double>>>)o).entrySet().stream().map((e)->new Pair<>(new String(new int[]{e.getKey()},0,1),e.getValue())).iterator();
				}
				@Override
				public Iterator<?> getComponentIterator(Object o){
					return ((NavigableSet<Pair<Integer,Double>>)o).stream().
							flatMap((p)->Stream.of(new String(new int[]{p.getKey()},0,1),p.getValue())).iterator();
				}
			});
			out.write(json);
		}
	}
	public static ErrataList read(File file){
		try{
			String json=Files.lines(file.toPath(),StandardCharsets.UTF_8).collect(Collectors.joining("\n"));
			return new ErrataList((Map<Integer,NavigableSet<Pair<Integer,Double>>>)JSONDecoder.walk(json,new JSONWalker<Map,NavigableSet>(){
				@Override
				public Map createMap(){
					return new HashMap<>();
				}
				@Override
				public NavigableSet createList(){
					return new TreeSet<>(COMPARATOR);
				}
				@Override
				public void onEntry(Object value,Object index,Map parent){
					parent.put(((String)index).codePointAt(0),value);
				}
				@Override
				public void onComponent(Object o,int i,NavigableSet l){
					if(i%2==0){
						l.add(new Pair<>(((String)o).codePointAt(0),2.0));
					}else{
						l.add(new Pair<>(((Pair<Integer,Double>)l.pollFirst()).getKey(),((Number)o).doubleValue()));
					}
				}
			}));
		}catch(Exception ex){
			Logger.getLogger(ErrataList.class.getName()).log(Level.INFO,null,ex);
			return new ErrataList();
		}
	}
	public static void main(String[] args) throws IOException{
		/*int[] codePoint="abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".codePoints().toArray();
		Font[] base=new Font[]{Font.decode("FreeMono"),Font.decode("FreeSans"),Font.decode("FreeSerif")};
		float[] size=new float[]{8,10,12};
		int[] style=new int[]{Font.PLAIN,Font.ITALIC,Font.BOLD};
		AffineTransform[] transforms=new AffineTransform[]{AffineTransform.getScaleInstance(2,2),AffineTransform.getScaleInstance(4,4)};
		Font[] fonts=getFontCombination(base,size,style,transforms);
		ErrataList errata=generate(getSamples(fonts,codePoint),new LinearClassifier(1),3,0.1);
		errata.write(new File("/home/kwong/.mathocr/default/erratas.json"));*/
		//System.out.println(read(new File("/home/kwong/.mathocr/default/erratas.json")).erratas);
		//read(new File("/home/kwong/.mathocr/default/erratas.json")).write(new File("/home/kwong/.mathocr/default/erratas2.json"));
	}
}
