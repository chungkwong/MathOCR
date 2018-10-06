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
package com.github.chungkwong.mathocr.text;
import java.awt.image.*;
import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;
import java.util.logging.*;
import javax.imageio.*;
/**
 *
 * @author Chan Chung Kwong
 */
public class Im2LatexTest extends FormulaTest{
	private static final File DATASET=new File("../datasets/im2latex");
	private static final File MAPPING=new File(DATASET,"im2latex_test.lst");
	private static final File FORMULAS=new File(DATASET,"im2latex_formulas.norm.lst");
	private static final File IMAGES=new File(DATASET,"images_processed");
	private final Iterator<String> iterator;
	private final ArrayList<String> formulas=new ArrayList<>(103558);
	private File image;
	private String text;
	public Im2LatexTest() throws IOException{
		Files.lines(FORMULAS.toPath(),StandardCharsets.ISO_8859_1).forEach((f)->formulas.add(f));
		iterator=Files.lines(MAPPING.toPath()).iterator();
	}
	@Override
	protected boolean next(){
		if(iterator.hasNext()){
			String[] split=iterator.next().split(" ");
			text=formulas.get(Integer.parseInt(split[0]));
			image=new File(IMAGES,split[1]+".png");
			return true;
		}else{
			return false;
		}
	}
	@Override
	protected BufferedImage getImage(){
		try{
			return ImageIO.read(image);
		}catch(IOException ex){
			Logger.getLogger(Im2LatexTest.class.getName()).log(Level.SEVERE,null,ex);
			return new BufferedImage(10,10,BufferedImage.TYPE_INT_ARGB);
		}
	}
	@Override
	protected String getText(){
		return text;
	}
	public static void main(String[] args) throws IOException{
		new Im2LatexTest().evaluate();
	}
}
