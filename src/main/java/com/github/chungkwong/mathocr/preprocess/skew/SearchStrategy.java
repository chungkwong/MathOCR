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
package com.github.chungkwong.mathocr.preprocess.skew;
import java.util.function.*;
/**
 * Search strategy for search based skew detectors
 *
 * @author Chan Chung Kwong
 */
public interface SearchStrategy{
	/**
	 * Search for skew angle
	 *
	 * @param cost the cost function, lower is better
	 * @return the angle in radius
	 */
	double search(DoubleFunction<Double> cost);
}
