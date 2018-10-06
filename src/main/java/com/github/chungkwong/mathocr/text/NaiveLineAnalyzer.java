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
import com.github.chungkwong.mathocr.text.structure.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.*;
/**
 * Naive implementation of LineAnalyzer
 *
 * @author Chan Chung Kwong
 */
public class NaiveLineAnalyzer implements LineAnalyzer{
	public static final String NAME="NAIVE";
	private static final int HORIZONTAL=0, SUBSCRIPT=1, SUPERSCRIPT=2;
	@Override
	public Line analysis(List<NavigableSet<CharacterCandidate>> characters){
		return analysisLine(resolveSymbols(characters));
	}
	private Span analysisMatrix(List<List<Span>> spans,int fontSize){
		spans=refineMatrix(spans,fontSize);
		int n=spans.size();
		List<List<List<Span>>> matrix=cutVertically(spans);
		int m=matrix.get(0).size();
		if(n==1){
			Span column=analysisColumn(matrix.get(0));
			if(column!=null){
				return column;
			}
		}
		List<List<Span>> array=new ArrayList<>(m);
		for(int i=0;i<m;i++){
			List<Span> row=new ArrayList<>(n);
			for(int j=0;j<n;j++){
				List<Span> cell=matrix.get(j).get(i);
				if(cell.size()==1){
					row.add(cell.get(0));
				}else if(m!=1||n!=1){
					row.add(analysisLine(cell));
				}else if(n>1){
					row.add(new Line(spans.stream().map((span)->analysisColumn(Collections.singletonList(span))).collect(Collectors.toList())));
				}else{
					row.add(analysisMess(cell));
				}
			}
			array.add(row);
		}
		if(m==2&&n==1&&array.get(0).get(0).getFontSize()<fontSize*7/10&&array.get(1).get(0).getFontSize()<fontSize*7/10){
			return new Line(Arrays.asList(new Subscript(array.get(1).get(0)),new Superscript(array.get(0).get(0))));
		}else if(m==1){
			return new Line(array.get(0));
		}
		return new Matrix(array);
	}
	private List<List<Span>> refineMatrix(List<List<Span>> spans,int fontSize){
		List<List<Span>> columns=new ArrayList<>();
		int lastRight=-fontSize-1;
		for(List<Span> span:spans){
			int left=span.stream().mapToInt((s)->s.getBox().getLeft()).min().getAsInt();
			if(left-lastRight>fontSize/4){
				columns.add(new ArrayList<>(span));
			}else{
				columns.get(columns.size()-1).addAll(span);
			}
			lastRight=span.stream().mapToInt((s)->s.getBox().getRight()).max().getAsInt();
		}
		return columns;
	}
	private Line analysisMess(List<Span> spans){
		Collections.sort(spans,Comparator.comparingInt((s)->s.getBox().getLeft()));
		return fixScript(spans);
	}
	private Line analysisLine(List<? extends Span> spans){
		List<List<Span>> columns=cutHorizontally(spans);
		List<Span> line=new ArrayList<>();
		int fontSize=estimateFontsize(spans);
		for(ListIterator<List<Span>> iterator=columns.listIterator();iterator.hasNext();){
			List<Span> next=iterator.next();
			Span curr;
			if(next.size()==1){
				curr=next.get(0);
			}else{
				int start=iterator.previousIndex();
				int end=start+1;
				while(iterator.hasNext()){
					List<Span> next1=iterator.next();
					if(next1.size()>1){
						++end;
					}else{
						iterator.previous();
						break;
					}
				}
				curr=analysisMatrix(columns.subList(start,end),fontSize);
			}
			if(!line.isEmpty()){
				Span space=getSpace(line.get(line.size()-1),curr);
				if(space!=null){
					line.add(space);
				}
			}
			line.add(curr);
		}
		fixDots(line);
		return fixScript(line);
	}
	private void fixDots(List<Span> spans){
		int found=0;
		for(ListIterator<Span> iterator=spans.listIterator();iterator.hasNext();){
			Span next=iterator.next();
			if(isDot(next)){
				++found;
				if(found==3){
					iterator.remove();
					Span last=iterator.previous();
					iterator.remove();
					if(last instanceof Symbol&&((Symbol)last).getCodePoint()==' '){
						last=iterator.previous();
						iterator.remove();
					}
					Span lastlast=iterator.previous();
					if(lastlast instanceof Symbol&&((Symbol)lastlast).getCodePoint()==' '){
						iterator.remove();
						lastlast=iterator.previous();
					}
					BoundBox box=BoundBox.union(lastlast.getBox(),last.getBox(),next.getBox());
					int r1=checkDirection(last,lastlast);
					int r2=checkDirection(next,last);
					if(r1==-1&&r2==1){
						iterator.set(getSymbol('\u2234',box));
					}else if(r1==1&&r2==-1){
						iterator.set(getSymbol('\u2235',box));
					}else if(r1==-1&&r2==-1){
						iterator.set(getSymbol('\u22F0',box));
					}else if(r1==1&&r2==-1){
						iterator.set(getSymbol('\u22F1',box));
					}else{
						iterator.set(getSymbol('\u22EF',box));
					}
					found=0;
				}
			}else if(!(next instanceof Symbol)||((Symbol)next).getCodePoint()!=' '){
				found=0;
			}
		}
	}
	private int checkDirection(Span curr,Span last){
		if(curr.getBox().getBottom()<last.getBox().getTop()-(curr.getBox().getLeft()-last.getBox().getLeft())/2){
			return -1;
		}else if(curr.getBox().getTop()>last.getBox().getBottom()+(curr.getBox().getLeft()-last.getBox().getLeft())/2){
			return 1;
		}else{
			return 0;
		}
	}
	private Line fixScript(List<Span> spans){
		List<Span> fixed=new ArrayList<>(spans.size());
		List<Span> scripts=new ArrayList<>();
		Span last=null;
		for(int i=0;i<spans.size();i++){
			Span span=spans.get(i);
			int rel=getRoughRelation(span,last);
			Span correction=toPositionAwareForm(spans.get(i),rel);
			if(correction!=null){
				fixed.add(correction);
			}else if(rel==HORIZONTAL){
				fixed.add(span);
				if(span.isBaseLineReliable()){
					last=span;
				}
			}else{
				scripts.add(span);
				if(rel==SUBSCRIPT){
					for(int j=i+1;j<spans.size()&&getRoughRelation(spans.get(j),last)==SUBSCRIPT;j++){
						scripts.add(spans.get(j));
					}
					fixed.add(new Subscript(scripts.size()==1?scripts.get(0):fixScript(scripts)));
				}else if(rel==SUPERSCRIPT){
					for(int j=i+1;j<spans.size()&&getRoughRelation(spans.get(j),last)==SUPERSCRIPT;j++){
						scripts.add(spans.get(j));
					}
					fixed.add(new Superscript(scripts.size()==1?scripts.get(0):fixScript(scripts)));
				}
				i+=scripts.size()-1;
				scripts.clear();
			}
		}
		return new Line(fixed);
	}
	private int getRoughRelation(Span child,Span parent){
		if(parent==null||(parent instanceof Symbol&&!parent.isBaseLineReliable())){
			return HORIZONTAL;
		}else if(child instanceof Symbol&&!child.isBaseLineReliable()){
			if(child.getBox().getTop()-parent.getBox().getTop()>parent.getBox().getHeight()*7/10){
				return SUBSCRIPT;
			}
			if(parent.getBox().getBottom()-child.getBox().getBottom()>parent.getBox().getHeight()*7/10){
				return SUPERSCRIPT;
			}
			return HORIZONTAL;
		}else{
			int relationship=getRelationship(child,parent);
			if(relationship==SUBSCRIPT){
				if(child.getBox().getTop()<parent.getBox().getTop()+parent.getBox().getHeight()*3/10){
					relationship=HORIZONTAL;
				}
			}else if(relationship==SUPERSCRIPT){
				if(child.getBox().getBottom()>parent.getBox().getBottom()-parent.getBox().getHeight()/2){
					relationship=HORIZONTAL;
				}
			}
			return relationship;
		}
		/*
		if(parent!=null&&parent.getBox().getHeight()>=child.getBox().getHeight()/3&&child.getFontSize()<=parent.getFontSize()*8/10){
			int dtop=child.getBox().getTop()-parent.getBox().getTop();
			if(!child.isBaseLineReliable()&&dtop>parent.getBox().getHeight()*8/10){
				return SUBSCRIPT;
			}
			if(child.isBaseLineReliable()&&dtop>parent.getBox().getHeight()*4/10
					&&child.getBaseLine()>parent.getBaseLine()+parent.getFontSize()/10){
				return SUBSCRIPT;
			}
			int dbottom=parent.getBox().getBottom()-child.getBox().getBottom();
			if(!child.isBaseLineReliable()&&dbottom>parent.getBox().getHeight()*8/10){
				return SUPERSCRIPT;
			}
			if(child.isBaseLineReliable()&&dbottom>parent.getBox().getHeight()*4/10
					&&child.getBaseLine()<parent.getBaseLine()-parent.getFontSize()*3/10){
				return SUPERSCRIPT;
			}
		}
		return HORIZONTAL;*/
	}
	private Span analysisColumn(List<List<Span>> column){
		int m=column.size();
		if(m==1){
			if(column.get(0).get(0) instanceof Symbol&&((Symbol)column.get(0).get(0)).getCodePoint()=='√'){
				return analysisRoot(column.get(0));
			}else if(column.get(0).size()==1){
				return column.get(0).get(0);
			}
		}
		if(m>=2&&column.get(0).size()==1){
			if(column.get(1).size()==1){
				if(m==2&isHorizontalLine(column.get(0).get(0))&&isHorizontalLine(column.get(1).get(0))){
					return getSymbol('=',BoundBox.union(column.get(0).get(0).getBox(),column.get(1).get(0).getBox()));
				}else if(isDot(column.get(0).get(0))&&isDot(column.get(1).get(0))){
					if(m==2){
						return getSymbol(':',BoundBox.union(column.get(0).get(0).getBox(),column.get(1).get(0).getBox()));
					}else if(m==3&&column.get(2).size()==1&&isDot(column.get(2).get(0))){
						return getSymbol('\u22EE',BoundBox.union(column.get(0).get(0).getBox(),column.get(2).get(0).getBox()));
					}
				}
			}
			Span hat=toHat(column.get(0).get(0));
			if(hat!=null){
				return new Over(analysisLine(flat(column,1,m)),hat);
			}
			Span operator=toBigOperator(column.get(0).get(0));
			if(operator!=null){
				return new Under(operator,analysisLine(flat(column,1,m)));
			}
		}
		if(m>=2&&column.get(m-1).size()==1){
			Span leg=toLeg(column.get(m-1).get(0));
			if(leg!=null){
				return new Under(analysisLine(flat(column,0,m-1)),leg);
			}
		}
		if(m>=3){
			int special=-1;
			Span div=null;
			for(int i=1;i<m-1;i++){
				if(column.get(i).size()==1){
					Span old=column.get(i).get(0);
					Span span=toBigOperator(old);
					if((span!=null||isHorizontalLine(old))&&(div==null||old.getBox().getWidth()>div.getBox().getWidth())){
						div=span;
						special=i;
					}
				}
			}
			if(special!=-1){
				Span over=analysisLine(flat(column,0,special));
				Span under=analysisLine(flat(column,special+1,m));
				if(div!=null){
					return new UnderOver(div,under,over);
				}else{
					return new Fraction(over,under);
				}
			}
		}
		return null;
	}
	private List<Span> flat(List<List<Span>> column,int from,int to){
		return column.subList(from,to).stream().flatMap((c)->c.stream()).collect(Collectors.toCollection(ArrayList::new));
	}
	private Span toHat(Span span){
		if(span instanceof Symbol){
			switch(((Symbol)span).getCodePoint()){
				case '˙':
				case '¨':
				case 'ˆ':
				case '¯':
				case '´':
				case 'ˇ':
				case 'ˋ':
				case '˘':
				case '°':
				case '˜':
				case '⏞':
				case '\u20D7':
					return span;
				case '.':
				case '⋅':
				case '•':
				case '∙':
					return correct('˙',(Symbol)span);
				case '^':
				case '˄':
					return correct('ˆ',(Symbol)span);
				case '~':
					return correct('˜',(Symbol)span);
				case '→':
					return correct('\u20D7',(Symbol)span);
				case '∘':
				case '。':
					return correct('°',(Symbol)span);
				case '-':
				case '_':
				case '‐':
				case '–':
				case '—':
				case '⌢':
				case '⌣':
				case '−':
					return correct('¯',(Symbol)span);
			}
		}
		return null;
	}
	private Span toLeg(Span span){
		if(span instanceof Symbol){
			switch(((Symbol)span).getCodePoint()){
				case '⏟':
					return span;
				case '-':
				case '_':
				case '‐':
				case '–':
				case '—':
				case '⌢':
				case '⌣':
				case '−':
					return correct('_',(Symbol)span);
			}
		}
		return null;
	}
	private Span toBigOperator(Span span){
		if(span instanceof Symbol){
			switch(((Symbol)span).getCodePoint()){
				case '∏':
				case '∐':
				case '∑':
				case '∫':
				case '∬':
				case '∭':
				case '∮':
				case '∯':
				case '∰':
				case '∱':
				case '∲':
				case '∳':
				case '⋀':
				case '⋁':
				case '⋂':
				case '⋃':
				case '⨀':
				case '⨁':
				case '⨂':
				case '⨃':
				case '⨄':
				case '⨅':
				case '⨆':
				case '⨉':
					return span;
				case 'Π':
					return correct('∏',(Symbol)span);
				case 'Σ':
					return correct('∑',(Symbol)span);
				case '∧':
					return correct('⋀',(Symbol)span);
				case '∨':
					return correct('⋁',(Symbol)span);
				case '∩':
					return correct('⋂',(Symbol)span);
				case '∪':
					return correct('⋃',(Symbol)span);
				case '⊕':
					return correct('⨁',(Symbol)span);
				case '⊗':
					return correct('⨂',(Symbol)span);
				case '⊍':
					return correct('⨃',(Symbol)span);
				case '⊌':
					return correct('⨄',(Symbol)span);
				case '⊓':
					return correct('⨅',(Symbol)span);
				case '⊔':
					return correct('⨆',(Symbol)span);
				case '×':
					return correct('⨉',(Symbol)span);
			}
		}
		return null;
	}
	private Span toPositionAwareForm(Span span,int position){
		if(span instanceof Symbol){
			int codePoint=((Symbol)span).getCodePoint();
			if(codePoint==' '){
				return span;
			}
			int modify=toPositionAwareForm(codePoint,position);
			if(modify!=-1){
				return correct(modify,(Symbol)span);
			}
		}
		return null;
	}
	private int toPositionAwareForm(int codePoint,int position){
		switch(codePoint){
			case '˙':
			case '.':
			case '⋅':
			case '•':
			case '∙':
				return position==HORIZONTAL?'⋅':(position==SUPERSCRIPT?'˙':'.');
			case ',':
			case '\'':
				return position==HORIZONTAL?',':(position==SUPERSCRIPT?'\'':',');
			case '’':
				return position==SUPERSCRIPT?codePoint:',';
			case '-':
			case '¯':
			case '_':
			case '‐':
			case '–':
			case '—':
			case '⌢':
			case '⌣':
			case '−':
				return position==HORIZONTAL?'-':(position==SUPERSCRIPT?'-':'_');
			case 'ˋ':
			case '`':
			case '、':
				return position==HORIZONTAL?'`':(position==SUPERSCRIPT?'`':'、');
			case '°':
			case '∘':
			case '。':
				return position==HORIZONTAL?'∘':(position==SUPERSCRIPT?'°':'。');
			case '˜':
				return '~';
			case '\u20D7':
				return '→';
			case '\u2026':
			case '\u22EF':
				return position==SUBSCRIPT?'\u2026':'\u22EF';
			default:
				return -1;
		}
	}
	private static boolean isHorizontalLine(Span symbol){
		return symbol instanceof Symbol&&"-¯_‐–—⌢⌣−".indexOf(((Symbol)symbol).getCodePoint())!=-1;
	}
	private static boolean isDot(Span symbol){
		return symbol instanceof Symbol&&".˙⋅•∙".indexOf(((Symbol)symbol).getCodePoint())!=-1;
	}
	private Symbol correct(int correction,Symbol wrong){
		if(wrong.getCodePoint()==correction){
			return wrong;
		}
		CharacterPrototype prototype=ModelManager.getCharacterList().getCharacter(correction);
		if(prototype!=null){
			return new Symbol(prototype.toCandidate(wrong.getBox(),wrong.getConfidence()));
		}else{
			return new Symbol(correction,wrong.getFamily(),wrong.getFontSize(),wrong.getStyle(),wrong.getBox(),wrong.getBaseLine(),wrong.getConfidence());
		}
	}
	private Symbol getSymbol(int codePoint,BoundBox box){
		CharacterPrototype prototype=ModelManager.getCharacterList().getCharacter(codePoint);
		if(prototype!=null){
			return new Symbol(prototype.toCandidate(box,1.0));
		}else{
			return new Symbol(codePoint,Font.SERIF,box.getWidth(),Font.PLAIN,box,box.getBottom(),1.0);
		}
	}
	private Span analysisRoot(List<Span> spans){
		Span root=spans.get(0);
		int fontSize=estimateFontsize(spans);
		List<Span> left=new ArrayList<>();
		List<Span> right=new ArrayList<>();
		for(Span child:spans.subList(1,spans.size())){
			if(child.getBox().getLeft()<root.getBox().getLeft()+fontSize/4){
				left.add(child);
			}else{
				right.add(child);
			}
		}
		if(left.isEmpty()){
			return new Radical(null,root,analysisLine(right));
		}else{
			return new Radical(analysisLine(left),root,analysisLine(right));
		}
	}
	private int estimateFontsize(List<? extends Span> spans){
		return median(spans.stream().mapToInt((s)->s.getFontSize()).toArray());
	}
	private int median(int... num){
		if(num.length==0){
			return Symbol.DEFAULT_SIZE;
		}
		Arrays.sort(num);
		return num[num.length/2];
	}
	private List<Symbol> resolveSymbols(List<NavigableSet<CharacterCandidate>> candidates){
		List<Symbol> characters=new ArrayList<>(candidates.size());
		for(Iterator<NavigableSet<CharacterCandidate>> iterator=candidates.iterator();iterator.hasNext();){
			characters.add(new Symbol(iterator.next().first()));
		}
		return characters;
	}
	private List<List<Span>> cutHorizontally(List<? extends Span> spans){
		Collections.sort(spans,Comparator.comparingInt((s)->s.getBox().getLeft()));
		List<List<Span>> columns=new ArrayList<>();
		List<Span> lastColumn=null;
		int lastRight=-1;
		for(Span span:spans){
			if(span.getBox().getLeft()<=lastRight){
				if(span.getBox().getRight()>lastRight){
					lastRight=span.getBox().getRight();
				}
				lastColumn.add(span);
			}else{
				lastRight=span.getBox().getRight();
				lastColumn=new ArrayList<>();
				lastColumn.add(span);
				columns.add(lastColumn);
			}
		}
		return columns;
	}
	private List<List<List<Span>>> cutVertically(List<List<Span>> columns){
		columns.forEach((column)->Collections.sort(column,Comparator.comparingInt((s)->s.getBox().getTop())));
		List<ListIterator<Span>> cellIterators=columns.stream().map((c)->c.listIterator()).collect(Collectors.toList());
		int bound=-1;
		int n=columns.size();
		List<List<List<Span>>> matrix=new ArrayList<>();
		for(int j=0;j<n;j++){
			matrix.add(new ArrayList<>());
		}
		int m=-1;
		while(cellIterators.stream().anyMatch((iter)->iter.hasNext())){
			boolean changed=false;
			int nextBound=Integer.MAX_VALUE;
			for(int j=0;j<cellIterators.size();j++){
				ListIterator<Span> cells=cellIterators.get(j);
				while(cells.hasNext()){
					Span next=cells.next();
					if(next.getBox().getTop()<=bound){
						matrix.get(j).get(m).add(next);
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
				for(List<List<Span>> list:matrix){
					list.add(new ArrayList<>());
				}
				bound=nextBound;
				++m;
			}
		}
		return matrix;
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
	private static final double[] PROBABILITY={
		528348/0.009861529660392148,
		15001/0.013195586738944665,
		8903/0.027787839624836656,};
	private static final double[][] MEAN={
		{1.0135851617645684,8.383093131682563E-4},
		{0.6747105237471085,0.14521376635106567},
		{0.7515797205037746,-0.23817089113652665}
	};
	private static final double[][][] INVERSE={
		{{24.691897478823577,-39.318012662812706},{-39.318012662812706,479.0521704647488}},
		{{61.03638849122157,-38.84997225490616},{-38.84997225490616,118.8204349130987}},
		{{34.5467574161527,-29.8233989085476},{-29.8233989085476,63.233047170290405}}
	};
	private static int getRelationship(Span child,Span parent){
		double scale=1.0/parent.getFontSize();
		double[] record=new double[]{
			child.getFontSize()*scale,
			(child.getBaseLine()-parent.getBaseLine())*scale};
		int bestIndex=0;
		double bestValue=-1;
		for(int t=0;t<PROBABILITY.length;t++){
			double tmp=0.0;
			for(int i=0;i<record.length;i++){
				for(int j=0;j<record.length;j++){
					tmp+=(record[i]-MEAN[t][i])*INVERSE[t][i][j]*(record[j]-MEAN[t][j]);
				}
			}
			double value=PROBABILITY[t]*Math.exp(-0.5*tmp);
			if(value>bestValue){
				bestIndex=t;
				bestValue=value;
			}
		}
		return bestIndex;
	}
}
