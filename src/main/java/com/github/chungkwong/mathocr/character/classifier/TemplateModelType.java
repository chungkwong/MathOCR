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
package com.github.chungkwong.mathocr.character.classifier;
import com.github.chungkwong.mathocr.common.Pair;
import com.github.chungkwong.mathocr.common.ConnectedComponent;
import com.github.chungkwong.mathocr.character.ModelType;
import com.github.chungkwong.mathocr.character.DataSet;
import java.io.*;
import java.util.*;
/**
 *
 * @author Chan Chung Kwong
 */
public class TemplateModelType implements ModelType<List<Pair<Integer,ConnectedComponent>>>{
	public static final String NAME="TEMPLATE";
	@Override
	public List<Pair<Integer,ConnectedComponent>> build(DataSet set){
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
	@Override
	public void write(List<Pair<Integer,ConnectedComponent>> model,String fileName) throws IOException{
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
	@Override
	public List<Pair<Integer,ConnectedComponent>> read(String fileName) throws IOException{
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
}
