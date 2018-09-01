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
import java.nio.file.*;
import java.util.*;
import java.util.logging.*;
import java.util.stream.*;
/**
 *
 * @author Chan Chung Kwong
 */
public class Icdar2017Test extends LocalizationTest{
	private static final String IMAGES="../datasets/icdar2017/ch8_validation_images";
	private static final String TRANSCRIPT="../datasets/icdar2017/ch8_validation_localization_transcription_gt_v2";
	@Override
	protected Iterator<File> getSamples(){
		return Arrays.stream(new File(IMAGES).listFiles()).iterator();
	}
	@Override
	protected List<BoundBox> getTruthBoxes(File file){
		return getTranscript(file).filter((line)->!line[9].equals("###")).
				map(this::getBoundBox).collect(Collectors.toList());
	}
	@Override
	protected List<BoundBox> getIgnoredBoxes(File file){
		return getTranscript(file).filter((line)->line[9].equals("###")).
				map(this::getBoundBox).collect(Collectors.toList());
	}
	private Stream<String[]> getTranscript(File image){
		try{
			return Files.lines(new File(TRANSCRIPT,"gt_"+image.getName().substring(0,image.getName().indexOf('.'))+".txt").toPath()).map((line)->line.split(",",10));
		}catch(IOException ex){
			Logger.getLogger(Icdar2017Test.class.getName()).log(Level.SEVERE,null,ex);
			return Stream.empty();
		}
	}
	private BoundBox getBoundBox(String[] line){
		int x1=Integer.parseInt(line[0]), y1=Integer.parseInt(line[1]);
		int x2=Integer.parseInt(line[2]), y2=Integer.parseInt(line[3]);
		int x3=Integer.parseInt(line[4]), y3=Integer.parseInt(line[5]);
		int x4=Integer.parseInt(line[6]), y4=Integer.parseInt(line[7]);
		return new BoundBox((x1+x4)/2,(x2+x3)/2,(y1+y2)/2,(y3+y4)/2);
	}
	public static void main(String[] args){
		new Icdar2017Test().test(new ContrastDetector());
	}
}
