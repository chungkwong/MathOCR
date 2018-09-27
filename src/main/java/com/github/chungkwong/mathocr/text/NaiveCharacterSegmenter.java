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
package com.github.chungkwong.mathocr.text;
import com.github.chungkwong.mathocr.common.ConnectedComponent;
import com.github.chungkwong.mathocr.character.ModelManager;
import com.github.chungkwong.mathocr.character.CharacterRecognizers;
import com.github.chungkwong.mathocr.character.CharacterRecognizer;
import com.github.chungkwong.mathocr.character.CharacterCandidate;
import com.github.chungkwong.mathocr.character.CharacterList;
import java.util.*;
import java.util.stream.*;
/**
 * Naive implementation of CharacterSegmenter, vertical projection is used, so
 * it will not work if subscript, superscript, kern or italic presents
 *
 * @author Chan Chung Kwong
 */
public class NaiveCharacterSegmenter implements CharacterSegmenter{
	public static final String NAME="NAIVE";
	public NaiveCharacterSegmenter(){
	}
	@Override
	public List<List<NavigableSet<CharacterCandidate>>> segment(TextLine block){
		CharacterRecognizer recognizer=CharacterRecognizers.REGISTRY.get();
		CharacterList list=ModelManager.getCharacterList();
		Object model=ModelManager.getModel(recognizer.getModelType());
		segment(block.getComponents());
		return Collections.singletonList(block.getComponents().stream().
				map((c)->recognizer.recognize(c,model,list)).collect(Collectors.toList()));
	}
	private void segment(List<ConnectedComponent> components){
		Collections.sort(components,ConnectedComponent.FROM_LEFT);
		ListIterator<ConnectedComponent> iterator=components.listIterator();
		int lastRight=-1;
		while(iterator.hasNext()){
			ConnectedComponent next=iterator.next();
			if(next.getLeft()<=lastRight){
				iterator.remove();
				iterator.previous().combineWith(next);
				iterator.next();
			}
			lastRight=Math.max(lastRight,next.getRight());
		}
	}
}
