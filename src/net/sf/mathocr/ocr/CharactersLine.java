/* CharactersLine.java
 * =========================================================================
 * This file is originally part of the MathOCR Project
 *
 * Copyright (C) 2015 Chan Chung Kwong
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 */
package net.sf.mathocr.ocr;
import java.util.*;
import net.sf.mathocr.common.*;
import net.sf.mathocr.layout.*;
/**
 * A data structure represent a line of characters
 */
public final class CharactersLine{
	ArrayList<ConnectedComponent> pool;
	List<Char> characters=new ArrayList<Char>();
	int index=-1;
	/**
	 * Construct a CharactersLine
	 * @param line the text line
	 */
	public CharactersLine(TextLine line){
		DataBase db=DataBase.DEFAULT_DATABASE;
		pool=line.getConnectedComponents();
		ComponentPool.combineAsGlyph(pool);
		recognize(db);
	}
	/**
	 * Construct a CharactersLine
	 * @param pool the ConnectedComponent that form the text line
	 */
	public CharactersLine(ArrayList<ConnectedComponent> pool){
		this.pool=pool;
		DataBase db=DataBase.DEFAULT_DATABASE;
		ComponentPool.combineAsGlyph(pool);
		recognize(db);
	}
	/**
	 * Construct a CharactersLine
	 * @param pool the ConnectedComponent that form the text line
	 * @param db the database
	 */
	public CharactersLine(ArrayList<ConnectedComponent> pool,DataBase db){
		this.pool=pool;
		ComponentPool.combineAsGlyph(pool);
		recognize(db);
	}
	/**
	 * Construct a CharactersLine
	 * @param charcaters the Char in the text line
	 */
	public CharactersLine(List<Char> characters){
		this.characters=characters;
	}
	/**
	 * Count number of components intersect with a given area
	 * @param left left bound
	 * @param right right bound
	 * @param top upper bound
	 * @param bottom lower bound
	 * @return the number
	 */
	private int count(int left,int right,int top,int bottom){
		int c=0;
		for(ConnectedComponent ele:pool)
			if(ele!=null){
				if(ele.getLeft()>right)
					break;
				if(left<=ele.getRight()&&top<=ele.getBottom()&&ele.getTop()<=bottom)
					++c;
			}
		return c;
	}
	private Candidate matchDots(){
		ConnectedComponent elei=pool.get(index);
		int len=pool.size();
		int topi=elei.getTop(),bottomi=elei.getBottom(),lefti=elei.getLeft(),righti=elei.getRight(),tri=topi-righti,bli=bottomi-lefti;
		int xi=(lefti+righti)/2,yi=(topi+bottomi)/2,flex=bottomi-topi+1;
		for(int j=index+1;j<len;j++){
			ConnectedComponent elej=pool.get(j);
			if(elej==null||!SpecialMatcher.isPossibleDot(elej))
				continue;
			int topj=elej.getTop(),bottomj=elej.getBottom(),leftj=elej.getLeft(),rightj=elej.getRight();
			int xj=(leftj+rightj)/2,yj=(topj+bottomj)/2;
			int option=-1;
			double scale=(bottomj-topj+1.0)/flex;
			if(xj-xi>flex*16)
				break;
			if(scale<0.8||scale>1.25)
				continue;
			if(yj>=topi&&yj<=bottomi)
				option=0;
			else if(xj>=lefti&&xj<=righti)
				option=1;
			else if(yj-xj<=bli&&yj-xj>=tri)
				option=2;
			else
				continue;
			for(int k=j+1;k<len;k++){
				ConnectedComponent elek=pool.get(k);
				if(elek==null||!SpecialMatcher.isPossibleDot(elek))
					continue;
				int topk=elek.getTop(),bottomk=elek.getBottom(),leftk=elek.getLeft(),rightk=elek.getRight();
				int xk=(leftk+rightk)/2,yk=(topk+bottomk)/2;
				int xmax=Math.max(Math.max(xi,xj),xk),xmin=Math.min(Math.min(xi,xj),xk),xmid=xi+xj+xk-xmax-xmin;
				int ymax=Math.max(Math.max(yi,yj),yk),ymin=Math.min(Math.min(yi,yj),yk),ymid=yi+yj+yk-ymax-ymin;
				scale=(bottomk-topk+1.0)/flex;
				if(xmax-xmin>flex*16)
					break;
				if(scale<0.8||scale>1.25||Math.abs(xmin+xmax-2*xmid)>flex||Math.abs(ymin+ymax-2*ymid)>flex||ymax-ymin>flex*16)
					continue;
				if(option==0&&yk>=topi&&yk<=bottomi&&count(xmin,xmax,ymid-(xmax-xmin+1)/3,ymid+(xmax-xmin+1)/3)==3){
					elei.combineWith(elej);
					elei.combineWith(elek);
					pool.set(j,null);
					pool.set(k,null);
					return new Candidate(new Symbol("","\\cdots \tVC",elei.getWidth(),elei.getHeight(),elei.getWidth(),elei.getWidth(),0,0,elei.getWidth()/2),1.0);
				}else if(option==1&&xk>=lefti&&xk<=righti&&count(xmid-(ymax-ymin+1)/2,xmid+(ymax-ymin+1)/2,ymin,ymax)==3){
					elei.combineWith(elej);
					elei.combineWith(elek);
					pool.set(j,null);
					pool.set(k,null);
					return new Candidate(new Symbol("","\\vdots \tVC",elei.getHeight(),elei.getHeight(),elei.getHeight(),elei.getHeight(),-elei.getHeight()/2,0,0),1.0);
				}else if(option==2&&topk-rightk<=bli&&bottomk-leftk>=tri&&count(xmin,xmax,ymin,ymax)==3){
					elei.combineWith(elej);
					elei.combineWith(elek);
					pool.set(j,null);
					pool.set(k,null);
					return new Candidate(new Symbol("","\\ddots \tVC",elei.getWidth(),elei.getHeight(),elei.getWidth(),elei.getHeight(),0,0,0),1.0);
				}else
					continue;
			}
		}
		return null;
	}
	private static final void addSpecialCandidate(Candidate cand,Char ch){
		if(cand!=null&&cand.getCertainty()>0.9)
			ch.getCandidates().add(cand);
	}
	private void matchSpecialSymbol(Char ch){
		ConnectedComponent ele=pool.get(index);
		if(!SpecialMatcher.usable)
			return;
		addSpecialCandidate(SpecialMatcher.isHorizontalLine(ele),ch);
		addSpecialCandidate(SpecialMatcher.isVerticalLine(ele),ch);
		addSpecialCandidate(SpecialMatcher.isLeftParen(ele),ch);
		addSpecialCandidate(SpecialMatcher.isRightParen(ele),ch);
		addSpecialCandidate(SpecialMatcher.isLeftBracket(ele),ch);
		addSpecialCandidate(SpecialMatcher.isRightBracket(ele),ch);
		addSpecialCandidate(SpecialMatcher.isLeftBrace(ele),ch);
		addSpecialCandidate(SpecialMatcher.isRightBrace(ele),ch);
		addSpecialCandidate(SpecialMatcher.isLeftFloor(ele),ch);
		addSpecialCandidate(SpecialMatcher.isRightFloor(ele),ch);
		addSpecialCandidate(SpecialMatcher.isLeftCeil(ele),ch);
		addSpecialCandidate(SpecialMatcher.isRightCeil(ele),ch);
		addSpecialCandidate(SpecialMatcher.isOverBrace(ele),ch);
		addSpecialCandidate(SpecialMatcher.isUnderBrace(ele),ch);
		addSpecialCandidate(SpecialMatcher.isRightArrow(ele),ch);
		addSpecialCandidate(SpecialMatcher.isLeftArrow(ele),ch);
		addSpecialCandidate(SpecialMatcher.isRootSign(ele),ch);
	}
	/**
	 * Recognize the character in the textline
	 * @param base the Database being used to match
	 */
	public void recognize(DataBase base){
		Collections.sort(pool,ConnectedComponent.FROM_LEFT);
		ConnectedComponent ele=null;
		Map<Glyph,Double> cand=base.getGlyphs(),candl=base.getHLineGlyphs(),candd=base.getDotGlyphs();
		CombinedMatcher matcher=base.getMatcher();
		boolean usedPixel=matcher.getMatchers().size()>=1&&matcher.getMatchers().get(matcher.getMatchers().size()-1) instanceof PixelsMatcher;
		while(++index<pool.size())
			if((ele=pool.get(index))!=null){
				Char ch=new Char(ele);
				Map<Glyph,Double> toCheck;
				TreeSet<Candidate> symlist=ch.getCandidates();
				boolean nondl=false,hl=false;
				if(SpecialMatcher.isPossibleHLine(ele)){
					toCheck=new HashMap<Glyph,Double>(candl);
					hl=true;
				}else if(SpecialMatcher.isPossibleDot(ele)){
					Candidate cd=matchDots();
					if(cd==null)
						toCheck=new HashMap<Glyph,Double>(candd);
					else{
						symlist.add(cd);
						characters.add(ch);
						continue;
					}
				}else{
					toCheck=new HashMap<Glyph,Double>(cand);
					toCheck=matcher.gauss(ele,toCheck);
					nondl=true;
				}
				//toCheck=matcher.gauss(ele,toCheck);
				double max=0;
				for(Map.Entry<Glyph,Double> entry:toCheck.entrySet()){
					Glyph glyph=entry.getKey();
					Symbol sym=glyph.getSymbol();
					List<Glyph> other=sym.getGlyphs();
					double certainty=entry.getValue();
					if(other.size()>1){
						List<Integer> inds=new ArrayList<Integer>();
						List<ConnectedComponent> eles=new ArrayList<ConnectedComponent>();
						eles.add(ele);
						double scale=glyph.getHeight()>glyph.getWidth()?(ele.getHeight()+0.0)/glyph.getHeight():(ele.getWidth()+0.0)/glyph.getWidth();
						double l=(sym.getLogicalHeight())*scale;
						for(Glyph otherglyph:other){
							if(otherglyph!=glyph){
								double cert=0.0;
								int ind=-1;
								double etop=ele.getTop()+(otherglyph.getYOffset()-glyph.getYOffset())*scale;
								double ebottom=ele.getBottom()+(otherglyph.getYOffset()+otherglyph.getHeight()-glyph.getYOffset()-glyph.getHeight())*scale;
								double eleft=ele.getLeft()+(otherglyph.getXOffset()-glyph.getXOffset())*scale;
								double eright=ele.getRight()+(otherglyph.getXOffset()+otherglyph.getWidth()-glyph.getXOffset()-glyph.getWidth())*scale;
								ListIterator<ConnectedComponent> iter=pool.listIterator(index+1);
								while(iter.hasNext()){
									ConnectedComponent ele2=iter.next();
									if(ele2==null)
										continue;
									if(ele2.getLeft()-eleft>0.15*l)
										break;
									double dist1=Math.hypot(etop-ele2.getTop(),eleft-ele2.getLeft())/l;
									double dist2=Math.hypot(ebottom-ele2.getBottom(),eright-ele2.getRight())/l;
									if(dist1<=0.15&&dist2<=0.15){
										double d;
										if(candl.containsKey(otherglyph))
											d=SpecialMatcher.isPossibleHLine(ele2)?1.0:0;
										else if(candd.containsKey(otherglyph))
											d=SpecialMatcher.isPossibleDot(ele2)?1.0:0;
										else
											d=1-PixelsMatcher.DEFAULT_MATCHER.getDistance(ele2,otherglyph.getConnectedComponent());
										d*=(1.0-dist1)*(1.0-dist2);
										if(d>cert){
											cert=d;
											ind=iter.previousIndex();
										}
									}
								}
								if(ind!=-1){
									inds.add(ind);
									eles.add(pool.get(ind));
								}else
									break;
							}
						}
						if(inds.size()+1==other.size()){
							certainty=1-PixelsMatcher.DEFAULT_MATCHER.getDistance(ConnectedComponent.combine(eles),sym.getConnectedComponent());
							symlist.add(new Candidate(sym,certainty,inds));
							max=Math.max(max,certainty);
						}
					}else{
						if(!usedPixel)
							certainty=1-PixelsMatcher.DEFAULT_MATCHER.getDistance(ele,sym.getConnectedComponent());
						symlist.add(new Candidate(sym,certainty));
						max=Math.max(max,certainty);
					}
				}
				double aspectratio=(double)ele.getWidth()/ele.getHeight();
				if(hl&&symlist.isEmpty())
					addSpecialCandidate(SpecialMatcher.isHorizontalLine(ele),ch);
				if(nondl&&(symlist.isEmpty()||max<0.9||aspectratio<=0.2||aspectratio>=2))
					matchSpecialSymbol(ch);
				if(!symlist.isEmpty()){
					if(symlist.first().getOtherIndex()!=null){
						for(Integer ind:symlist.first().getOtherIndex())
							if(pool.get(ind)!=null){
								ele.combineWith(pool.get(ind));
								pool.set(ind,null);
							}
						while(symlist.size()>1)
							symlist.pollLast();
					}else{
						Iterator<Candidate> it=symlist.iterator();
						while(it.hasNext())
							if(it.next().getOtherIndex()!=null)
								it.remove();
					}
					Iterator<Candidate> it=symlist.iterator();
					Candidate first=it.next();
					max=first.getScore();
					while(it.hasNext())
						if(it.next().getScore()<0.95*max)
							it.remove();
					characters.add(ch);
				}
			}
		pool=null;
	}
	/**
	 * Get the characters in the text line
	 * @return the characters
	 */
	public List<Char> getCharacters(){
		return characters;
	}
}