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
import com.github.chungkwong.mathocr.character.classifier.*;
import com.github.chungkwong.mathocr.character.feature.*;
import com.github.chungkwong.mathocr.common.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.nio.*;
import java.nio.charset.*;
import java.util.*;
import java.util.logging.*;
import javax.swing.*;
/**
 * Assume that the
 * dataset(http://www.nlpr.ia.ac.cn/databases/download/feature_data/HWDB1.1tst_gnt.zip
 * and
 * http://www.nlpr.ia.ac.cn/databases/download/feature_data/HWDB1.1trn_gnt.zip)
 * is unpacked at <code>../datasets</code>
 *
 * @author Chan Chung Kwong
 */
public class HwdbViewer extends JPanel implements ActionListener{
	private final JLabel identity=new JLabel();
	private final JLabel bitmap=new JLabel();
	private final JButton next=new JButton("NEXT");
	private final CharacterIterator iterator=new CharacterIterator(true);
	public HwdbViewer() throws FileNotFoundException{
		super(new BorderLayout());
		add(identity,BorderLayout.NORTH);
		add(new JScrollPane(bitmap),BorderLayout.CENTER);
		next.addActionListener(this);
		add(next,BorderLayout.SOUTH);
	}
	@Override
	public void actionPerformed(ActionEvent e){
		if(iterator.next()){
			identity.setText(new String(new int[]{iterator.getCodePoint()},0,1));
			bitmap.setIcon(new ImageIcon(iterator.getImage()));
		}
	}
	private static final Collection<String> FEATURES=Arrays.asList(Gradient.NAME,AspectRatio.NAME,Moments.NAME,CrossNumber.NAME,Grid.NAME);
	private static void train(){
		DataSet dataSet=new DataSet(FEATURES);
		CharacterIterator characterIterator=new CharacterIterator(false);
		while(characterIterator.next()){
			ConnectedComponent component=new ConnectedComponent(characterIterator.getImage());
			CharacterPrototype prototype=new CharacterPrototype(characterIterator.getCodePoint(),component.getBox(),Font.SERIF,1024,Font.PLAIN);
			dataSet.addSample(prototype,component);
		}
		dataSet.train(Collections.singleton(LinearModelType.NAME),new File(System.getProperty("user.home"),".mathocr/hw"));
	}
	private static void test(){
		SingleCharacterTest test=SingleCharacterTest.loadModel(new LinearClassifier(1),new File(System.getProperty("user.home"),".mathocr/hw"));
		CharacterIterator characterIterator=new CharacterIterator(false);
		int count=0;
		while(characterIterator.next()){
			ConnectedComponent component=new ConnectedComponent(characterIterator.getImage());
			test.addSample(component,characterIterator.getCodePoint());
			if(++count%10000==0){
				System.out.println(test.getConfusionMatrix().getAccuracy());
			}
		}
		test.printResult();
	}
	public static void main(String[] args) throws FileNotFoundException{
		/*JFrame f=new JFrame();
		f.getContentPane().add(new HwdbViewer());
		f.setExtendedState(JFrame.MAXIMIZED_BOTH);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);*/
		//train();
		test();
	}
	private static class CharacterIterator{
		private Iterator<File> files;
		private DataInputStream in;
		private int codePoint;
		private BufferedImage image;
		private final CharsetDecoder gb=Charset.forName("GB2312").newDecoder();
		int count=0;
		public CharacterIterator(boolean test){
			if(test){
				files=Arrays.asList(new File("../datasets/hwdb/HWDB1.1tst_gnt/").listFiles()).iterator();
			}else{
				files=Arrays.asList(new File("../datasets/hwdb/HWDB1.1trn_gnt/").listFiles()).iterator();
			}
		}
		public boolean next(){
			try{
				int size=in.readInt();
				byte c0=in.readByte();
				byte c1=in.readByte();
				codePoint=gb.decode(ByteBuffer.wrap(new byte[]{c0,c1})).codePoints().findFirst().getAsInt();
				int width=in.read()|(in.read()<<8);
				int height=in.read()|(in.read()<<8);
				if(width<=0||width>=200||height<=0||height>=200){
					System.out.println(width+"x"+height);
				}
				int len=width*height;
				int[] rgb=new int[len];
				for(int ind=0;ind<len;ind++){
					int gray=in.read();
					rgb[ind]=gray==255?0xFFFFFFFF:0xFF000000;
				}
				image=new BufferedImage(width,height,BufferedImage.TYPE_BYTE_BINARY);
				image.setRGB(0,0,width,height,rgb,0,width);
				if(++count%100==0){
					System.out.println(count);
				}
				return true;
			}catch(Exception ex){
				Logger.getLogger(HwdbViewer.class.getName()).log(Level.SEVERE,null,ex);
				try{
					if(in!=null){
						in.close();
					}
					if(files.hasNext()){
						File file=files.next();
						System.out.println(file);
						in=new DataInputStream(new FileInputStream(file));
						return next();
					}else{
						return false;
					}
				}catch(IOException ex1){
					Logger.getLogger(HwdbViewer.class.getName()).log(Level.SEVERE,null,ex1);
					return false;
				}
			}
		}
		public int getCodePoint(){
			return codePoint;
		}
		public BufferedImage getImage(){
			return image;
		}
	}
}
