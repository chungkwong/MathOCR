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
import com.github.chungkwong.mathocr.character.*;
import com.github.chungkwong.mathocr.common.*;
import com.github.chungkwong.mathocr.text.structure.*;
import java.util.*;
/**
 *
 * @author Chan Chung Kwong
 */
public class GraphLineAnalyzer implements LineAnalyzer{
	public static final String NAME="GRAPH";
	private static final int HORIZONTAL=0, SUBSCRIPT=1, SUPERSCRIPT=2, UNDER=3, UPPER=4;
	@Override
	public Line analysis(List<NavigableSet<CharacterCandidate>> characters){
		Span span=analysisMatrix(characters);
		if(span!=null){
			return new Line(Arrays.asList(span));
		}else{
			return analysisLine(characters);
		}
	}
	private Span analysisMatrix(List<NavigableSet<CharacterCandidate>> characters){
		return null;
	}
	private Line analysisLine(List<NavigableSet<CharacterCandidate>> characters){
		for(NavigableSet<CharacterCandidate> character:characters){
			if(character.first().getCodePoint()=='\u221A'){
			}
		}
		return null;
	}
	private static boolean isHorizontalLine(int codepoint){
		return codepoint==0x5f||codepoint==0x9f||codepoint==0x2010||codepoint==0x2013||codepoint==0x2014||codepoint==0x2212;
	}
	private static int getRelationship(CharacterCandidate child,CharacterCandidate parent){
		BoundBox record=child.getBox();
		BoundBox parentRecord=parent.getBox();
		if(Math.min(record.getRight(),parentRecord.getRight())-Math.max(record.getLeft(),parentRecord.getLeft())>Math.min(record.getWidth(),parentRecord.getWidth())*0.9){
			if(record.getTop()>parentRecord.getBottom()){
				return UNDER;
			}else if(record.getBottom()<parentRecord.getTop()){
				return UPPER;
			}
		}
		if(record.getLeft()<=parentRecord.getRight()&&parentRecord.getLeft()<=record.getRight()){
			if(isPunct(child.getCodePoint())){
				return UPPER;
			}else if(isPunct(parent.getCodePoint())){
				return UNDER;
			}
		}
		if(isPunct(child.getCodePoint())||isPunct(parent.getCodePoint())||"−-=".indexOf(parent.getCodePoint())!=-1||child.getCodePoint()=='='){
			return HORIZONTAL;
		}
		double scale=1.0/getHeight(parent);
		return geuss(getVector(child,parent));
	}
	private static final double[] PROBABILITY={
		528348/0.009861529660392148,
		1624/0.1040702141517769,
		4568/0.13751945042954178,
		15001/0.013195586738944665,
		8903/0.027787839624836656,
		10/0.03218247825547289,
		2/2.3352062410536323E-11};
	private static final double[][] MEAN={
		{1.0135851617645684,8.383093131682563E-4},
		{0.6981082097177692,-0.5600019555303543},
		{0.7803256850738456,0.06626289511917687},
		{0.6747105237471085,0.14521376635106567},
		{0.7515797205037746,-0.23817089113652665},
		{0.5826839009682183,-0.24853120309597915},
		{0.5716590297356976,0.1294603524457424}
	};
	private static final double[][][] INVERSE={
		{{24.691897478823577,-39.318012662812706},{-39.318012662812706,479.0521704647488}},
		{{14.696752648571493,15.418378773373924},{15.418378773373924,22.4578394462599}},
		{{5.954351567308771,-0.610403675236202},{-0.610403675236202,8.943071816596268}},
		{{61.03638849122157,-38.84997225490616},{-38.84997225490616,118.8204349130987}},
		{{34.5467574161527,-29.8233989085476},{-29.8233989085476,63.233047170290405}},
		{{96.91875428112918,-1.8057101215910305},{-1.8057101215910305,9.9957955033575}},
		{{9.635430802333482E15,7.194030500933896E16},{7.194030500933896E16,5.3712258341216954E17}}
	};
	private static boolean isPunct(int codepoint){
		return "¯′″˜".indexOf(codepoint)!=-1;
	}
	private static int geuss(double[] record){
		int bestIndex=0;
		double bestValue=-1;
		for(int t=0;t<PROBABILITY.length;t++){
			if(t!=HORIZONTAL&&t!=SUBSCRIPT&&t!=SUPERSCRIPT){
				continue;
			}
			double tmp=0.0;
			for(int i=0;i<record.length;i++){
				for(int j=0;j<record.length;j++){
					tmp+=(record[i]-MEAN[t][i])*INVERSE[t][i][j]*(record[j]-MEAN[t][j]);
				}
			}
			double value=PROBABILITY[t]*Math.exp(-0.5*tmp);
			if(value>bestValue){
				bestIndex=t;
				bestValue=value;
			}
		}
		return bestIndex;
	}
	private static double[] getVector(CharacterCandidate record,CharacterCandidate parentRecord){
		double scale=1.0/getHeight(parentRecord);
		return new double[]{
			getHeight(record)*scale,
			(getBaseline(record)-getBaseline(parentRecord))*scale};
	}
	private static double getHeight(CharacterCandidate record){
		CharacterPrototype character=ModelManager.getCharacterList().getCharacter(record.getCodePoint());
		if(character!=null){
			return ((double)character.getFontSize())*Math.max(record.getBox().getWidth(),record.getBox().getHeight())/1024;
		}else{
			return Math.max(record.getBox().getWidth(),record.getBox().getHeight());
		}
	}
	private static double getBaseline(CharacterCandidate record){
		CharacterPrototype character=ModelManager.getCharacterList().getCharacter(record.getCodePoint());
		if(character!=null){
			return record.getBox().getBottom()-record.getBox().getHeight()*character.getBox().getBottom()/character.getBox().getHeight();
		}else{
			return record.getBox().getBottom();
		}
	}
}
