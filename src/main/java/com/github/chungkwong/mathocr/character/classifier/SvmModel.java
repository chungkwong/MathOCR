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
import java.util.*;
import libsvm.*;
/**
 *
 * @author Chan Chung Kwong
 */
public class SvmModel{
	private final svm_model model;
	private final List<String> features;
	public SvmModel(svm_model model,List<String> features){
		this.model=model;
		this.features=features;
	}
	public svm_model getModel(){
		return model;
	}
	public List<String> getFeatures(){
		return features;
	}
}
