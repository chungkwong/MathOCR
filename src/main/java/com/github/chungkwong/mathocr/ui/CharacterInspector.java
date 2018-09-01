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
import com.github.chungkwong.mathocr.text.LineAnalyzers;
import com.github.chungkwong.mathocr.text.CharacterSegmenters;
import com.github.chungkwong.mathocr.text.TextLine;
import com.github.chungkwong.mathocr.layout.logical.Page;
import com.github.chungkwong.mathocr.common.Pair;
import com.github.chungkwong.mathocr.common.BoundBox;
import com.github.chungkwong.mathocr.character.CharacterPrototype;
import com.github.chungkwong.mathocr.character.CharacterCandidate;
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
public class CharacterInspector extends Inspector<Pair<TextLine,BufferedImage>,Page,Page> implements ActionListener,MouseListener,MouseMotionListener,IconPaint{
	private List<List<NavigableSet<CharacterCandidate>>> candidates;
	private JButton del, modify, add, delRow, addRow, next;
	private char status='N';
	private int lastx=-1, lasty;
	private PageIcon icon;
	private JSpinner index;
	private BoundBox box;
	private JToolBar bar=new JToolBar(JToolBar.VERTICAL);
	public CharacterInspector(){
		bar.add(new JLabel(ENVIRONMENT.getTranslation("CHAR_RECOGNITION")));
		del=addButton("DELETE");
		modify=addButton("MODIFY");
		add=addButton("ADD");
		bar.addSeparator();
		delRow=addButton("DELETE_SEGMENT");
		addRow=addButton("ADD_SEGMENT");
		bar.addSeparator();
		next=addButton("NEXT");
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
	protected void onCreated(Pair<TextLine,BufferedImage> src){
		box=src.getKey().getBox();
		candidates=CharacterSegmenters.REGISTRY.get().segment(src.getKey());
		icon=new PageIcon(src.getValue(),this);
		icon.getContent().addMouseListener(this);
		icon.getContent().addMouseMotionListener(this);
		index=new JSpinner(new SpinnerNumberModel(0,0,candidates.size()-1,1));
		index.addChangeListener((e)->icon.repaint());
		bar.add(index);
		add(new JScrollPane(icon),BorderLayout.CENTER);
	}
	@Override
	protected void onReturned(Page val){
		ret(null);
	}
	@Override
	public void actionPerformed(ActionEvent e){
		lastx=-1;
		status='N';
		int i=((Number)index.getValue()).intValue();
		switch(e.getActionCommand()){
			case "DELETE":
				status='D';
				break;
			case "MODIFY":
				status='M';
				break;
			case "ADD":
				status='A';
				break;
			case "Add SEGMENT":
				candidates.add(i,new ArrayList<>());
				if(candidates.size()>1){
					((SpinnerNumberModel)index.getModel()).setMaximum(candidates.size()+1);
				}
				icon.repaint();
				break;
			case "DELETE SEGMENT":
				candidates.remove(i);
				if(i>0){
					index.setValue(i-1);
				}
				if(!candidates.isEmpty()){
					((SpinnerNumberModel)index.getModel()).setMaximum(candidates.size()-1);
				}
				break;
			case "NEXT":
				JOptionPane.showMessageDialog(null,LineAnalyzers.REGISTRY.get().analysis(candidates).toString());
				ret(null);
				return;
		}
		icon.repaint();
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
		if(status=='A'){
			if(lastx==-1){
				lastx=x;
				lasty=y;
			}else{
				TreeSet<CharacterCandidate> node=new TreeSet<>();
				node.add(selectSymbol().toCandidate(new BoundBox(Math.min(lastx,x),Math.max(lastx,x),Math.min(lasty,y),Math.max(lasty,y)),1.0));
				getCandidate().add(node);
				lastx=-1;
				icon.repaint();
			}
			return;
		}
		NavigableSet<CharacterCandidate> cand=null;
		for(NavigableSet<CharacterCandidate> ele:getCandidate()){
			if(ele.isEmpty()){
				continue;
			}
			CharacterCandidate c=ele.first();
			if(x>=c.getBox().getLeft()&&x<=c.getBox().getRight()&&y<=c.getBox().getBottom()&&y>=c.getBox().getTop()){
				if(cand==null||c.getBox().getHeight()*c.getBox().getWidth()<cand.first().getBox().getHeight()*cand.first().getBox().getWidth()){
					cand=ele;
				}
			}
		}
		if(cand==null){
			return;
		}
		if(status=='D'){
			getCandidate().remove(cand);
		}else if(status=='M'){
			CharacterCandidate c=selectSymbol().toCandidate(cand.first().getBox(),1.0);
			cand.clear();
			cand.add(c);
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
	public void mouseDragged(MouseEvent e){
	}
	@Override
	public void mouseMoved(MouseEvent e){
		double scale=icon.getScale();
		int x=(int)(e.getX()/scale+0.5), y=(int)(e.getY()/scale+0.5);
		BoundBox cand=null;
		NavigableSet<CharacterCandidate> candidate=null;
		for(NavigableSet<CharacterCandidate> ele:getCandidate()){
			BoundBox b=ele.first().getBox();
			if(x>=b.getLeft()&&x<=b.getRight()&&y<=b.getBottom()&&y>=b.getTop()){
				if(cand==null||b.getHeight()*b.getWidth()<cand.getHeight()*cand.getWidth()){
					cand=b;
					candidate=ele;
				}
			}
		}
		if(candidate!=null){
			icon.getContent().setToolTipText(candidate.stream().map((c)->c.toString()).collect(Collectors.joining(";")));
		}else{
			icon.getContent().setToolTipText("");
		}
	}
	@Override
	public void paintOn(Graphics2D g2d){
		g2d.setColor(Color.RED);
		g2d.drawRect(box.getLeft(),box.getTop(),box.getWidth()+1,box.getHeight()+1);
		for(NavigableSet<CharacterCandidate> ele:getCandidate()){
			if(ele.isEmpty()){
				continue;
			}
			CharacterCandidate c=ele.first();
			if(c.getScore()>0.9){
				g2d.setColor(Color.BLUE);
			}else{
				g2d.setColor(Color.GREEN);
			}
			g2d.drawLine(c.getBox().getLeft(),c.getBaseLine(),c.getBox().getRight(),c.getBaseLine());
			g2d.drawLine(c.getBox().getLeft(),c.getBox().getTop(),c.getBox().getLeft(),c.getBox().getBottom());
		}
	}
	private List<NavigableSet<CharacterCandidate>> getCandidate(){
		return candidates.isEmpty()?Collections.emptyList():candidates.get(((Number)index.getValue()).intValue());
	}
	public CharacterPrototype selectSymbol(){
		final JDialog dia=new JDialog();
		dia.setLayout(new BorderLayout());
		Box fields=Box.createVerticalBox();
		JTextField code=new JTextField();
		JFormattedTextField ph=new JFormattedTextField((Integer)30), pw=new JFormattedTextField((Integer)20), ya=new JFormattedTextField((Integer)25);
		FontChooser fontChooser=new FontChooser();
		fields.add(new JLabel(ENVIRONMENT.getTranslation("LATEX_CODE")));
		fields.add(code);
		fields.add(new JLabel(ENVIRONMENT.getTranslation("PIXEL_WIDTH")));
		fields.add(pw);
		fields.add(new JLabel(ENVIRONMENT.getTranslation("PIXEL_HEIGHT")));
		fields.add(ph);
		fields.add(new JLabel(ENVIRONMENT.getTranslation("PIXEL_ASCENT")));
		fields.add(ya);
		fields.add(new JLabel(ENVIRONMENT.getTranslation("FONT")));
		fields.add(fontChooser);
		dia.add(fields,BorderLayout.CENTER);
		JButton confirm=new JButton(ENVIRONMENT.getTranslation("CONFIRM"));
		confirm.addActionListener((ActionEvent e)->{
			dia.setVisible(false);
		});
		dia.add(confirm,BorderLayout.SOUTH);
		dia.pack();
		dia.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		dia.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		dia.setVisible(true);
		Font font=fontChooser.getFont();
		return new CharacterPrototype(code.getText().codePointAt(0),
				new BoundBox(0,(Integer)pw.getValue()-1,-(Integer)ya.getValue(),-(Integer)ya.getValue()+(Integer)ph.getValue()-1),
				font.getFamily(),font.getSize(),font.getStyle());
	}
}
