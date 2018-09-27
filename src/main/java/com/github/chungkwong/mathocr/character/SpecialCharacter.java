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
import com.github.chungkwong.mathocr.common.*;
import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.util.List;
import java.io.*;
import java.util.logging.*;
/**
 *
 * @author Chan Chung Kwong
 */
public class SpecialCharacter{
	private Font cmex;
	private ConnectedComponent parenlefttp, parenleftbt, parenleftex, parenrighttp, parenrightbt, parenrightex,
			bracketlefttp, bracketleftbt, bracketleftex, bracketrighttp, bracketrightbt, bracketrightex,
			bracelefttp, braceleftmid, braceleftbt, bracerighttp, bracerightmid, bracerightbt, braceex,
			bracedownleft, bracedownright, braceupleft, braceupright, rightarrow, leftarrow,
			surd1, surd2, surd3, surd4, radicalbt, radicalvertex, radicaltp;
	public SpecialCharacter(float scale){
		try{
			cmex=Font.createFont(Font.TYPE1_FONT,SpecialCharacter.class.getResourceAsStream("cmex10.pfb")).deriveFont(10.0f).deriveFont(AffineTransform.getScaleInstance(scale,scale));
			parenlefttp=getComponent(cmex,0x31);
			parenleftbt=getComponent(cmex,0x41);
			parenleftex=getComponent(cmex,0x43);
			parenrighttp=getComponent(cmex,0x32);
			parenrightbt=getComponent(cmex,0x42);
			parenrightex=getComponent(cmex,0x44);
			bracketlefttp=getComponent(cmex,0x33);
			bracketleftbt=getComponent(cmex,0x35);
			bracketleftex=getComponent(cmex,0x37);
			bracketrighttp=getComponent(cmex,0x34);
			bracketrightbt=getComponent(cmex,0x36);
			bracketrightex=getComponent(cmex,0x38);
			bracelefttp=getComponent(cmex,0x39);
			braceleftmid=getComponent(cmex,0x3d);
			braceleftbt=getComponent(cmex,0x3b);
			bracerighttp=getComponent(cmex,0x3a);
			bracerightmid=getComponent(cmex,0x3e);
			bracerightbt=getComponent(cmex,0x3c);
			braceex=getComponent(cmex,0x3f);
			bracedownleft=getComponent(cmex,0x7b);
			bracedownright=getComponent(cmex,0x7c);
			braceupleft=getComponent(cmex,0x7d);
			braceupright=getComponent(cmex,0x7e);
			rightarrow=getComponent(cmex,0x00);
			leftarrow=getComponent(cmex,0x00);
			surd1=getComponent(cmex,0x71);
			surd2=getComponent(cmex,0x72);
			surd3=getComponent(cmex,0x73);
			surd4=getComponent(cmex,0x74);
			radicalbt=getComponent(cmex,0x75);
			radicalvertex=getComponent(cmex,0x76);
			radicaltp=getComponent(cmex,0x77);
		}catch(FontFormatException|IOException ex){
			Logger.getLogger(SpecialCharacter.class.getName()).log(Level.SEVERE,null,ex);
		}
	}
	public void addTo(DataSet set){
		addTo(getComponent(cmex,0x01),'(',set);
		addTo(getComponent(cmex,0x02),')',set);
		addTo(getComponent(cmex,0x03),'[',set);
		addTo(getComponent(cmex,0x04),']',set);
		addTo(getComponent(cmex,0x05),'⌊',set);
		addTo(getComponent(cmex,0x06),'⌋',set);
		addTo(getComponent(cmex,0x07),'⌈',set);
		addTo(getComponent(cmex,0x08),'⌉',set);
		addTo(getComponent(cmex,0x09),'{',set);
		addTo(getComponent(cmex,0x0A),'}',set);
		addTo(getComponent(cmex,0x0B),'〈',set);
		addTo(getComponent(cmex,0x0C),'〉',set);
		addTo(getComponent(cmex,0x0D),'|',set);
		addTo(getComponent(cmex,0x0E),'∥',set);
		addTo(getComponent(cmex,0x0F),'/',set);
		addTo(getComponent(cmex,0x10),'\\',set);
		addTo(getComponent(cmex,0x11),'(',set);
		addTo(getComponent(cmex,0x12),')',set);
		addTo(getComponent(cmex,0x13),'(',set);
		addTo(getComponent(cmex,0x14),')',set);
		addTo(getComponent(cmex,0x15),'[',set);
		addTo(getComponent(cmex,0x16),']',set);
		addTo(getComponent(cmex,0x17),'⌊',set);
		addTo(getComponent(cmex,0x18),'⌋',set);
		addTo(getComponent(cmex,0x19),'⌈',set);
		addTo(getComponent(cmex,0x1A),'⌉',set);
		addTo(getComponent(cmex,0x1B),'{',set);
		addTo(getComponent(cmex,0x1C),'}',set);
		addTo(getComponent(cmex,0x1D),'〈',set);
		addTo(getComponent(cmex,0x1E),'〉',set);
		addTo(getComponent(cmex,0x1F),'/',set);
		addTo(getComponent(cmex,0x20),'\\',set);
		addTo(getComponent(cmex,0x21),'(',set);
		addTo(getComponent(cmex,0x22),')',set);
		addTo(getComponent(cmex,0x23),'[',set);
		addTo(getComponent(cmex,0x24),']',set);
		addTo(getComponent(cmex,0x25),'⌊',set);
		addTo(getComponent(cmex,0x26),'⌋',set);
		addTo(getComponent(cmex,0x27),'⌈',set);
		addTo(getComponent(cmex,0x28),'⌉',set);
		addTo(getComponent(cmex,0x29),'{',set);
		addTo(getComponent(cmex,0x2A),'}',set);
		addTo(getComponent(cmex,0x2B),'〈',set);
		addTo(getComponent(cmex,0x2C),'〉',set);
		addTo(getComponent(cmex,0x2D),'/',set);
		addTo(getComponent(cmex,0x2E),'\\',set);
		addTo(getComponent(cmex,0x2F),'/',set);
		addTo(getComponent(cmex,0x30),'\\',set);
		addTo(getComponent(cmex,0x45),'〈',set);
		addTo(getComponent(cmex,0x46),'〉',set);
		addTo(getComponent(cmex,0x47),'∐',set);
		addTo(getComponent(cmex,0x48),'∐',set);
		addTo(getComponent(cmex,0x49),'∮',set);
		addTo(getComponent(cmex,0x4a),'∮',set);
		addTo(getComponent(cmex,0x4b),'⨀',set);
		addTo(getComponent(cmex,0x4c),'⨀',set);
		addTo(getComponent(cmex,0x4d),'⊕',set);
		addTo(getComponent(cmex,0x4e),'⊕',set);
		addTo(getComponent(cmex,0x4f),'⊖',set);
		addTo(getComponent(cmex,0x50),'⊖',set);
		addTo(getComponent(cmex,0x51),'∑',set);
		addTo(getComponent(cmex,0x52),'∏',set);
		addTo(getComponent(cmex,0x53),'∫',set);
		addTo(getComponent(cmex,0x54),'∪',set);
		addTo(getComponent(cmex,0x55),'∩',set);
		addTo(getComponent(cmex,0x56),'⊎',set);
		addTo(getComponent(cmex,0x57),'∧',set);
		addTo(getComponent(cmex,0x58),'∨',set);
		addTo(getComponent(cmex,0x59),'∑',set);
		addTo(getComponent(cmex,0x5a),'∏',set);
		addTo(getComponent(cmex,0x5b),'∫',set);
		addTo(getComponent(cmex,0x5c),'∪',set);
		addTo(getComponent(cmex,0x5d),'∩',set);
		addTo(getComponent(cmex,0x5e),'⊎',set);
		addTo(getComponent(cmex,0x5f),'∧',set);
		addTo(getComponent(cmex,0x60),'∨',set);
		addTo(getComponent(cmex,0x61),'∐',set);
		addTo(getComponent(cmex,0x62),'∐',set);
		addTo(getComponent(cmex,0x63),'˄',set);
		addTo(getComponent(cmex,0x64),'˄',set);
		addTo(getComponent(cmex,0x65),'˄',set);
		addTo(getComponent(cmex,0x66),'˜',set);
		addTo(getComponent(cmex,0x67),'˜',set);
		addTo(getComponent(cmex,0x68),'˜',set);
		addTo(getComponent(cmex,0x69),'[',set);
		addTo(getComponent(cmex,0x6a),']',set);
		addTo(getComponent(cmex,0x6b),'⌊',set);
		addTo(getComponent(cmex,0x6c),'⌋',set);
		addTo(getComponent(cmex,0x6d),'⌈',set);
		addTo(getComponent(cmex,0x6e),'⌉',set);
		addTo(getComponent(cmex,0x6f),'{',set);
		addTo(getComponent(cmex,0x70),'}',set);
		addTo(getComponent(cmex,0x78),'∥',set);
		addTo(getComponent(cmex,0x79),'↑',set);
		addTo(getComponent(cmex,0x7a),'↓',set);
		addTo(getComponent(cmex,0x7f),'⇑',set);
		addTo(getComponent(cmex,0x80),'⇓',set);
		for(int ex=1;ex<10;ex*=2){
			addToAfterFix(getLeftParen(ex),'(',set);
			addToAfterFix(getLeftBracket(ex),'[',set);
			addToAfterFix(getLeftBrace(ex),'{',set);
			addToAfterFix(getLeftCeil(ex),'⌈',set);
			addToAfterFix(getLeftFloor(ex),'⌊',set);
			addToAfterFix(getRightParen(ex),')',set);
			addToAfterFix(getRightBracket(ex),']',set);
			addToAfterFix(getRightBrace(ex),'}',set);
			addToAfterFix(getRightCeil(ex),'⌉',set);
			addToAfterFix(getRightFloor(ex),'⌋',set);
			addToAfterFix(getOverBrace(ex),'⏞',set);
			addToAfterFix(getUnderBrace(ex),'⏟',set);
			addToAfterFix(getHorizontalLine(ex),'—',set);
			addToAfterFix(getVerticalLine(ex),'|',set);
			addToAfterFix(getRootSign1(ex),'√',set);
			addToAfterFix(getRootSign2(ex),'√',set);
			addToAfterFix(getRootSign3(ex),'√',set);
			addToAfterFix(getRootSign4(ex),'√',set);
			addToAfterFix(getRootSign5(ex,2*ex),'√',set);
		}
	}
	private static void addTo(ConnectedComponent ele,int codePoint,DataSet set){
		set.addSample(new CharacterPrototype(codePoint,new BoundBox(0,ele.getWidth()-1,-ele.getHeight()+1,0),Font.SERIF,ele.getHeight(),Font.PLAIN),ele);
	}
	private static void addToAfterFix(ConnectedComponent ele,int codePoint,DataSet set){
		ele.fix();
		set.addSample(new CharacterPrototype(codePoint,new BoundBox(0,ele.getWidth()-1,-ele.getHeight()+1,0),Font.SERIF,ele.getHeight(),Font.PLAIN),ele);
	}
	private static ConnectedComponent getComponent(Font font,int glyphCode){
		FontRenderContext context=new FontRenderContext(null,false,true);
		GlyphVector glyphVector=font.createGlyphVector(context,new int[]{glyphCode});
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
	private ConnectedComponent getLeftParen(int ex){
		int w=Math.max(parenlefttp.getWidth(),parenleftbt.getWidth());
		int h=parenlefttp.getHeight()+ex*parenleftex.getHeight()+parenleftbt.getHeight();
		ConnectedComponent e=new ConnectedComponent(0,w-1,0,h-1);
		List<RunLength> runlengths=e.getRunLengths();
		runlengths.addAll(parenlefttp.getRunLengths());
		for(int k=0, i=parenlefttp.getHeight();k<ex;k++,i+=parenleftex.getHeight()){
			for(RunLength rl:parenleftex.getRunLengths()){
				runlengths.add(new RunLength(rl.getY()+i,rl.getX(),rl.getCount()));
			}
		}
		int i0=h-parenleftbt.getHeight();
		for(RunLength rl:parenleftbt.getRunLengths()){
			runlengths.add(new RunLength(rl.getY()+i0,rl.getX(),rl.getCount()));
		}
		return e;
	}
	private ConnectedComponent getRightParen(int ex){
		int w=Math.max(parenrighttp.getWidth(),parenrightbt.getWidth());
		int h=parenrighttp.getHeight()+ex*parenrightex.getHeight()+parenrightbt.getHeight();
		int j=w-parenrightex.getWidth();
		ConnectedComponent e=new ConnectedComponent(0,w-1,0,h-1);
		List<RunLength> runlengths=e.getRunLengths();
		runlengths.addAll(parenrighttp.getRunLengths());
		for(int k=0, i=parenrighttp.getHeight();k<ex;k++,i+=parenrightex.getHeight()){
			for(RunLength rl:parenrightex.getRunLengths()){
				runlengths.add(new RunLength(rl.getY()+i,rl.getX()+j,rl.getCount()));
			}
		}
		int i0=h-parenrightbt.getHeight();
		for(RunLength rl:parenrightbt.getRunLengths()){
			runlengths.add(new RunLength(rl.getY()+i0,rl.getX(),rl.getCount()));
		}
		return e;
	}
	private ConnectedComponent getLeftBracket(int ex){
		int w=Math.max(bracketlefttp.getWidth(),bracketleftbt.getWidth());
		int h=bracketlefttp.getHeight()+ex*bracketleftex.getHeight()+bracketleftbt.getHeight();
		ConnectedComponent e=new ConnectedComponent(0,w-1,0,h-1);
		List<RunLength> runlengths=e.getRunLengths();
		runlengths.addAll(bracketlefttp.getRunLengths());
		for(int k=0, i=bracketlefttp.getHeight();k<ex;k++,i+=bracketleftex.getHeight()){
			for(RunLength rl:bracketleftex.getRunLengths()){
				runlengths.add(new RunLength(rl.getY()+i,rl.getX(),rl.getCount()));
			}
		}
		int i0=h-bracketleftbt.getHeight();
		for(RunLength rl:bracketleftbt.getRunLengths()){
			runlengths.add(new RunLength(rl.getY()+i0,rl.getX(),rl.getCount()));
		}
		return e;
	}
	private ConnectedComponent getRightBracket(int ex){
		int w=Math.max(bracketrighttp.getWidth(),bracketrightbt.getWidth());
		int h=bracketrighttp.getHeight()+ex*bracketrightex.getHeight()+bracketrightbt.getHeight();
		int j=w-bracketrightex.getWidth();
		ConnectedComponent e=new ConnectedComponent(0,w-1,0,h-1);
		List<RunLength> runlengths=e.getRunLengths();
		runlengths.addAll(bracketrighttp.getRunLengths());
		for(int k=0, i=bracketrighttp.getHeight();k<ex;k++,i+=bracketrightex.getHeight()){
			for(RunLength rl:bracketrightex.getRunLengths()){
				runlengths.add(new RunLength(rl.getY()+i,rl.getX()+j,rl.getCount()));
			}
		}
		int i0=h-bracketrightbt.getHeight();
		for(RunLength rl:bracketrightbt.getRunLengths()){
			runlengths.add(new RunLength(rl.getY()+i0,rl.getX(),rl.getCount()));
		}
		return e;
	}
	public ConnectedComponent getLeftBrace(int ex){
		int w=Math.max(bracelefttp.getWidth(),braceleftbt.getWidth())+braceleftmid.getWidth()-braceex.getWidth();
		int h=bracelefttp.getHeight()+braceleftbt.getHeight()+braceleftmid.getHeight()+2*ex*braceex.getHeight();
		int j=braceleftmid.getWidth()-braceex.getWidth();
		ConnectedComponent e=new ConnectedComponent(0,w-1,0,h-1);
		List<RunLength> runlengths=e.getRunLengths();
		int dx=w-bracelefttp.getWidth();
		for(RunLength rl:bracelefttp.getRunLengths()){
			runlengths.add(new RunLength(rl.getY(),rl.getX()+dx,rl.getCount()));
		}
		int i=bracelefttp.getHeight();
		for(int k=0;k<ex;k++,i+=braceex.getHeight()){
			for(RunLength rl:braceex.getRunLengths()){
				runlengths.add(new RunLength(rl.getY()+i,rl.getX()+j,rl.getCount()));
			}
		}
		for(RunLength rl:braceleftmid.getRunLengths()){
			runlengths.add(new RunLength(rl.getY()+i,rl.getX(),rl.getCount()));
		}
		i+=braceleftmid.getHeight();
		for(int k=0;k<ex;k++,i+=braceex.getHeight()){
			for(RunLength rl:braceex.getRunLengths()){
				runlengths.add(new RunLength(rl.getY()+i,rl.getX()+j,rl.getCount()));
			}
		}
		dx=w-braceleftbt.getWidth();
		for(RunLength rl:braceleftbt.getRunLengths()){
			runlengths.add(new RunLength(rl.getY()+i,rl.getX()+dx,rl.getCount()));
		}
		return e;
	}
	public ConnectedComponent getRightBrace(int ex){
		int w=Math.max(bracerighttp.getWidth(),bracerightbt.getWidth())+bracerightmid.getWidth()-braceex.getWidth();
		int h=bracerighttp.getHeight()+braceleftbt.getHeight()+bracerightmid.getHeight()+2*ex*braceex.getHeight();
		int j=bracerighttp.getWidth()-braceex.getWidth();
		ConnectedComponent e=new ConnectedComponent(0,w-1,0,h-1);
		List<RunLength> runlengths=e.getRunLengths();
		runlengths.addAll(bracerighttp.getRunLengths());
		int i=bracerighttp.getHeight();
		for(int k=0;k<ex;k++,i+=braceex.getHeight()){
			for(RunLength rl:braceex.getRunLengths()){
				runlengths.add(new RunLength(rl.getY()+i,rl.getX()+j,rl.getCount()));
			}
		}
		j=w-bracerightmid.getWidth();
		for(RunLength rl:bracerightmid.getRunLengths()){
			runlengths.add(new RunLength(rl.getY()+i,rl.getX()+j,rl.getCount()));
		}
		i+=bracerightmid.getHeight();
		j=bracerightbt.getWidth()-braceex.getWidth();
		for(int k=0;k<ex;k++,i+=braceex.getHeight()){
			for(RunLength rl:braceex.getRunLengths()){
				runlengths.add(new RunLength(rl.getY()+i,rl.getX()+j,rl.getCount()));
			}
		}
		for(RunLength rl:bracerightbt.getRunLengths()){
			runlengths.add(new RunLength(rl.getY()+i,rl.getX(),rl.getCount()));
		}
		return e;
	}
	public ConnectedComponent getLeftCeil(int ex){
		int w=bracketlefttp.getWidth();
		int h=bracketlefttp.getHeight()+ex*bracketleftex.getHeight();
		ConnectedComponent e=new ConnectedComponent(0,w-1,0,h-1);
		List<RunLength> runlengths=e.getRunLengths();
		runlengths.addAll(bracketlefttp.getRunLengths());
		for(int k=0, i=bracketlefttp.getHeight();k<ex;k++,i+=bracketleftex.getHeight()){
			for(RunLength rl:bracketleftex.getRunLengths()){
				runlengths.add(new RunLength(rl.getY()+i,rl.getX(),rl.getCount()));
			}
		}
		return e;
	}
	public ConnectedComponent getRightCeil(int ex){
		int w=bracketrighttp.getWidth();
		int h=bracketrighttp.getHeight()+ex*bracketrightex.getHeight();
		int j=w-bracketrightex.getWidth();
		ConnectedComponent e=new ConnectedComponent(0,w-1,0,h-1);
		List<RunLength> runlengths=e.getRunLengths();
		runlengths.addAll(bracketrighttp.getRunLengths());
		for(int k=0, i=bracketrighttp.getHeight();k<ex;k++,i+=bracketrightex.getHeight()){
			for(RunLength rl:bracketrightex.getRunLengths()){
				runlengths.add(new RunLength(rl.getY()+i,rl.getX()+j,rl.getCount()));
			}
		}
		return e;
	}
	private ConnectedComponent getLeftFloor(int ex){
		int w=bracketleftbt.getWidth();
		int h=ex*bracketleftex.getHeight()+bracketleftbt.getHeight();
		ConnectedComponent e=new ConnectedComponent(0,w-1,0,h-1);
		List<RunLength> runlengths=e.getRunLengths();
		for(int k=0, i=0;k<ex;k++,i+=bracketleftex.getHeight()){
			for(RunLength rl:bracketleftex.getRunLengths()){
				runlengths.add(new RunLength(rl.getY()+i,rl.getX(),rl.getCount()));
			}
		}
		int i0=h-bracketleftbt.getHeight();
		for(RunLength rl:bracketleftbt.getRunLengths()){
			runlengths.add(new RunLength(rl.getY()+i0,rl.getX(),rl.getCount()));
		}
		return e;
	}
	private ConnectedComponent getRightFloor(int ex){
		int w=bracketrightbt.getWidth();
		int h=bracketrightbt.getHeight()+ex*bracketrightex.getHeight();
		int j=w-bracketrightex.getWidth();
		ConnectedComponent e=new ConnectedComponent(0,w-1,0,h-1);
		List<RunLength> runlengths=e.getRunLengths();
		for(int k=0, i=0;k<ex;k++,i+=bracketrightex.getHeight()){
			for(RunLength rl:bracketrightex.getRunLengths()){
				runlengths.add(new RunLength(rl.getY()+i,rl.getX()+j,rl.getCount()));
			}
		}
		int i0=h-bracketrightbt.getHeight();
		for(RunLength rl:bracketrightbt.getRunLengths()){
			runlengths.add(new RunLength(rl.getY()+i0,rl.getX(),rl.getCount()));
		}
		return e;
	}
	public ConnectedComponent getHorizontalLine(int ex){
		int h=bracketleftex.getWidth(), w=bracketleftex.getHeight()*ex;
		ConnectedComponent e=new ConnectedComponent(0,w-1,0,h-1);
		List<RunLength> runlengths=e.getRunLengths();
		for(int i=0;i<h;i++){
			runlengths.add(new RunLength(i,0,w-1));
		}
		return e;
	}
	public ConnectedComponent getVerticalLine(int ex){
		int h=bracketleftex.getHeight()*ex, w=bracketleftex.getWidth();
		ConnectedComponent e=new ConnectedComponent(0,w-1,0,h-1);
		List<RunLength> runlengths=e.getRunLengths();
		for(int i=0;i<h;i++){
			runlengths.add(new RunLength(i,0,w-1));
		}
		return e;
	}
	public ConnectedComponent getUnderBrace(int ex){
		int h=braceupleft.getHeight()+bracedownright.getHeight()-braceex.getWidth();
		int w=braceupleft.getWidth()+bracedownright.getWidth()+braceupright.getWidth()+bracedownleft.getWidth()+braceex.getHeight()*2*ex;
		ConnectedComponent e=new ConnectedComponent(0,w-1,0,h-1);
		List<RunLength> runlengths=e.getRunLengths();
		runlengths.addAll(braceupleft.getRunLengths());
		int dx=braceupleft.getWidth(), dy=ex*braceex.getHeight()-1;
		for(int i=h-bracedownright.getHeight();i<braceupleft.getHeight();i++){
			runlengths.add(new RunLength(i,dx,dy));
		}
		dx+=dy;
		dy=h-bracedownright.getHeight();
		for(RunLength rl:bracedownright.getRunLengths()){
			runlengths.add(new RunLength(rl.getY()+dy,rl.getX()+dx,rl.getCount()));
		}
		dx+=bracedownright.getWidth();
		dy=h-bracedownleft.getHeight();
		for(RunLength rl:bracedownleft.getRunLengths()){
			runlengths.add(new RunLength(rl.getY()+dy,rl.getX()+dx,rl.getCount()));
		}
		dx+=bracedownleft.getWidth();
		dy=ex*braceex.getHeight()-1;
		for(int i=h-bracedownleft.getHeight();i<braceupright.getHeight();i++){
			runlengths.add(new RunLength(i,dx,dy));
		}
		dx=w-braceupright.getWidth();
		for(RunLength rl:braceupright.getRunLengths()){
			runlengths.add(new RunLength(rl.getY(),rl.getX()+dx,rl.getCount()));
		}
		return e;
	}
	public ConnectedComponent getOverBrace(int ex){
		int h=braceupleft.getHeight()+bracedownright.getHeight()-braceex.getWidth();
		int w=braceupleft.getWidth()+bracedownright.getWidth()+braceupright.getWidth()+bracedownleft.getWidth()+braceex.getHeight()*2*ex;
		ConnectedComponent e=new ConnectedComponent(0,w-1,0,h-1);
		List<RunLength> runlengths=e.getRunLengths();
		int dy=dy=h-bracedownleft.getHeight();
		for(RunLength rl:bracedownleft.getRunLengths()){
			runlengths.add(new RunLength(rl.getY()+dy,rl.getX(),rl.getCount()));
		}
		int dx=bracedownleft.getWidth();
		dy=ex*braceex.getHeight()-1;
		for(int i=h-bracedownright.getHeight();i<braceupleft.getHeight();i++){
			runlengths.add(new RunLength(i,dx,dy));
		}
		dx+=dy;
		for(RunLength rl:braceupright.getRunLengths()){
			runlengths.add(new RunLength(rl.getY(),rl.getX()+dx,rl.getCount()));
		}
		dx+=braceupright.getWidth();
		for(RunLength rl:braceupleft.getRunLengths()){
			runlengths.add(new RunLength(rl.getY(),rl.getX()+dx,rl.getCount()));
		}
		dx+=braceupleft.getWidth();
		dy=ex*braceex.getHeight()-1;
		for(int i=h-bracedownleft.getHeight();i<braceupright.getHeight();i++){
			runlengths.add(new RunLength(i,dx,dy));
		}
		dx+=dy+1;
		dy=h-bracedownright.getHeight();
		for(RunLength rl:bracedownright.getRunLengths()){
			runlengths.add(new RunLength(rl.getY()+dy,rl.getX()+dx,rl.getCount()));
		}
		return e;
	}
	/*public ConnectedComponent getRightArrow(int ex){
		ConnectedComponent rarrow=map.get("\\rightarrow ").getConnectedComponent();
		int h=rarrow.getHeight(), w=ele.getWidth()*h/ele.getHeight();
		if(w<rarrow.getWidth()){
			return null;
		}
		ConnectedComponent e=new ConnectedComponent(0,w-1,0,h-1);
		List<RunLength> runlengths=e.getRunLengths();
		int dx=w-rarrow.getWidth();
		for(RunLength rl:rarrow.getRunLengths()){
			runlengths.add(new RunLength(rl.getY(),rl.getX()+dx,rl.getCount()));
		}
		--dx;
		for(int i=(h-arrowthick)/2;i<(h+arrowthick)/2;i++){
			runlengths.add(new RunLength(i,0,dx));
		}
		//print(e);
		return makeCandidate(ele,e,"\\rightarrow ");
	}
	public ConnectedComponent getLeftArrow(int ex){
		int h=rightarrow.getHeight(), w=ele.getWidth()*h/ele.getHeight(), x0=.getRight()+1
		, dx=w-1-x0;
		if(w<rarrow.getWidth()){
			return null;
		}
		ConnectedComponent e=new ConnectedComponent(0,w-1,0,h-1);
		List<RunLength> runlengths=e.getRunLengths();
		for(RunLength rl:rarrow.getRunLengths()){
			runlengths.add(rl);
		}
		for(int i=(h-arrowthick)/2;i<(h+arrowthick)/2;i++){
			runlengths.add(new RunLength(i,x0,dx));
		}
		//print(e);
		return makeCandidate(ele,e,"\\rightarrow ");
	}*/
	private ConnectedComponent getRootSign1(int ex){
		ConnectedComponent surd=surd1;
		int h=surd.getHeight();
		int w=surd.getWidth()+ex*bracketleftex.getHeight();
		ConnectedComponent e=new ConnectedComponent(0,w-1,0,h-1);
		List<RunLength> runLengths=e.getRunLengths();
		for(RunLength rl:surd.getRunLengths()){
			if(rl.getY()<bracketleftex.getWidth()){
				runLengths.add(new RunLength(rl.getY(),rl.getX(),w-1-rl.getX()));
			}else{
				runLengths.add(rl);
			}
		}
		return e;
	}
	private ConnectedComponent getRootSign2(int ex){
		ConnectedComponent surd=surd2;
		int h=surd.getHeight();
		int w=surd.getWidth()+ex*bracketleftex.getHeight();
		ConnectedComponent e=new ConnectedComponent(0,w-1,0,h-1);
		List<RunLength> runLengths=e.getRunLengths();
		for(RunLength rl:surd.getRunLengths()){
			if(rl.getY()<bracketleftex.getWidth()){
				runLengths.add(new RunLength(rl.getY(),rl.getX(),w-1-rl.getX()));
			}else{
				runLengths.add(rl);
			}
		}
		return e;
	}
	private ConnectedComponent getRootSign3(int ex){
		ConnectedComponent surd=surd3;
		int h=surd.getHeight();
		int w=surd.getWidth()+ex*bracketleftex.getHeight();
		ConnectedComponent e=new ConnectedComponent(0,w-1,0,h-1);
		List<RunLength> runLengths=e.getRunLengths();
		for(RunLength rl:surd.getRunLengths()){
			if(rl.getY()<bracketleftex.getWidth()){
				runLengths.add(new RunLength(rl.getY(),rl.getX(),w-1-rl.getX()));
			}else{
				runLengths.add(rl);
			}
		}
		return e;
	}
	private ConnectedComponent getRootSign4(int ex){
		ConnectedComponent surd=surd4;
		int h=surd.getHeight();
		int w=surd.getWidth()+ex*bracketleftex.getHeight();
		ConnectedComponent e=new ConnectedComponent(0,w-1,0,h-1);
		List<RunLength> runLengths=e.getRunLengths();
		for(RunLength rl:surd.getRunLengths()){
			if(rl.getY()<bracketleftex.getWidth()){
				runLengths.add(new RunLength(rl.getY(),rl.getX(),w-1-rl.getX()));
			}else{
				runLengths.add(rl);
			}
		}
		return e;
	}
	private ConnectedComponent getRootSign5(int exh,int exv){
		int w=radicalbt.getWidth()+radicaltp.getWidth()+exh*radicalvertex.getHeight()-radicalvertex.getWidth();
		int h=radicalbt.getHeight()+radicaltp.getHeight()+exv*radicalvertex.getHeight();
		ConnectedComponent e=new ConnectedComponent(0,w-1,0,h-1);
		List<RunLength> runLengths=e.getRunLengths();
		int j=radicalbt.getWidth()-radicalvertex.getWidth();
		for(RunLength rl:radicaltp.getRunLengths()){
			if(rl.getY()<radicalvertex.getWidth()){
				runLengths.add(new RunLength(rl.getY(),rl.getX()+j,w-1-rl.getX()-j));
			}else{
				runLengths.add(new RunLength(rl.getY(),rl.getX()+j,rl.getCount()));
			}
		}
		int i=radicaltp.getHeight();
		for(int k=0;k<exv;k++,i+=radicalvertex.getHeight()){
			for(RunLength rl:radicalvertex.getRunLengths()){
				runLengths.add(new RunLength(rl.getY()+i,rl.getX()+j,rl.getCount()));
			}
		}
		for(RunLength rl:radicalbt.getRunLengths()){
			runLengths.add(new RunLength(rl.getY()+i,rl.getX(),rl.getCount()));
		}
		return e;
	}
	public static void main(String[] args){
		SpecialCharacter characters=new SpecialCharacter(4);
		System.out.println(characters.getLeftParen(2));
		System.out.println(characters.getLeftBracket(2));
		System.out.println(characters.getLeftBrace(2));
		System.out.println(characters.getLeftFloor(2));
		System.out.println(characters.getLeftCeil(2));
		System.out.println(characters.getRightParen(2));
		System.out.println(characters.getRightBracket(2));
		System.out.println(characters.getRightBrace(2));
		System.out.println(characters.getRightFloor(2));
		System.out.println(characters.getRightCeil(2));
		System.out.println(characters.getRootSign1(2));
		System.out.println(characters.getRootSign2(2));
		System.out.println(characters.getRootSign3(2));
		System.out.println(characters.getRootSign4(2));
		System.out.println(characters.getRootSign5(2,2));
		System.out.println(characters.getUnderBrace(2));
		System.out.println(characters.getOverBrace(2));
	}
}
