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
package com.github.chungkwong.mathocr.layout.physical;
import com.github.chungkwong.mathocr.layout.logical.LogicalBlock;
import com.github.chungkwong.mathocr.layout.logical.Image;
import java.awt.image.*;
import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.logging.*;
import javax.imageio.*;
import static com.github.chungkwong.mathocr.Environment.ENVIRONMENT;

/**
 * Image recognizer
 *
 * @author Chan Chung Kwong
 */
public class ImageRecognizer implements BlockRecognizer{
	@Override
	public List<LogicalBlock> recognize(PhysicalBlock block,BufferedImage input){
		try{
			File file=Files.createTempFile(new File(ENVIRONMENT.getString("OUTPUT_FOLDER")).toPath(),"image",".png").toFile();
			ImageIO.write(input.getSubimage(block.getBox().getLeft(),block.getBox().getTop(),block.getBox().getWidth(),block.getBox().getHeight()),"png",file);
			return Collections.singletonList(new Image(file.getAbsolutePath(),null,block.getBox()));
		}catch(Exception ex){
			Logger.getGlobal().log(Level.SEVERE,"",ex);
			return Collections.emptyList();
		}
	}
}
