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
import com.github.chungkwong.mathocr.character.classifier.*;
import com.github.chungkwong.mathocr.character.feature.*;
import com.github.chungkwong.mathocr.common.*;
import com.github.chungkwong.mathocr.preprocess.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import javax.imageio.*;
import javax.xml.parsers.*;
import libsvm.*;
import org.w3c.dom.*;
/**
 *
 * @author Chan Chung Kwong
 */
public class TextualClassifier{
	private final svm_model model;
	public TextualClassifier(svm_model model){
		this.model=model;
	}
	public boolean isTextual(ConnectedComponent ele){
		return (int)(svm.svm_predict(model,getFeature(ele))+0.5)==1;
	}
	public static svm_node[] getFeature(ConnectedComponent ele){
		double[] extract=new Gradient(5,5).extract(ele);
		svm_node[] vector=new svm_node[extract.length];
		for(int i=0;i<extract.length;i++){
			svm_node node0=new svm_node();
			node0.index=i;
			node0.value=extract[i];
			vector[i]=node0;
		}
		return vector;
	}
	public static void main(String[] args) throws Exception{
		test();
	}
	private static final String MODEL_FILE=new File(System.getProperty("user.home"),".mathocr/TEXTUAL").getAbsolutePath();
	private static void train() throws IOException,Exception{
		svm_problem problem=new svm_problem();
		List<svm_node[]> textData=new LinkedList<>();
		List<svm_node[]> nonTextData=new LinkedList<>();
		Icdar2003 dataset=new Icdar2003();
		for(File sample:dataset){
			System.out.println(sample.getName());
			List<BoundBox> boxes=dataset.getBoxes(sample);
			BufferedImage image=ImageIO.read(sample);
			BufferedImage side=CombinedPreprocessor.getDefaultCombinedPreprocessor().apply(image,false);
			for(ConnectedComponent ele:new ComponentPool(side).getComponents()){
				BoundBox box=ele.getBox();
				if(ele.getWidth()>16&&ele.getHeight()>16){
					if(boxes.stream().allMatch((b)->!BoundBox.isIntersect(box,b))){
						nonTextData.add(getFeature(ele));
					}
				}
			}
			image=new ColorInvert(false).apply(image,true);
			side=CombinedPreprocessor.getDefaultCombinedPreprocessor().apply(image,true);
			for(ConnectedComponent ele:new ComponentPool(side).getComponents()){
				BoundBox box=ele.getBox();
				if(ele.getWidth()>16&&ele.getHeight()>16){
					if(boxes.stream().allMatch((b)->!BoundBox.isIntersect(box,b))){
						nonTextData.add(getFeature(ele));
					}
				}
			}
		}
		System.out.println(textData.size()+":"+nonTextData.size());
		int count=textData.size()+nonTextData.size();
		problem.l=count;
		problem.x=new svm_node[count][];
		problem.y=new double[count];
		int i=0;
		for(svm_node[] features:nonTextData){
			problem.x[i]=features;
			problem.y[i]=0;
			++i;
		}
		for(svm_node[] features:textData){
			problem.x[i]=features;
			problem.y[i]=1;
			++i;
		}
		svm_parameter param=new SvmModelType().getParameter();
		param.gamma=1/(problem.x[0].length+0.0);
		param.C=1;
		svm_model model=svm.svm_train(problem,param);
		svm.svm_save_model(MODEL_FILE,model);
	}
	private static void test() throws Exception{
		TextualClassifier classifier=new TextualClassifier(svm.svm_load_model(MODEL_FILE));
		Icdar2003 dataset=new Icdar2003();
		ConfusionMatrix matrix=new ConfusionMatrix();
		for(File sample:dataset){
			System.out.println(sample.getName());
			List<BoundBox> boxes=dataset.getBoxes(sample);
			BufferedImage image=ImageIO.read(sample);
			BufferedImage side=CombinedPreprocessor.getDefaultCombinedPreprocessor().apply(image,false);
			for(ConnectedComponent ele:new ComponentPool(side).getComponents()){
				BoundBox box=ele.getBox();
				if(ele.getWidth()>16&&ele.getHeight()>16){
					if(boxes.stream().allMatch((b)->!BoundBox.isIntersect(box,b)||box.getHeight()>b.getHeight()+10||box.getWidth()>b.getWidth()+10)){
						matrix.advanceFrequency(0,classifier.isTextual(ele)?1:0);
					}else if(boxes.stream().anyMatch((b)->BoundBox.isContaining(b,box))){
						matrix.advanceFrequency(1,classifier.isTextual(ele)?1:0);
					}
				}
			}
			image=new ColorInvert(false).apply(image,true);
			side=CombinedPreprocessor.getDefaultCombinedPreprocessor().apply(image,true);
			for(ConnectedComponent ele:new ComponentPool(side).getComponents()){
				BoundBox box=ele.getBox();
				if(ele.getWidth()>16&&ele.getHeight()>16){
					if(boxes.stream().allMatch((b)->!BoundBox.isIntersect(box,b)||box.getHeight()>b.getHeight()+10||box.getWidth()>b.getWidth()+10)){
						matrix.advanceFrequency(0,classifier.isTextual(ele)?1:0);
					}else if(boxes.stream().anyMatch((b)->BoundBox.isContaining(b,box))){
						matrix.advanceFrequency(1,classifier.isTextual(ele)?1:0);
					}
				}
			}
		}
		System.out.println(matrix);
	}
	public static interface GroundTruth extends Iterable<File>{
		List<BoundBox> getBoxes(File file);
	}
	private static class Icdar2003 implements GroundTruth{
		private static final String DIR="../datasets/scene/scene/SceneTrialTrain/";
		private final Document document;
		public Icdar2003() throws Exception{
			DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
			factory.setIgnoringElementContentWhitespace(true);
			document=factory.newDocumentBuilder().parse(new File(DIR,"locations.xml"));
		}
		@Override
		public Iterator<File> iterator(){
			List<File> files=new ArrayList<>();
			NodeList images=document.getElementsByTagName("image");
			for(int i=0;i<images.getLength();i++){
				NodeList image=images.item(i).getChildNodes();
				for(int j=0;j<image.getLength();j++){
					if(image.item(j).getNodeName().equals("imageName")){
						String path=image.item(j).getTextContent();
						files.add(new File(DIR+path));
						break;
					}
				}
			}
			return files.iterator();
		}
		@Override
		public List<BoundBox> getBoxes(File file){
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
	}
}
