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
package com.github.chungkwong.mathocr.common;
import java.util.*;
/**
 * List that only keep limited number of elements in ascending order
 *
 * @author Chan Chung Kwong
 * @param <T> type of elements
 */
public class LimitedSortedList<T>{
	private final int k;
	private final List<T> base;
	private final Comparator<T> comparator;
	/**
	 * Create a LimitedSortedList
	 *
	 * @param limit number of elements to be kept
	 * @param comparator being used to compare elements
	 */
	public LimitedSortedList(int limit,Comparator<T> comparator){
		this.k=limit;
		this.base=new ArrayList<>(limit);
		this.comparator=comparator;
	}
	/**
	 * Add a element to the list
	 *
	 * @param e the element
	 */
	public void add(T e){
		int index=Collections.binarySearch(base,e,comparator);
		if(index<0){
			index=-(index+1);
		}
		if(index<k){
			if(base.size()>=k){
				base.remove(base.size()-1);
			}
			base.add(index,e);
		}
	}
	/**
	 * @return the elements of the list
	 */
	public List<T> getElements(){
		return base;
	}
}
