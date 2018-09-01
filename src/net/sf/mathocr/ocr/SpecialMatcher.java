/* SpecialMatcher.java
 * =========================================================================
 * This file is originally part of the MathOCR Project
 *
 * Copyright (C) 2014,2015 Chan Chung Kwong
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
package net.sf.mathocr.ocr;
import net.sf.mathocr.common.*;
import net.sf.mathocr.ocr.*;
import java.util.*;
/**
 * Matcher used to match special symbol including root sign,arrow and delimiters
 */
public final class SpecialMatcher{
	static PixelsMatcher matcher=PixelsMatcher.DEFAULT_MATCHER;
	public static HashMap<String,Glyph> map=new HashMap<String,Glyph>();
	/**
	 * Determine if SpecialMatcher is really
	 */
	public static boolean usable=false;
	private static int bracethick,arrowthick,rootthick,w1,w2,w3,w4;
	public SpecialMatcher(){
	}
	/**
	 * Check if necessary information is gained
	 * @return result
	 */
	public static boolean checkUsable(){
		String[] keys=new String[]{"\\parenlefttp","\\parenleftbt","\\parenleftex","\\parenrighttp","\\parenrightbt","\\parenrightex",
		"\\bracketlefttp","\\bracketleftbt","\\bracketleftex","\\bracketrighttp","\\bracketrightbt","\\bracketrightex",
		"\\bracelefttp","\\braceleftmid","\\braceleftbt","\\bracerighttp","\\bracerightmid","\\bracerightbt","\\braceex",
		"\\bracedownleft","\\bracedownright","\\braceupleft","\\braceupright","\\rightarrow ","\\leftarrow ",
		"\\surd1","\\surd2","\\surd3","\\surd4","\\radicalbt","\\radicalvertex","\\radicaltp","\\int ","\\oint ","-","|","\\dot{}"};
		Set<String> avil=map.keySet();
		for(String str:keys)
			if(!avil.contains(str)){
				System.out.println("Missing: "+str);
				usable=false;
				return false;
			}
		ConnectedComponent ele=map.get("\\radicaltp").getConnectedComponent();
		int w=ele.getWidth()/2;
		rootthick=0;
		for(RunLength rl:ele.getRunLengths())
			if(rl.getCount()>w)
				++rootthick;
		ele=map.get("\\rightarrow ").getConnectedComponent();
		w=ele.getWidth()/2;
		arrowthick=0;
		for(RunLength rl:ele.getRunLengths())
			if(rl.getCount()>w)
				++arrowthick;
		bracethick=map.get("\\braceex").getWidth();
		w1=map.get("\\surd1").getConnectedComponent().getWidth();
		w2=map.get("\\surd2").getConnectedComponent().getWidth();
		w3=map.get("\\surd3").getConnectedComponent().getWidth();
		w4=map.get("\\surd4").getConnectedComponent().getWidth();
		usable=true;
		return true;
	}
	private static final Candidate makeCandidate(ConnectedComponent ele,ConnectedComponent proto,String name){
		Symbol sym=new Symbol("",name+"\tVC",proto.getWidth(),proto.getHeight(),proto.getWidth(),proto.getHeight(),0,0,0);
		Glyph glyph=new Glyph(sym,"",0,0,0,null,0,null,null);
		sym.addGlyph(glyph);
		return new Candidate(sym,1-matcher.getDistance(ele,proto));
	}
	/**
	 * Check if ele is a left paren
	 * @param ele to be matched
	 * @return 1.0 if impossible or the Hausdoff distance between ele and a left paren
	 */
	public static Candidate isLeftParen(ConnectedComponent ele){
		ConnectedComponent parenlefttp=map.get("\\parenlefttp").getConnectedComponent(),parenleftbt=map.get("\\parenleftbt").getConnectedComponent()
		,parenleftex=map.get("\\parenleftex").getConnectedComponent();
		int w=Math.max(parenlefttp.getWidth(),parenleftbt.getWidth()),h=ele.getHeight()*w/ele.getWidth(),mw=parenleftex.getWidth()-1;
		if(h<parenlefttp.getHeight()+parenleftbt.getHeight())
			return null;
		ConnectedComponent e=new ConnectedComponent(0,w-1,0,h-1);
		List<RunLength> runlengths=e.getRunLengths();
		runlengths.addAll(parenlefttp.getRunLengths());
		int i0=h-parenleftbt.getHeight();
		for(int i=parenlefttp.getHeight();i<i0;i++)
			runlengths.add(new RunLength(i,0,mw));
		for(RunLength rl:parenleftbt.getRunLengths())
			runlengths.add(new RunLength(rl.getY()+i0,rl.getX(),rl.getCount()));
		//print(e);
		return makeCandidate(ele,e,"(");
	}
	/**
	 * Check if ele is a right paren
	 * @param ele to be matched
	 * @return 1.0 if impossible or the Hausdoff distance between ele and a right paren
	 */
	public static Candidate isRightParen(ConnectedComponent ele){
		ConnectedComponent parenrighttp=map.get("\\parenrighttp").getConnectedComponent(),parenrightbt=map.get("\\parenrightbt").getConnectedComponent()
		,parenrightex=map.get("\\parenrightex").getConnectedComponent();
		int w=Math.max(parenrighttp.getWidth(),parenrightbt.getWidth()),h=ele.getHeight()*w/ele.getWidth(),mw=parenrightex.getWidth()-1,j=w-mw-1;
		if(h<parenrighttp.getHeight()+parenrightbt.getHeight())
			return null;
		ConnectedComponent e=new ConnectedComponent(0,w-1,0,h-1);
		List<RunLength> runlengths=e.getRunLengths();
		runlengths.addAll(parenrighttp.getRunLengths());
		int i0=h-parenrightbt.getHeight();
		for(int i=parenrighttp.getHeight();i<i0;i++)
			runlengths.add(new RunLength(i,j,mw));
		for(RunLength rl:parenrightbt.getRunLengths())
			runlengths.add(new RunLength(rl.getY()+i0,rl.getX(),rl.getCount()));
		//print(e);
		return makeCandidate(ele,e,")");
	}
	/**
	 * Check if ele is a left bracket
	 * @param ele to be matched
	 * @return 1.0 if impossible or the Hausdoff distance between ele and a left bracket
	 */
	public static Candidate isLeftBracket(ConnectedComponent ele){
		ConnectedComponent bracketlefttp=map.get("\\bracketlefttp").getConnectedComponent(),bracketleftbt=map.get("\\bracketleftbt").getConnectedComponent()
		,bracketleftex=map.get("\\bracketleftex").getConnectedComponent();
		int w=Math.max(bracketlefttp.getWidth(),bracketleftbt.getWidth()),h=ele.getHeight()*w/ele.getWidth(),mw=bracketleftex.getWidth()-1;
		if(h<bracketlefttp.getHeight()+bracketleftbt.getHeight())
			return null;
		ConnectedComponent e=new ConnectedComponent(0,w-1,0,h-1);
		List<RunLength> runlengths=e.getRunLengths();
		runlengths.addAll(bracketlefttp.getRunLengths());
		int i0=h-bracketleftbt.getHeight();
		for(int i=bracketlefttp.getHeight();i<i0;i++)
			runlengths.add(new RunLength(i,0,mw));
		for(RunLength rl:bracketleftbt.getRunLengths())
			runlengths.add(new RunLength(rl.getY()+i0,rl.getX(),rl.getCount()));
		//print(e);
		return makeCandidate(ele,e,"[");
	}
	/**
	 * Check if ele is a right bracket
	 * @param ele to be matched
	 * @return 1.0 if impossible or the Hausdoff distance between ele and a right bracket
	 */
	public static Candidate isRightBracket(ConnectedComponent ele){
		ConnectedComponent bracketrighttp=map.get("\\bracketrighttp").getConnectedComponent(),bracketrightbt=map.get("\\bracketrightbt").getConnectedComponent()
		,bracketrightex=map.get("\\bracketrightex").getConnectedComponent();
		int w=Math.max(bracketrighttp.getWidth(),bracketrightbt.getWidth()),h=ele.getHeight()*w/ele.getWidth(),mw=bracketrightex.getWidth()-1,j=w-mw-1;
		if(h<bracketrighttp.getHeight()+bracketrightbt.getHeight())
			return null;
		ConnectedComponent e=new ConnectedComponent(0,w-1,0,h-1);
		List<RunLength> runlengths=e.getRunLengths();
		runlengths.addAll(bracketrighttp.getRunLengths());
		int i0=h-bracketrightbt.getHeight();
		for(int i=bracketrighttp.getHeight();i<i0;i++)
			runlengths.add(new RunLength(i,j,mw));
		for(RunLength rl:bracketrightbt.getRunLengths())
			runlengths.add(new RunLength(rl.getY()+i0,rl.getX(),rl.getCount()));
		//print(e);
		return makeCandidate(ele,e,"]");
	}
	/**
	 * Check if ele is a left brace
	 * @param ele to be matched
	 * @return 1.0 if impossible or the Hausdoff distance between ele and a left brace
	 */
	public static Candidate isLeftBrace(ConnectedComponent ele){
		ConnectedComponent bracelefttp=map.get("\\bracelefttp").getConnectedComponent(),braceleftbt=map.get("\\braceleftbt").getConnectedComponent(),
		braceleftmid=map.get("\\braceleftmid").getConnectedComponent(),braceleftex=map.get("\\braceex").getConnectedComponent();
		int w=bracelefttp.getWidth()+braceleftmid.getWidth()-braceleftex.getWidth(),h=ele.getHeight()*w/ele.getWidth(),mw=braceleftex.getWidth()-1;
		if(h<bracelefttp.getHeight()+braceleftmid.getHeight()+braceleftbt.getHeight())
			return null;
		ConnectedComponent e=new ConnectedComponent(0,w-1,0,h-1);
		List<RunLength> runlengths=e.getRunLengths();
		int dx=w-bracelefttp.getWidth();
		for(RunLength rl:bracelefttp.getRunLengths())
			runlengths.add(new RunLength(rl.getY(),rl.getX()+dx,rl.getCount()));
		int pt1=(h-braceleftmid.getHeight())/2,pt2=(h+braceleftmid.getHeight())/2,pt3=h-braceleftbt.getHeight();
		dx=braceleftmid.getWidth()-mw-1;
		for(int i=bracelefttp.getHeight();i<pt1;i++)
			runlengths.add(new RunLength(i,dx,mw));
		for(RunLength rl:braceleftmid.getRunLengths())
			runlengths.add(new RunLength(rl.getY()+pt1,rl.getX(),rl.getCount()));
		for(int i=pt2;i<pt3;i++)
			runlengths.add(new RunLength(i,dx,mw));
		dx=w-braceleftbt.getWidth();
		for(RunLength rl:braceleftbt.getRunLengths())
			runlengths.add(new RunLength(rl.getY()+pt3,rl.getX()+dx,rl.getCount()));
		//print(e);
		return makeCandidate(ele,e,"\\{");
	}
	/**
	 * Check if ele is a right brace
	 * @param ele to be matched
	 * @return 1.0 if impossible or the Hausdoff distance between ele and a right brace
	 */
	public static Candidate isRightBrace(ConnectedComponent ele){
		ConnectedComponent bracerighttp=map.get("\\bracerighttp").getConnectedComponent(),bracerightbt=map.get("\\bracerightbt").getConnectedComponent(),
		bracerightmid=map.get("\\bracerightmid").getConnectedComponent(),bracerightex=map.get("\\braceex").getConnectedComponent();
		int w=bracerighttp.getWidth()+bracerightmid.getWidth()-bracerightex.getWidth(),h=ele.getHeight()*w/ele.getWidth(),mw=bracerightex.getWidth()-1;
		if(h<bracerighttp.getHeight()+bracerightmid.getHeight()+bracerightbt.getHeight())
			return null;
		ConnectedComponent e=new ConnectedComponent(0,w-1,0,h-1);
		List<RunLength> runlengths=e.getRunLengths();
		runlengths.addAll(bracerighttp.getRunLengths());
		int pt1=(h-bracerightmid.getHeight())/2,pt2=(h+bracerightmid.getHeight())/2,pt3=h-bracerightbt.getHeight();
		int dx=bracerighttp.getWidth()-mw-1;
		for(int i=bracerighttp.getHeight();i<pt1;i++)
			runlengths.add(new RunLength(i,dx,mw));
		dx=w-bracerightmid.getWidth();
		for(RunLength rl:bracerightmid.getRunLengths())
			runlengths.add(new RunLength(rl.getY()+pt1,rl.getX()+dx,rl.getCount()));
		dx=bracerightbt.getWidth()-mw-1;
		for(int i=pt2;i<pt3;i++)
			runlengths.add(new RunLength(i,dx,mw));
		dx=w-bracerightbt.getWidth();
		for(RunLength rl:bracerightbt.getRunLengths())
			runlengths.add(new RunLength(rl.getY()+pt3,rl.getX(),rl.getCount()));
		//print(e);
		return makeCandidate(ele,e,"\\}");
	}
	/**
	 * Check if ele is a left ceil
	 * @param ele to be matched
	 * @return 1.0 if impossible or the Hausdoff distance between ele and a left ceil
	 */
	public static Candidate isLeftCeil(ConnectedComponent ele){
		ConnectedComponent bracketlefttp=map.get("\\bracketlefttp").getConnectedComponent(),bracketleftex=map.get("\\bracketleftex").getConnectedComponent();
		int w=bracketlefttp.getWidth(),h=ele.getHeight()*w/ele.getWidth(),mw=bracketleftex.getWidth()-1;
		if(h<bracketlefttp.getHeight())
			return null;
		ConnectedComponent e=new ConnectedComponent(0,w-1,0,h-1);
		List<RunLength> runlengths=e.getRunLengths();
		runlengths.addAll(bracketlefttp.getRunLengths());
		for(int i=bracketlefttp.getHeight();i<h;i++)
			runlengths.add(new RunLength(i,0,mw));
		//print(e);
		return makeCandidate(ele,e,"\\lceil ");
	}
	/**
	 * Check if ele is a right ceil
	 * @param ele to be matched
	 * @return 1.0 if impossible or the Hausdoff distance between ele and a right ceil
	 */
	public static Candidate isRightCeil(ConnectedComponent ele){
		ConnectedComponent bracketrighttp=map.get("\\bracketrighttp").getConnectedComponent(),bracketrightex=map.get("\\bracketrightex").getConnectedComponent();
		int w=bracketrighttp.getWidth(),h=ele.getHeight()*w/ele.getWidth(),mw=bracketrightex.getWidth()-1;
		if(h<bracketrighttp.getHeight())
			return null;
		ConnectedComponent e=new ConnectedComponent(0,w-1,0,h-1);
		List<RunLength> runlengths=e.getRunLengths();
		runlengths.addAll(bracketrighttp.getRunLengths());
		for(int i=bracketrighttp.getHeight(),dx=w-mw-1;i<h;i++)
			runlengths.add(new RunLength(i,dx,mw));
		//print(e);
		return makeCandidate(ele,e,"\\rceil ");
	}
	/**
	 * Check if ele is a left floor
	 * @param ele to be matched
	 * @return 1.0 if impossible or the Hausdoff distance between ele and a left floor
	 */
	public static Candidate isLeftFloor(ConnectedComponent ele){
		ConnectedComponent bracketleftbt=map.get("\\bracketleftbt").getConnectedComponent(),bracketleftex=map.get("\\bracketleftex").getConnectedComponent();
		int w=bracketleftbt.getWidth(),h=ele.getHeight()*w/ele.getWidth(),mw=bracketleftex.getWidth()-1;
		if(h<bracketleftbt.getHeight())
			return null;
		ConnectedComponent e=new ConnectedComponent(0,w-1,0,h-1);
		List<RunLength> runlengths=e.getRunLengths();
		int dy=h-bracketleftbt.getHeight();
		for(int i=0;i<dy;i++)
			runlengths.add(new RunLength(i,0,mw));
		for(RunLength rl:bracketleftbt.getRunLengths())
			runlengths.add(new RunLength(rl.getY()+dy,rl.getX(),rl.getCount()));
		//print(e);
		return makeCandidate(ele,e,"\\lfloor ");
	}
	/**
	 * Check if ele is a right floor
	 * @param ele to be matched
	 * @return 1.0 if impossible or the Hausdoff distance between ele and a right floor
	 */
	public static Candidate isRightFloor(ConnectedComponent ele){
		ConnectedComponent bracketrightbt=map.get("\\bracketrightbt").getConnectedComponent(),bracketrightex=map.get("\\bracketrightex").getConnectedComponent();
		int w=bracketrightbt.getWidth(),h=ele.getHeight()*w/ele.getWidth(),mw=bracketrightex.getWidth()-1;
		if(h<bracketrightbt.getHeight())
			return null;
		ConnectedComponent e=new ConnectedComponent(0,w-1,0,h-1);
		List<RunLength> runlengths=e.getRunLengths();
		int dy=h-bracketrightbt.getHeight(),dx=w-mw-1;
		for(int i=0;i<dy;i++)
			runlengths.add(new RunLength(i,dx,mw));
		for(RunLength rl:bracketrightbt.getRunLengths())
			runlengths.add(new RunLength(rl.getY()+dy,rl.getX(),rl.getCount()));
		//print(e);
		return makeCandidate(ele,e,"\\rfloor ");
	}
	/**
	 * Check if ele is a horizontal line
	 * @param ele to be matched
	 * @return 1.0 if impossible or the Hausdoff distance between ele and a horizontal line
	 */
	public static Candidate isHorizontalLine(ConnectedComponent ele){
		int h=ele.getHeight(),w=ele.getWidth();
		if(w<5*h)
			return null;
		ConnectedComponent e=new ConnectedComponent(0,w-1,0,h-1);
		List<RunLength> runlengths=e.getRunLengths();
		for(int i=0;i<h;i++)
			runlengths.add(new RunLength(i,0,w-1));
		//print(e);
		return new Candidate(map.get("-").getSymbol(),1-matcher.getDistance(ele,e));
	}
	/**
	 * Check if ele is a vertical line
	 * @param ele to be matched
	 * @return 1.0 if impossible or the Hausdoff distance between ele and a vertical line
	 */
	public static Candidate isVerticalLine(ConnectedComponent ele){
		int h=ele.getHeight(),w=ele.getWidth();
		if(h<5*w)
			return null;
		ConnectedComponent e=new ConnectedComponent(0,w-1,0,h-1);
		List<RunLength> runlengths=e.getRunLengths();
		for(int i=0;i<h;i++)
			runlengths.add(new RunLength(i,0,w-1));
		//print(e);
		return makeCandidate(ele,e,"|");
	}
	/**
	 * Check if ele is a under brace
	 * @param ele to be matched
	 * @return 1.0 if impossible or the Hausdoff distance between ele and a under brace
	 */
	public static Candidate isUnderBrace(ConnectedComponent ele){
		ConnectedComponent bracedownleft=map.get("\\bracedownleft").getConnectedComponent(),bracedownright=map.get("\\bracedownright").getConnectedComponent(),
		braceupleft=map.get("\\braceupleft").getConnectedComponent(),braceupright=map.get("\\braceupright").getConnectedComponent();
		int h=braceupleft.getHeight()+bracedownright.getHeight()-bracethick,w=ele.getWidth()*h/ele.getHeight(),mid=w/2;
		if(w<bracedownleft.getWidth()+bracedownright.getWidth()+braceupleft.getWidth()+braceupright.getWidth())
			return null;
		ConnectedComponent e=new ConnectedComponent(0,w-1,0,h-1);
		List<RunLength> runlengths=e.getRunLengths();
		runlengths.addAll(braceupleft.getRunLengths());
		int dx=braceupleft.getWidth(),dy=mid-bracedownright.getWidth()-dx-1;
		for(int i=h-bracedownright.getHeight();i<braceupleft.getHeight();i++)
			runlengths.add(new RunLength(i,dx,dy));
		dx=mid-bracedownright.getWidth();
		dy=h-bracedownright.getHeight();
		for(RunLength rl:bracedownright.getRunLengths())
			runlengths.add(new RunLength(rl.getY()+dy,rl.getX()+dx,rl.getCount()));
		dy=h-bracedownleft.getHeight();
		for(RunLength rl:bracedownleft.getRunLengths())
			runlengths.add(new RunLength(rl.getY()+dy,rl.getX()+mid,rl.getCount()));
		dx=mid+bracedownleft.getWidth();
		dy=w-bracedownright.getWidth()-mid-braceupleft.getWidth();
		for(int i=h-bracedownleft.getHeight();i<braceupright.getHeight();i++)
			runlengths.add(new RunLength(i,dx,dy));
		dx=w-braceupright.getWidth();
		for(RunLength rl:braceupright.getRunLengths())
			runlengths.add(new RunLength(rl.getY(),rl.getX()+dx,rl.getCount()));
		//print(e);
		return makeCandidate(ele,e,"\\underbrace{}");
	}
	/**
	 * Check if ele is a over brace
	 * @param ele to be matched
	 * @return 1.0 if impossible or the Hausdoff distance between ele and a over brace
	 */
	public static Candidate isOverBrace(ConnectedComponent ele){
		ConnectedComponent bracedownleft=map.get("\\bracedownleft").getConnectedComponent(),bracedownright=map.get("\\bracedownright").getConnectedComponent(),
		braceupleft=map.get("\\braceupleft").getConnectedComponent(),braceupright=map.get("\\braceupright").getConnectedComponent();
		int h=braceupleft.getHeight()+bracedownright.getHeight()-bracethick,w=ele.getWidth()*h/ele.getHeight(),mid=w/2;
		if(w<bracedownleft.getWidth()+bracedownright.getWidth()+braceupleft.getWidth()+braceupright.getWidth())
			return null;
		ConnectedComponent e=new ConnectedComponent(0,w-1,0,h-1);
		List<RunLength> runlengths=e.getRunLengths();
		int dx=0,dy=h-bracedownleft.getHeight();
		for(RunLength rl:bracedownleft.getRunLengths())
			runlengths.add(new RunLength(rl.getY()+dy,rl.getX(),rl.getCount()));
		dx=bracedownleft.getWidth();
		dy=mid-braceupright.getWidth()-dx-1;
		for(int i=h-bracedownleft.getHeight();i<braceupright.getHeight();i++)
			runlengths.add(new RunLength(i,dx,dy));
		dx=mid-braceupright.getWidth();
		for(RunLength rl:braceupright.getRunLengths())
			runlengths.add(new RunLength(rl.getY(),rl.getX()+dx,rl.getCount()));
		for(RunLength rl:braceupleft.getRunLengths())
			runlengths.add(new RunLength(rl.getY(),rl.getX()+mid,rl.getCount()));
		dx=mid+braceupleft.getWidth();
		dy=w-braceupright.getWidth()-mid-bracedownleft.getWidth();
		for(int i=h-bracedownright.getHeight();i<braceupleft.getHeight();i++)
			runlengths.add(new RunLength(i,dx,dy));
		dx=w-bracedownright.getWidth();
		dy=h-bracedownright.getHeight();
		for(RunLength rl:bracedownright.getRunLengths())
			runlengths.add(new RunLength(rl.getY()+dy,rl.getX()+dx,rl.getCount()));
		//print(e);
		return makeCandidate(ele,e,"\\overbrace{}");
	}
	/**
	 * Check if ele is a right arrow
	 * @param ele to be matched
	 * @return 1.0 if impossible or the Hausdoff distance between ele and a right arrow
	 */
	public static Candidate isRightArrow(ConnectedComponent ele){
		ConnectedComponent rarrow=map.get("\\rightarrow ").getConnectedComponent();
		int h=rarrow.getHeight(),w=ele.getWidth()*h/ele.getHeight();
		if(w<rarrow.getWidth())
			return null;
		ConnectedComponent e=new ConnectedComponent(0,w-1,0,h-1);
		List<RunLength> runlengths=e.getRunLengths();
		int dx=w-rarrow.getWidth();
		for(RunLength rl:rarrow.getRunLengths())
			runlengths.add(new RunLength(rl.getY(),rl.getX()+dx,rl.getCount()));
		--dx;
		for(int i=(h-arrowthick)/2;i<(h+arrowthick)/2;i++)
			runlengths.add(new RunLength(i,0,dx));
		//print(e);
		return makeCandidate(ele,e,"\\rightarrow ");
	}
	/**
	 * Check if ele is a right arrow
	 * @param ele to be matched
	 * @return 1.0 if impossible or the Hausdoff distance between ele and a right arrow
	 */
	public static Candidate isLeftArrow(ConnectedComponent ele){
		ConnectedComponent rarrow=map.get("\\leftarrow ").getConnectedComponent();
		int h=rarrow.getHeight(),w=ele.getWidth()*h/ele.getHeight(),x0=rarrow.getRight()+1,dx=w-1-x0;
		if(w<rarrow.getWidth())
			return null;
		ConnectedComponent e=new ConnectedComponent(0,w-1,0,h-1);
		List<RunLength> runlengths=e.getRunLengths();
		for(RunLength rl:rarrow.getRunLengths())
			runlengths.add(rl);
		for(int i=(h-arrowthick)/2;i<(h+arrowthick)/2;i++)
			runlengths.add(new RunLength(i,x0,dx));
		//print(e);
		return makeCandidate(ele,e,"\\rightarrow ");
	}
	private static Candidate isRootSign1(ConnectedComponent ele){
		ConnectedComponent surd=map.get("\\surd1").getConnectedComponent();
		int w=ele.getWidth()*surd.getHeight()/ele.getHeight();
		if(w<w1)
			return null;
		--w;
		Iterator<RunLength> iter=surd.getRunLengths().iterator();
		for(int i=0;i<rootthick;i++){
			RunLength rl=iter.next();
			rl.reset(i,rl.getX(),w-rl.getX());
		}
		surd.cordRight=w;
		//print(surd);
		return makeCandidate(ele,surd,"\\sqrt{}");
	}
	private static Candidate isRootSign2(ConnectedComponent ele){
		ConnectedComponent surd=map.get("\\surd2").getConnectedComponent();
		int w=ele.getWidth()*surd.getHeight()/ele.getHeight();
		if(w<w2)
			return null;
		--w;
		Iterator<RunLength> iter=surd.getRunLengths().iterator();
		for(int i=0;i<rootthick;i++){
			RunLength rl=iter.next();
			rl.reset(i,rl.getX(),w-rl.getX());
		}
		surd.cordRight=w;
		//print(surd);
		return makeCandidate(ele,surd,"\\sqrt{}");
	}
	private static Candidate isRootSign3(ConnectedComponent ele){
		ConnectedComponent surd=map.get("\\surd3").getConnectedComponent();
		int w=ele.getWidth()*surd.getHeight()/ele.getHeight();
		if(w<w3)
			return null;
		--w;
		Iterator<RunLength> iter=surd.getRunLengths().iterator();
		for(int i=0;i<rootthick;i++){
			RunLength rl=iter.next();
			rl.reset(i,rl.getX(),w-rl.getX());
		}
		surd.cordRight=w;
		//print(surd);
		return makeCandidate(ele,surd,"\\sqrt{}");
	}
	private static Candidate isRootSign4(ConnectedComponent ele){
		ConnectedComponent surd=map.get("\\surd4").getConnectedComponent();
		int w=ele.getWidth()*surd.getHeight()/ele.getHeight();
		if(w<w4)
			return null;
		--w;
		Iterator<RunLength> iter=surd.getRunLengths().iterator();
		for(int i=0;i<rootthick;i++){
			RunLength rl=iter.next();
			rl.reset(i,rl.getX(),w-rl.getX());
		}
		surd.cordRight=w;
		//print(surd);
		return makeCandidate(ele,surd,"\\sqrt{}");
	}
	private static Candidate isRootSign5(ConnectedComponent ele){
		int j0=0,prevy=-1;
		for(RunLength rl:ele.getRunLengths()){
			int tmp=rl.getY();
			if(tmp-ele.getTop()>ele.getHeight()/4)
				break;
			if(tmp!=prevy&&rl.getCount()<ele.getWidth()/15){
				j0=rl.getX()+rl.getCount()+1-ele.getLeft();
				break;
			}
			prevy=tmp;
		}
		if(j0==0)
			return null;
		//int j0=ele.getRunLengths().get(0).getX()+ele.getRunLengths().get(0).getCount()+1-ele.getLeft();
		ConnectedComponent bt=map.get("\\radicalbt").getConnectedComponent(),vertex=map.get("\\radicalvertex").getConnectedComponent();
		int h=ele.getHeight()*bt.getWidth()/j0,w=ele.getWidth()*bt.getWidth()/j0;
		ConnectedComponent e=new ConnectedComponent(0,w-1,0,h-1);
		List<RunLength> runlengths=e.getRunLengths();
		if(h<bt.getHeight()||w<bt.getWidth())
			return null;
		int j1=bt.getWidth()-rootthick,j2=w-1-j1;
		for(int i=0;i<rootthick;i++)
			runlengths.add(new RunLength(i,j1,j2));
		int dy=h-bt.getHeight();
		for(int i=rootthick;i<dy;i++)
			runlengths.add(new RunLength(i,j1,rootthick-1));
		for(RunLength rl:bt.getRunLengths())
			runlengths.add(new RunLength(rl.getY()+dy,rl.getX(),rl.getCount()));
		//print(e);
		return makeCandidate(ele,e,"\\sqrt{}");
	}
	/**
	 * Check if ele is a root sign
	 * @param ele to be matched
	 * @return 1.0 if impossible or the Hausdoff distance between ele and a root sign
	 */
	public static Candidate isRootSign(ConnectedComponent ele){
		Candidate curr=isRootSign1(ele);
		Candidate[] test=new Candidate[]{isRootSign2(ele),isRootSign3(ele),isRootSign4(ele),isRootSign5(ele)};
		for(Candidate d:test)
			if(curr==null||(d!=null&&d.getCertainty()>curr.getCertainty()))
				curr=d;
		return curr;
	}
	public static boolean isPossibleRootSign(ConnectedComponent ele){
		Candidate cand=isRootSign(ele);
		return cand!=null&&cand.getCertainty()>0.9;
	}
	/**
	 * Check if ele is a integral sign
	 * @param ele to be matched
	 * @return 1.0 if impossible or the Hausdoff distance between ele and a integral sign
	 */
	public static final boolean isIntegralSign(ConnectedComponent ele){
		return Math.min(matcher.getDistance(ele,map.get("\\int ").getConnectedComponent()),matcher.getDistance(ele,map.get("\\oint ").getConnectedComponent()))<0.1;
	}
	/**
	 * Check if ele is likely horizontal line
	 * @param ele to be matched
	 * @return result
	 */
	public static final boolean isPossibleHLine(ConnectedComponent ele){
		return ele.getWidth()>=5*ele.getHeight()&&ele.getDensity()>0.9;
	}
	/**
	 * Check if ele is likely a dot
	 * @param ele to be matched
	 * @return result
	 */
	public static boolean isPossibleDot(ConnectedComponent ele){
		double aspectratio=(double)ele.getWidth()/ele.getHeight();
		return aspectratio>=0.8&&aspectratio<=1.25&&ele.getDensity()>=0.7;
	}
}