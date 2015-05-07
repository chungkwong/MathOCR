/* SymbolExpression.java
 * =========================================================================
 * This file is originally part of the MathOCR Project
 *
 * Copyright (C) 2015 Chan Chung Kwong
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 */
package net.sf.mathocr.ofr;
import net.sf.mathocr.common.*;
import net.sf.mathocr.ocr.*;
/**
 * A data structure representing a single symbol
 */
public final class SymbolExpression extends Expression{
	Symbol sym;
	Char ch;
	/**
	 * Construct a SymbolExpression
	 * @param ch a character
	 */
	public SymbolExpression(Char ch){
		this.ch=ch;
		this.pleft=ch.getLeft();
		this.pright=ch.getRight();
		this.ptop=ch.getTop();
		this.pbottom=ch.getBottom();
		update(ch.getCandidates().first().getSymbol());
	}
	/**
	 * Construct a SymbolExpression
	 * @param sym the symbol
	 * @param pleft physical left bound
	 * @param pright physical right bound
	 * @param ptop physical lower bound
	 * @param pbottom physical upper bound
	 */
	public SymbolExpression(Symbol sym,int pleft,int pright,int ptop,int pbottom){
		this.pleft=pleft;
		this.pright=pright;
		this.ptop=ptop;
		this.pbottom=pbottom;
		ch=new Char(sym,pleft,pright,ptop,pbottom);
		update(sym);
	}
	/**
	 * Change the symbol and physical bounding box
	 * @param sym new symbol
	 * @param pleft new left physical bound
	 * @param pright new right physical bound
	 * @param ptop new upper physical bound
	 * @param pbottom new lower physical bound
	 */
	public void update(Symbol sym,int pleft,int pright,int ptop,int pbottom){
		this.pleft=pleft;
		this.pright=pright;
		this.ptop=ptop;
		this.pbottom=pbottom;
		update(sym);
	}
	/**
	 * Change the symbol
	 * @param sym new symbol
	 */
	public void update(Symbol sym){
		float pwidth=pright-pleft+1,pheight=pbottom-ptop+1,lwidth=sym.getPixelWidth(),lheight=sym.getPixelHeight();
		scale=lwidth>lheight?pwidth/lwidth:pheight/lheight;
		x=pleft+(int)Math.round(sym.getXOffset()*scale);
		y=ptop+(int)Math.round((sym.getAscent())*scale);
		ltop=y-(int)Math.round(sym.getLogicalAscent()*scale);
		lbottom=ltop+(int)Math.round(sym.getLogicalHeight()*scale);
		lleft=x;
		lright=lleft+(int)Math.round(sym.getLogicalWidth()*scale);
		if(sym.isVarSize()){
			sup=(ptop+y)/2;
			sub=pbottom;
		}else{
			sup=y-(lbottom-ltop)/5;
			sub=y+(lbottom-ltop)/10;
		}
		baseLineFixed=!sym.isVarSize();
		notcustomized=!sym.isCustomized();
		LaTeXString=sym.getMathModeName();
		textString=sym.getTextModeName().equals("")||sym.getTextModeName().startsWith("\\textit{")?null:sym.getTextModeName();
		this.sym=sym;
	}
	/**
	 * Get the symbol
	 * @return the symbol
	 */
	public Symbol getSymbol(){
		return sym;
	}
	/**
	 * Get the character
	 * @return the character
	 */
	public Char getChar(){
		return ch;
	}
}