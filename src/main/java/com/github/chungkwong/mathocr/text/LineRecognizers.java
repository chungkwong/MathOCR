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
package com.github.chungkwong.mathocr.text;
import com.github.chungkwong.mathocr.Registry;
import com.github.chungkwong.mathocr.text.LineRecognizer;
import static com.github.chungkwong.mathocr.Environment.ENVIRONMENT;
/**
 *
 * @author Chan Chung Kwong
 */
public class LineRecognizers{
	public static final Registry<LineRecognizer> REGISTRY=new Registry<>("OCR_ENGINE",new BuiltinLineRecognizer());
	static{
		REGISTRY.register("Native",new BuiltinLineRecognizer());
		REGISTRY.register("Ocrad",new ExternalLineRecognizer("ocrad ",""));
		REGISTRY.register("Tesseract",new ExternalLineRecognizer("tesseract "," stdout "+ENVIRONMENT.getString("TESSERACT_PARAMETER")));
		REGISTRY.register("GOCR",new ExternalLineRecognizer("gocr ",""));
		REGISTRY.register("Baidu",new BaiduLineRecognizer());
	}
}
