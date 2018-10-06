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
import com.github.chungkwong.mathocr.character.*;
import java.io.*;
/**
 *
 * @author Chan Chung Kwong
 */
public class ModelBuilder{
	public static void main(String[] args) throws Exception{
		//buildModel(".mathocr/chinese","chinese_train_set.xml");
		buildModel(".mathocr/default","math_train_set.xml");
		//buildModel(".mathocr/simple","im2latex_train_set.xml");
		//buildModel(".mathocr/infty","infty_train_set.xml");
	}
	public static void buildModel(String output,String input) throws Exception{
		File dir=new File(new File(System.getProperty("user.home")),output);
		TrainSet set=TrainSet.load(ModelBuilder.class.getResourceAsStream(input));
		dir.mkdirs();
		set.train(dir);
	}
}
