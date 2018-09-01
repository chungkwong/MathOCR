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
/**
 *
 * @author Chan Chung Kwong
 */
public class ModelManager{
	private static String path;
	private static CharacterList list;
	private static Map<String,Object> models=new HashMap<>();
	public static CharacterList getCharacterList(){
		String currPath=ENVIRONMENT.getString("DATA_DIRECTORY");
		if(!currPath.equals(path)||list==null){
			list=getCharacterList(new File(currPath));
			path=currPath;
			models.clear();
		}
		return list;
	}
	public static CharacterList getCharacterList(File data){
		try{
			return CharacterList.read(new File(data,"index"));
		}catch(IOException ex){
			Logger.getLogger(ModelManager.class.getName()).log(Level.SEVERE,null,ex);
			return null;
		}
	}
	public static Object getModel(String type){
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
