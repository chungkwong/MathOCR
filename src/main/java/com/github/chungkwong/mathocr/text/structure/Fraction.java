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
package com.github.chungkwong.mathocr.text.structure;
import com.github.chungkwong.mathocr.common.BoundBox;
/**
 * Fraction
 *
 * @author Chan Chung Kwong
 */
public class Fraction extends Span{
	private final Span numerator, denominator;
	/**
	 * Create a fraction
	 *
	 * @param numerator
	 * @param denominator
	 */
	public Fraction(Span numerator,Span denominator){
		super(BoundBox.union(numerator.getBox(),denominator.getBox()),
				(numerator.getBox().getBottom()+denominator.getBox().getTop())/2);
		this.numerator=numerator;
		this.denominator=denominator;
	}
	/**
	 * @return numerator
	 */
	public Span getNumerator(){
		return numerator;
	}
	/**
	 * @return denominator
	 */
	public Span getDenominator(){
		return denominator;
	}
	@Override
	public String toString(){
		return '('+numerator.toString()+")/("+denominator.toString()+')';
	}
	@Override
	public boolean isBaseLineReliable(){
		return false;
	}
	@Override
	public int getFontSize(){
		return (numerator.getFontSize()+denominator.getFontSize())/2;
	}
}
