/* Partition.java
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
package net.sf.mathocr.common;
import java.util.*;
/**
 * A data structure representing partition of objects
 */
public final class Partition{
	ArrayList<Integer> parent;
	ArrayList<Integer> rank;
	Linkable work;
	/**
	 * Construct a Partition without any set
	 * @param work indicating addition work when linking two sets
	 */
	public Partition(Linkable work){
		parent=new ArrayList<Integer>();
		rank=new ArrayList<Integer>();
		this.work=work;
	}
	/**
	 * Construct a Partition with some sets
	 * @param work indicating addition work when linking two sets
	 * @param n number of set containing exactly one element at frist
	 */
	public Partition(Linkable work,int n){
		parent=new ArrayList<Integer>(n);
		rank=new ArrayList<Integer>(n);
		for(int i=0;i<n;i++){
			parent.add(null);
			rank.add(0);
		}
		this.work=work;
	}
	/**
	 * Make a new set containing exactly one element
	 */
	public void makeSet(){
		parent.add(null);
		rank.add(0);
	}
	/**
	 * Find the root of the set containing a element
	 * @param n the index of the element
	 */
	public int findRoot(int n){
		Stack<Integer> stack=new Stack<Integer>();
		while(parent.get(n)!=null){
			stack.push(n);
			n=parent.get(n);
		}
		while(!stack.empty())
			parent.set(stack.pop(),n);
		return n;
	}
	/**
	 * Link two sets
	 * @param m root of the frist set
	 * @param n root of the second set
	 */
	private void link(int m,int n){
		if(m==n)
			return;
		int rankm=rank.get(m),rankn=rank.get(n);
		if(rankm>rankn){
			parent.set(n,m);
			if(work!=null)
				work.link(n,m);
		}else{
			parent.set(m,n);
			if(work!=null)
				work.link(m,n);
			if(rankm==rankn)
				rank.set(n,rankn+1);
		}
	}
	/**
	 * Combine two sets
	 * @param m an element contained in the first set
	 * @param n an element contained in the second set
	 */
	public void union(int m,int n){
		link(findRoot(m),findRoot(n));
	}
	/**
	 * Check if a element is root
	 * @param n the index the element
	 */
	public boolean isRoot(int n){
		return parent.get(n)==null;
	}
}