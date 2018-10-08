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
import java.io.*;
import java.util.*;
import java.util.logging.*;
import static com.github.chungkwong.mathocr.Environment.ENVIRONMENT;
import java.nio.file.*;
/**
 *
 * @author Chan Chung Kwong
 */
public class ModelManager{
	private static String path;
	private static CharacterList list, smallList;
	private static ErrataList erratas;
	private static Map<String,Object> models=new HashMap<>();
	public static CharacterList getCharacterList(){
		loadDefault();
		return list;
	}
	public static CharacterList getCharacterList(File data){
		try{
			return CharacterList.read(new File(data,"index"));
		}catch(IOException ex){
			Logger.getLogger(ModelManager.class.getName()).log(Level.SEVERE,null,ex);
			return new CharacterList(new ArrayList<>());
		}
	}
	public static CharacterList getSmallCharacterList(){
		loadDefault();
		return smallList;
	}
	public static ErrataList getErrataList(){
		loadDefault();
		return erratas;
	}
	public static ErrataList getErrataList(File data){
		if(new File(data,"erratas.json").exists()){
			return ErrataList.read(new File(data,"erratas.json"));
		}else{
			return erratas;
		}
	}
	private static void loadDefault(){
		String currPath=ENVIRONMENT.getString("DATA_DIRECTORY");
		if(!currPath.equals(path)||list==null){
			File directory=new File(currPath);
			File directorySmall=new File(directory.getParentFile(),directory.getName()+"_small");
			if(!directory.exists()||!directorySmall.exists()){
				try{
					directory.mkdirs();
					directorySmall.mkdirs();
					Files.copy(ModelManager.class.getResourceAsStream("default/index"),new File(directory,"index").toPath());
					Files.copy(ModelManager.class.getResourceAsStream("default/LINEAR"),new File(directory,"LINEAR").toPath());
					Files.copy(ModelManager.class.getResourceAsStream("default/LINEAR_f"),new File(directory,"LINEAR_f").toPath());
					Files.copy(ModelManager.class.getResourceAsStream("default_small/index"),new File(directorySmall,"index").toPath());
					Files.copy(ModelManager.class.getResourceAsStream("default_small/LINEAR"),new File(directorySmall,"LINEAR").toPath());
					Files.copy(ModelManager.class.getResourceAsStream("default_small/LINEAR_f"),new File(directorySmall,"LINEAR_f").toPath());
				}catch(IOException ex){
					Logger.getLogger(ModelManager.class.getName()).log(Level.SEVERE,"Failed to load model,please set data directory",ex);
				}
			}
			list=getCharacterList(directory);
			smallList=getCharacterList(directorySmall);
			erratas=getErrataList(new File(currPath));
			path=currPath;
			models.clear();
		}
	}
	public static Object getSmallModel(String type){
		loadDefault();
		File dir=new File(path);
		return getModel(type,new File(dir.getParentFile(),dir.getName()+"_small"));
	}
	public static Object getModel(String type){
		loadDefault();
		if(!models.containsKey(type)){
			models.put(type,getModel(type,new File(path)));
		}
		return models.get(type);
	}
	public static Object getModel(String type,File data){
		try{
			return ModelTypes.REGISTRY.get(type).read(new File(data,type).getAbsolutePath());
		}catch(IOException ex){
			Logger.getLogger(ModelManager.class.getName()).log(Level.SEVERE,null,ex);
			return null;
		}
	}
}

