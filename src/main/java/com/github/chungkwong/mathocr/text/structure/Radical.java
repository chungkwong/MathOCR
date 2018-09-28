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
/**
 * Radical expression
 *
 * @author Chan Chung Kwong
 */
public class Radical extends Span{
	private final Span power, radicand;
	/**
	 * Create a radical expression
	 *
	 * @param power
	 * @param radicand
	 */
	public Radical(Span power,Span root,Span radicand){
		super(root.getBox(),radicand.getBaseLine());
		this.power=power;
		this.radicand=radicand;
	}
	/**
	 * @return power
	 */
	public Span getPower(){
		return power;
	}
	/**
	 * @return radicand
	 */
	public Span getRadicand(){
		return radicand;
	}
	@Override
	public String toString(){
		String main="√("+radicand.toString()+")";
		return power!=null?'('+power.toString()+')'+main:main;
	}
	@Override
	public boolean isBaseLineReliable(){
		return radicand.isBaseLineReliable();
	}
	@Override
	public int getFontSize(){
		return radicand.getFontSize();
	}
}
