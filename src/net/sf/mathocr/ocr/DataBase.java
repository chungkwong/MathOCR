/* DataBase.java
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
package net.sf.mathocr.ocr;
import java.io.*;
import java.util.*;
import net.sf.mathocr.common.*;
import static net.sf.mathocr.Environment.env;
/**
 * A data structure to manage database of glyphs, it is not thread-safe
 */
public final class DataBase{
	public static final DataBase DEFAULT_DATABASE=new DataBase();
	Map<Glyph,Double> glyphs,hlines,dots;
	Vector<Symbol> symbols;
	GridMatcher gridMatcher;
	MomentsMatcher momentsMatcher;
	HoleMatcher holeMatcher;
	ChopMatcher chopMatcher;
	/**
	 * Construct a DataBase with default data files
	 */
	public DataBase(){
		loadDefault();
	}
	/**
	 * Construct a DataBase
	 * @param files the data files
	 */
	public DataBase(File[] files){
		init();
		for(File file:files)
			try{
				addDataSource(new FileInputStream(file));
			}catch(FileNotFoundException ex){
				ex.printStackTrace();
			}
		allSampleAdded();
	}
	/**
	 * Initize data structures, must be called before doing any other thing with them
	 */
	void init(){
		glyphs=new HashMap<Glyph,Double>();
		hlines=new HashMap<Glyph,Double>();
		dots=new HashMap<Glyph,Double>();
		symbols=new Vector<Symbol>();
		gridMatcher=new GridMatcher();
		momentsMatcher=new MomentsMatcher();
		holeMatcher=new HoleMatcher();
		chopMatcher=new ChopMatcher();
	}
	/**
	 * Must be called after adding data files and before using the DataBase again
	 */
	void allSampleAdded(){
		gridMatcher.allSampleAdded();
		momentsMatcher.allSampleAdded();
		SpecialMatcher.checkUsable();
		Collections.sort(symbols);
	}
	/**
	 * Initize the DataBase using default settings
	 */
	void loadDefault(){
		init();
		String[] path=new String[]{"CMBSY10","CMB10","CMMI10","CMMIB10","CMR10","CMEX10","EUFB10","MSAM10","MSBM10","RSFS10","CMSY10"};
		for(String name:path)
			addDataSource(getClass().getResourceAsStream("/net/sf/mathocr/resources/"+name+".ttf.lg"));
		String[] extra=env.getString("EXTRA_DATAFILE").split(":");
		for(String pth:extra)
			if(!pth.isEmpty())
				try{
					addDataSource(new FileInputStream(pth));
				}catch(Exception ex){
					ex.printStackTrace();
				}
		allSampleAdded();
	}
	/**
	 * Reload the default database
	 */
	public static void reloadDefaultDataBase(){
		DEFAULT_DATABASE.loadDefault();
	}
	/**
	 * Add a data file to this DataBase(For internal use only)
	 * @param src the InputStream of a data file
	 */
	void addDataSource(InputStream src){
		//Map<Glyph,Double> hl=new HashMap<Glyph,Double>(),dt=new HashMap<Glyph,Double>();
		try(ObjectInputStream in=new ObjectInputStream(src)){
			while(true){
				String name=in.readUTF();
				Symbol sym=new Symbol(in.readUTF(),name,in.readInt(),in.readInt(),in.readInt(),in.readInt(),in.readInt(),in.readInt(),in.readInt());
				symbols.add(sym);
				while(in.readUTF().equals("GLYPH")){
					String chop=in.readUTF();
					Glyph glyph=new Glyph(sym,chop,in.readInt(),in.readInt(),in.readFloat()
					,new float[]{in.readFloat(),in.readFloat(),in.readFloat(),in.readFloat(),in.readFloat()},in.readInt()
					,new float[]{in.readFloat(),in.readFloat(),in.readFloat(),in.readFloat(),in.readFloat(),in.readFloat(),in.readFloat(),in.readFloat(),in.readFloat()}
					,(ConnectedComponent)in.readObject());
					double aspectratio=(double)glyph.getWidth()/glyph.getHeight();
					if(name.endsWith("\tP")){
						SpecialMatcher.map.put(name.substring(0,name.length()-2),glyph);
					}else if(aspectratio>=5&&glyph.getDensity()>0.9){
						hlines.put(glyph,1.0);
						sym.addGlyph(glyph);
						if(name.equals("-"))
							SpecialMatcher.map.put("-",glyph);
					}else if(aspectratio>=0.8&&aspectratio<=1.25&&glyph.getDensity()>=0.7){
						dots.put(glyph,1.0);
						sym.addGlyph(glyph);
						if(name.equals("\\dot\tH"))
							SpecialMatcher.map.put("\\dot{}",glyph);
					}else{
						gridMatcher.addSample(glyph);
						momentsMatcher.addSample(glyph);
						holeMatcher.addSample(glyph.getNumberOfHoles(),glyph);
						chopMatcher.addSample(glyph.getChop(),glyph);
						sym.addGlyph(glyph);
						glyphs.put(glyph,1.0);
						if(name.equals("\\int \tV")||name.equals("\\oint \tV"))
							SpecialMatcher.map.put(name.substring(0,name.length()-2),glyph);
						else if(name.equals("|"))
							SpecialMatcher.map.put("|",glyph);
					}
				}
				//hlines.putAll(hl);
				//dots.putAll(dt);
				//hl.clear();
				//dt.clear();
			}
		}catch(Exception ex){
			//ex.printStackTrace();
		}
	}
	/**
	 * Get a CombinedMatcher
	 * @param useHole use HoleMatcher or not
	 * @param useAspect use AspectRatioMatcher or not
	 * @param useChop use ChopMatcher or not
	 * @param useGrid use GridMatcher or not
	 * @param useMoment use MomentsMatcher or not
	 * @param useProj use ProjectionMatcher or not
	 * @param usePixels use PixelsMatcher or not
	 * @return a CombinedMatcher
	 */
	public CombinedMatcher getMatcher(boolean useHole,boolean useAspect,boolean useChop,boolean useGrid,boolean useMoment,boolean useProj,boolean usePixels){
		ArrayList<Matcher> matchers=new ArrayList<Matcher>(5);
		if(useHole)
			matchers.add(holeMatcher);
		if(useAspect)
			matchers.add(new AspectRatioMatcher());
		if(useChop)
			matchers.add(chopMatcher);
		if(useGrid)
			matchers.add(gridMatcher);
		if(useMoment)
			matchers.add(momentsMatcher);
		if(useProj){
			matchers.add(new ProjectionMatcher());
			matchers.add(new ProjectionMatcher2());
		}
		if(usePixels)
			matchers.add(new PixelsMatcher());
		return new CombinedMatcher(matchers);
	}
	/**
	 * Get a CombinedMatcher with default options
	 * @return a CombinedMatcher
	 */
	public CombinedMatcher getMatcher(){
		return getMatcher(env.getBoolean("USE_HOLE_MATCHER"),env.getBoolean("USE_ASPECT_RATIO_MATCHER"),env.getBoolean("USE_CHOP_MATCHER")
		,env.getBoolean("USE_GRID_MATCHER"),env.getBoolean("USE_MOMENTS_MATCHER"),env.getBoolean("USE_PROJECTION_MATCHER"),env.getBoolean("USE_PIXEL_MATCHER"));
	}
	/**
	 * Get reguler glyphs recorded
	 * @return a set of glyphs included in the DataBase
	 */
	public Map<Glyph,Double> getGlyphs(){
		return glyphs;
	}
	/**
	 * Get glyphs recorded which shape like a horizontal line
	 *
	 * @return a set of glyphs included in the DataBase
	 */
	public Map<Glyph,Double> getHLineGlyphs(){
		return hlines;
	}
	/**
	 * Get the glyphs recorded which shape like a dot
	 * @return a set of glyphs included in the DataBase
	 */
	public Map<Glyph,Double> getDotGlyphs(){
		return dots;
	}
	/**
	 * Get symbols recorded
	 * @return a set of symbols included in the DataBase
	 */
	public Vector<Symbol> getSymbols(){
		return symbols;
	}
	/**
	 * Get Symbol by name
	 * @param name the name of the symbol
	 * @return symbol
	 */
	public Symbol getSymbol(String name){
		/*for(Symbol sym:symbols)
			if(sym.getMathModeName().equals(name))
				return sym;
		return null;*/
		int lower=0,upper=symbols.size()-1;
		Symbol found=null;
		while(lower<=upper){
			int mid=(lower+upper)/2;
			int sign=name.compareTo(symbols.get(mid).toString());
			if(sign<0)
				upper=mid-1;
			else if(sign>0)
				lower=mid+1;
			else{
				found=symbols.get(mid);
				upper=mid-1;
			}
		}
		return found;
	}
}