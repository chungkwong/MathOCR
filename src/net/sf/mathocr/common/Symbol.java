/* Symbol.java
 * =========================================================================
 * This file is originally part of the MathOCR Project
 *
 * Copyright (C) 2014,2015 Chan Chung Kwong
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
package net.sf.mathocr.common;
import java.util.*;
/**
 * A data structure that represent a symbol in the data base
 */
public final class Symbol implements Comparable<Symbol>{
	static int currId=0;
	//int id;
	int pixels_width,pixels_height,logical_width,logical_height;
	int x_offset,ascent,logical_ascent;
	boolean varSize=false,hat=false,customized=false;
	String textname,mathname,name;
	List<Glyph> glyphs=new LinkedList<Glyph>();
	ConnectedComponent template;
	/**
	 * Construct a sumbol
	 * @param textname  LaTeX name in text mode
	 * @param mathname  LaTeX name in math mode
	 * @param pixels_width pixel width
	 * @param pixels_height pixel height
	 * @param logical_width logical width
	 * @param logical_height logical height
	 * @param x_offset x offset
	 * @param ascent ascent
	 * @param logical_ascent logical ascent
	 */
	public Symbol(String textname,String mathname,int pixels_width,int pixels_height,int logical_width,int logical_height,int x_offset,int ascent,int logical_ascent){
		this.pixels_width=pixels_width;
		this.pixels_height=pixels_height;
		this.logical_width=logical_width;
		this.logical_height=logical_height;
		this.x_offset=x_offset;
		this.ascent=ascent;
		this.logical_ascent=logical_ascent;
		//id=currId++;
		if(mathname.endsWith("\tC")){
			varSize=true;
			customized=true;
			mathname=mathname.substring(0,mathname.length()-2);
		}else if(mathname.endsWith("\tV")){
			varSize=true;
			mathname=mathname.substring(0,mathname.length()-2);
			this.ascent=pixels_height/2;
			this.logical_height=logical_ascent+pixels_height;
			this.logical_ascent=this.logical_height/2;
		}else if(mathname.endsWith("\tVC")){
			varSize=true;
			customized=true;
			mathname=mathname.substring(0,mathname.length()-3);
			this.ascent=pixels_height/2;
			this.logical_height=logical_ascent+pixels_height;
			this.logical_ascent=this.logical_height/2;
		}else if(mathname.endsWith("\tS")){
			varSize=true;
			mathname=mathname.substring(0,mathname.length()-2);
			this.ascent=pixels_height/2;
			this.logical_height=pixels_height;
			this.logical_ascent=pixels_height/2;
		}else if(mathname.endsWith("\tH")){
			hat=true;
			mathname=mathname.substring(0,mathname.length()-2);
			mathname=mathname.concat("{}");
		}else if(mathname.endsWith("\tVH")){
			hat=true;
			varSize=true;
			mathname=mathname.substring(0,mathname.length()-3);
			mathname=mathname.concat("{}");
			this.logical_height=logical_ascent;
		}
		this.textname=textname;
		this.mathname=mathname;
		this.name=mathname.isEmpty()?textname:mathname;
	}
	/**
	 * Get the pixel width
	 * @return the pixel width
	 */
	public int getPixelWidth(){
		return pixels_width;
	}
	/**
	 * Get the pixel height
	 * @return the pixel height
	 */
	public int getPixelHeight(){
		return pixels_height;
	}
	/**
	 * Get the logical width
	 * @return the logical width
	 */
	public int getLogicalWidth(){
		return logical_width;
	}
	/**
	 * Get the logical height
	 * @return the logical height
	 */
	public int getLogicalHeight(){
		return logical_height;
	}
	/**
	 * Get the offset
	 * @return the offset
	 */
	public int getXOffset(){
		return x_offset;
	}
	/**
	 * Get the ascent
	 * @return the ascent
	 */
	public int getAscent(){
		return ascent;
	}
	/**
	 * Get the logical ascent
	 * @return the logical ascent
	 */
	public int getLogicalAscent(){
		return logical_ascent;
	}
	/**
	 * Add a glyph
	 * @param glyph a glyph to add
	 */
	public void addGlyph(Glyph glyph){
		glyphs.add(glyph);
	}
	/**
	 * Get the glyphs
	 * @return the glyphs
	 */
	public List<Glyph> getGlyphs(){
		return glyphs;
	}
	/**
	 * Get the template of the symbol
	 * @return the template
	 */
	public ConnectedComponent getConnectedComponent(){
		if(template==null){
			if(glyphs.size()==1)
				template=glyphs.get(0).getConnectedComponent();
			else{
				template=new ConnectedComponent(0,pixels_width-1,0,pixels_height-1);
				List<RunLength> runlengths=template.getRunLengths();
				for(Glyph g:glyphs){
					int dx=g.getXOffset(),dy=g.getYOffset();
					for(RunLength rl:g.getConnectedComponent().getRunLengths())
						runlengths.add(new RunLength(rl.getY()+dy,rl.getX()+dx,rl.getCount()));
				}
			}
		}
		return template;
	}
	/**
	 * Get the id of the symbol
	 * @return the id
	 */
	/*public int getId(){
		return id;
	}*/
	/**
	 * Check if this symbol have no fixed baseline
	 * @return if this symbol have no fixed baseline
	 */
	public boolean isVarSize(){
		return varSize;
	}
	/**
	 * Check if this symbol is customized
	 * @return if this symbol is customized
	 */
	public boolean isCustomized(){
		return customized;
	}
	/**
	 * Check if this symbol is a hat
	 * @return if this symbol is a hat
	 */
	public boolean isHat(){
		return hat;
	}
	/**
	 * Check if the symbol is available in text mode
	 * @return the result
	 */
	public boolean isTextModeSymbol(){
		return !textname.isEmpty();
	}
	/**
	 * Check if the symbol is available in math mode
	 * @return the result
	 */
	public boolean isMathModeSymbol(){
		return !mathname.isEmpty();
	}
	/**
	 * Get the name in text mode
	 * @return name
	 */
	public String getTextModeName(){
		return textname;
	}
	/**
	 * Get the name in math mode
	 * @return name
	 */
	public String getMathModeName(){
		return mathname;
	}
	/**
	 * Get the name of the symbol
	 * @return the name
	 */
	public String toString(){
		return name;
	}
	/**
	 * Compare name
	 * @return result
	 */
	public int compareTo(Symbol sym){
		return name.compareTo(sym.name);
	}
}