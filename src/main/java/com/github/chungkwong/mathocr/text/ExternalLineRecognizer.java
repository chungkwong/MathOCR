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
import com.github.chungkwong.mathocr.text.structure.Line;
import java.awt.image.*;
import java.io.*;
import java.nio.file.Files;
import java.util.logging.*;
import javax.imageio.*;
import com.github.chungkwong.mathocr.text.LineRecognizer;
import java.util.*;

/**
 * Line recognizer using external command
 *
 * @author Chan Chung Kwong
 */
public class ExternalLineRecognizer implements LineRecognizer{
	private final String prefix, suffix;
	/**
	 * Create a line recognizer
	 *
	 * @param prefix prefix part of the command before file name
	 * @param suffix suffix part of the command after file name
	 */
	public ExternalLineRecognizer(String prefix,String suffix){
		this.prefix=prefix;
		this.suffix=suffix;
	}
	@Override
	public Line recognize(TextLine block,BufferedImage input){
		try{
			File file=Files.createTempFile("mathocr",".pbm").toFile();
			ImageIO.write(input.getSubimage(block.getBox().getLeft(),block.getBox().getTop(),block.getBox().getRight()-block.getBox().getLeft()+1,block.getBox().getBottom()-block.getBox().getTop()+1),"pnm",file);
			Process process=Runtime.getRuntime().exec(prefix+file.getAbsolutePath()+suffix);
			process.waitFor();
			StringBuilder buf;
			try(BufferedReader in=new BufferedReader(new InputStreamReader(process.getInputStream()))){
				buf=new StringBuilder();
				String str;
				while((str=in.readLine())!=null){
					buf.append(str);
				}
			}
			file.delete();
			return Line.fromLine(buf,block.getBox());
		}catch(Exception ex){
			System.err.println("Check if "+prefix+" is installed.");
			Logger.getGlobal().log(Level.SEVERE,"",ex);
			return new Line(Collections.emptyList());
		}
	}
}
