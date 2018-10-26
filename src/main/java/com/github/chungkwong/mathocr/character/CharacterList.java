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
package com.github.chungkwong.mathocr.character;
import com.github.chungkwong.mathocr.common.BoundBox;
import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;
/**
 *
 * @author Chan Chung Kwong
 */
public class CharacterList{
	private final List<CharacterPrototype> prototypes;
	private Map<Integer,CharacterPrototype> index;
	public CharacterList(List<CharacterPrototype> prototypes){
		this.prototypes=prototypes;
	}
	public List<CharacterPrototype> getCharacters(){
		return prototypes;
	}
	public CharacterPrototype getCharacter(int codePoint){
		if(index==null){
			index=prototypes.stream().collect(Collectors.toMap((p)->p.getCodePoint(),(p)->p));
		}
		return index.get(codePoint);
	}
	public void write(File file) throws IOException{
		BufferedWriter out=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file),StandardCharsets.UTF_8));
		for(CharacterPrototype prototype:prototypes){
			out.write(Integer.toString(prototype.getCodePoint()));
			out.write('\t');
			out.write(Integer.toString(prototype.getBox().getLeft()));
			out.write('\t');
			out.write(Integer.toString(prototype.getBox().getRight()));
			out.write('\t');
			out.write(Integer.toString(prototype.getBox().getTop()));
			out.write('\t');
			out.write(Integer.toString(prototype.getBox().getBottom()));
			out.write('\t');
			out.write(prototype.getFamily());
			out.write('\t');
			out.write(Integer.toString(prototype.getFontSize()));
			out.write('\t');
			out.write(Integer.toString(prototype.getStyle()));
			out.write('\n');
		}
		out.close();
	}
	public static CharacterList read(File file) throws IOException{
		ArrayList<CharacterPrototype> chars=new ArrayList<>();
		Files.lines(file.toPath()).forEach((l)->{
			chars.add(parseCharacterPrototype(l));
		});
		return new CharacterList(chars);
	}
	private static CharacterPrototype parseCharacterPrototype(String line){
		String[] split=line.split("\t");
		BoundBox box=new BoundBox(Integer.parseInt(split[1]),Integer.parseInt(split[2]),Integer.parseInt(split[3]),Integer.parseInt(split[4]));
		return new CharacterPrototype(Integer.parseInt(split[0]),box,split[5],Integer.parseInt(split[6]),Integer.parseInt(split[7]),true);
	}
}
