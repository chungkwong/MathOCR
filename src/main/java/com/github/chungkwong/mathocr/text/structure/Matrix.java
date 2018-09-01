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
package com.github.chungkwong.mathocr.text.structure;
import com.github.chungkwong.mathocr.common.BoundBox;
import java.util.*;
/**
 * Matrix
 *
 * @author Chan Chung Kwong
 */
public class Matrix extends Span{
	private final List<List<Span>> matrix;
	/**
	 * Create a matrix
	 *
	 * @param matrix list of rows(list of cell)
	 */
	public Matrix(List<List<Span>> matrix){
		super(BoundBox.union(matrix.stream().flatMap((line)->line.stream()).map((line)->line.getBox()).toArray(BoundBox[]::new)),
				calculateBaseline(matrix));
		this.matrix=matrix;
	}
	private static int calculateBaseline(List<List<Span>> matrix){
		int top=matrix.get(0).stream().mapToInt((span)->span.getBox().getTop()).min().orElse(0);
		int bottom=matrix.get(matrix.size()-1).stream().mapToInt((span)->span.getBox().getBottom()).max().orElse(0);
		return (top+bottom)/2;
	}
	/**
	 * Get the cells
	 *
	 * @return list of rows(list of cell)
	 */
	public List<List<Span>> getMatrix(){
		return matrix;
	}
	/**
	 * @return number of rows
	 */
	public int getRowCount(){
		return matrix.size();
	}
	/**
	 * @return number of columns
	 */
	public int getColumnCount(){
		return matrix.stream().mapToInt((row)->row.size()).max().getAsInt();
	}
	@Override
	public String toString(){
		StringBuilder buf=new StringBuilder();
		buf.append('\n');
		matrix.forEach((row)->{
			row.forEach((cell)->buf.append(cell.toString()).append('\t'));
			buf.append('\n');
		});
		return buf.toString();
	}
	@Override
	public boolean isBaseLineReliable(){
		return false;
	}
	@Override
	public int getFontSize(){
		return (int)matrix.stream().flatMap((line)->line.stream()).
				mapToInt((cell)->cell.getFontSize()).average().orElse(Symbol.DEFAULT_SIZE);
	}
}
