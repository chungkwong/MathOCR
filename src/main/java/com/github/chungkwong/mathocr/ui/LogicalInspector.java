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
import static com.github.chungkwong.mathocr.Environment.ENVIRONMENT;
import com.github.chungkwong.mathocr.common.*;
import com.github.chungkwong.mathocr.layout.logical.*;
import com.github.chungkwong.mathocr.text.structure.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.List;
import java.util.*;
import java.util.stream.*;
import javax.swing.*;
/**
 *
 * @author Chan Chung Kwong
 */
public class LogicalInspector extends Inspector<Pair<List<LogicalBlock>,BufferedImage>,Page,Page> implements ActionListener,MouseListener,IconPaint{
	private JButton del, modify, next;
	private JComboBox<String> type=new JComboBox<String>(new String[]{ENVIRONMENT.getTranslation("IMAGE"),ENVIRONMENT.getTranslation("TABLE"),ENVIRONMENT.getTranslation("TITLE"),
		ENVIRONMENT.getTranslation("AUTHOR"),ENVIRONMENT.getTranslation("PARAGRAPH"),ENVIRONMENT.getTranslation("LISTING"),ENVIRONMENT.getTranslation("HEADING"),ENVIRONMENT.getTranslation("CAPTION")});
	private JCheckBox noStart=new JCheckBox(ENVIRONMENT.getTranslation("NO_START"),false), noEnd=new JCheckBox(ENVIRONMENT.getTranslation("NO_END"),false);
	private JTextField additional=new JTextField();
	private LogicalBlock curr;
	private JTextArea area=new JTextArea();
	private PageIcon icon;
	private JToolBar bar=new JToolBar(JToolBar.VERTICAL);
	private List<LogicalBlock> logicalBlocks;
	public LogicalInspector(){
		del=addButton("DELETE");
		modify=addButton("MODIFY");
		bar.addSeparator();
		type.setAlignmentX(0);
		bar.add(type);
		area.setAlignmentX(0);
		area.setLineWrap(true);
		bar.add(area);
		noStart.setAlignmentX(0);
		bar.add(noStart);
		noEnd.setAlignmentX(0);
		bar.add(noEnd);
		additional.setAlignmentX(0);
		JLabel lab=new JLabel(ENVIRONMENT.getTranslation("ADDITIONAL_INFO"));
		lab.setAlignmentX(0);
		bar.add(lab);
		additional.setAlignmentX(0);
		bar.add(additional);
		bar.addSeparator();
		next=addButton("NEXT");
		add(new JScrollPane(bar),BorderLayout.EAST);
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
	protected void onCreated(Pair<List<LogicalBlock>,BufferedImage> src){
		this.logicalBlocks=src.getKey();
		icon=new PageIcon(src.getValue(),this);
		icon.getContent().addMouseListener(this);
		add(new JScrollPane(icon),BorderLayout.CENTER);
	}
	@Override
	protected void onReturned(Page val){
	}
	@Override
	public void actionPerformed(ActionEvent e){
		switch(e.getActionCommand()){
			case "DELETE":
				logicalBlocks.remove(curr);
				curr=null;
				setCurrent();
				icon.repaint();
				break;
			case "MODIFY":
				if(curr!=null){
					if(curr instanceof TextBlock&&type.getSelectedIndex()>=2){
						String content=area.getText();
						TextBlock updated=null;
						boolean nostart=noStart.isSelected(), noend=noEnd.isSelected();
						switch(type.getSelectedIndex()){
							case 2:
								updated=new Title((TextBlock)curr);
								break;
							case 3:
								updated=new Author((TextBlock)curr);
								break;
							case 4:
								updated=new Paragraph((TextBlock)curr);
								break;
							case 5:
								updated=new Listing(Listing.testItem(content),(TextBlock)curr);
								break;
							case 6:
								updated=new Heading(Integer.parseInt(additional.getText()),(TextBlock)curr);
								break;
							case 7:
								updated=new Caption((TextBlock)curr);
								break;
						}
						logicalBlocks.set(logicalBlocks.indexOf(curr),updated);
						curr=updated;
					}else if(curr instanceof com.github.chungkwong.mathocr.layout.logical.Image&&type.getSelectedIndex()==0){
						((com.github.chungkwong.mathocr.layout.logical.Image)curr).setCaption(Line.fromText(additional.getText()));
					}else if(curr instanceof Table&&type.getSelectedIndex()==1){
						((Table)curr).setCaption(Line.fromText(additional.getText()));
					}else{
						JOptionPane.showMessageDialog(null,ENVIRONMENT.getTranslation("NOT_ALLOWED"));
					}
				}
				icon.repaint();
				break;
			case "NEXT":
				ret(new Page(logicalBlocks));
				break;
		}
	}
	@Override
	public void mouseClicked(MouseEvent e){
	}
	@Override
	public void mousePressed(MouseEvent e){
		double scale=icon.getScale();
		int x=(int)(e.getX()/scale+0.5), y=(int)(e.getY()/scale+0.5);
		ListIterator<LogicalBlock> iter=logicalBlocks.listIterator();
		while(iter.hasNext()){
			LogicalBlock block=iter.next();
			if(x>=block.getBox().getLeft()&&x<=block.getBox().getRight()
					&&y<=block.getBox().getBottom()&&y>=block.getBox().getTop()){
				curr=block;
			}
		}
		setCurrent();
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
		ListIterator<LogicalBlock> iter=logicalBlocks.listIterator();
		while(iter.hasNext()){
			LogicalBlock block=iter.next();
			Rectangle rect=new Rectangle(block.getBox().getLeft(),block.getBox().getTop(),block.getBox().getWidth(),block.getBox().getHeight());
			if(block==curr){
				g2d.setColor(Color.RED);
				//pageEdit.getPageIcon().scrollRectToVisible(rect);
			}else{
				g2d.setColor(Color.BLUE);
			}
			g2d.draw(rect);
			g2d.drawString(Integer.toString(iter.previousIndex())+type.getItemAt(getTypeNumber(block)),block.getBox().getLeft(),block.getBox().getBottom());
		}
	}
	private static int getTypeNumber(LogicalBlock block){
		if(block instanceof com.github.chungkwong.mathocr.layout.logical.Image){
			return 0;
		}else if(block instanceof Table){
			return 1;
		}else if(block instanceof Title){
			return 2;
		}else if(block instanceof Author){
			return 3;
		}else if(block instanceof Paragraph){
			return 4;
		}else if(block instanceof Listing){
			return 5;
		}else if(block instanceof Heading){
			return 6;
		}else if(block instanceof Caption){
			return 7;
		}
		return -1;
	}
	private void setCurrent(){
		if(curr==null){
			area.setText("");
			additional.setText("");
		}else{
			if(curr instanceof com.github.chungkwong.mathocr.layout.logical.Image){
				type.setSelectedIndex(0);
				area.setText(((com.github.chungkwong.mathocr.layout.logical.Image)curr).getPath());
				additional.setText(toString(((com.github.chungkwong.mathocr.layout.logical.Image)curr).getCaption()));
			}else if(curr instanceof Table){
				type.setSelectedIndex(1);
				area.setText(((Table)curr).getPath());
				additional.setText(toString(((Table)curr).getCaption()));
			}else if(curr instanceof Title){
				type.setSelectedIndex(2);
				additional.setText("");
			}else if(curr instanceof Author){
				type.setSelectedIndex(3);
				additional.setText("");
			}else if(curr instanceof Paragraph){
				type.setSelectedIndex(4);
				additional.setText("");
			}else if(curr instanceof Listing){
				type.setSelectedIndex(5);
				additional.setText(Integer.toString(((Listing)curr).getIndent()));
			}else if(curr instanceof Heading){
				type.setSelectedIndex(6);
				additional.setText(Integer.toString(((Heading)curr).getFontSize()));
			}else if(curr instanceof Caption){
				type.setSelectedIndex(7);
				additional.setText("");
			}
			if(curr instanceof TextBlock){
				area.setText(toString(((TextBlock)curr).getLines()));
				noStart.setSelected(((TextBlock)curr).isNoStart());
				noEnd.setSelected(((TextBlock)curr).isNoEnd());
			}
		}
	}
	private static String toString(java.util.List<Line> lines){
		if(lines==null){
			return "";
		}else{
			return lines.stream().map(Line::toString).collect(Collectors.joining("\n"));
		}
	}
}
