/* Environment.java
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
package net.sf.mathocr;
import java.util.*;
import java.util.prefs.*;
/**
 * Store global settings
 */
public final class Environment{
   /**
    * Environment that contains currrent global settings
    */
	public static Environment env=loadFromPreference();
	ResourceBundle translation;
	HashMap<String,Boolean> boolMap;
	HashMap<String,Integer> intMap;
	HashMap<String,Float> floatMap;
	HashMap<String,String> strMap;
	/**
	 * Initialize Environment
	 */
	protected Environment(){
		translation=ResourceBundle.getBundle("net.sf.mathocr.resources.gui");
		boolMap=new HashMap<String,Boolean>();
		intMap=new HashMap<String,Integer>();
		floatMap=new HashMap<String,Float>();
		strMap=new HashMap<String,String>();
	}
	/**
	 * Get the local name corresponding to a code
	 * @param code the code
	 * @param code the local name
	 */
	public String getTranslation(String code){
		return translation.getString(code);
	}
	/**
	 * Get the keys of the boolean properties
	 * @return the keys
	 */
	public Set<String> getBooleanKeySet(){
		return boolMap.keySet();
	}
	/**
	 * Get the value of a boolean property
	 * @param key the key
	 * @return the value
	 */
	public Boolean getBoolean(String key){
		return boolMap.get(key);
	}
	/**
	 * Set the value of a boolean property
	 * @param key the key
	 * @param val the value
	 */
	public void setBoolean(String key,Boolean val){
		boolMap.put(key,val);
	}
	/**
	 * Get the keys of the integer properties
	 * @return the keys
	 */
	public Set<String> getIntegerKeySet(){
		return intMap.keySet();
	}
	/**
	 * Get the value of a integer property
	 * @param key the key
	 * @return the value
	 */
	public Integer getInteger(String key){
		return intMap.get(key);
	}
	/**
	 * Set the value of a integer property
	 * @param key the key
	 * @param val the value
	 */
	public void setInteger(String key,Integer val){
		intMap.put(key,val);
	}
	/**
	 * Get the keys of the floating point properties
	 * @return the keys
	 */
	public Set<String> getFloatKeySet(){
		return floatMap.keySet();
	}
	/**
	 * Get the value of a floating point property
	 * @param key the key
	 * @return the value
	 */
	public Float getFloat(String key){
		return floatMap.get(key);
	}
	/**
	 * Set the value of a floating point property
	 * @param key the key
	 * @param val the value
	 */
	public void setFloat(String key,Float val){
		floatMap.put(key,val);
	}
	/**
	 * Get the keys of the string properties
	 * @return the keys
	 */
	public Set<String> getStringKeySet(){
		return strMap.keySet();
	}
	/**
	 * Get the value of a string property
	 * @param key the key
	 * @return the value
	 */
	public String getString(String key){
		return strMap.get(key);
	}
	/**
	 * Set the value of a string property
	 * @param key the key
	 * @param val the value
	 */
	public void setString(String key,String val){
		strMap.put(key,val);
		if(key.equals("EXTRA_DATAFILE"))
			net.sf.mathocr.ocr.DataBase.reloadDefaultDataBase();
	}
	/**
	 * Restore saved preference to original setting
	 */
	public void clearPreference()throws BackingStoreException{
		Preferences pref=Preferences.userNodeForPackage(Environment.class);
		pref.removeNode();
		pref.flush();
		env=loadFromPreference();
	}
	/**
	 * Load a Environment from saved preference
	 */
	public static Environment loadFromPreference(){
		Environment env=new Environment();
		Preferences pref=Preferences.userNodeForPackage(Environment.class);
		env.boolMap.put("APPLY_MEAN_FILTER",pref.getBoolean("APPLY_MEAN_FILTER",false));
		env.boolMap.put("APPLY_MEDIAN_FILTER",pref.getBoolean("APPLY_MEDIAN_FILTER",false));
		env.boolMap.put("APPLY_NOISE_FILTER",pref.getBoolean("APPLY_NOISE_FILTER",false));
		env.boolMap.put("APPLY_KFILL_FILTER",pref.getBoolean("APPLY_KFILL_FILTER",false));
		env.boolMap.put("DETECT_INVERT",pref.getBoolean("DETECT_INVERT",false));
		env.boolMap.put("CLEAN_EDGES",pref.getBoolean("CLEAN_EDGES",false));
		env.boolMap.put("SKIP_PREPROCESS_PANE",pref.getBoolean("SKIP_PREPROCESS_PANE",true));
		env.boolMap.put("SKIP_COMPONENT_PANE",pref.getBoolean("SKIP_COMPONENT_PANE",true));
		env.boolMap.put("SKIP_LAYOUT_PANE",pref.getBoolean("SKIP_LAYOUT_PANE",true));
		env.boolMap.put("SKIP_RECOGNIZE_PANE",pref.getBoolean("SKIP_RECOGNIZE_PANE",true));
		env.boolMap.put("USE_HOLE_MATCHER",pref.getBoolean("USE_HOLE_MATCHER",false));
		env.boolMap.put("USE_ASPECT_RATIO_MATCHER",pref.getBoolean("USE_ASPECT_RATIO_MATCHER",true));
		env.boolMap.put("USE_CHOP_MATCHER",pref.getBoolean("USE_CHOP_MATCHER",false));
		env.boolMap.put("USE_GRID_MATCHER",pref.getBoolean("USE_GRID_MATCHER",true));
		env.boolMap.put("USE_MOMENTS_MATCHER",pref.getBoolean("USE_MOMENTS_MATCHER",true));
		env.boolMap.put("USE_PROJECTION_MATCHER",pref.getBoolean("USE_PROJECTION_MATCHER",true));
		env.boolMap.put("USE_PIXEL_MATCHER",pref.getBoolean("USE_PIXEL_MATCHER",false));
		env.intMap.put("KFILL_WINDOW",pref.getInt("KFILL_WINDOW",3));
		env.intMap.put("MANUAL_THREHOLD_LIMIT",pref.getInt("MANUAL_THREHOLD_LIMIT",192));
		env.intMap.put("SAUVOLA_WINDOW",pref.getInt("SAUVOLA_WINDOW",15));
		env.floatMap.put("SAUVOLA_WEIGHT",pref.getFloat("SAUVOLA_WEIGHT",0.2f));
		env.strMap.put("THREHOLD_METHOD",pref.get("THREHOLD_METHOD","Sauvola"));
		env.strMap.put("SKEW_DETECT_METHOD",pref.get("SKEW_DETECT_METHOD","PP"));
		env.strMap.put("OCR_ENGINE",pref.get("OCR_ENGINE","Ocrad"));
		env.strMap.put("TESSERACT_PARAMETER",pref.get("TESSERACT_PARAMETER","-psm 6 -l chi_sim"));
		env.strMap.put("OUTPUT_FOLDER",pref.get("OUTPUT_FOLDER",System.getProperty("user.dir")));
		env.strMap.put("OUTPUT_FORMAT",pref.get("OUTPUT_FORMAT","LaTeX"));
		env.strMap.put("EXTRA_DATAFILE",pref.get("EXTRA_DATAFILE",""));
		return env;
	}
	/**
	 * Save settings as preference
	 */
	public void saveAsPreference(){
		Preferences pref=Preferences.userNodeForPackage(Environment.class);
		for(Map.Entry<String,Boolean> entry:boolMap.entrySet())
			pref.putBoolean(entry.getKey(),entry.getValue());
		for(Map.Entry<String,Integer> entry:intMap.entrySet())
			pref.putInt(entry.getKey(),entry.getValue());
		for(Map.Entry<String,Float> entry:floatMap.entrySet())
			pref.putFloat(entry.getKey(),entry.getValue());
		for(Map.Entry<String,String> entry:strMap.entrySet())
			pref.put(entry.getKey(),entry.getValue());
		try{
			pref.flush();
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
}