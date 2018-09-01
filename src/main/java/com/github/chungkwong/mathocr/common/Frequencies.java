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
import java.util.stream.*;
/**
 *
 * @author Chan Chung Kwong
 */
public class Frequencies<T>{
	private final Map<T,Counter> frequency;
	/**
	 * Create a frequencies table backed by TreeMap
	 */
	public Frequencies(){
		frequency=new TreeMap<>();
	}
	/**
	 * Create a frequencies table
	 *
	 * @param useHashMap if true, the table is backed by HashMap
	 */
	public Frequencies(boolean useHashMap){
		frequency=useHashMap?new HashMap<>():new TreeMap<>();
	}
	/**
	 * Create a frequencies table
	 *
	 * @param frequency the source
	 */
	public Frequencies(Map<T,Counter> frequency){
		this.frequency=frequency;
	}
	/**
	 * Create a frequencies table
	 *
	 * @param tokens the objects to be recorded
	 */
	public Frequencies(Stream<T> tokens){
		this.frequency=tokens.collect(Collectors.groupingBy((e)->e,
				Collectors.collectingAndThen(Collectors.counting(),(c)->new Counter(c))));
	}
	/**
	 * Increase the frequency of a given object by one
	 *
	 * @param token the given object
	 */
	public void advanceFrequency(T token){
		Counter counter=frequency.get(token);
		if(counter==null){
			counter=new Counter(1);
			frequency.put(token,counter);
		}else{
			counter.advance();
		}
	}
	/**
	 * Increase the frequency of a given object by a given value
	 *
	 * @param token the given object
	 * @param times the given value
	 */
	public void advanceFrequency(T token,long times){
		Counter counter=frequency.get(token);
		if(counter==null){
			counter=new Counter(times);
			frequency.put(token,counter);
		}else{
			counter.advance(times);
		}
	}
	/**
	 * Merge frequencies into this table
	 *
	 * @param toMerge the source
	 */
	public void merge(Frequencies<T> toMerge){
		toMerge.frequency.forEach((k,v)->advanceFrequency(k,v.getCount()));
	}
	/**
	 * Set the frequency of a object to zero
	 *
	 * @param token the object
	 */
	public void reset(T token){
		frequency.remove(token);
	}
	/**
	 * Get the frequency of a object
	 *
	 * @param token the object
	 * @return the frequency
	 */
	public long getFrequency(T token){
		Counter counter=frequency.get(token);
		return counter==null?0:counter.getCount();
	}
	/**
	 * @return the number of unique objects found
	 */
	public int getTokenCount(){
		return frequency.size();
	}
	/**
	 * Map representation of the table
	 *
	 * @return the map
	 */
	public Map<T,Counter> toMap(){
		return frequency;
	}
	@Override
	public boolean equals(Object obj){
		return obj instanceof Frequencies&&Objects.equals(frequency,((Frequencies)obj).frequency);
	}
	@Override
	public int hashCode(){
		int hash=7;
		hash=31*hash+Objects.hashCode(this.frequency);
		return hash;
	}
	@Override
	public String toString(){
		return frequency.toString();
	}
}
