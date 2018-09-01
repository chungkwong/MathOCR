/* ImageBlock.java
 * =========================================================================
 * This file is originally part of the MathOCR Project
 *
 * Copyright (C) 2015 Chan Chung Kwong
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 */
package net.sf.mathocr.layout;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import static net.sf.mathocr.Environment.env;
/**
 * A physical block type representing image
 */
public final class ImageBlock implements BlockType{
   /**A Image Block*/
	public static final ImageBlock DEFAULT_BLOCK=new ImageBlock();
	/**
	 * Add a Image to the page
	 * @param block the physical block
	 */
	public void recognize(Block block){
		try{
			File file=File.createTempFile("image",".png",new File(env.getString("OUTPUT_FOLDER")));
			ImageIO.write(block.getPage().getInputImage().getSubimage(block.getLeft(),block.getTop(),block.getWidth(),block.getHeight()),"png",file);
			block.getPage().getLogicalBlocks().addLast(new Image(file.getAbsolutePath(),"",block.getLeft(),block.getRight(),block.getTop(),block.getBottom()));
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	public String toString(){
		return "IMAGE";
	}
}