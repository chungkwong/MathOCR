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
import com.github.chungkwong.mathocr.character.feature.Gradient;
import com.github.chungkwong.mathocr.Registry;
import com.github.chungkwong.mathocr.character.feature.*;
/**
 *
 * @author Chan Chung Kwong
 */
public class Features{
	public static final Registry<Feature> REGISTRY=new Registry<>("FEATURE",new Gradient(5,5,true));
	static{
		REGISTRY.register(AspectRatio.NAME,new AspectRatio());
		REGISTRY.register(Grid.NAME,new Grid(5,5));
		REGISTRY.register(Gradient.NAME,new Gradient(5,5,true));
		REGISTRY.register(Gradient.FULL_NAME,new Gradient(5,5,false));
		REGISTRY.register(Moments.NAME,new Moments());
		REGISTRY.register(CrossNumber.NAME,new CrossNumber());
	}
}
