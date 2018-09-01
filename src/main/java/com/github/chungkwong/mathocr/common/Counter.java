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
/**
 *
 * @author Chan Chung Kwong
 */
public class Counter{
	private long count;
	/**
	 * Create a counter with initial value 0
	 */
	public Counter(){
	}
	/**
	 * Create a counter
	 *
	 * @param count initial value
	 */
	public Counter(long count){
		this.count=count;
	}
	/**
	 * @return current value of the counter
	 */
	public long getCount(){
		return count;
	}
	/**
	 * Advance the current value by one
	 */
	public void advance(){
		++count;
	}
	/**
	 * Add given value to the current value of the counter
	 *
	 * @param times to be added
	 */
	public void advance(long times){
		count+=times;
	}
	@Override
	public boolean equals(Object obj){
		return obj instanceof Counter&&((Counter)obj).count==count;
	}
	@Override
	public int hashCode(){
		int hash=5;
		hash=97*hash+Long.hashCode(count);
		return hash;
	}
	@Override
	public String toString(){
		return Long.toString(count);
	}
}
