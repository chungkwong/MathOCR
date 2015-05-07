/* HorizontalExpression.java
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
/**
 * A data structure representing horizontal aligning expressions
 */
public final class HorizontalExpression extends Expression{
	public static final int REL_LEFT_RIGHT=0,REL_NORMAL_SUP=1,REL_NORMAL_SUB=2,REL_SUP_NORMAL=3,REL_SUB_NORMAL=4;
	/**
	 * Construct a HorizontalExpression
	 * @param l left part
	 * @param r right part
	 * @param type REL_LEFT_RIGHT,REL_NORMAL_SUP,REL_NORMAL_SUB,REL_SUP_NORMAL or REL_SUB_NORMAL
	 */
	public HorizontalExpression(Expression l,Expression r,int type){
		pleft=Math.min(l.getPhysicalLeft(),r.getPhysicalLeft());
		pright=Math.max(l.getPhysicalRight(),r.getPhysicalRight());
		ptop=Math.min(l.getPhysicalTop(),r.getPhysicalTop());
		pbottom=Math.max(l.getPhysicalBottom(),r.getPhysicalBottom());
		lleft=Math.min(l.getLogicalLeft(),r.getLogicalLeft());
		lright=Math.max(l.getLogicalRight(),r.getLogicalRight());
		ltop=Math.min(l.getLogicalTop(),r.getLogicalTop());
		lbottom=Math.max(l.getLogicalBottom(),r.getLogicalBottom());
		switch(type){
			case REL_LEFT_RIGHT:
				x=l.getX();
				y=l.getY();
				if(l.isBaseLineFixed()){
					sub=l.getSubscriptBound();
					sup=l.getSuperscriptBound();
					baseLineFixed=true;
					scale=l.getScale();
					notcustomized=l.isNotCustomized();
					/*if(r.isBaseLineFixed()){
						int w=l.getLogicalWidth()+r.getLogicalWidth();
						y=((y*l.getLogicalWidth()+r.getY()*r.getLogicalWidth())*2+w)/(2*w);
					}*/
				}else if(r.isBaseLineFixed()){
					sub=r.getSubscriptBound();
					sup=r.getSuperscriptBound();
					baseLineFixed=true;
					scale=r.getScale();
					y=r.getY();
					notcustomized=r.isNotCustomized();
				}else{
					sub=r.getSubscriptBound();
					sup=r.getSuperscriptBound();
					baseLineFixed=false;
					if(l.getScale()>r.getScale()){
						scale=l.getScale();
						notcustomized=l.isNotCustomized();
					}else{
						scale=r.getScale();
						y=r.getY();
						notcustomized=r.isNotCustomized();
					}
				}
				LaTeXString=l.toString()+getSpaceName(l,r)+r.toString();
				if(l.isText()&&r.isText())
					textString=l.getText()+getSpaceName(l,r).replace("\\ "," ")+r.getText();
				break;
			case REL_NORMAL_SUP:
				x=l.getX();
				y=l.getY();
				sub=l.getSubscriptBound();
				sup=l.getSuperscriptBound();
				baseLineFixed=l.isBaseLineFixed();
				scale=l.getScale();
				notcustomized=l.isNotCustomized();
				LaTeXString=l.toString()+"^{"+r.toString()+"}";
				break;
			case REL_NORMAL_SUB:
				x=l.getX();
				y=l.getY();
				sub=l.getSubscriptBound();
				sup=l.getSuperscriptBound();
				baseLineFixed=l.isBaseLineFixed();
				scale=l.getScale();
				notcustomized=l.isNotCustomized();
				LaTeXString=l.toString()+"_{"+r.toString()+"}";
				break;
			case REL_SUP_NORMAL:
				x=r.getX();
				y=r.getY();
				sub=r.getSubscriptBound();
				sup=r.getSuperscriptBound();
				baseLineFixed=r.isBaseLineFixed();
				scale=r.getScale();
				notcustomized=r.isNotCustomized();
				LaTeXString="^{"+l.toString()+"}"+r.toString();
				break;
			case REL_SUB_NORMAL:
				x=r.getX();
				y=r.getY();
				sub=r.getSubscriptBound();
				sup=r.getSuperscriptBound();
				baseLineFixed=r.isBaseLineFixed();
				scale=r.getScale();
				notcustomized=r.isNotCustomized();
				LaTeXString="_{"+l.toString()+"}"+r.toString();
				break;
		}
	}
	static String getSpaceName(Expression l,Expression r){
		int d=r.getLogicalLeft()-l.getLogicalRight(),s=(int)(Math.max(l.getScale(),r.getScale())*30);
		if(!r.isNotCustomized())
			s=(int)(l.getScale()*30);
		else if(!l.isNotCustomized())
			s=(int)(r.getScale()*30);
		String rs=r.toString();
		if(d>=s*5/4)
			return "\\quad ";
		else if(d>=s*3/10&&(rs.startsWith("\\math")||rs.startsWith("\\text")))
			return "\\ ";
		return "";
	}
}