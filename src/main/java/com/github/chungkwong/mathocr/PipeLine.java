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
package com.github.chungkwong.mathocr;
import com.github.chungkwong.mathocr.text.LineSegmenters;
import com.github.chungkwong.mathocr.text.TextLine;
import com.github.chungkwong.mathocr.preprocess.skew.SkewDetectors;
import com.github.chungkwong.mathocr.preprocess.Preprocessor;
import com.github.chungkwong.mathocr.preprocess.CombinedPreprocessor;
import com.github.chungkwong.mathocr.preprocess.Rotate;
import com.github.chungkwong.mathocr.layout.physical.PhysicalBlock;
import com.github.chungkwong.mathocr.layout.physical.BlockClassifiers;
import com.github.chungkwong.mathocr.layout.physical.PageSegmenters;
import com.github.chungkwong.mathocr.layout.physical.BlockOrderers;
import com.github.chungkwong.mathocr.layout.physical.BlockRecognizers;
import com.github.chungkwong.mathocr.layout.logical.DocumentEncoders;
import com.github.chungkwong.mathocr.layout.logical.DocumentAssemblers;
import com.github.chungkwong.mathocr.layout.logical.Document;
import com.github.chungkwong.mathocr.layout.logical.LogicalBlock;
import com.github.chungkwong.mathocr.layout.logical.Page;
import com.github.chungkwong.mathocr.layout.logical.PageAnalyzers;
import com.github.chungkwong.mathocr.common.ComponentPool;
import java.awt.image.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.stream.*;
import javax.imageio.*;
/**
 * Recognition pipeline
 *
 * @author Chan Chung Kwong
 */
public class PipeLine{
	private final Iterator<? extends Object> sources;
	private final List<Page> pages=new LinkedList<>();
	private BufferedImage input, modified;
	private ComponentPool components;
	private List<PhysicalBlock> physicalBlocks;
	private Map<PhysicalBlock,String> types;
	private HashMap<PhysicalBlock,List<TextLine>> lines;
	private List<LogicalBlock> logicalBlocks;
	private Object source;
	/**
	 * Create a recognition pipeline
	 *
	 * @param src iterate over page images(BufferedImage/InputStream/URL/File)
	 */
	public PipeLine(Iterator<? extends Object> src){
		this.sources=src;
	}
	/**
	 * Check if there are unprocessed page
	 *
	 * @return
	 */
	public boolean hasNextPage(){
		return sources.hasNext();
	}
	/**
	 * Prepare for next page
	 */
	public void nextPage(){
		if(source!=null){
			pages.add(new Page(getLogicalBlocks()));
			logicalBlocks=null;
		}
		source=sources.next();
		modified=input=load(source);
	}
	/**
	 * @return source of current page
	 */
	public Object getSource(){
		return source;
	}
	/**
	 * @return the input page image
	 */
	public BufferedImage getInputImage(){
		return input;
	}
	/**
	 * @return the modified page image
	 */
	public BufferedImage getModifiedImage(){
		return modified;
	}
	/**
	 * Preprocess page image using default procedure
	 */
	public void preprocessByDefault(){
		modified=CombinedPreprocessor.getDefaultCombinedPreprocessor().apply(modified,false);
		double angle=SkewDetectors.REGISTRY.get().detect(modified);
		BufferedImage binaryNoSkew, colorNoSkew;
		if(Math.abs(angle)>Math.PI/900){
			Rotate correct=new Rotate(angle);
			modified=correct.apply(modified,true);
			input=correct.apply(input,true);
		}
	}
	/**
	 * Preprocess page image
	 *
	 * @param preprocessor preprocessor to be applied
	 */
	public void preprocess(Preprocessor preprocessor){
		modified=preprocessor.apply(modified,true);
	}
	/**
	 * Preprocess input page image, being used to keep alignment with modified
	 * image
	 *
	 * @param preprocessor preprocessor to be applied
	 */
	public void preprocessInput(Preprocessor preprocessor){
		input=preprocessor.apply(input,true);
	}
	/**
	 * Undo all preprocessing by reloading page image from source
	 */
	public void undo(){
		modified=input=load(source);
	}
	/**
	 * @return connected components
	 */
	public ComponentPool getComponents(){
		if(components==null){
			if(getModifiedImage().getType()!=BufferedImage.TYPE_BYTE_BINARY){
				preprocessByDefault();
			}
			components=new ComponentPool(getModifiedImage());
			modified=null;
		}
		return components;
	}
	/**
	 * @return physical blocks
	 */
	public List<PhysicalBlock> getPhysicalBlocks(){
		if(physicalBlocks==null){
			physicalBlocks=BlockOrderers.REGISTRY.get().order(
					PageSegmenters.REGISTRY.get().segment(getComponents()));
			components=null;
		}
		return physicalBlocks;
	}
	/**
	 * Sort physical block to read order
	 */
	public void sortPhysicalBlocks(){
		physicalBlocks=BlockOrderers.REGISTRY.get().order(getPhysicalBlocks());
	}
	/**
	 * @return mapping between physical blocks and their geussed type
	 */
	public Map<PhysicalBlock,String> getPhysicalBlockTypes(){
		if(types==null){
			types=new HashMap<>();
			getPhysicalBlocks().forEach((p)->types.put(p,BlockClassifiers.REGISTRY.get().classify(p,getInputImage())));
		}
		return types;
	}
	/**
	 * @return logical blocks
	 */
	public List<LogicalBlock> getLogicalBlocks(){
		if(logicalBlocks==null){
			logicalBlocks=getPhysicalBlocks().stream().
					flatMap((p)->BlockRecognizers.REGISTRY.get(getPhysicalBlockTypes().get(p)).recognize(p,getInputImage()).stream()).collect(Collectors.toList());
			logicalBlocks=PageAnalyzers.REGISTRY.get().analysis(logicalBlocks);
			physicalBlocks=null;
			types=null;
			lines=null;
		}
		return logicalBlocks;
	}
	/**
	 * @return mapping between physical blocks and its contained text lines
	 */
	public HashMap<PhysicalBlock,List<TextLine>> getTextLine(){
		if(lines==null&&physicalBlocks!=null){
			lines=new HashMap<>();
			getPhysicalBlocks().forEach((p)->lines.put(p,LineSegmenters.REGISTRY.get().segment(p)));
		}
		return lines;
	}
	/**
	 * @return recognition result
	 */
	public String getResult(){
		while(sources.hasNext()){
			nextPage();
		}
		if(source!=null){
			pages.add(new Page(getLogicalBlocks()));
		}
		Document document=DocumentAssemblers.REGISTRY.get().assemble(pages);
		return DocumentEncoders.REGISTRY.get().encode(document);
	}
	private static BufferedImage load(Object src){
		try{
			if(src instanceof File){
				return ImageIO.read((File)src);
			}else if(src instanceof InputStream){
				return ImageIO.read((InputStream)src);
			}else if(src instanceof URL){
				return ImageIO.read((URL)src);
			}else if(src instanceof BufferedImage){
				return (BufferedImage)src;
			}else{
				throw new RuntimeException("Input type not supported");
			}
		}catch(Exception ex){
			throw new RuntimeException(ex);
		}
	}
}
