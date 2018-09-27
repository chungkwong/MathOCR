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
import com.github.chungkwong.mathocr.text.structure.Fraction;
import com.github.chungkwong.mathocr.text.structure.Matrix;
import com.github.chungkwong.mathocr.text.structure.Superscript;
import com.github.chungkwong.mathocr.text.structure.Line;
import com.github.chungkwong.mathocr.text.structure.Span;
import com.github.chungkwong.mathocr.text.structure.Subscript;
import com.github.chungkwong.mathocr.common.BoundBox;
import com.github.chungkwong.mathocr.text.structure.Symbol;
import java.util.*;
import java.util.stream.*;
/**
 * Naive implementation of LineAnalyzer
 *
 * @author Chan Chung Kwong
 */
public class NaiveLineAnalyzer implements LineAnalyzer{
	public static final String NAME="NAIVE";
	public NaiveLineAnalyzer(){
	}
	@Override
	public Line analysis(List<NavigableSet<CharacterCandidate>> candidates){
		List<Symbol> characters=resolve(candidates);
		double score=0;
		int[] baseLineAndSize=getBaseLineAndSize(characters);
		return layoutH(characters,baseLineAndSize[0],baseLineAndSize[1]);
	}
	private Span layoutV(List<List<Span>> columns,int baseline,int fontsize){
		List<List<Span>> spans=new ArrayList<>(columns.size());
		for(List<Span> column:columns){
			spans.add(new ArrayList<>(column));
		}
		ListIterator<List<Span>> colIterator=spans.listIterator();
		int lastRight=colIterator.next().stream().mapToInt((s)->s.getBox().getRight()).max().getAsInt();
		while(colIterator.hasNext()){
			List<Span> next=colIterator.next();
			int left=next.stream().mapToInt((s)->s.getBox().getRight()).min().getAsInt();
			if(left-lastRight>fontsize/4){
				colIterator.remove();
				colIterator.previous().addAll(next);
				colIterator.next();
			}
			lastRight=next.stream().mapToInt((s)->s.getBox().getRight()).max().getAsInt();
		}
		spans.forEach((column)->Collections.sort(column,Comparator.comparingInt((s)->s.getBox().getTop())));
		int n=spans.size();
		List<List<List<Span>>> matrix=new ArrayList<>();
		List<ListIterator<Span>> cellIterators=spans.stream().map((c)->c.listIterator()).collect(Collectors.toList());
		int bound=-1;
		while(cellIterators.stream().anyMatch((iter)->iter.hasNext())){
			boolean changed=false;
			int nextBound=Integer.MAX_VALUE;
			for(int j=0;j<cellIterators.size();j++){
				ListIterator<Span> cells=cellIterators.get(j);
				while(cells.hasNext()){
					Span next=cells.next();
					if(next.getBox().getTop()<=bound){
						matrix.get(matrix.size()-1).get(j).add(next);
						changed=true;
						if(next.getBox().getBottom()>bound){
							bound=next.getBox().getBottom();
						}
					}else{
						if(next.getBox().getTop()<nextBound){
							nextBound=next.getBox().getTop();
						}
						cells.previous();
						break;
					}
				}
			}
			if(!changed&&nextBound!=Integer.MAX_VALUE){
				List<List<Span>> row=new ArrayList<>(n);
				for(int j=0;j<n;j++){
					row.add(new ArrayList<>());
				}
				matrix.add(row);
				bound=nextBound;
			}
		}
		int m=matrix.size();
		List<List<Span>> array=new ArrayList<>();
		for(int i=0;i<m;i++){
			List<Span> row=new ArrayList<>(n);
			int subBaseLine=getBaseLineAndSize(matrix.get(i).stream().flatMap((r)->r.stream()).collect(Collectors.toList()))[0];
			for(int j=0;j<n;j++){
				List<Span> cell=matrix.get(i).get(j);
				if(cell.size()==1){
					row.add(cell.get(0));
				}else if(m!=1||n!=1){
					row.add(layoutH(cell,subBaseLine,fontsize));
				}else if(columns.size()>1){
					row.add(new Line(columns.stream().map((span)->layoutV(Collections.singletonList(span),baseline,fontsize)).collect(Collectors.toList())));
				}else{
					row.add(new Line(cell));
				}
			}
			array.add(row);
		}
		if(n==1){
			if(m==1){
				return new Line(array.get(0));
			}
			if(m==2&&array.get(0).get(0).getFontSize()<fontsize*7/10&&array.get(1).get(0).getFontSize()<fontsize*7/10){
				return new Line(Arrays.asList(new Subscript(array.get(1).get(0)),new Superscript(array.get(0).get(0))));
			}
			if(m==2&&array.get(0).get(0) instanceof Symbol&&isHorizontalLine((Symbol)array.get(0).get(0))){
				if(array.get(1).get(0) instanceof Symbol&&isHorizontalLine((Symbol)array.get(1).get(0))){
					BoundBox box=BoundBox.union(array.get(0).get(0).getBox(),array.get(1).get(0).getBox());
					return new Symbol(ModelManager.getCharacterList().getCharacter('=').toCandidate(box,1.0));
				}
			}
			if(m==3&&array.get(1).get(0) instanceof Symbol){
				Span over=array.get(0).get(0);
				Symbol middle=(Symbol)array.get(1).get(0);
				Span under=array.get(2).get(0);
				if(isHorizontalLine(middle)
						&&middle.getBox().getWidth()>(Math.max(over.getBox().getRight(),under.getBox().getRight())-Math.min(over.getBox().getLeft(),under.getBox().getLeft()))*9/10){
					return new Fraction(over,under);
				}
			}
		}
		return new Matrix(array);
	}
	private boolean isHorizontalLine(Symbol symbol){
		return "-¯_‐–—⌢⌣".indexOf(symbol.getCodePoint())!=-1;
	}
	private Line layoutH(List<? extends Span> spans,int baseline,int fontsize){
		Collections.sort(spans,Comparator.comparingInt((s)->s.getBox().getLeft()));
		int bound=-1;
		List<Span> line=new ArrayList<>();
		List<List<Span>> columns=new ArrayList<>();
		List<Span> pool=new ArrayList<>();
		for(Span span:spans){
			if(span.getBox().getLeft()>bound){
				if(pool.size()>1){
					columns.add(pool);
					pool=new ArrayList<>();
				}else if(pool.size()==1){
					if(!columns.isEmpty()){
						line.add(layoutV(columns,baseline,fontsize));
						columns.clear();
					}
					if(!line.isEmpty()){
						Span space=getSpace(line.get(line.size()-1),pool.get(0));
						if(space!=null){
							line.add(space);
						}
					}
					line.addAll(pool);
					pool.clear();
				}
			}
			pool.add(span);
			if(span.getBox().getRight()>bound){
				bound=span.getBox().getRight();
			}
		}
		if(pool.size()>1){
			columns.add(pool);
			line.add(layoutV(columns,baseline,fontsize));
		}else if(pool.size()==1){
			if(!columns.isEmpty()){
				line.add(layoutV(columns,baseline,fontsize));
			}
			if(!line.isEmpty()){
				Span space=getSpace(line.get(line.size()-1),pool.get(0));
				if(space!=null){
					line.add(space);
				}
			}
			line.addAll(pool);
		}
		return fixScript(line,baseline,fontsize);
	}
	private Line fixScript(List<Span> spans,int baseline,int fontsize){
		List<Span> fixed=new ArrayList<>(spans.size());
		List<Span> scripts=new ArrayList<>();
		int baselineTop=baseline, baselineBottom=baseline;
		Span last=null;
		for(int i=0;i<spans.size();i++){
			for(int j=i;j<spans.size()&&isSubscript(spans.get(j),last,baseline,fontsize);j++){
				scripts.add(spans.get(j));
			}
			if(!scripts.isEmpty()&&scripts.size()<spans.size()){
				int[] baseLineAndSize=getBaseLineAndSize(scripts);
				fixed.add(new Subscript(scripts.size()==1?scripts.get(0):fixScript(scripts,baseLineAndSize[0],baseLineAndSize[1])));
				i+=scripts.size()-1;
				scripts.clear();
				continue;
			}
			for(int j=i;j<spans.size()&&isSuperscript(spans.get(j),last,baseline,fontsize);j++){
				scripts.add(spans.get(j));
			}
			if(!scripts.isEmpty()&&scripts.size()<spans.size()){
				int[] baseLineAndSize=getBaseLineAndSize(scripts);
				fixed.add(new Superscript(scripts.size()==1?scripts.get(0):fixScript(scripts,baseLineAndSize[0],baseLineAndSize[1])));
				i+=scripts.size()-1;
				scripts.clear();
				continue;
			}
			Span span=spans.get(i);
			fixed.add(span);
			last=span;
		}
		return new Line(fixed);
	}
	private boolean isSubscript(Span span,Span last,int baseline,int fontsize){
		if(span.getFontSize()<=fontsize*8/10&&last!=null&&span.getBox().getTop()-last.getBox().getTop()>last.getBox().getHeight()*4/10){
			if(span.isBaseLineReliable()){
				return span.getBaseLine()>baseline-fontsize/10;
			}else{
				return Math.abs(span.getBaseLine()-last.getBaseLine())>fontsize*2/10;
			}
		}else{
			return false;
		}
	}
	private boolean isSuperscript(Span span,Span last,int baseline,int fontsize){
		if(span.getFontSize()<=fontsize*8/10&&last!=null&&last.getBox().getBottom()-span.getBox().getBottom()>last.getBox().getHeight()*4/10){
			if(span.isBaseLineReliable()){
				return span.getBaseLine()<baseline-fontsize*3/10;
			}else{
				return Math.abs(span.getBaseLine()-last.getBaseLine())>fontsize*2/10;
			}
		}else{
			return false;
		}
	}
	private Span getSpace(Span last,Span curr){
		int localSize=Math.max(curr.getFontSize(),last.getFontSize());
		if(curr.getBox().getLeft()-last.getBox().getRight()>localSize/4){
			BoundBox box=new BoundBox(last.getBox().getRight(),curr.getBox().getLeft(),
					Math.min(curr.getBox().getTop(),last.getBox().getTop()),
					Math.max(curr.getBox().getBottom(),last.getBox().getBottom()));
			return new Symbol(' ',Symbol.DEFAULT_FAMILY,localSize,Symbol.DEFAULT_STYLE,box,Math.max(curr.getBaseLine(),last.getBaseLine()),1.0);
		}else{
			return null;
		}
	}
	private List<Symbol> resolve(List<NavigableSet<CharacterCandidate>> candidates){
		int[] baseLineAndSize=predictBaseLineAndSize(candidates);
		double baseline=baseLineAndSize[0];
		double size=baseLineAndSize[1];
		List<Symbol> characters=new ArrayList<>(candidates.size());
		Iterator<NavigableSet<CharacterCandidate>> iterator=candidates.iterator();
		if(iterator.hasNext()){
			characters.add(select(iterator.next(),baseline,size));
			while(iterator.hasNext()){
				characters.add(select(iterator.next(),baseline,size));
			}
		}
		return characters;
	}
	private Symbol select(NavigableSet<CharacterCandidate> candidates,double baseLine,double size){
		return new Symbol(candidates.first());
//Stream<CharacterCandidate> stream=candidates.stream();
		//return new Symbol(stream.max(Comparator.comparingDouble((c)->c.getScore()*SCORE_COEF
		//		-Math.abs((c.getBaseLine()-baseLine)/size)*LINE_COEF-Math.abs((c.getFontSize()-size)/size)*SIZE_COEF)).get());
	}
	//private static final int SCORE_COEF=2, SIZE_COEF=1, LINE_COEF=1;
	private int[] predictBaseLineAndSize(List<NavigableSet<CharacterCandidate>> characters){
		int[] baseLines=characters.stream().mapToInt((c)->c.first().getBaseLine()).toArray();
		int[] sizes=characters.stream().mapToInt((c)->c.first().getFontSize()).toArray();
		return new int[]{median(baseLines),median(sizes)};
	}
	private int[] getBaseLineAndSize(List<? extends Span> characters){
		int[] baseLines=characters.stream().mapToInt((c)->c.getBaseLine()).toArray();
		int[] sizes=characters.stream().mapToInt((c)->c.getFontSize()).toArray();
		return new int[]{median(baseLines),median(sizes)};
	}
	private int median(int... num){
		if(num.length==0){
			return Symbol.DEFAULT_SIZE;
		}
		Arrays.sort(num);
		return num[num.length/2];
	}
}
