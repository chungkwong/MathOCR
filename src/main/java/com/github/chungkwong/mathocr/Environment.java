/* Environment.java
 * =========================================================================
 * This file is originally part of the MathOCR Project
 *
 * Copyright (C) 2014,2015 Chan Chung Kwong
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 */
package com.github.chungkwong.mathocr;
import java.io.*;
import java.util.*;
import java.util.logging.*;
import java.util.prefs.*;
/**
 * Store global settings
 */
public final class Environment{
	/**
	 * Environment that contains currrent global settings
	 */
	public static Environment ENVIRONMENT=loadFromPreference();
	private final ResourceBundle translation=ResourceBundle.getBundle("com.github.chungkwong.mathocr.ui.gui");
	private final HashMap<String,Boolean> boolMap=new HashMap<>();
	private final HashMap<String,Integer> intMap=new HashMap<>();
	private final HashMap<String,Float> floatMap=new HashMap<>();
	private final HashMap<String,String> strMap=new HashMap<>();
	/**
	 * Initialize Environment
	 */
	private Environment(){
	}
	/**
	 * Get the local name corresponding to a code
	 *
	 * @param code the code
	 * @return the local name
	 */
	public String getTranslation(String code){
		try{
			return translation.getString(code);
		}catch(MissingResourceException ex){
			Logger.getGlobal().log(Level.INFO,"",ex);
			return code;
		}
	}
	/**
	 * Get the keys of the boolean properties
	 *
	 * @return the keys
	 */
	public Set<String> getBooleanKeySet(){
		return boolMap.keySet();
	}
	/**
	 * Get the value of a boolean property
	 *
	 * @param key the key
	 * @return the value
	 */
	public Boolean getBoolean(String key){
		return boolMap.get(key);
	}
	/**
	 * Set the value of a boolean property
	 *
	 * @param key the key
	 * @param val the value
	 */
	public void setBoolean(String key,Boolean val){
		boolMap.put(key,val);
	}
	/**
	 * Get the keys of the integer properties
	 *
	 * @return the keys
	 */
	public Set<String> getIntegerKeySet(){
		return intMap.keySet();
	}
	/**
	 * Get the value of a integer property
	 *
	 * @param key the key
	 * @return the value
	 */
	public Integer getInteger(String key){
		return intMap.get(key);
	}
	/**
	 * Set the value of a integer property
	 *
	 * @param key the key
	 * @param val the value
	 */
	public void setInteger(String key,Integer val){
		intMap.put(key,val);
	}
	/**
	 * Get the keys of the floating point properties
	 *
	 * @return the keys
	 */
	public Set<String> getFloatKeySet(){
		return floatMap.keySet();
	}
	/**
	 * Get the value of a floating point property
	 *
	 * @param key the key
	 * @return the value
	 */
	public Float getFloat(String key){
		return floatMap.get(key);
	}
	/**
	 * Set the value of a floating point property
	 *
	 * @param key the key
	 * @param val the value
	 */
	public void setFloat(String key,Float val){
		floatMap.put(key,val);
	}
	/**
	 * Get the keys of the string properties
	 *
	 * @return the keys
	 */
	public Set<String> getStringKeySet(){
		return strMap.keySet();
	}
	/**
	 * Get the value of a string property
	 *
	 * @param key the key
	 * @return the value
	 */
	public String getString(String key){
		return strMap.get(key);
	}
	/**
	 * Set the value of a string property
	 *
	 * @param key the key
	 * @param val the value
	 */
	public void setString(String key,String val){
		strMap.put(key,val);
	}
	/**
	 * Restore saved preference to original setting
	 */
	public void clearPreference() throws BackingStoreException{
		Preferences pref=Preferences.userNodeForPackage(Environment.class);
		pref.removeNode();
		pref.flush();
		ENVIRONMENT=loadFromPreference();
	}
	private static Environment loadFromPreference(){
		Environment env=new Environment();
		Preferences pref=Preferences.userNodeForPackage(Environment.class);
		env.boolMap.put("APPLY_MEAN_FILTER",pref.getBoolean("APPLY_MEAN_FILTER",false));
		env.boolMap.put("APPLY_MEDIAN_FILTER",pref.getBoolean("APPLY_MEDIAN_FILTER",false));
		env.boolMap.put("APPLY_NOISE_FILTER",pref.getBoolean("APPLY_NOISE_FILTER",false));
		env.boolMap.put("APPLY_KFILL_FILTER",pref.getBoolean("APPLY_KFILL_FILTER",false));
		env.boolMap.put("DETECT_INVERT",pref.getBoolean("DETECT_INVERT",false));
		env.boolMap.put("CLEAN_EDGES",pref.getBoolean("CLEAN_EDGES",false));
		env.intMap.put("KFILL_WINDOW",pref.getInt("KFILL_WINDOW",3));
		env.intMap.put("MANUAL_THREHOLD_LIMIT",pref.getInt("MANUAL_THREHOLD_LIMIT",192));
		env.intMap.put("SAUVOLA_WINDOW",pref.getInt("SAUVOLA_WINDOW",15));
		env.floatMap.put("SAUVOLA_WEIGHT",pref.getFloat("SAUVOLA_WEIGHT",0.1f));
		env.strMap.put("THREHOLD_METHOD",pref.get("THREHOLD_METHOD","Sauvola"));
		env.strMap.put("SKEW_DETECT_METHOD",pref.get("SKEW_DETECT_METHOD","PP"));
		env.strMap.put("OCR_ENGINE",pref.get("OCR_ENGINE","Native"));
		env.strMap.put("TESSERACT_PARAMETER",pref.get("TESSERACT_PARAMETER","-psm 6"));// -l chi_sim
		env.strMap.put("OUTPUT_FOLDER",pref.get("OUTPUT_FOLDER",System.getProperty("user.dir")));
		env.strMap.put("OUTPUT_FORMAT",pref.get("OUTPUT_FORMAT","LaTeX"));
		env.strMap.put("DATA_DIRECTORY",pref.get("DATA_DIRECTORY",new File(new File(System.getProperty("user.home")),".mathocr/default").getAbsolutePath()));
		env.strMap.put("CHARACTER_RECOGNIZER",pref.get("CHARACTER_RECOGNIZER","LINEAR"));
		env.strMap.put("PAGE_ANALYZER",pref.get("PAGE_ANALYZER","NAIVE"));
		env.strMap.put("DOCUMENT_ASSEMBLER",pref.get("DOCUMENT_ASSEMBLER","NAIVE"));
		env.strMap.put("BLOCK_CLASSIFIER",pref.get("BLOCK_CLASSIFIER","NAIVE"));
		env.strMap.put("PAGE_SEGMENTER",pref.get("PAGE_SEGMENTER","XY_CUT"));
		env.strMap.put("BLOCK_ORDERER",pref.get("BLOCK_ORDERER","NAIVE"));
		env.strMap.put("CHARACTER_SEGMENTER",pref.get("CHARACTER_SEGMENTER","NAIVE"));
		env.strMap.put("LINE_ANALYZER",pref.get("LINE_ANALYZER","NAIVE"));
		env.strMap.put("LINE_SEGMENT_METHOD",pref.get("LINE_SEGMENT_METHOD","PROJECTION"));
		try{
			Properties secret=new Properties();
			secret.load(Environment.class.getResourceAsStream("/com/github/chungkwong/mathocr/ui/secret.properties"));
			secret.entrySet().forEach((e)->env.strMap.put(Objects.toString(e.getKey()),Objects.toString(e.getValue())));
		}catch(Exception ex){
			Logger.getLogger(Environment.class.getName()).log(Level.SEVERE,null,ex);
		}
		return env;
	}
	/**
	 * Save settings as preference
	 */
	public void saveAsPreference(){
		Preferences pref=Preferences.userNodeForPackage(Environment.class);
		for(Map.Entry<String,Boolean> entry:boolMap.entrySet()){
			pref.putBoolean(entry.getKey(),entry.getValue());
		}
		for(Map.Entry<String,Integer> entry:intMap.entrySet()){
			pref.putInt(entry.getKey(),entry.getValue());
		}
		for(Map.Entry<String,Float> entry:floatMap.entrySet()){
			pref.putFloat(entry.getKey(),entry.getValue());
		}
		for(Map.Entry<String,String> entry:strMap.entrySet()){
			pref.put(entry.getKey(),entry.getValue());
		}
		try{
			pref.flush();
		}catch(Exception ex){
			Logger.getGlobal().log(Level.SEVERE,"",ex);
		}
	}
}
