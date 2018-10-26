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
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.util.logging.*;
import java.util.stream.*;
import javax.imageio.*;
/**
 * Assume that the
 * dataset(http://rrc.cvc.uab.es/downloads/ch9_validation_images.zip and
 * http://rrc.cvc.uab.es/downloads/ch9_validation_localization_transcription_gt.zip)
 * is unpacked at <code>../datasets/detext</code>
 *
 * @author Chan Chung Kwong
 */
public class DetextTest extends LocalizationTest{
	private static final String IMAGES="../datasets/detext/ch9_validation_images";
	private static final String TRANSCRIPT="../datasets/detext/ch9_validation_localization_transcription_gt";
	@Override
	protected Iterator<File> getSamples(){
		return Arrays.stream(new File(IMAGES).listFiles()).iterator();
	}
	@Override
	protected List<BoundBox> getTruthBoxes(File file){
		return getTranscript(file).filter((line)->!line[8].equals("###")).
				map(this::getBoundBox).collect(Collectors.toList());
	}
	@Override
	protected List<BoundBox> getIgnoredBoxes(File file){
		return getTranscript(file).filter((line)->line[8].equals("###")).
				map(this::getBoundBox).collect(Collectors.toList());
	}
	private Stream<String[]> getTranscript(File image){
		try{
			return Files.lines(new File(TRANSCRIPT,"gt_"+image.getName().substring(0,image.getName().indexOf('.'))+".txt").toPath()).map((line)->line.split(",",9));
		}catch(IOException ex){
			Logger.getLogger(Icdar2017Test.class.getName()).log(Level.SEVERE,null,ex);
			return Stream.empty();
		}
	}
	private BoundBox getBoundBox(String[] line){
		if(line[0].codePointAt(0)==0xFEFF){
			line[0]=line[0].substring(line[0].offsetByCodePoints(0,1),line[0].length());
		}
		int x1=Integer.parseInt(line[0]), y1=Integer.parseInt(line[1]);
		int x2=Integer.parseInt(line[2]), y2=Integer.parseInt(line[3]);
		int x3=Integer.parseInt(line[4]), y3=Integer.parseInt(line[5]);
		int x4=Integer.parseInt(line[6]), y4=Integer.parseInt(line[7]);
		return new BoundBox((x1+x4)/2,(x2+x3)/2,(y1+y2)/2,(y3+y4)/2);
	}
	private static void generateTestResult() throws IOException{
		DetextDetector detextDetector=new DetextDetector();
		for(File listFile:new File("../datasets/detext/ch9_test_images").listFiles()){
			System.out.println(listFile);
			List<LineCandidate> detect=detextDetector.detect(ImageIO.read(listFile));
			StringBuilder buf=new StringBuilder();
			boolean first=true;
			for(LineCandidate lineCandidate:detect){
				if(first){
					first=false;
				}else{
					buf.append("\r\n");
				}
				BoundBox box=lineCandidate.getBox();
				buf.append(box.getLeft()).append(',').append(box.getTop()).append(',');
				buf.append(box.getRight()).append(',').append(box.getTop()).append(',');
				buf.append(box.getRight()).append(',').append(box.getBottom()).append(',');
				buf.append(box.getLeft()).append(',').append(box.getBottom()).append(',');
				buf.append(1.0);
			}
			String fileName="res_"+listFile.getName().substring(0,listFile.getName().indexOf('.'))+".txt";
			Files.write(new File("../datasets/detext/ch9_test_result",fileName).toPath(),buf.toString().getBytes(StandardCharsets.UTF_8));
		}
	}
	public static void main(String[] args) throws IOException{
		//generateTestResult();
		new DetextTest().test(new DetextDetector());
	}
}
