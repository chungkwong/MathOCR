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
import com.github.chungkwong.mathocr.layout.logical.DocumentEncoders;
import com.github.chungkwong.mathocr.layout.logical.Document;
import com.github.chungkwong.mathocr.layout.logical.Page;
import java.awt.*;
import javax.swing.*;
/**
 *
 * @author Chan Chung Kwong
 */
public class OutputInspector extends Inspector<Document,Object,Page>{
	private Document document;
	@Override
	protected void onCreated(Document src){
		this.document=src;
		JComboBox<String> formats=new JComboBox<>(DocumentEncoders.REGISTRY.names().toArray(new String[0]));
		add(formats,BorderLayout.NORTH);
		JTextArea content=new JTextArea();
		add(new JScrollPane(content),BorderLayout.CENTER);
		formats.addActionListener((e)->{
			content.setText(DocumentEncoders.REGISTRY.get((String)formats.getSelectedItem()).encode(document));
		});
		formats.setSelectedIndex(0);
	}
	@Override
	protected void onReturned(Object val){
	}
}
