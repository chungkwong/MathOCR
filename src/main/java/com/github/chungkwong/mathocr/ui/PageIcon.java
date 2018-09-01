/* PageIcon.java
 * =========================================================================
 * This file is originally part of the MathOCR Project
 *
 * Copyright (C) 2014,2015 Chan Chung Kwong
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 */
package com.github.chungkwong.mathocr.ui;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.text.*;
import javax.swing.*;
/**
 * A component being used to displayed image of a page
 */
public final class PageIcon extends JPanel implements Icon,MouseMotionListener{
	private BufferedImage page;
	private double scale=1.0;
	private IconPaint painter;
	private JLabel content=new JLabel();
	public PageIcon(){
		super(new BorderLayout());
		content.setVerticalAlignment(SwingConstants.TOP);
		content.setHorizontalAlignment(SwingConstants.LEFT);
		content.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		content.addMouseMotionListener(this);
		add(content,BorderLayout.CENTER);
		JFormattedTextField scaler=new JFormattedTextField(DecimalFormat.getPercentInstance());
		scaler.setValue(1.0);
		scaler.addActionListener((e)->{
			setScale(((Number)scaler.getValue()).doubleValue());
		});
		add(scaler,BorderLayout.SOUTH);
	}
	public PageIcon(BufferedImage page,IconPaint painter){
		this();
		setPainter(painter);
		setPage(page);
	}
	public void setPage(BufferedImage page){
		this.page=page;
		if(page!=null){
			content.setSize(page.getWidth(),page.getHeight());
		}
		content.setIcon(this);
		repaint();
	}
	public JLabel getContent(){
		return content;
	}
	/**
	 * Get the height of the icon
	 *
	 * @return the height of the icon
	 */
	public int getIconHeight(){;
		if(page!=null){
			return (int)(page.getHeight()*scale);
		}else{
			return 0;
		}
	}
	/**
	 * Get the width of the icon
	 *
	 * @return the width of the icon
	 */
	public int getIconWidth(){
		if(page!=null){
			return (int)(page.getWidth()*scale);
		}else{
			return 0;
		}
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
	@Override
	public void paintIcon(Component c,Graphics g,int x,int y){
		Graphics2D g2d=(Graphics2D)g;
		g2d.scale(scale,scale);
		g2d.setFont(new Font("Monospaced",Font.PLAIN,(int)(12/scale)));
		if(page!=null){
			g2d.drawImage(page,null,x,y);
			if(painter!=null){
				painter.paintOn(g2d);
			}
		}
	}
	/**
	 * Not used
	 */
	@Override
	public void mouseDragged(MouseEvent e){
	}
	/**
	 *
	 * @param e the event
	 */
	@Override
	public void mouseMoved(MouseEvent e){
		setToolTipText("("+(int)(e.getX()/scale)+","+(int)(e.getY()/scale)+")");
	}
}
