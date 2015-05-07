/* AdjointGraph.java
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
import static net.sf.mathocr.ofr.LogicalLine.*;
/**
 * A data structure representing subexpressions
 */
public final class AdjointGraph{
	LinkedList<Expression> expressions;
	LinkedList<Edge> edges=new LinkedList<Edge>(),specialEdges=new LinkedList<Edge>();
	boolean displayed=false;
	static HashSet<String> bigops=new HashSet<String>();
	static{
		bigops.add("\\bigsqcup ");bigops.add("\\oint ");bigops.add("\\bigodot ");bigops.add("\\bigoplus ");bigops.add("\\bigotimes ");
		bigops.add("\\sum ");bigops.add("\\prod ");bigops.add("\\int ");bigops.add("\\bigcup ");bigops.add("\\bigcap ");
		bigops.add("\\biguplus ");bigops.add("\\bigwedge ");bigops.add("\\bigvee ");
		bigops.add("\\lim ");bigops.add("\\sup ");bigops.add("\\inf ");bigops.add("\\max ");bigops.add("\\min ");
	}
	/**
	 * Construct a AdjointGraph
	 * @param expressions the symbols
	 * @param displayed if the formula is displayed
	 */
	public AdjointGraph(LinkedList<Expression> expressions,boolean displayed){
		this.expressions=expressions;
		this.displayed=displayed;
		constructGraph();
	}
	static boolean isBigOperator(String str){
		return bigops.contains(str);
	}
	int searchUpper(int y,int left,int right){
		TreeSet<Interval> black=new TreeSet<Interval>();
		for(Expression e:expressions){
			//if(e.getLogicalLeft()>right)
			//	break;
			if(e.getLogicalLeft()<=right&&left<=e.getLogicalRight()&&e.getY()<=y&&(!e.toString().equals("\\sqrt{}")||e.getPhysicalWidth()<right-left))
				black.add(new Interval(e.getUpper(),e.getLower()));
		}
		if(black.isEmpty())
			return y;
		Interval iv=black.last();
		int last=iv.start;
		while((iv=black.lower(iv))!=null){
			if(iv.end<last)
				break;
			if(iv.start<last)
				last=iv.start;
		}
		return last;
	}
	int searchLower(int y,int left,int right,Expression except){
		TreeSet<Interval> black=new TreeSet<Interval>();
		for(Expression e:expressions){
			//if(e.getLogicalLeft()>right)
			//	break;
			if(e.getLogicalLeft()<=right&&left<=e.getLogicalRight()&&e.getY()>=y&&e!=except&&(!e.toString().equals("\\sqrt{}")||e.getPhysicalWidth()<right-left))
				black.add(new Interval(e.getLower(),e.getUpper()));
		}
		if(black.isEmpty())
			return y;
		Interval iv=black.first();
		int last=iv.start;
		while((iv=black.higher(iv))!=null){
			if(iv.end>last)
				break;
			if(iv.start>last)
				last=iv.start;
		}
		return last;
	}
	/**
	 * Initialize the symbol adjoint graph
	 */
	void constructGraph(){
		for(Expression expr:expressions){
			Symbol sym=((SymbolExpression)expr).getSymbol();
			int left=expr.getLogicalLeft(),right=expr.getLogicalRight(),top=expr.getUpper(),bottom=expr.getLower();
			boolean special=false;
			if(sym.isVarSize()){
				switch(sym.toString()){
					case "\\sqrt{}":
						int pw=expr.getPhysicalWidth()/4,prev=-1,w=0;
						boolean toolong=false;
						for(RunLength rl:((SymbolExpression)expr).getChar().getComponent().getRunLengths())
							if(rl.getY()==prev){
								w+=rl.getCount();
							}else{
								if(toolong&&w<pw)
									left=rl.getX();
								toolong=w>=pw;
								w=rl.getCount();
								prev=rl.getY();
							}
						special=true;
						break;
					case "\\hline":case "\\underbrace{}":case "\\overbrace{}":
						top=searchUpper(top-1,left,right);
						bottom=searchLower(bottom+1,left,right,expr);
						special=true;
						break;
					case "\\underline{}":case "\\underrightarrow{}":
						top=searchUpper(top-1,left,right);
						special=true;
						break;
					case "\\overline{}":case "\\overrightarrow{}":
						bottom=searchLower(bottom+1,left,right,expr);
						special=true;
						break;
				}
			}else if(sym.isHat()){
				left=expr.getPhysicalLeft();
				right=expr.getPhysicalRight();
				int low=searchLower(expr.getPhysicalBottom(),left,right,expr);
				if(low!=expr.getPhysicalBottom()){
					bottom=low;
					special=true;
				}
			}
			if(bigops.contains(sym.toString())){
				int lb=-1,rb=-1,ltop=expr.getLogicalTop(),lbottom=expr.getLogicalBottom();
				boolean over=false;
				top=expr.getPhysicalTop();
				bottom=expr.getPhysicalBottom();
				for(Expression e:expressions){
					if(e!=expr&&e.getLogicalTop()<=lbottom&&ltop<=e.getLogicalBottom()&&(!e.toString().equals("\\sqrt{}")||e.getPhysicalWidth()<right-left)){
						if(e.getPhysicalLeft()<left){
							if(e.getLogicalTop()<=bottom&&top<=e.getLogicalBottom())
								lb=Math.max(lb,bigops.contains(e.toString())?(e.getLogicalRight()+left)/2:e.getLogicalRight());
						}else if(e.getPhysicalLeft()>right){
							if(rb==-1&&e.getLogicalTop()<=bottom&&top<=e.getLogicalBottom())
								rb=bigops.contains(e.toString())?(right+e.getLogicalLeft())/2:e.getLogicalLeft();
						}else if(e.getPhysicalLeft()<=expr.getPhysicalRight()&&expr.getPhysicalLeft()<=e.getPhysicalRight()){
							if(e.getY()<expr.getY())
								over=true;
							special=true;
						}
					}
				}
				//System.out.println(lb+":"+rb);
				if(special){
					left=lb+1;
					if(rb!=-1)
						right=rb-1;
					if(over)
						top=searchUpper(top-1,left,right);
					bottom=searchLower(bottom+1,left,right,expr);
				}
			}else if(LogicalLine.isOpenDelimiter(sym.toString())||LogicalLine.isCloseDelimiter(sym.toString())){
				left=expr.getPhysicalLeft();
				right=expr.getPhysicalRight();
			}
			Edge edge=new Edge(expr,left,right,top,bottom,special);
			//System.out.println(expr+":"+left+":"+right+":"+top+":"+bottom+":"+special);
			edges.add(edge);
			if(special)
				specialEdges.add(edge);
		}
		Collections.sort(edges);
		Collections.sort(specialEdges,new Comparator<Edge>(){
			public int compare(Edge edge1,Edge edge2){
				return (edge1.right-edge1.left)-(edge2.right-edge2.left);
			}
		});
		injectSimpleHat();
		for(Edge edge:edges)
			if(!edge.special)
				push(edge);
	}
	void injectSimpleHat(){
		ListIterator<Edge> iter=specialEdges.listIterator();
		while(iter.hasNext()){
			Edge edge=iter.next();
			if(((SymbolExpression)edge.expr).getSymbol().isHat()||edge.toString().equals("\\overline{}")||edge.toString().equals("\\overrightarrow{}")){
				String name=edge.toString();
				int left=edge.left,right=edge.right,top=edge.top,bottom=edge.bottom,y=edge.expr.getY(),width=edge.expr.getPhysicalWidth();
				Edge under=null;
				for(Edge e:edges)
					if((!e.special)&&e.left<=right&&left<=e.right&&top<=e.bottom&&e.top<=bottom)
						if((Math.min(right,e.right)-Math.max(left,e.left))*10>=width*8)
							if(under==null)
								under=e;
							else{
								under=null;
								break;
							}
				if(under!=null){
					under.setExpression(new HatExpression(edge.expr,under.expr));
					edge.remove();
					edges.remove(edge);
					iter.remove();
				}
			}
		}
	}
	void push(Edge edge){
		int rightbound=edge.rightbound;
		boolean founded=false;
		for(Edge edge2:edges){
			if(edge2==edge){
				founded=true;
				continue;
			}
			if(founded){
				if(edge2.left>rightbound)
					break;
				if(edge2.expr.getPhysicalLeft()>rightbound&&!edge2.toString().equals("\\vdots "))
					continue;
				if(edge.top<=edge2.bottom&&edge2.top<=edge.bottom){
					if(edge2.special){
						if(edge2.left>(edge.right+3*edge.left)/4){
							rightbound=Math.min(rightbound,edge2.left);
							if(!(edge2.expr instanceof SymbolExpression && ((SymbolExpression)edge2.expr).getSymbol().isHat()))
								while(edge.getOutDegree()>0)
									Edge.unlink(edge,edge.getOutEdge().first());
							break;
						}else
							rightbound=Math.min(rightbound,edge2.right);
					}else{
						//System.out.println(edge+";"+edge2+";"+edge.top+";"+edge.bottom+";"+edge2.top+";"+edge2.bottom);
						Edge.link(edge,edge2);
						//if(!(edge2.expr instanceof SymbolExpression&&((SymbolExpression)edge2.expr).getSymbol().isHat()))
						rightbound=Math.min(rightbound,(3*edge2.right+edge2.left)/4);
					}
				}
			}else if(edge2.special&&edge.top<=edge2.bottom&&edge2.top<=edge.bottom&&edge2.right>edge.left){
				rightbound=Math.min(rightbound,edge2.right);
			}
		}
		edge.rightbound=rightbound;
	}
	boolean pull(Edge edge){
		int l=edge.left,t=edge.top,b=edge.bottom,lb=0;
		Edge rightest=null;
		for(Edge edge2:edges){
			if(edge2.left>=l){
				break;
			}
			if(edge2.left>=lb&&edge2.top<=b&&t<=edge2.bottom){
				if(edge2.special){
					if(edge2.right>l)
						edge.rightbound=Math.min(edge.rightbound,edge2.right);
					else{
						lb=edge2.right+1;
					}
					rightest=null;
				}else if(rightest==null||edge2.right>rightest.right){
					rightest=edge2;
				}
			}
		}
		if(rightest!=null){
			Edge.link(rightest,edge);
			rightest.rightbound=Math.min(rightest.rightbound,(3*edge.right+edge.left)/4);
			return true;
		}
		return false;
	}
	/*void pull(Edge edge){
		int l=edge.left,t=edge.top,b=edge.bottom;
		ListIterator<Edge> iter=edges.listIterator(edges.size());
		while(iter.hasPrevious()){
			Edge edge2=iter.previous();
			if(edge2.left<l&&edge2.top<=b&&t<=edge2.bottom){//&&l<=edge2.rightbound
				if(edge2.special){
					if(edge2.right>l)
						edge.rightbound=Math.min(edge.rightbound,edge2.right);
					else
						break;
				}else{
					Edge.link(edge2,edge);
					edge2.rightbound=Math.min(edge2.rightbound,(3*edge.right+edge.left)/4);
					break;
				}
			}
		}
	}*/
	static final boolean isPossibleAdjoint(Expression l,Expression r,boolean displayed){
		return displayed||(l.isMath()&&r.isMath())||(l.isText()&&r.isText());
	}
	boolean combineLine(boolean strict){
		boolean changed=false;
		ListIterator<Edge> iter=edges.listIterator(edges.size());
		while(iter.hasPrevious()){
			//for(Edge e:edges)
			//	System.out.println(e.expr+";"+e.outEdge+";"+e.inEdge);
			Edge edge=iter.previous();
			if(edge.inEdge.size()==1&&edge.inEdge.first().outEdge.size()==1){
				Edge edgel=edge.inEdge.first();
				int rel=checkRelation(edgel.expr,edge.expr,strict);
				if(rel!=REL_UNKNOWN&&isPossibleAdjoint(edgel.expr,edge.expr,displayed)&&HorizontalExpression.getSpaceName(edgel.expr,edge.expr).isEmpty()){
					//System.out.println(edgel.expr.toString()+";"+edge.expr.toString());
					edgel.setExpression(new HorizontalExpression(edgel.expr,edge.expr,rel));
					edgel.outEdge=edge.outEdge;
					for(Edge e:edge.outEdge){
						e.inEdge.remove(edge);
						e.inEdge.add(edgel);
					}
					iter.remove();
					//if(edge.expr.toString().endsWith("\\int ")||edge.expr.toString().endsWith("\\oint "))
					//	push(edger);
					//if(edgel.outEdge.isEmpty())
					edgel.rightbound=edge.rightbound;
					push(edgel);
					changed=true;
				}
			}
			//System.out.println();
		}
		return changed;
	}
	boolean combineRightScript(){
		boolean changed=false;
		ListIterator<Edge> iter=edges.listIterator(edges.size());
		while(iter.hasPrevious()){
			Edge edge=iter.previous();
			if(edge.outEdge.size()==2&&edge.outEdge.first().inEdge.size()==1&&edge.outEdge.last().inEdge.size()==1){
				Edge edge1=edge.outEdge.first(),edge2=edge.outEdge.last();
				if((HorizontalExpression.getSpaceName(edge.expr,edge1.expr).isEmpty()||HorizontalExpression.getSpaceName(edge.expr,edge2.expr).isEmpty())){
					if(edge1.left<=edge2.right&&edge2.left<=edge1.right){
						if(edge1.expr.getY()>edge2.expr.getY()){
							Edge tmp=edge1;
							edge1=edge2;
							edge2=tmp;
						}
						if(edge1.outEdge.isEmpty()&&edge2.outEdge.isEmpty()){
							edge.setExpression(new HorizontalExpression(new HorizontalExpression(edge.expr,edge1.expr,REL_NORMAL_SUP),edge2.expr,REL_NORMAL_SUB));
							edge.outEdge.clear();
							edge1.remove();
							edge2.remove();
							edges.remove(edge1);
							edges.remove(edge2);
							edge.rightbound=Math.min(edge1.rightbound,edge2.rightbound);
							push(edge);
							changed=true;
							break;
						}else if(edge1.getOutEdge().size()==1&&edge2.getOutEdge().size()==1&&edge1.getOutEdge().first()==edge2.getOutEdge().first()){
							Edge edge3=edge1.getOutEdge().first();
							if(edge3.inEdge.size()>2)
								continue;
							edge.setExpression(new HorizontalExpression(new HorizontalExpression(edge.expr,edge1.expr,REL_NORMAL_SUP),edge2.expr,REL_NORMAL_SUB));
							edge.outEdge.clear();
							edge3.inEdge.remove(edge1);
							edge3.inEdge.remove(edge2);
							if(!edge.outEdge.contains(edge3)){
								edge.outEdge.add(edge3);
								edge3.inEdge.add(edge);
							}
							edge1.remove();
							edge2.remove();
							edges.remove(edge1);
							edges.remove(edge2);
							edge.rightbound=Math.min(edge1.rightbound,edge2.rightbound);
							changed=true;
							break;
						}
					}
				}
			}
		}
		return changed;
	}
	boolean combineLeftScript(){
		boolean changed=false;
		ListIterator<Edge> iter=edges.listIterator();
		while(iter.hasNext()){
			Edge edge=iter.next();
			if(edge.inEdge.size()==2&&edge.inEdge.first().outEdge.size()==1&&edge.inEdge.last().outEdge.size()==1){
				Edge edge1=edge.inEdge.first(),edge2=edge.inEdge.last();
				if((HorizontalExpression.getSpaceName(edge1.expr,edge.expr).isEmpty()||HorizontalExpression.getSpaceName(edge2.expr,edge.expr).isEmpty())
				&&edge1.inEdge.isEmpty()&&edge2.inEdge.isEmpty()){
					if(edge1.left<=edge2.right&&edge2.left<=edge1.right){
						if(edge1.expr.getY()>edge2.expr.getY()){
							Edge tmp=edge1;
							edge1=edge2;
							edge2=tmp;
						}
						edge.setExpression(new HorizontalExpression(edge1.expr,new HorizontalExpression(edge2.expr,edge.expr,REL_SUB_NORMAL),REL_SUP_NORMAL));
						edge.inEdge.clear();
						edge.rightbound=Math.min(edge1.rightbound,edge2.rightbound);
						edge1.remove();
						edge2.remove();
						edges.remove(edge1);
						edges.remove(edge2);
						changed=true;
						break;
					}
				}
			}
		}
		return changed;
	}
	private final void removeEdge(Edge edge){
		if(edge.special)
			specialEdges.remove(edge);
		edges.remove(edge);
	}
	boolean combineSpecial(){
		for(Edge edge:specialEdges){
			//if(!(edge.expr instanceof SymbolExpression))
			//	continue;
			String name=edge.toString();
			int left=edge.left,right=edge.right,top=edge.top,bottom=edge.bottom,y=edge.expr.getY(),width=edge.expr.getLogicalWidth();
			if(((SymbolExpression)edge.expr).getSymbol().isHat()){
				Edge under=null;
				for(Edge e:edges)
					if((!e.special||e.expr.getLogicalWidth()<width)&&e.left<=right&&left<=e.right&&top<=e.bottom&&e.top<=bottom)
						if(under==null)
							under=e;
						else{
							under=null;
							break;
						}
				if(under!=null){
					if(under.special)
						specialEdges.remove(under);
					under.setExpression(new HatExpression(edge.expr,under.expr));
					edge.remove();
					edges.remove(edge);
					specialEdges.remove(edge);
					push(under);
					pull(under);
					return true;
				}
				continue;
			}
			switch(name){
				case "\\hline":case "\\underbrace{}":case "\\overbrace{}":case "\\overline{}":case "\\underline{}":case "\\overrightarrow{}":case "\\underrightarrow{}":
					Edge over=null,under=null;
					for(Edge e:edges)
						if((!e.special||e.expr.getLogicalWidth()<width)&&e.left<=right&&left<=e.right&&top<=e.bottom&&e.top<=bottom)
							if(e.expr.getPhysicalTop()>=y||e.expr.getPhysicalBottom()<=y)
								if(e.expr.getY()<=y){
									if(over==null)
										over=e;
									else{
										over=null;
										break;
									}
								}else{
									if(under==null)
										under=e;
									else{
										under=null;
										break;
									}
								}
					if(over!=null&&under!=null){
						if(name.equals("\\hline")){
							edge.setExpression(new FractionExpression(edge.expr,over.expr,under.expr));
							over.remove();
							under.remove();
							removeEdge(over);
							removeEdge(under);
							specialEdges.remove(edge);
							push(edge);
							pull(edge);
							return true;
						}else if(name.equals("\\overbrace{}")){
							edge.setExpression(new BraceExpression(edge.expr,under.expr,over.expr));
							over.remove();
							under.remove();
							removeEdge(over);
							removeEdge(under);
							specialEdges.remove(edge);
							push(edge);
							pull(edge);
							return true;
						}else if(name.equals("\\underbrace{}")){
							edge.setExpression(new BraceExpression(edge.expr,over.expr,under.expr));
							over.remove();
							under.remove();
							removeEdge(over);
							removeEdge(under);
							specialEdges.remove(edge);
							push(edge);
							pull(edge);
							return true;
						}
					}else if(over!=null){
						if(name.equals("\\underline{}")){
							edge.setExpression(new HatExpression(edge.expr,over.expr));
							over.remove();
							removeEdge(over);
							specialEdges.remove(edge);
							push(edge);
							pull(edge);
							return true;
						}else if(name.equals("\\underrightarrow{}")){
							edge.setExpression(new HatExpression(over.expr,edge.expr));
							over.remove();
							removeEdge(over);
							specialEdges.remove(edge);
							push(edge);
							pull(edge);
							return true;
						}
					}else if(under!=null){
						if(name.equals("\\overline{}")||name.equals("\\overrightarrow{}")){
							edge.setExpression(new HatExpression(edge.expr,under.expr));
							under.remove();
							removeEdge(under);
							specialEdges.remove(edge);
							push(edge);
							pull(edge);
							return true;
						}
					}
					break;
				case "\\sqrt{}":
					y=edge.expr.getPhysicalLeft();
					Edge ord=null,cont=null;
					for(Edge e:edges)
						if((!e.special||e.expr.getLogicalWidth()<width)&&e.left<=right&&y<=e.right&&top<=e.bottom&&e.top<=bottom)
							if(e.expr.getPhysicalRight()<=left){
								if(ord==null)
									ord=e;
								else{
									ord=null;
									cont=null;
									break;
								}
							}else{
								if(cont==null)
									cont=e;
								else{
									cont=null;
									break;
								}
							}
					if(cont!=null){
						if(ord!=null){
							edge.setExpression(new RadicalExpression(edge.expr,ord.expr,cont.expr));
							ord.remove();
							removeEdge(ord);
						}else{
							edge.setExpression(new RadicalExpression(edge.expr,null,cont.expr));
						}
						cont.remove();
						removeEdge(cont);
						specialEdges.remove(edge);
						push(edge);
						pull(edge);
						return true;
					}
					break;
				default:
					Edge sub=null,sup=null;
					for(Edge e:edges)
						if((!e.special||e.expr.getLogicalWidth()<width)&&e.left<=right&&left<=e.right&&top<=e.expr.getLogicalBottom()&&e.expr.getLogicalTop()<=bottom)
							if(e.expr.getY()<=y){
								if(sup==null)
									sup=e;
								else{
									sup=null;
									sub=null;
									break;
								}
							}else{
								if(sub==null)
									sub=e;
								else{
									sub=null;
									sup=null;
									break;
								}
							}
					if(sub!=null){
						ArrayList<Expression> substack=new ArrayList<Expression>(5);
						while(sub!=null){
							substack.add(sub.expr);
							sub.remove();
							removeEdge(sub);
							left=sub.expr.getPhysicalLeft();
							right=sub.expr.getPhysicalRight();
							top=sub.expr.getLogicalTop();
							bottom=sub.expr.getLogicalBottom();
							sub=null;
							for(Edge e:edges)
								if(e!=edge&&(!e.special||e.expr.getLogicalWidth()<width)
								&&e.expr.getPhysicalLeft()<=right&&left<=e.expr.getPhysicalRight()&&e.expr.getLogicalTop()<=bottom&&top<=e.expr.getLogicalBottom())
									if(sub==null)
										sub=e;
									else{
										sub=null;
										break;
									}
						}
						Expression stack=null;
						if(substack.size()==1)
							stack=substack.get(0);
						else
							stack=new SubstackExpression(substack);
						if(sup!=null){
							edge.setExpression(new HorizontalExpression(new HorizontalExpression(edge.expr,sup.expr,REL_NORMAL_SUP),stack,REL_NORMAL_SUB));
							sup.remove();
							removeEdge(sup);
						}else{
							edge.setExpression(new HorizontalExpression(edge.expr,stack,REL_NORMAL_SUB));
						}
						specialEdges.remove(edge);
						push(edge);
						pull(edge);
						return true;
					}
			}
		}
		return false;
	}
	boolean combineSpace(){
		boolean changed=false;
		ListIterator<Edge> iter=edges.listIterator(edges.size());
		while(iter.hasPrevious()){
			Edge edge=iter.previous();
			if(edge.inEdge.size()==1&&edge.inEdge.first().outEdge.size()==1){
				Edge edgel=edge.inEdge.first();
				if(!displayed&&edge.expr.isText()!=edgel.expr.isText())
					continue;
				edgel.setExpression(new HorizontalExpression(edgel.expr,edge.expr,REL_LEFT_RIGHT));
				edgel.outEdge=edge.outEdge;
				for(Edge e:edge.outEdge){
					e.inEdge.remove(edge);
					e.inEdge.add(edgel);
				}
				edgel.rightbound=edge.rightbound;
				iter.remove();
				changed=true;
			}
		}
		return changed;
	}
	boolean resolveLoop(){
		boolean changed=false;
		Partition part=new Partition(null,edges.size());
		HashMap<Edge,Integer> map=new HashMap<Edge,Integer>();
		TreeSet<WeightedPair<Edge>> links=new TreeSet<WeightedPair<Edge>>();
		int i=0;
		for(Edge edge:edges){
			map.put(edge,i++);
			for(Edge e:edge.outEdge)
				links.add(new WeightedPair<Edge>(edge,e,e.left-edge.right));
		}
		for(WeightedPair<Edge> pair:links){
			try{
				int carind=map.get(pair.car),cdrind=map.get(pair.cdr);
				if(part.findRoot(carind)==part.findRoot(cdrind)){
					Edge.unlink(pair.car,pair.cdr);
					changed=true;
				}else{
					part.union(carind,cdrind);
				}
			}catch(Exception ex){
				//ex.printStackTrace(System.out);
				//System.out.println(pair==null);
				//for(WeightedPair<Edge> p:links)
				//	System.out.println(p.car+"\t"+p.cdr);
				/*System.out.println();
				for(Edge edge:edges)
					System.out.println(edge.expr+"\t"+edge.inEdge+"\t"+edge.outEdge);
				System.out.println();
				for(Edge edge:specialEdges)
					System.out.println(edge.expr+"\t"+edge.inEdge+"\t"+edge.outEdge);
				System.out.println();*/
			}
		}
		return changed;
	}
	private static final boolean isMatrixStart(String expr){
		return expr.endsWith("(")||expr.endsWith("[")||expr.endsWith("\\{")||expr.endsWith("|")||expr.endsWith("\\lfloor")
		||expr.endsWith("\\lceil")||expr.endsWith("\\langle");
	}
	boolean combineMatrix(){
		ListIterator<Edge> iter=edges.listIterator();
		outside:while(iter.hasNext()){
			Edge edge=iter.next();
			int m=edge.outEdge.size();
			if(m>=2&&isMatrixStart(edge.expr.toString())){
				ArrayList<Expression>[] matrix=new ArrayList[m];
				ArrayList<Edge> tobedel=new ArrayList<Edge>(edge.outEdge);
				int[] tp=new int[m+1],bt=new int[m];
				int i=0,n=0;
				int top=edge.top,bottom=edge.bottom,right=edge.left;
				for(Edge e:edge.outEdge){
					matrix[i]=new ArrayList<Expression>();
					matrix[i++].add(e.expr);
					right=Math.max(right,e.right);
				}
				for(i=0;i<m;i++){
					for(int k=i+1;k<m;k++)
						if(matrix[k].get(0).getUpper()<matrix[i].get(0).getUpper()){
							ArrayList<Expression> tmp=matrix[k];
							matrix[k]=matrix[i];
							matrix[i]=tmp;
						}
					tp[i]=matrix[i].get(0).getUpper();
					bt[i]=matrix[i].get(0).getLower();
					if(i!=0&&tp[i]<=bt[i-1])
						continue outside;
				}
				tp[m]=Integer.MAX_VALUE;
				ListIterator<Edge> it=edges.listIterator(iter.nextIndex());
				out:while(it.hasNext()){
					Edge e=it.next();
					if(e.top<=bottom&&top<=e.bottom){
						for(int j=0;j<m;j++){
							if(e.expr==matrix[j].get(0))
								continue;
							if(e.top<=bt[j]&&tp[j]<=e.bottom){
								if(e.getBottom()>=tp[j+1])
									break out;
								if(e.left>right){
									++n;
									for(int k=0;k<m;k++)
										matrix[k].add(null);
								}
								tobedel.add(e);
								if(matrix[j].get(n)==null)
									matrix[j].set(n,e.expr);
								else
									matrix[j].set(n,new HorizontalExpression(matrix[j].get(n),e.expr,REL_LEFT_RIGHT));
								tp[j]=Math.min(tp[j],e.expr.getUpper());
								bt[j]=Math.max(bt[j],e.expr.getLower());
								right=Math.max(right,e.expr.getLogicalRight());
								break;
							}
						}
					}
				}
				edge.setExpression(new HorizontalExpression(edge.expr,new MatrixExpression(matrix),REL_LEFT_RIGHT));
				edge.outEdge.clear();
				edge.rightbound=Integer.MAX_VALUE;
				for(Edge e:tobedel){
					e.remove();
					removeEdge(e);
				}
				push(edge);
				return true;
			}
		}
		return false;
	}
	public void paintOn(java.awt.Graphics2D g2d){
		for(Edge edge:edges)
			for(Edge e:edge.getOutEdge()){
				g2d.drawLine(edge.right,edge.expr.getY(),e.left,e.expr.getY());
			}
	}
	/**
	 * Rewrite the graph using only one rule
	 * @return if any changed is made
	 */
	public boolean stepwise(){
		//for(Edge e:edges)
		//	System.out.println(e.expr.toString()+":"+e.inEdge+":"+e.outEdge);
		//System.out.println();
		return edges.size()>1&&(combineLine(true)||combineLine(false)||combineRightScript()||combineLeftScript()||combineSpecial()
		||combineMatrix()||combineSpace()||resolveLoop());
	}
	/*
	 * Rewrite the graph util no more change can be made
	 */
	public void rewrite(){
		while(stepwise());
		if(edges.size()>1){
			for(Edge edge:edges){
				edge.inEdge.clear();
				edge.outEdge.clear();
				edge.rightbound=Integer.MAX_VALUE;
			}
			for(Edge edge:edges){
				if(!edge.special)
					push(edge);
			}
			while(stepwise());
		}
		/*int siz=edges.size();
		while(siz>1){
			boolean changed=false;
			for(Edge edge:edges)
				if(!edge.special)
					changed|=pull(edge);
			if(changed){
				while(stepwise());
				if(edges.size()<siz)
					siz=edges.size();
				else{
					changed=false;
					for(Edge edge:edges)
						if(!edge.special)
							changed|=pull(edge);
					if(changed){
						while(stepwise());
						if(edges.size()<siz)
							siz=edges.size();
						else
							break;
					}else
						break;
				}
			}else
				break;
		}*/
		output();
	}
	/**
	 * Put back the expressions
	 */
	public void output(){
		expressions.clear();
		for(Edge e:edges)
			expressions.add(e.expr);
	}
}
class WeightedPair<T> implements Comparable<WeightedPair<T>>{
	T car,cdr;
	int weight;
	WeightedPair(T car,T cdr,int weight){
		this.car=car;
		this.cdr=cdr;
		this.weight=weight;
	}
	public int compareTo(WeightedPair<T> obj){
		return weight-obj.weight;
	}
}
class Interval implements Comparable<Interval>{
	int start,end;
	Interval(int start,int end){
		this.start=start;
		this.end=end;
	}
	public int compareTo(Interval in){
		return end!=in.end?end-in.end:start-in.start;
	}
}
/**
 * A data structure representing a node in a symbol adjoint graph
 */
class Edge implements Comparable<Edge>{
	Expression expr;
	boolean special;
	int left,right,top,bottom,rightbound=Integer.MAX_VALUE;
	TreeSet<Edge> inEdge=new TreeSet<Edge>(),outEdge=new TreeSet<Edge>();
	Edge(Expression expr,int left,int right,int top,int bottom,boolean special){
		this.expr=expr;
		this.left=left;
		this.right=right;
		this.top=top;
		this.bottom=bottom;
		this.special=special;
	}
	Expression getExpression(){
		return expr;
	}
	void setExpression(Expression expr){
		this.expr=expr;
		left=expr.getLogicalLeft();
		right=expr.getLogicalRight();
		top=expr.getUpper();
		bottom=expr.getLower();
		special=false;
	}
	int getLeft(){
		return left;
	}
	int getRight(){
		return right;
	}
	int getTop(){
		return top;
	}
	int getBottom(){
		return bottom;
	}
	int getInDegree(){
		return inEdge.size();
	}
	int getOutDegree(){
		return outEdge.size();
	}
	TreeSet<Edge> getInEdge(){
		return inEdge;
	}
	TreeSet<Edge> getOutEdge(){
		return outEdge;
	}
	static void link(Edge edge1,Edge edge2){
		edge1.outEdge.add(edge2);
		edge2.inEdge.add(edge1);
	}
	static void unlink(Edge edge1,Edge edge2){
		edge1.getOutEdge().remove(edge2);
		edge2.getInEdge().remove(edge1);
	}
	void remove(){
		for(Edge edge:inEdge)
			edge.outEdge.remove(this);
		for(Edge edge:outEdge)
			edge.inEdge.remove(this);
	}
	public String toString(){
		return expr.toString();
	}
	public int hashCode(){
		return expr.hashCode();
	}
	public int compareTo(Edge e){
		if(left!=e.left)
			return left-e.left;
		else if(top!=e.top)
			return top-e.top;
		else
			return expr.toString().compareTo(e.expr.toString());
	}
}