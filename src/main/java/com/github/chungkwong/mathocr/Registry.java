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
package com.github.chungkwong.mathocr;
import static com.github.chungkwong.mathocr.Environment.ENVIRONMENT;
import java.util.*;
/**
 * Registry
 *
 * @author Chan Chung Kwong
 * @param <T> type of object to be registered
 */
public class Registry<T>{
	private final Map<String,T> DETECTORS=new HashMap<>();
	private final String defVariable;
	private final T fallback;
	/**
	 * Create a registry
	 *
	 * @param defVariable the String variable the indicated default name
	 * @param fallback the object used if name is not found
	 */
	public Registry(String defVariable,T fallback){
		this.defVariable=defVariable;
		this.fallback=fallback;
	}
	/**
	 * Register a object
	 *
	 * @param name the name
	 * @param detector the value
	 */
	public void register(String name,T detector){
		DETECTORS.put(name,detector);
	}
	/**
	 * Get a object with default name
	 *
	 * @return the value
	 */
	public T get(){
		return DETECTORS.getOrDefault(ENVIRONMENT.getString(defVariable),fallback);
	}
	/**
	 * Get a registed object
	 *
	 * @param name the registed name
	 * @return the object
	 */
	public T get(String name){
		return DETECTORS.getOrDefault(name,fallback);
	}
	/**
	 * Get registed names
	 *
	 * @return names
	 */
	public Set<String> names(){
		return DETECTORS.keySet();
	}
}
