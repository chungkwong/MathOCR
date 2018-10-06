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
import com.github.chungkwong.mathocr.text.structure.*;
import java.util.*;
/**
 *
 * @author Chan Chung Kwong
 */
public class SptialCharacterSegmenter implements CharacterSegmenter{
	public static final String NAME="SPTIAL";
	public SptialCharacterSegmenter(){
	}
	@Override
	public List<List<NavigableSet<CharacterCandidate>>> segment(TextLine block){
		List<NavigableSet<CharacterCandidate>> characters=new ArrayList<>();
		CharacterRecognizer recognizer=CharacterRecognizers.REGISTRY.get();
		CharacterList list=ModelManager.getCharacterList();
		Object model=ModelManager.getModel(recognizer.getModelType());
		CharacterList smallList=ModelManager.getSmallCharacterList();
		Object smallModel=ModelManager.getSmallModel(recognizer.getModelType());
		List<ConnectedComponent> components=block.getComponents();
		fixContaining(components);
		int threhold=estimateSmallThrehold(components);
		Collections.sort(components,ConnectedComponent.FROM_LEFT);
		List<NavigableSet<CharacterCandidate>> symbols=new ArrayList<>(components.size());
		for(Iterator<ConnectedComponent> iterator=components.iterator();iterator.hasNext();){
			ConnectedComponent next=iterator.next();
			if(next.getWidth()<threhold&&next.getHeight()<threhold){
				symbols.add(recognizer.recognize(next,smallModel,smallList));
			}else{
				symbols.add(recognizer.recognize(next,model,list));
			}
		}
		fixSplitCharacter(symbols,components,recognizer,list,model);
		return Collections.singletonList(symbols);
	}
	private void fixContaining(List<ConnectedComponent> components){
		int len=components.size();
		Collections.sort(components,ConnectedComponent.FROM_LEFT);
		Partition partition=new Partition(new Linkable(){
			public void link(int m,int n){
				components.get(n).combineWith(components.get(m));
				components.set(m,null);
			}
		},len);
		byte[] possibleRoot=new byte[len];
		for(int i=0;i<len;i++){
			ConnectedComponent ele=components.get(i);
			if(ele==null){
				continue;
			}
			int left=ele.getLeft(), right=ele.getRight(), top=ele.getTop(), bottom=ele.getBottom(), area=(right-left+1)*(bottom-top+1);
			for(int j=i+1;j<len;j++){
				ConnectedComponent ele2=components.get(j);
				if(ele2==null){
					continue;
				}
				int left2=ele2.getLeft(), right2=ele2.getRight(), top2=ele2.getTop(), bottom2=ele2.getBottom(), area2=(right2-left2+1)*(bottom2-top2+1);
				if(left2>right){
					break;
				}
				int overlap=(Math.min(right,right2)-left2+1)*(Math.min(bottom,bottom2)-Math.max(top,top2)+1);
				if(left<=right2&&top<=bottom2&&top2<=bottom&&overlap>Math.min(area,area2)/5){
					if(area>=area2){
						if(possibleRoot[i]==0){
							possibleRoot[i]=canContainsOther(ele)?(byte)1:-(byte)1;
						}
						if(possibleRoot[i]==1){
							continue;
						}
					}else{
						if(possibleRoot[j]==0){
							possibleRoot[j]=canContainsOther(ele2)?(byte)1:-(byte)1;
						}
						if(possibleRoot[j]==1){
							continue;
						}
					}
					partition.union(i,j);
				}
			}
		}
		trim(components);
	}
	private boolean canContainsOther(ConnectedComponent component){
		CharacterRecognizer recognizer=CharacterRecognizers.REGISTRY.get();
		NavigableSet<CharacterCandidate> candidates=recognizer.recognize(component,ModelManager.getModel(recognizer.getModelType()),ModelManager.getCharacterList());
		return !candidates.isEmpty()&&"√∫∬∭∮∯∰∱∲∳".indexOf(candidates.first().getCodePoint())!=-1;
	}
	private static void trim(List<ConnectedComponent> components){
		ListIterator<ConnectedComponent> iter=components.listIterator();
		while(iter.hasNext()){
			if(iter.next()==null){
				iter.remove();
			}
		}
	}
	private int estimateSmallThrehold(List<ConnectedComponent> components){
		if(components.size()<5){
			return 0;
		}
		return median(components.stream().mapToInt((c)->Math.max(c.getHeight(),c.getWidth())).toArray())/3;
	}
	private int median(int... num){
		if(num.length==0){
			return Symbol.DEFAULT_SIZE;
		}
		Arrays.sort(num);
		return num[num.length/2];
	}
	private void fixSplitCharacter(List<NavigableSet<CharacterCandidate>> symbols,List<ConnectedComponent> components,
			CharacterRecognizer recognizer,CharacterList list,Object model){
		List<Integer> neighbors=new ArrayList<>();
		for(int i=0;i<components.size();i++){
			ConnectedComponent first=components.get(i);
			if(symbols.get(i)==null||!canBePartOfOther(symbols.get(i))){
				continue;
			}
			neighbors.clear();
			int threhold=Math.max(first.getWidth(),first.getHeight());
			int found=0;
			double minScore=symbols.get(i).first().getScore();
			for(int j=i+1;j<components.size();j++){
				if(symbols.get(j)==null||!canBePartOfOther(symbols.get(j))){
					continue;
				}
				ConnectedComponent second=components.get(j);
				if(second.getLeft()>first.getRight()){
					break;
				}else if(second.getBottom()>=first.getTop()-threhold/2&&second.getTop()<=first.getBottom()+threhold/2
						&&second.getTop()>=first.getTop()-threhold*3/2&&second.getBottom()<=first.getBottom()+threhold*3/2
						&&second.getRight()<first.getRight()+threhold/4){
					neighbors.add(j);
					if(symbols.get(j).first().getScore()<minScore){
						minScore=symbols.get(j).first().getScore();
					}
					if(neighbors.size()>=3){
						break;
					}
				}
			}
			if(neighbors.size()>=1&&neighbors.size()<=2){
				ConnectedComponent alternative=new ConnectedComponent(first.getBox());
				alternative.getRunLengths().addAll(first.getRunLengths());
				neighbors.forEach((j)->alternative.combineWith(components.get(j)));
				NavigableSet<CharacterCandidate> candidates=recognizer.recognize(alternative,model,list);
				if(!candidates.isEmpty()&&candidates.first().getScore()>minScore){
					symbols.set(i,candidates);
					neighbors.forEach((j)->symbols.set(j,null));
				}
			}
		}
		symbols.removeIf((s)->s==null);
	}
	private boolean canBePartOfOther(NavigableSet<CharacterCandidate> symbol){
		return "√∫∬∭∮∯∰∱∲∳∑∏Σ".indexOf(symbol.first().getCodePoint())==-1;
	}
}
