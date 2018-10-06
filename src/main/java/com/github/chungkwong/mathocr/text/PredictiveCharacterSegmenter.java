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
import com.github.chungkwong.mathocr.character.*;
import com.github.chungkwong.mathocr.common.*;
import java.util.*;
/**
 * Segment character assuming fix-width font is used
 *
 * @author Chan Chung Kwong
 */
public class PredictiveCharacterSegmenter implements CharacterSegmenter{
	public static final String NAME="PREDICTIVE";
	public PredictiveCharacterSegmenter(){
	}
	private static final double MAX_ASPECT_RATIO=1.5;
	private static final double EXPECTED_ASPECT_RATIO=1.0;
	@Override
	public List<List<NavigableSet<CharacterCandidate>>> segment(TextLine block){
		Collections.sort(block.getComponents(),ConnectedComponent.FROM_LEFT);
		List<NavigableSet<CharacterCandidate>> characters=new ArrayList<>();
		CharacterRecognizer recognizer=CharacterRecognizers.REGISTRY.get();
		CharacterList list=ModelManager.getCharacterList();
		Object model=ModelManager.getModel(recognizer.getModelType());
		CharacterList smallList=ModelManager.getSmallCharacterList();
		Object smallModel=ModelManager.getSmallModel(recognizer.getModelType());
		int lineWidth=block.getBox().getWidth();
		int lineHeight=block.getBox().getHeight();
		int maxCharacterWidth=(int)(lineHeight*MAX_ASPECT_RATIO);
		int threhold=lineHeight/3;
		int expectedCharacterWidth=Math.min(estimateNonSpaceWidth(block),maxCharacterWidth);
		List<ConnectedComponent> components=block.getComponents();
		for(int i=0;i<components.size();i++){
			ConnectedComponent component=new ConnectedComponent(components.get(i).getBox());
			component.getRunLengths().addAll(components.get(i).getRunLengths());
			int lastRight=component.getRight();
			int limit=component.getLeft()+maxCharacterWidth;
			NavigableSet<CharacterCandidate> bestCandidates=null;
			for(int j=i+1;j<=components.size();j++){
				if(j==components.size()||components.get(j).getLeft()>Math.min(lastRight,limit)){
					ConnectedComponent remainder=null;
					if(component.getRight()>limit){
						if(bestCandidates!=null){
							break;
						}
						remainder=component.splitHorizontally(component.getLeft()+expectedCharacterWidth);
					}
					NavigableSet<CharacterCandidate> candidates=component.getWidth()<threhold&&component.getHeight()<threhold?recognizer.recognize(component,smallModel,smallList):recognizer.recognize(component,model,list);
					if(!candidates.isEmpty()&&(bestCandidates==null||candidates.first().getScore()>bestCandidates.first().getScore())){
						bestCandidates=candidates;
						if(remainder!=null){
							components.set(j-1,remainder);
							i=j-2;
							break;
						}else{
							i=j-1;
						}
					}
				}
				if(j<components.size()&&components.get(j).getLeft()<=limit){
					ConnectedComponent tmp=components.get(j);
					component.combineWith(tmp);
					lastRight=Math.max(lastRight,tmp.getRight());
				}
			}
			characters.add(bestCandidates);
		}
		return Collections.singletonList(characters);
	}
	private int estimateNonSpaceWidth(TextLine block){
		int lastRight=-1, lastLeft=-1;
		List<Integer> widths=new ArrayList<>();
		for(ListIterator<ConnectedComponent> iterator=block.getComponents().listIterator();iterator.hasNext();){
			ConnectedComponent next=iterator.next();
			if(next.getLeft()<=lastRight){
				lastRight=Math.max(lastRight,next.getRight());
			}else{
				if(lastLeft!=-1){
					widths.add(lastRight-lastLeft+1);
				}
				lastLeft=next.getLeft();
				lastRight=next.getRight();
			}
		}
		if(widths.size()>=5){
			widths.add(lastRight-lastLeft+1);
			Collections.sort(widths);
			return widths.get(widths.size()/2);
		}else{
			return (int)(block.getBox().getHeight()*EXPECTED_ASPECT_RATIO);
		}
	}
}
