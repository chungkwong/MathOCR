/* Main.java
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
package net.sf.mathocr;
import java.io.*;
import net.sf.mathocr.gui.*;
import net.sf.mathocr.layout.*;
import static net.sf.mathocr.Environment.env;
/**
 * Entrance of the program
 */
public final class Main{
	static{
		javax.imageio.spi.IIORegistry.getDefaultInstance().registerServiceProvider(new net.sf.mathocr.common.PNMImageReaderSpi());
		javax.imageio.spi.IIORegistry.getDefaultInstance().registerServiceProvider(new net.sf.mathocr.common.PNMImageWriterSpi());
		net.sf.mathocr.ocr.DataBase.DEFAULT_DATABASE.toString();
	}
	/**
	 * Entrance of the program
	 * @param args command line arguments
	 */
	public static void main(String[] args) throws Exception{
		if(args.length>=1&&args[0].equals("-cli")){
			System.err.println("MathOCR--A scientific document recognition system");
			System.err.println();
			System.err.println("Copyright (C) 2014,2015 Chan Chung Kwong");
			System.err.println();
			System.err.println("MathOCR is free software: you can redistribute it and/or modify it ");
			System.err.println("under the terms of the GNU General Public License as published by");
			System.err.println("the Free Software Foundation, either version 3 of the License, ");
			System.err.println("or (at your option) any later version.");
			System.err.println();
			int i=1;
			boolean memorize=false,expr=false;
			while(i<args.length&&args[i].startsWith("-")){
				char c=args[i].charAt(1);
				int ind=args[i].indexOf("=");
				if(ind!=-1){
					String key=args[i].substring(2,ind),val=args[i].substring(ind+1);
					switch(c){
						case 's':
							env.setString(key,val);
							break;
						case 'b':
							env.setBoolean(key,Boolean.valueOf(val));
							break;
						case 'i':
							env.setInteger(key,Integer.valueOf(val));
							break;
						case 'f':
							env.setFloat(key,Float.valueOf(val));
							break;
					}
				}else if(c=='m')
					memorize=true;
				else if(c=='r')
					env.clearPreference();
				else if(c=='e')
					expr=true;
				else
					break;
				++i;
			}
			if(memorize){
				env.saveAsPreference();
			}
			if(args.length==i){
				System.err.println("Usage: java -jar MathOCR.jar -cli [options] file...\nOptions:\n");
				System.err.println("  -e\tRecognize mathematical expression(s) instead of a document");
				System.err.println("  -r\tRestore all settings to default and discard all memorized parameters");
				System.err.println("  -bAPPLY_MEAN_FILTER=[true|false]");
				System.err.println("  -bAPPLY_MEDIAN_FILTER=[true|false]");
				System.err.println("  -sTHREHOLD_METHOD=[Manual|Otsu|Sauvola]");
				System.err.println("  -iMANUAL_THREHOLD_LIMIT=integer");
				System.err.println("  -iSAUVOLA_WINDOW=integer");
				System.err.println("  -fSAUVOLA_WEIGHT=float");
				System.err.println("  -bAPPLY_KFILL_FILTER=[true|false]");
				System.err.println("  -iKFILL_WINDOW=integer");
				System.err.println("  -bAPPLY_NOISE_FILTER=[true|false]");
				System.err.println("  -bDETECT_INVERT=[true|false]");
				System.err.println("  -sSKEW_DETECT_METHOD=[NONE|PPA|PCP|PP|TC|HT|NN|CC]");
				System.err.println("  -bCLEAN_EDGES=[true|false]");
				System.err.println("  -sOCR_ENGINE=[Native|Tesseract|Ocrad|GOCR]");
				System.err.println("  -sEXTRA_DATAFILE=file[:file[...]]");
				System.err.println("  -sTESSERACT_PARAMETER=param");
				System.err.println("  -sOUTPUT_FOLDER=directory");
				System.err.println("  -sOUTPUT_FORMAT=[LaTeX|HTML]");
				System.err.println("  -m\tMemorize parameters");
			}else{
				long time=System.currentTimeMillis();
				if(expr){
					for(;i<args.length;i++){
						System.out.println(BatchProcessor.recognizeFormula(javax.imageio.ImageIO.read(new File(args[i]))));
					}
				}else{
					Document doc=new Document();
					for(;i<args.length;i++){
						doc.addPage(new Page(new File(args[i]),doc));
					}
					System.out.println(BatchProcessor.recognize(doc,env.getString("OUTPUT_FORMAT").toUpperCase().equals("LATEX")?BatchProcessor.LATEX:BatchProcessor.HTML));
				}
				System.err.println("Time used:"+(System.currentTimeMillis()-time)+"ms");
			}
			System.err.println();
		}else{
			new MainFrame();
		}
	}
}