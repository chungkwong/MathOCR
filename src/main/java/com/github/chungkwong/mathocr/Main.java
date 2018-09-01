/* Main.java
 * =========================================================================
 * This file is originally part of the MathOCR Project
 *
 * Copyright (C) 2014,2015,2018 Chan Chung Kwong
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
package com.github.chungkwong.mathocr;
import static com.github.chungkwong.mathocr.Environment.ENVIRONMENT;
import com.github.chungkwong.mathocr.ui.*;
import java.io.*;
import java.util.*;
/**
 * Entrance of the program
 */
public final class Main{
	/**
	 * Entrance of the program
	 *
	 * @param args command line arguments
	 */
	public static void main(String[] args) throws Exception{
		if(args.length>=1&&args[0].equals("-cli")){
			System.err.println("MathOCR--A scientific document recognition system");
			System.err.println();
			System.err.println("Copyright (C) 2014,2015,2018 Chan Chung Kwong");
			System.err.println();
			System.err.println("MathOCR is free software: you can redistribute it and/or modify it ");
			System.err.println("under the terms of the GNU Affero General Public License as published by");
			System.err.println("the Free Software Foundation, either version 3 of the License, ");
			System.err.println("or (at your option) any later version.");
			System.err.println();
			int i=1;
			boolean memorize=false;
			while(i<args.length&&args[i].startsWith("-")){
				char c=args[i].charAt(1);
				int ind=args[i].indexOf("=");
				if(ind!=-1){
					String key=args[i].substring(2,ind), val=args[i].substring(ind+1);
					switch(c){
						case 's':
							ENVIRONMENT.setString(key,val);
							break;
						case 'b':
							ENVIRONMENT.setBoolean(key,Boolean.valueOf(val));
							break;
						case 'i':
							ENVIRONMENT.setInteger(key,Integer.valueOf(val));
							break;
						case 'f':
							ENVIRONMENT.setFloat(key,Float.valueOf(val));
							break;
					}
				}else if(c=='m'){
					memorize=true;
				}else if(c=='r'){
					ENVIRONMENT.clearPreference();
				}else{
					break;
				}
				++i;
			}
			if(memorize){
				ENVIRONMENT.saveAsPreference();
			}
			if(args.length==i){
				System.err.println("Usage: java -jar MathOCR.jar -cli [options] file...\nOptions:\n");
				System.err.println("  -r\tRestore all settings to default and discard all memorized parameters");
				System.err.println("  -bAPPLY_MEAN_FILTER=[true|false]");
				System.err.println("  -bAPPLY_MEDIAN_FILTER=[true|false]");
				System.err.println("  -bAPPLY_NOISE_FILTER=[true|false]");
				System.err.println("  -bAPPLY_KFILL_FILTER=[true|false]");
				System.err.println("  -bCLEAN_EDGES=[true|false]");
				System.err.println("  -bDETECT_INVERT=[true|false]");
				System.err.println("  -iKFILL_WINDOW=integer");
				System.err.println("  -iMANUAL_THREHOLD_LIMIT=integer");
				System.err.println("  -iSAUVOLA_WINDOW=integer");
				System.err.println("  -fSAUVOLA_WEIGHT=float");
				System.err.println("  -sTHREHOLD_METHOD=[Manual|Otsu|Sauvola]");
				System.err.println("  -sSKEW_DETECT_METHOD=[NONE|PPA|PCP|PP|TC|HT|NN|CC]");
				System.err.println("  -sOCR_ENGINE=[Native|Tesseract|Ocrad|GOCR]");
				System.err.println("  -sEXTRA_DATAFILE=file[:file[...]]");
				System.err.println("  -sTESSERACT_PARAMETER=param");
				System.err.println("  -sDATA_DIRECTORY=directory");
				System.err.println("  -sOUTPUT_FOLDER=directory");
				System.err.println("  -sOUTPUT_FORMAT=[LaTeX|HTML|Plain]");
				System.err.println("  -sCHARACTER_RECOGNIZER=LINEAR");
				System.err.println("  -sPAGE_ANALYZER=NAIVE");
				System.err.println("  -sDOCUMENT_ASSEMBLER=NAIVE");
				System.err.println("  -sBLOCK_CLASSIFIER=NAIVE");
				System.err.println("  -sPAGE_SEGMENTER=XY_CUT");
				System.err.println("  -sBLOCK_ORDERER=NAIVE");
				System.err.println("  -sCHARACTER_SEGMENTER=NAIVE");
				System.err.println("  -sLINE_ANALYZER=NAIVE");
				System.err.println("  -m\tMemorize parameters");
			}else{
				long time=System.currentTimeMillis();
				List<Object> files=new LinkedList<>();
				for(;i<args.length;i++){
					files.add(new File(args[i]));
				}
				System.out.println(new PipeLine(files.iterator()).getResult());
				System.err.println("Time used:"+(System.currentTimeMillis()-time)+"ms");
			}
			System.err.println();
		}else{
			new MainFrame();
		}
	}
}
