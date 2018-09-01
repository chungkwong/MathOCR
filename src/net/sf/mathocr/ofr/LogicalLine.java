/* LogicalLine.java
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
package net.sf.mathocr.ofr;
import java.util.*;
import net.sf.mathocr.common.*;
import net.sf.mathocr.ocr.*;
/**
 * A data structure representing a line of expression
 */
public final class LogicalLine{
	LinkedList<Expression> expressions=new LinkedList<Expression>();
	AdjointGraph graph;
	String result=null;
	boolean displayed;
	/**
	 * Construct a LogicalLine
	 * @param line characters in the text line
	 */
	public LogicalLine(CharactersLine line,boolean displayed){
		this.displayed=displayed;
		for(Char ch:line.getCharacters())
			expressions.add(new SymbolExpression(ch));
		correctSpecial();
		Collections.sort(expressions);
		correctRecognition();
		graph=new AdjointGraph(expressions,displayed);
	}
	void correctSpecial(){
		int left=0,right=0,top=0,bottom=0,y=0;
		boolean hasOver,hasUnder;
		for(Expression expr:expressions){
			if(expr==null)
				continue;
			TreeSet<Candidate> symbols=((SymbolExpression)expr).getChar().getCandidates();
			Iterator<Candidate> iter=symbols.iterator();
			boolean changed=false;
			out:while(iter.hasNext()){
				Candidate cand=iter.next();
				Symbol sym=cand.getSymbol();
				double cert=cand.getCertainty();
				switch(sym.toString()){
					case "-":
					case "--":
					case "---":
					case "\\bar{}":
						//((SymbolExpression)expr).update(sym);
						left=expr.getPhysicalLeft();right=expr.getPhysicalRight();
						top=expr.getPhysicalTop()-(right-left);bottom=expr.getPhysicalBottom()+(right-left)/2;
						y=expr.getPhysicalBottom();
						hasOver=false;hasUnder=false;
						int frac=-1,thre=expr.getPhysicalHeight()*4,low=0;
						ListIterator<Expression> it=expressions.listIterator();
						while(it.hasNext()){
							Expression e=it.next();
							if(e!=null&&e!=expr&&e.getPhysicalLeft()<=right&&left<=e.getPhysicalRight()&&e.getUpper()<bottom&&top<e.getLower()
							&&!e.toString().equals("\\sqrt{}")){
								String ename=e.toString();
								if(AdjointGraph.isBigOperator(ename)&&e.getPhysicalTop()>=bottom)
									continue;
								if(ename.equals("-")||ename.equals("--")||ename.equals("---")||ename.equals("\\hline")||ename.equals("\\overline{}")){
									if(e.getPhysicalWidth()>=(right-left)+(thre<<1))
										continue;
									if(Math.abs(e.getPhysicalLeft()-left)<thre&&Math.abs(e.getPhysicalRight()-right)<thre)
										frac=(frac==-1?it.previousIndex():-2);
								}
								if(e.getPhysicalBottom()<y){
									hasOver=true;
									low=Math.max(low,e.getLower());
								}else
									hasUnder=true;
							}
						}
						if(hasOver&&hasUnder){
							cand.setSymbol(new Symbol("","\\hline\tC",expr.getPhysicalWidth(),expr.getPhysicalHeight(),expr.getPhysicalWidth()
							,expr.getPhysicalHeight(),0,0,0));
							changed=true;
						}else if(!hasOver&&hasUnder){
							cand.setSymbol(new Symbol("","\\overline{}\tC",expr.getPhysicalWidth(),expr.getPhysicalHeight(),expr.getPhysicalWidth()
							,expr.getPhysicalHeight(),0,0,0));
							changed=true;
						}else if(hasOver&&!hasUnder){
							if(frac<0){
								if(low>expr.getPhysicalTop()-(right-left)/2){
									cand.setSymbol(new Symbol("","\\underline{}\tC",expr.getPhysicalWidth(),expr.getPhysicalHeight(),expr.getPhysicalWidth()
									,expr.getPhysicalHeight(),0,0,0));
									changed=true;
								}
							}else{
								symbols.clear();
								ConnectedComponent ele=((SymbolExpression)expr).getChar().getComponent();
								ele.combineWith(((SymbolExpression)expressions.get(frac)).getChar().getComponent());
								symbols.add(new Candidate(new Symbol("","=\tC",ele.getWidth(),ele.getHeight(),ele.getWidth(),ele.getHeight(),0,ele.getHeight(),ele.getHeight()),1.0));
								((SymbolExpression)expr).update(symbols.first().getSymbol(),ele.getLeft(),ele.getRight(),ele.getTop(),ele.getBottom());
								expressions.set(frac,null);
								break out;
							}
						}
						break;
					case "\\rightarrow ":
						//((SymbolExpression)expr).update(sym);
						left=expr.getPhysicalLeft();
						right=expr.getPhysicalRight();
						top=expr.getPhysicalTop()-expr.getPhysicalHeight();
						bottom=expr.getPhysicalBottom()+expr.getPhysicalHeight();
						y=expr.getY();
						hasUnder=false;
						hasOver=false;
						for(Expression e:expressions)
							if(e!=null&&e!=expr&&e.getPhysicalLeft()<=right&&left<=e.getPhysicalRight()&&e.getPhysicalTop()<bottom&&top<e.getPhysicalBottom()&&!e.toString().equals("\\sqrt{}"))
								if(e.getY()>y){
									hasUnder=true;
									break;
								}else if(e.getPhysicalLeft()>=left&&e.getPhysicalRight()<=right){
									hasOver=true;
									break;
								}
						if(hasUnder){
							cand.setSymbol(new Symbol("","\\overrightarrow{}\tC",expr.getPhysicalWidth(),expr.getPhysicalHeight(),expr.getPhysicalWidth()
							,expr.getPhysicalHeight(),0,0,0));
							changed=true;
						}else if(hasOver){
							cand.setSymbol(new Symbol("","\\underrightarrow{}\tC",expr.getPhysicalWidth(),expr.getPhysicalHeight(),expr.getPhysicalWidth()
							,expr.getPhysicalHeight(),0,0,0));
							changed=true;
						}
						break;
					case ".":
					case "\\cdot ":
						((SymbolExpression)expr).update(sym);
						left=expr.getPhysicalLeft();
						right=expr.getPhysicalRight();
						top=expr.getPhysicalTop();
						bottom=expr.getLower();
						hasUnder=false;
						for(Expression e:expressions)
							if(e!=null&&e!=expr&&e.getPhysicalLeft()<=right&&left<=e.getPhysicalRight()&&e.getUpper()<bottom&&top<e.getLower()&&!e.toString().equals("\\sqrt{}")){
								hasUnder=true;
								break;
							}
						if(hasUnder){
							cand.setSymbol(SpecialMatcher.map.get("\\dot{}").getSymbol());
							changed=true;
						}
						break;
				}
			}
			if(changed){
				((SymbolExpression)expr).update(symbols.first().getSymbol());
			}
		}
		ListIterator<Expression> it=expressions.listIterator();
		while(it.hasNext())
			if(it.next()==null)
				it.remove();
	}
	static final boolean isOpenDelimiter(String str){
		return str.equals("(")||str.equals("[")||str.equals("\\{")||str.equals("\\lfloor")||str.equals("\\lceil")||str.equals("\\langle")||str.equals("|");
	}
	static final boolean isCloseDelimiter(String str){
		return str.equals(")")||str.equals("]")||str.equals("\\}")||str.equals("\\rfloor")||str.equals("\\rceil")||str.equals("\\rangle")||str.equals("|");
	}
	public static final int REL_UNKNOWN=-1,REL_LEFT_RIGHT=0,REL_NORMAL_SUP=1,REL_NORMAL_SUB=2,REL_SUP_NORMAL=3,REL_SUB_NORMAL=4;
	private static int modifyRelation(Expression l,Expression r,int rel){
		String str1=l.toString(),str2=r.toString();
		if(rel==REL_SUP_NORMAL||rel==REL_SUB_NORMAL){
			if(str1.startsWith("\\stackrel{")||str1.equals("\\log ")||str1.equals("\\deg ")||str1.equals("\\exp ")||str1.equals("\\dim ")||str1.equals("\\arg ")||str1.equals("\\sum "))
				return REL_LEFT_RIGHT;
			if(isCloseDelimiter(str1)&&r.getUpper()>=l.getUpper()&&r.getLower()<=l.getLower())
				return REL_LEFT_RIGHT;
			if(str2.startsWith(")")||str2.startsWith("]")||str2.startsWith("\\}")||str2.startsWith("\\rceil ")||str2.startsWith("\\rfloor ")||str2.startsWith("\\rangle ")||str2.startsWith("|"))
				return REL_LEFT_RIGHT;
			if(str1.endsWith("(")||str1.endsWith("[")||str1.endsWith("\\{")||str1.endsWith("\\lceil ")||str1.endsWith("\\lfloor ")||str1.endsWith("\\langle "))
				return REL_LEFT_RIGHT;
			if(str1.endsWith(")")||str1.endsWith("]")||str1.endsWith("\\}")||str1.endsWith("\\rceil ")||str1.endsWith("\\rfloor ")||str1.endsWith("\\rangle "))
				return REL_LEFT_RIGHT;
			if(str1.endsWith("\\surd ")||str1.endsWith("\\rightarrow ")||str2.startsWith("\\rightarrow ")||str1.endsWith("\\cdots ")||str1.endsWith("\\vdots ")||str1.endsWith("\\ddots "))
				return REL_LEFT_RIGHT;
			if(str1.endsWith("=")&&r.getUpper()<=l.getLower()&&l.getUpper()<=r.getLower())
				return REL_LEFT_RIGHT;
		}else if(rel==REL_NORMAL_SUP||rel==REL_NORMAL_SUB){
			if(str2.startsWith("\\stackrel{")||str2.equals("\\log ")||str2.equals("\\deg ")||str2.equals("\\exp ")||str2.equals("\\dim ")||str2.equals("\\arg "))
				return REL_LEFT_RIGHT;
			if(isOpenDelimiter(str2)&&l.getUpper()>=r.getUpper()&&l.getLower()<=r.getLower())
				return REL_LEFT_RIGHT;
			if(str1.endsWith("(")||str1.endsWith("[")||str1.endsWith("\\{")||str1.endsWith("\\lceil ")||str1.endsWith("\\lfloor ")||str1.endsWith("\\langle ")
			||(str1.endsWith("|")&&r.getUpper()>=l.getUpper()&&r.getLower()<=l.getLower()))
				return REL_LEFT_RIGHT;
			if(str2.startsWith(")")||str2.startsWith("]")||str2.startsWith("\\}")||str2.startsWith("\\rceil ")||str2.startsWith("\\rfloor ")||str2.startsWith("\\rangle "))
				return REL_LEFT_RIGHT;
			if(str1.endsWith("\\surd ")||str1.endsWith("=")||str2.startsWith("\\rightarrow ")||str1.endsWith("\\rightarrow ")||str2.startsWith("\\cdots ")||str2.startsWith("\\vdots ")||str2.startsWith("\\ddots "))
				return REL_LEFT_RIGHT;
			if(str2.startsWith("=")&&r.getUpper()<=l.getLower()&&l.getUpper()<=r.getLower())
				return REL_LEFT_RIGHT;
		}
		if((l instanceof FractionExpression||str2.startsWith("\\frac{"))&&(l.getLower()-r.getLower())*(r.getPhysicalTop()-l.getPhysicalTop())>=0)
			return REL_LEFT_RIGHT;
		return rel;
	}
	static int modifyRelation(Expression l,Expression r,int rel,boolean strict){
		int newrel=modifyRelation(l,r,rel);
		if(strict&&(rel!=newrel))
			return REL_UNKNOWN;
		else
			return newrel;
	}
	static int checkRelation(Expression l,Expression r,boolean strict){
		if(l.getScale()>=r.getScale()||!r.isNotCustomized()){
			int sup=l.getSuperscriptBound(),sub=l.getSubscriptBound();
			int y=r.getY();
			if(y>=sup&&y<=sub)
				return REL_LEFT_RIGHT;
			if(y<sup&&r.getLogicalBottom()>l.getLogicalTop())
				return modifyRelation(l,r,REL_NORMAL_SUP,strict);
			if(y>sub&&r.getLogicalTop()<l.getLogicalBottom())
				return modifyRelation(l,r,REL_NORMAL_SUB,strict);
		}else{
			int sup=r.getSuperscriptBound(),sub=r.getSubscriptBound();
			int y=l.getY();
			if(y>=sup&&y<=sub)
				return REL_LEFT_RIGHT;
			if(y<sup&&l.getLogicalBottom()>r.getLogicalTop())
				return modifyRelation(l,r,REL_SUP_NORMAL,strict);
			if(y>sub&&l.getLogicalTop()<r.getLogicalBottom())
				return modifyRelation(l,r,REL_SUB_NORMAL,strict);
		}
		return REL_UNKNOWN;
	}
	void correctRecognition(){
		ListIterator<Expression> iter=expressions.listIterator();
		while(iter.hasNext()){
			Expression expr=iter.next();
			if(!expr.isBaseLineFixed())
				continue;
			int left=expr.getLogicalLeft(),right=expr.getLogicalRight()+(int)(expr.getScale()*12),top=expr.getLogicalTop(),bottom=expr.getLogicalBottom();
			ListIterator<Expression> it=expressions.listIterator(iter.previousIndex()+1);
			Expression todo=null;
			while(it.hasNext()){
				Expression e=it.next();
				if(e.getPhysicalLeft()>right)
					break;
				if(e.getLogicalBottom()<top||e.getLogicalTop()>bottom)
					continue;
				if(todo==null&&e.isBaseLineFixed())
					todo=e;
				else{
					todo=null;
					break;
				}
			}
			if(todo!=null&&checkRelation(expr,todo,false)!=REL_LEFT_RIGHT){
				TreeSet<Candidate> cand1=((SymbolExpression)expr).getChar().getCandidates(),cand2=((SymbolExpression)todo).getChar().getCandidates();
				Symbol sym1=((SymbolExpression)expr).getSymbol(),sym2=((SymbolExpression)todo).getSymbol();
				int i1=0,min=Integer.MAX_VALUE;
				for(Candidate c1:cand1){
					if(c1.getSymbol().isVarSize())
						continue;
					((SymbolExpression)expr).update(c1.getSymbol());
					int i2=0;
					for(Candidate c2:cand2){
						if(c2.getSymbol().isVarSize())
							continue;
						((SymbolExpression)todo).update(c2.getSymbol());
						double scale=expr.getScale()/todo.getScale();
						if(scale>0.9&&scale<1.11&&i1+i2<min&&checkRelation(expr,todo,false)==REL_LEFT_RIGHT){
							min=i1+i2;
							sym1=c1.getSymbol();
							sym2=c2.getSymbol();
						}
						++i2;
					}
					++i1;
				}
				((SymbolExpression)expr).update(sym1);
				((SymbolExpression)todo).update(sym2);
			}
		}
	}

	/**
	 * Recognize the text line
	 */
	public String recognize(){
		graph.rewrite();
		return toString();
	}
	/**
	 * Get corresponding AdjointGraph
	 */
	public AdjointGraph getAdjointGraph(){
		return graph;
	}
	/**
	 * Get the font size of the line
	 * @return font size
	 */
	public int getFontSize(){
		int sum=0,count=0;
		for(Expression expr:expressions){
			sum+=(int)(expressions.getFirst().getScale()*40);
			++count;
		}
		return count==0?30:sum/count;
	}
	/**
	 * Get the expressions in the text line
	 * @return the expressions
	 */
	public List<Expression> getExpressions(){
		return expressions;
	}
	/**
	 * Get recognition result
	 * @return LaTeX code
	 */
	public String toString(){
		if(result!=null)
			return result;
		StringBuilder buf=new StringBuilder();
		if(displayed||expressions.size()==1){
			buf.append("$$");
			for(Expression expr:expressions)
				buf.append(expr.toString());
			buf.append("$$");
		}else
			for(Expression expr:expressions)
				if(expr.isText()&&!displayed)
					buf.append(expr.getText());
				else
					buf.append(" $").append(expr.toString()).append("$ ");
		result=Convertor.simplifyLaTeX(buf.toString());
		return result;
	}
}