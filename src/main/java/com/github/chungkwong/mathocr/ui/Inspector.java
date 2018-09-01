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
import java.awt.*;
import javax.swing.*;
/**
 *
 * @author Chan Chung Kwong
 */
public abstract class Inspector<S,T,R> extends JPanel{
	private JPanel pane;
	private Inspector<?,R,?> parent;
	protected Inspector(){
		super(new BorderLayout());
	}
	protected Inspector(JPanel pane){
		super(new BorderLayout());
		this.pane=pane;
	}
	protected abstract void onCreated(S src);
	protected abstract void onReturned(T val);
	protected <X> void call(Inspector<X,?,T> inspector,X val){
		inspector.pane=pane;
		inspector.parent=this;
		inspector.onCreated(val);
		pane.remove(this);
		pane.add(inspector,BorderLayout.CENTER);
		pane.validate();
	}
	protected void ret(R val){
		parent.onReturned(val);
		pane.remove(this);
		pane.add(parent,BorderLayout.CENTER);
		pane.validate();
		pane.repaint();
	}
}
