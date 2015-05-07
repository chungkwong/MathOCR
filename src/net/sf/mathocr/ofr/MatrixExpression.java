/* MatrixExpression.java
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
public final class MatrixExpression extends Expression{
	public MatrixExpression(ArrayList<Expression>[] expr){
		pleft=ptop=lleft=ltop=sup=Integer.MAX_VALUE;
		pright=pbottom=lright=lbottom=sub=0;
		int m=expr.length,n=expr[0].size();
		StringBuilder str=new StringBuilder();
		str.append("\\begin{array}{");
		for(int j=0;j<n;j++){
			int cl=Integer.MAX_VALUE,cr=0,sl=0,sr=0,count=0;
			for(int i=0;i<m;i++){
				Expression cell=expr[i].get(j);
				if(cell!=null){
					cl=Math.min(cl,cell.getLogicalLeft());
					sl+=cell.getLogicalLeft();
					cr=Math.max(cr,cell.getLogicalRight());
					sr+=cell.getLogicalRight();
					if(cell.isNotCustomized()){
						notcustomized=true;
						scale=Math.max(scale,cell.getScale());
					}
					if(j==0){
						pleft=Math.min(pleft,cell.getPhysicalLeft());
						lleft=Math.min(lleft,cell.getLogicalLeft());
					}
					if(i==0){
						ptop=Math.min(ptop,cell.getPhysicalTop());
						ltop=Math.min(ltop,cell.getLogicalTop());
						sup=Math.min(sup,cell.getSuperscriptBound());
					}
					if(j==n-1){
						pright=Math.max(pright,cell.getPhysicalRight());
						lright=Math.max(lright,cell.getLogicalRight());
					}
					if(i==m-1){
						pbottom=Math.max(pbottom,cell.getPhysicalBottom());
						lbottom=Math.max(lbottom,cell.getLogicalBottom());
						sub=Math.max(sub,cell.getSubscriptBound());
					}
					++count;
				}
			}
			sl/=count;
			sr/=count;
			int diff=(cr-sr)-(sl-cl);
			if(Math.abs(diff)<=12*scale)
				str.append('c');
			else if(diff>0)
				str.append('l');
			else
				str.append('r');
		}
		str.append("}\n");
		for(int i=0;i<m;i++){
			if(expr[i].get(0)!=null)
				str.append(expr[i].get(0).toString());
			for(int j=1;j<n;j++){
				str.append(" & ");
				if(expr[i].get(j)!=null)
					str.append(expr[i].get(j).toString());
			}
			str.append("\\\\\n");
		}
		str.append("\\end{array}\n");
		x=lleft;
		y=(ltop+lbottom)/2;
		baseLineFixed=false;
		LaTeXString=str.toString();
	}
}