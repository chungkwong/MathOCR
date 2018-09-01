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
package com.github.chungkwong.mathocr.ui;
import com.github.chungkwong.mathocr.layout.logical.Page;
import com.github.chungkwong.mathocr.common.Pair;
import com.github.chungkwong.mathocr.common.BoundBox;
import com.github.chungkwong.mathocr.common.ConnectedComponent;
import com.github.chungkwong.mathocr.common.ComponentPool;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;
import static com.github.chungkwong.mathocr.Environment.ENVIRONMENT;
/**
 *
 * @author Chan Chung Kwong
 */
public class ComponentInspector extends Inspector<BufferedImage,Page,Page> implements ActionListener,MouseListener,IconPaint{
	private JButton ccdel, ccmerge, ccsplitH, ccsplitV, layoutA, clean, filter;
	private PageIcon icon;
	private BufferedImage image;
	private JToolBar bar=new JToolBar(JToolBar.VERTICAL);
	private ComponentPool components;
	private char status='N';
	private ConnectedComponent curr=null;
	public ComponentInspector(){
		bar.add(new JLabel(ENVIRONMENT.getTranslation("COMPONENT_ANALYSIS")));
		ccdel=addButton("DELETE");
		ccmerge=addButton("MERGE");
		ccsplitH=addButton("HSPLIT");
		ccsplitV=addButton("VSPLIT");
		bar.addSeparator();
		clean=addButton("CLEAN_EDGES");
		filter=addButton("NOISE_REMOVE");
		bar.addSeparator();
		layoutA=addButton("NEXT");
		add(bar,BorderLayout.EAST);
	}
	private final JButton addButton(String com){
		JButton button=new JButton(ENVIRONMENT.getTranslation(com));
		button.setActionCommand(com);
		button.addActionListener(this);
		button.setAlignmentX(0);
		button.setMaximumSize(button.getPreferredSize());
		bar.add(button);
		return button;
	}
	public void actionPerformed(ActionEvent e){
		curr=null;
		status='N';
		switch(e.getActionCommand()){
			case "DELETE":
				status='D';
				break;
			case "MERGE":
				status='M';
				break;
			case "HSPLIT":
				status='H';
				break;
			case "VSPLIT":
				status='V';
				break;
			case "CLEAN_EDGES":
				components.cleanPageEdges(image.getWidth(),image.getHeight());
				break;
			case "NOISE_REMOVE":
				BoundBox box=components.getBoundBox();
				components.filterNoise(image.getWidth(),image.getHeight());
				break;
			case "NEXT":
				call(new PhysicalInspector(),new Pair<>(components,image));
				break;
		}
		icon.repaint();
	}
	/**
	 * Not used
	 */
	public void mouseClicked(MouseEvent e){
	}
	/**
	 * Not used
	 */
	public void mouseEntered(MouseEvent e){
	}
	/**
	 * Not used
	 */
	public void mouseExited(MouseEvent e){
	}
	/**
	 * Change recognition result of the symbol that the mouse pointed to
	 *
	 * @param e the event
	 */
	public void mousePressed(MouseEvent e){
		if(status=='N'){
			return;
		}
		double scale=icon.getScale();
		int x=(int)(e.getX()/scale+0.5), y=(int)(e.getY()/scale+0.5);
		ConnectedComponent cand=null;
		for(ConnectedComponent ele:components.getComponents()){
			if(x>=ele.getLeft()&&x<=ele.getRight()&&y<=ele.getBottom()&&y>=ele.getTop()){
				if(cand==null||ele.getHeight()*ele.getWidth()<cand.getHeight()*cand.getWidth()){
					cand=ele;
				}
			}
		}
		if(cand==null){
			curr=null;
			return;
		}
		if(status=='D'){
			components.getComponents().remove(cand);
		}else if(status=='M'){
			if(curr==null){
				curr=cand;
			}else{
				curr.combineWith(cand);
				components.getComponents().remove(cand);
				curr=null;
			}
		}else if(status=='H'){
			components.getComponents().add(cand.splitHorizontally(x));
		}else if(status=='V'){
			components.getComponents().add(cand.splitVertically(y));
		}
		icon.repaint();
	}
	/**
	 * Not used
	 *
	 * @param e
	 */
	@Override
	public void mouseReleased(MouseEvent e){
	}
	@Override
	public void paintOn(Graphics2D g2d){
		g2d.setColor(Color.RED);
		for(ConnectedComponent ele:components.getComponents()){
			g2d.drawRect(ele.getLeft(),ele.getTop(),ele.getWidth(),ele.getHeight());
		}
	}
	@Override
	protected void onCreated(BufferedImage src){
		image=src;
		components=new ComponentPool(src);
		icon=new PageIcon(image,this);
		icon.getContent().addMouseListener(this);
		add(new JScrollPane(icon),BorderLayout.CENTER);
	}
	@Override
	protected void onReturned(Page val){
		ret(val);
	}
}
