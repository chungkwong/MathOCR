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
import com.github.chungkwong.mathocr.text.structure.Line;
import com.github.chungkwong.mathocr.character.CharacterCandidate;
import java.util.*;
/**
 * Line analyzer
 *
 * @author Chan Chung Kwong
 */
public interface LineAnalyzer{
	/**
	 * Resolve layout and identity of the line
	 *
	 * @param characters candidates of character segment
	 * @return the line
	 */
	Line analysis(List<List<NavigableSet<CharacterCandidate>>> characters);
}
