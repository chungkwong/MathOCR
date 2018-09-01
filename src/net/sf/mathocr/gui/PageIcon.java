/* PageIcon.java
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
package net.sf.mathocr.gui;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.*;
import javax.swing.*;
import net.sf.mathocr.layout.*;
/**
 * A component being used to displayed image of a page
 */
public final class PageIcon extends JLabel implements Icon,MouseMotionListener{
	Page page;
	double scale=1.0;
	IconPaint painter;
	public PageIcon(){
		super();
		setVerticalAlignment(SwingConstants.TOP);
		setHorizontalAlignment(SwingConstants.LEFT);
		setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		addMouseMotionListener(this);
	}
	public void setPage(Page page){
		this.page=page;
		setIcon(this);
		repaint();
	}
	/**
	 * Get the height of the icon
	 * @return the height of the icon
	 */
	public int getIconHeight(){
		BufferedImage image=page.getModifiedImage();
		if(image!=null)
			return (int)(image.getHeight()*scale);
		else
			return 0;
	}
	/**
	 * Get the width of the icon
	 * @return the width of the icon
	 */
	public int getIconWidth(){
		BufferedImage image=page.getModifiedImage();
		if(image!=null)
			return (int)(image.getWidth()*scale);
		else
			return 0;
	}
	public double getScale(){
		return scale;
	}
	public void setScale(double scale){
		this.scale=scale;
		repaint();
		revalidate();
	}
	public void setPainter(IconPaint painter){
		this.painter=painter;
	}
	/*
	 * paint the icon
	 * @param g the graphics to paint
	 */
	public void paintIcon(Component c,Graphics g,int x,int y){
		Graphics2D g2d=(Graphics2D)g;
		g2d.scale(scale,scale);
		g2d.setFont(new Font("Monospaced",Font.PLAIN,(int)(12/scale)));
		BufferedImage image=page.getModifiedImage();
		if(image!=null){
			g2d.drawImage(image,null,x,y);
			if(painter!=null)
				painter.paintOn(g2d);
		}
	}
	/**
	 * Not used
	 */
	public void mouseDragged(MouseEvent e){}
	/**
	 *
	 * @param e the event
	 */
	public void mouseMoved(MouseEvent e){
		setToolTipText("("+(int)(e.getX()/scale)+","+(int)(e.getY()/scale)+")");
	}
}