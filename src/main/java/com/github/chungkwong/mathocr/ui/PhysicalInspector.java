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
import com.github.chungkwong.mathocr.text.LineSegmenters;
import com.github.chungkwong.mathocr.text.TextLine;
import com.github.chungkwong.mathocr.layout.physical.PhysicalBlock;
import com.github.chungkwong.mathocr.layout.physical.BlockClassifiers;
import com.github.chungkwong.mathocr.layout.physical.PageSegmenters;
import com.github.chungkwong.mathocr.layout.physical.BlockOrderers;
import com.github.chungkwong.mathocr.layout.physical.BlockRecognizers;
import com.github.chungkwong.mathocr.layout.logical.LogicalBlock;
import com.github.chungkwong.mathocr.layout.logical.Page;
import com.github.chungkwong.mathocr.layout.logical.PageAnalyzers;
import com.github.chungkwong.mathocr.common.Pair;
import com.github.chungkwong.mathocr.common.ComponentPool;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.*;
import java.util.List;
import java.util.stream.*;
import javax.swing.*;
import static com.github.chungkwong.mathocr.Environment.ENVIRONMENT;
/**
 *
 * @author Chan Chung Kwong
 */
public class PhysicalInspector extends Inspector<Pair<ComponentPool,BufferedImage>,Page,Page> implements ActionListener,MouseListener,IconPaint{
	private List<PhysicalBlock> physicalBlocks;
	private Map<PhysicalBlock,String> types;
	private HashMap<PhysicalBlock,List<TextLine>> lines;
	private JToolBar bar=new JToolBar(JToolBar.VERTICAL);
	private PageIcon icon;
	private JButton create, del, merge, splitH, splitV, classify, fixtype, autoSort, manualSort, extractLine, splitLine, mergeLine, ocr, next;
	private JComboBox<String> type;
	private char status='N';
	private PhysicalBlock curr=null;
	private int readOrd=0;
	private BufferedImage image;
	public PhysicalInspector(){
		add(new JLabel(ENVIRONMENT.getTranslation("LAYOUT_ANALYSIS")));
		//create=addButton("ADD");
		del=addButton("DELETE");
		merge=addButton("MERGE");
		splitH=addButton("HSPLIT");
		splitV=addButton("VSPLIT");
		bar.addSeparator();
		classify=addButton("REGION_CLASSIFY");
		classify=addButton("REGION_MANUAL");
		type=new JComboBox<String>(new String[]{ENVIRONMENT.getTranslation("TEXT"),ENVIRONMENT.getTranslation("IMAGE"),ENVIRONMENT.getTranslation("TABLE")});
		type.setAlignmentX(0);
		type.setMaximumSize(type.getPreferredSize());
		bar.add(type);
		bar.addSeparator();
		autoSort=addButton("READING_ORDER_SORT");
		manualSort=addButton("READING_ORDER_MANUAL");
		bar.addSeparator();
		extractLine=addButton("EXTRACT_LINES");
		splitLine=addButton("SPLIT_LINE");
		mergeLine=addButton("MERGE_LINE");
		ocr=addButton("PREVIEW_TEXT");
		bar.addSeparator();
		next=addButton("NEXT");
		splitLine.setEnabled(false);
		mergeLine.setEnabled(false);
		ocr.setEnabled(false);
		next.setEnabled(false);
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
	@Override
	protected void onCreated(Pair<ComponentPool,BufferedImage> src){
		ComponentPool components=src.getKey();
		image=src.getValue();
		physicalBlocks=BlockOrderers.REGISTRY.get().order(
				PageSegmenters.REGISTRY.get().segment(components));
		types=new HashMap<>();
		physicalBlocks.forEach((p)->types.put(p,BlockClassifiers.REGISTRY.get().classify(p,image)));
		icon=new PageIcon(image,this);
		icon.getContent().addMouseListener(this);
		add(new JScrollPane(icon),BorderLayout.CENTER);
	}
	@Override
	protected void onReturned(Page val){
		if(val!=null){
			ret(val);
		}
	}
	@Override
	public void actionPerformed(ActionEvent e){
		curr=null;
		status='N';
		readOrd=0;
		switch(e.getActionCommand()){
			case "ADD":
				status='A';
				break;
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
			case "REGION_MANUAL":
				status='C';
				break;
			case "REGION_CLASSIFY":
				icon.repaint();
				del.setEnabled(false);
				merge.setEnabled(false);
				splitH.setEnabled(false);
				splitV.setEnabled(false);
				extractLine.setEnabled(true);
				break;
			case "READING_ORDER_SORT":
				physicalBlocks=BlockOrderers.REGISTRY.get().order(physicalBlocks);
				icon.repaint();
				break;
			case "READING_ORDER_MANUAL":
				status='S';
				break;
			case "EXTRACT_LINES":
				lines=new HashMap<>();
				physicalBlocks.forEach((p)->lines.put(p,LineSegmenters.REGISTRY.get().segment(p)));
				icon.repaint();
				splitLine.setEnabled(true);
				mergeLine.setEnabled(true);
				ocr.setEnabled(true);
				next.setEnabled(true);
				break;
			case "SPLIT_LINE":
				status='s';
				break;
			case "MERGE_LINE":
				status='m';
				break;
			case "PREVIEW_TEXT":
				status='o';
				break;
			case "NEXT":
				List<LogicalBlock> logicalBlocks=physicalBlocks.stream().
						flatMap((p)->BlockRecognizers.REGISTRY.get(types.get(p)).recognize(p,image).stream()).collect(Collectors.toList());
				logicalBlocks=PageAnalyzers.REGISTRY.get().analysis(logicalBlocks);
				call(new LogicalInspector(),new Pair<>(logicalBlocks,image));
				break;
		}
	}
	@Override
	public void mouseClicked(MouseEvent e){
	}
	@Override
	public void mousePressed(MouseEvent e){
		if(status=='N'){
			return;
		}
		double scale=icon.getScale();
		int x=(int)(e.getX()/scale+0.5), y=(int)(e.getY()/scale+0.5);
		PhysicalBlock cand=null;
		int candind=-1;
		ListIterator<PhysicalBlock> iter=physicalBlocks.listIterator();
		while(iter.hasNext()){
			PhysicalBlock block=iter.next();
			if(x>=block.getBox().getLeft()&&x<=block.getBox().getRight()&&y<=block.getBox().getBottom()&&y>=block.getBox().getTop()){
				if(cand==null||block.getBox().getHeight()*block.getBox().getWidth()<cand.getBox().getHeight()*cand.getBox().getWidth()){
					cand=block;
					candind=iter.previousIndex();
				}
			}
		}
		if(cand==null){
			curr=null;
			return;
		}
		if(status=='D'){
			physicalBlocks.remove(cand);
		}else if(status=='M'){
			if(curr==null){
				curr=cand;
			}else{
				cand.merge(curr);
				physicalBlocks.remove(curr);
				curr=null;
			}
		}else if(status=='H'){
			physicalBlocks.add(cand.splitHorizontally(x));
		}else if(status=='V'){
			physicalBlocks.add(cand.splitVertically(y));
		}else if(status=='S'){
			physicalBlocks.set(candind,physicalBlocks.get(readOrd));
			physicalBlocks.set(readOrd,cand);
			readOrd=(readOrd==physicalBlocks.size()-1)?0:readOrd+1;
		}else if(status=='C'){
			switch(type.getSelectedIndex()){
				case 0:
					types.put(cand,"TEXT");
					break;
				case 1:
					types.put(cand,"IMAGE");
					break;
				case 2:
					types.put(cand,"TABLE");
					break;
			}
		}else if(status=='s'){
			List<TextLine> paragraph=lines.get(cand);
			if(lines!=null){
				ListIterator<TextLine> it=paragraph.listIterator();
				while(it.hasNext()){
					TextLine line=it.next();
					if(x<=line.getBox().getRight()&&line.getBox().getLeft()<=x&&y<=line.getBox().getBottom()&&line.getBox().getTop()<=y){
						it.add(line.splitVertically(y));
					}
				}
			}
		}else if(status=='m'){
			java.util.List<TextLine> paragraph=lines.get(cand);
			if(lines!=null){
				ListIterator<TextLine> it=paragraph.listIterator();
				while(it.hasNext()){
					TextLine line=it.next();
					if(x<=line.getBox().getRight()&&line.getBox().getLeft()<=x&&y<=line.getBox().getBottom()&&line.getBox().getTop()<=y&&it.hasNext()){
						it.remove();
						it.next().merge(line);
					}
				}
			}
		}else if(status=='o'){
			java.util.List<TextLine> paragraph=lines.get(cand);
			if(lines!=null){
				ListIterator<TextLine> it=paragraph.listIterator();
				while(it.hasNext()){
					TextLine line=it.next();
					if(x<=line.getBox().getRight()&&line.getBox().getLeft()<=x&&y<=line.getBox().getBottom()&&line.getBox().getTop()<=y){
						call(new CharacterInspector(),new Pair<>(line,image));
						return;
					}
				}
			}
		}
		icon.repaint();
	}
	@Override
	public void mouseReleased(MouseEvent e){
	}
	@Override
	public void mouseEntered(MouseEvent e){
	}
	@Override
	public void mouseExited(MouseEvent e){
	}
	@Override
	public void paintOn(Graphics2D g2d){
		ListIterator<PhysicalBlock> iter=physicalBlocks.listIterator();
		while(iter.hasNext()){
			PhysicalBlock ele=iter.next();
			String t=types.getOrDefault(ele,"TEXT");
			g2d.setColor(Color.BLUE);
			g2d.drawRect(ele.getBox().getLeft(),ele.getBox().getTop(),ele.getBox().getWidth(),ele.getBox().getHeight());
			g2d.drawString(Integer.toString(iter.previousIndex())+(t!=null?ENVIRONMENT.getTranslation(t):""),ele.getBox().getLeft(),ele.getBox().getBottom());
			if(lines!=null){
				java.util.List<TextLine> paragraph=lines.get(ele);
				if(lines!=null){
					g2d.setColor(Color.GREEN);
					for(TextLine line:paragraph){
						g2d.drawRect(line.getBox().getLeft(),line.getBox().getTop(),line.getBox().getWidth(),line.getBox().getHeight());
						g2d.drawString(getAlignName(line.getAlignment()),line.getBox().getLeft(),line.getBox().getBottom());
					}
				}
			}
		}
	}
	private static String getAlignName(int align){
		switch(align){
			case TextLine.ALIGN_LEFT:
				return "L";
			case TextLine.ALIGN_RIGHT:
				return "R";
			case TextLine.ALIGN_CENTER:
				return "C";
			case TextLine.ALIGN_FULL:
				return "F";
			default:
				return "";
		}
	}
}
