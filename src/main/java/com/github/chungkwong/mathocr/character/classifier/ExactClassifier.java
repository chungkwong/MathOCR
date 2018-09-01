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
import com.github.chungkwong.mathocr.common.ConnectedComponent;
import com.github.chungkwong.mathocr.character.Features;
import com.github.chungkwong.mathocr.character.CharacterCandidate;
import com.github.chungkwong.mathocr.character.CharacterRecognizer;
import com.github.chungkwong.mathocr.character.CharacterList;
import java.util.*;
/**
 *
 * @author Chan Chung Kwong
 */
public class ExactClassifier implements CharacterRecognizer{
	private final String modelName;
	public ExactClassifier(String modelName){
		this.modelName=modelName;
	}
	@Override
	public NavigableSet<CharacterCandidate> recognize(ConnectedComponent component,Object modelRaw,CharacterList list){
		Map<Object,List<Integer>> model=(Map<Object,List<Integer>>)modelRaw;
		Object feature=Features.REGISTRY.get(modelName).extract(component);
		List<Integer> prototypes=model.getOrDefault(feature,Collections.emptyList());
		TreeSet<CharacterCandidate> candidates=new TreeSet<>();
		prototypes.stream().map((i)->list.getCharacters().get(i)).
				forEach((prototype)->prototype.toCandidate(component.getBox(),1.0));
		return candidates;
	}
	@Override
	public String getModelType(){
		return modelName;
	}
}
