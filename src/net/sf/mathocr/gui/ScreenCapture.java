/* ScreenCapture.java
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
package net.sf.mathocr.gui;
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.io.*;
import javax.imageio.*;
import javax.swing.*;
/**
 * A GUI component being used to capture part of the screen
 */
public final class ScreenCapture extends JFrame implements MouseListener,MouseMotionListener{
	int h,w,x,y;
	MainFrame mf;
	int orgst;
	public ScreenCapture(MainFrame mf){
		super();
		this.mf=mf;
		orgst=mf.f.getState();
		mf.f.setState(JFrame.ICONIFIED);
		addMouseListener(this);
		addMouseMotionListener(this);
		setSize(400,300);
		setUndecorated(true);
		setAlwaysOnTop(true);
		try{
			setOpacity(0f);
		}catch(Exception ex){
			com.sun.awt.AWTUtilities.setWindowOpaque(this,false);
		}
		setResizable(true);
		setVisible(true);
	}
	public void mouseClicked(MouseEvent e){
		if(e.getX()<=10&&e.getY()<=10)
			try{
				Rectangle rect=getBounds();
				setVisible(false);
				BufferedImage img=new Robot().createScreenCapture(rect);
				File tmp=File.createTempFile("formula",".png");
				ImageIO.write(img,"png",tmp);
				mf.f.setState(orgst);
				mf.showFormulaPane(new File[]{tmp});
			}catch(Exception ex){
				ex.printStackTrace();
			}
	}
	public void mouseEntered(MouseEvent e){}
	public void mouseExited(MouseEvent e){}
	public void mousePressed(MouseEvent e){
		x=e.getX();
		y=e.getY();
		if(x<=10)
			w=-1;
		else if(x>=getWidth()-10)
			w=1;
		else
			w=0;
		if(y<=10)
			h=-1;
		else if(y>=getHeight()-10)
			h=1;
		else
			h=0;
	}
	public void mouseReleased(MouseEvent e){}
	public void mouseDragged(MouseEvent e){
		int newx=getX(),newy=getY(),newwidth=getWidth(),newheight=getHeight();
		int dx=e.getX()-x,dy=e.getY()-y;
		if(w==1){
			newwidth+=dx;
			x+=dx;
		}else if(w==-1){
			newx+=dx;
			newwidth-=dx;
		}
		if(h==1){
			newheight+=dy;
			y+=dy;
		}else if(h==-1){
			newy+=dy;
			newheight-=dy;
		}
		if(w==0&&h==0){
			newx+=dx;
			newy+=dy;
		}
		this.setBounds(newx,newy,newwidth,newheight);
	}
	public void mouseMoved(MouseEvent e){
		int x=e.getX(),y=e.getY(),w=0,h=0;
		if(x<=10)
			w=-1;
		else if(x>=getWidth()-10)
			w=1;
		if(y<=10)
			h=-1;
		else if(y>=getHeight()-10)
			h=1;
		if(w==-1&&h==-1)
			setCursor(Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR));
		else if(w==-1&&h==0)
			setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
		else if(w==-1&&h==1)
			setCursor(Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR));
		else if(w==0&&h==-1)
			setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
		else if(w==0&&h==1)
			setCursor(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
		else if(w==1&&h==-1)
			setCursor(Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR));
		else if(w==1&&h==0)
			setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
		else if(w==1&&h==1)
			setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
		else
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}
	public void paint(Graphics g){
		g.clearRect(0,0,getWidth(),getHeight());
		g.setColor(Color.RED);
		g.drawRect(0,0,getWidth()-1,getHeight()-1);
		g.fillRect(0,0,10,10);
	}
}