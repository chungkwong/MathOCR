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
import com.github.chungkwong.mathocr.character.classifier.SvmClassifier;
import com.github.chungkwong.mathocr.character.classifier.DistanceClassifier;
import com.github.chungkwong.mathocr.character.classifier.LinearClassifier;
import com.github.chungkwong.mathocr.Registry;
/**
 *
 * @author Chan Chung Kwong
 */
public class CharacterRecognizers{
	public static final Registry<CharacterRecognizer> REGISTRY=new Registry<>("CHARACTER_RECOGNIZER",new LinearClassifier());
	static{
		REGISTRY.register(LinearClassifier.NAME,new LinearClassifier());
		REGISTRY.register(SvmClassifier.NAME,new SvmClassifier());
		REGISTRY.register(DistanceClassifier.NAME,new DistanceClassifier(DistanceClassifier.EUCLID_DISTANCE));
	}
}
