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
import com.github.chungkwong.mathocr.text.structure.*;
import java.util.*;
/**
 *
 * @author Chan Chung Kwong
 */
public class NaiveLineValidator implements LineValidator{
	public static final String NAME="NAIVE";
	@Override
	public double validate(Span output){
		if(output instanceof Symbol){
			return ((Symbol)output).getConfidence();
		}else if(output instanceof Line){
			Line span=(Line)output;
			double score=1.0;
			for(Span cell:span.getSpans()){
				score*=validate(cell);
			}
			return score;
		}else if(output instanceof Fraction){
			Fraction span=(Fraction)output;
			return validate(span.getNumerator())*validate(span.getDenominator());
		}else if(output instanceof Radical){
			Radical span=(Radical)output;
			double score=validate(span.getRadicand());
			if(span.getPower()!=null){
				score*=validate(span.getPower());
			}
			return score;
		}else if(output instanceof Matrix){
			Matrix span=(Matrix)output;
			double score=1.0;
			for(List<Span> row:span.getMatrix()){
				for(Span cell:row){
					score*=validate(cell);
				}
			}
			return score;
		}else if(output instanceof Over){
			Over span=(Over)output;
			return validate(span.getContent())*validate(span.getOver());
		}else if(output instanceof Under){
			Under span=(Under)output;
			return validate(span.getContent())*validate(span.getUnder());
		}else if(output instanceof UnderOver){
			UnderOver span=(UnderOver)output;
			return validate(span.getContent())*validate(span.getUnder())*validate(span.getOver());
		}else{
			return 0.5;
		}
	}
}
