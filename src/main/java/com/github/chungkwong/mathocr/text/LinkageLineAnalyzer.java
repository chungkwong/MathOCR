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
import com.github.chungkwong.mathocr.common.*;
import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;
import libsvm.*;
/**
 *
 * @author Chan Chung Kwong
 */
public class LinkageLineAnalyzer{
	public static final int TOP=-1, HORIZONTAL=0, UPPER=1, UNDER=2, LSUP=5, LSUB=6, RSUB=3, RSUP=4;
	private static final String TRAIN_FILE="../datasets/InftyCDB-1-6152/InftyCDB-1/InftyCDB-1/InftyCDB-1.csv";
	private static final String LINK_MODEL="src/main/resources/com/github/chungkwong/mathocr/resources/linkage.svm";
	private static svm_model model;
	private static final double[] PROB={0.946107,0.002908,0.008180,0.026862,0.015943};
	private static final double[] RATIO_MEAN={1.0366191947961425,0.5075539376128319,1.1592813323220512,0.5965849629835016,0.5667408231368187};
	private static final double[] RATIO_STDVAR={0.5354560756066058,0.33639313693231415,0.9137015496965221,0.1945078864775817,0.0022246941045456145};
	private static final double[] DTOP_MEAN={-0.013771624076702538,-0.6820104199085981,0.5505914882728324,-0.27788727408558794,0.6087319243604005};
	private static final double[] DTOP_STDVAR={0.45521882017818616,0.4559496441806732,0.4004813363496435,0.2292923576083536,0.0119577308120133};
	private static final double[] DBOTTOM_MEAN={0.017605016549669793,-0.4327534671340412,1.3256434439947695,-0.5877558559196694,0.1996662958843159};
	private static final double[] DBOTTOM_STDVAR={0.2504276404846458,0.3736496295191759,0.9811144197884054,0.4128384576466331,0.010011123470522843};
	public static int classifyLinkage(BoundBox box,BoundBox parentBox){
		if(box.getLeft()<=parentBox.getRight()&&parentBox.getLeft()<=parentBox.getRight()){
			if(Math.min(box.getRight(),parentBox.getRight())-Math.max(box.getLeft(),parentBox.getLeft())>Math.min(parentBox.getWidth(),box.getWidth())/2){
				return box.getBottom()>parentBox.getBottom()?UPPER:UNDER;
			}
		}
		int scale=Math.max(parentBox.getWidth(),parentBox.getHeight());
		if(Math.max(box.getWidth(),box.getHeight())<scale*0.8){
			if(box.getTop()-parentBox.getTop()>0.55*scale){
				return RSUP;
			}
			if(box.getBottom()-parentBox.getBottom()<-0.5*scale){
				return RSUB;
			}
		}
		return HORIZONTAL;
	}
	/*
	public static int classifyLinkage(BoundBox box,BoundBox parentBox){
		double scale=1.0/Math.max(parentBox.getWidth(),parentBox.getHeight());
		double ratio=Math.max(box.getWidth(),box.getHeight())*scale;
		double dtop=(box.getTop()-parentBox.getTop())*scale;
		double dbottom=(box.getBottom()-parentBox.getBottom())*scale;
		double[][] probC=new double[][]{
			{2*getP((ratio-RATIO_MEAN[0])/RATIO_STDVAR[0]),2*getP((dtop-DTOP_MEAN[0])/DTOP_STDVAR[0]),2*getP((dbottom-DBOTTOM_MEAN[0])/DBOTTOM_STDVAR[0])},
			{2*getP((ratio-RATIO_MEAN[1])/RATIO_STDVAR[1]),getP((dtop-DTOP_MEAN[1])/DTOP_STDVAR[1]),getP((dbottom-DBOTTOM_MEAN[1])/DBOTTOM_STDVAR[1])},
			{2*getP((ratio-RATIO_MEAN[2])/RATIO_STDVAR[2]),1-getP((dtop-DTOP_MEAN[2])/DTOP_STDVAR[2]),1-getP((dbottom-DBOTTOM_MEAN[2])/DBOTTOM_STDVAR[2])},
			{getP((ratio-RATIO_MEAN[3])/RATIO_STDVAR[3]),getP((dtop-DTOP_MEAN[3])/DTOP_STDVAR[3]),getP((dbottom-DBOTTOM_MEAN[3])/DBOTTOM_STDVAR[3])},
			{getP((ratio-RATIO_MEAN[4])/RATIO_STDVAR[4]),1-getP((dtop-DTOP_MEAN[4])/DTOP_STDVAR[4]),1-getP((dbottom-DBOTTOM_MEAN[4])/DBOTTOM_STDVAR[4])},};
		for(int i=0;i<5;i++){
			for(int j=0;j<3;j++){
				probC[i][j]*=PROB[i];
			}
		}
		double[] prob={1,1,1,1,1};
		for(int j=0;j<3;j++){
			double sum=0;
			for(int i=0;i<5;i++){
				sum+=probC[i][j];
			}
			for(int i=0;i<5;i++){
				prob[i]*=probC[i][j]/sum;
			}
		}
		double max=0;
		int index=7;
		for(int i=0;i<5;i++){
			if(prob[i]>max){
				max=prob[i];
				index=i;
			}
		}
		return index;
	}
	 */
	private static final double[] P=new double[]{
		0.50000,0.49601,0.49202,0.48803,0.48405,0.48006,0.47608,0.47210,0.46812,0.46414,
		0.46017,0.45620,0.45224,0.44828,0.44433,0.44034,0.43640,0.43251,0.42858,0.42465,
		0.42074,0.41683,0.41294,0.40905,0.40517,0.40129,0.39743,0.39358,0.38974,0.38591,
		0.38209,0.37828,0.37448,0.37070,0.36693,0.36317,0.35942,0.35569,0.35197,0.34827,
		0.34458,0.34090,0.33724,0.33360,0.32997,0.32636,0.32276,0.31918,0.31561,0.31207,
		0.30854,0.30503,0.30153,0.29806,0.29460,0.29116,0.28774,0.28434,0.28096,0.27760,
		0.27425,0.27093,0.26763,0.26435,0.26109,0.25785,0.25463,0.25143,0.24825,0.24510,
		0.24196,0.23885,0.23576,0.23270,0.22965,0.22663,0.22363,0.22065,0.21770,0.21476,
		0.21186,0.20897,0.20611,0.20327,0.20045,0.19766,0.19489,0.19215,0.18943,0.18673,
		0.18406,0.18141,0.17879,0.17619,0.17361,0.17106,0.16853,0.16602,0.16354,0.16109,
		0.15866,0.15625,0.15386,0.15151,0.14917,0.14686,0.14457,0.14231,0.14007,0.13786,
		0.13567,0.13350,0.13136,0.12924,0.12714,0.12507,0.12302,0.12100,0.11900,0.11702,
		0.11507,0.11314,0.11123,0.10935,0.10749,0.10565,0.10383,0.10204,0.10027,0.09853,
		0.09680,0.09510,0.09342,0.09176,0.09012,0.08851,0.08692,0.08534,0.08379,0.08226,
		0.08076,0.07927,0.07780,0.07636,0.07493,0.07353,0.07215,0.07078,0.06944,0.06811,
		0.06681,0.06552,0.06426,0.06301,0.06178,0.06057,0.05938,0.05821,0.05705,0.05592,
		0.05480,0.05370,0.05262,0.05155,0.05050,0.04947,0.04846,0.04746,0.04648,0.04551,
		0.04457,0.04363,0.04272,0.04182,0.04093,0.04006,0.03920,0.03836,0.03754,0.03673,
		0.03593,0.03515,0.03438,0.03362,0.03288,0.03216,0.03144,0.03074,0.03005,0.02938,
		0.02872,0.02807,0.02743,0.02680,0.02619,0.02559,0.02500,0.02442,0.02385,0.02330,
		0.02275,0.02222,0.02169,0.02118,0.02068,0.02018,0.01970,0.01923,0.01876,0.01831,
		0.01786,0.01743,0.01700,0.01659,0.01618,0.01578,0.01539,0.01500,0.01463,0.01426,
		0.01390,0.01355,0.01321,0.01287,0.01255,0.01222,0.01191,0.01160,0.01130,0.01101,
		0.01072,0.01044,0.01017,0.00990,0.00964,0.00939,0.00914,0.00889,0.00866,0.00842,
		0.00820,0.00798,0.00776,0.00755,0.00734,0.00714,0.00695,0.00676,0.00657,0.00639,
		0.00621,0.00604,0.00587,0.00570,0.00554,0.00539,0.00523,0.00508,0.00494,0.00480,
		0.00466,0.00453,0.00440,0.00427,0.00415,0.00402,0.00391,0.00379,0.00368,0.00357,
		0.00347,0.00336,0.00326,0.00317,0.00307,0.00298,0.00289,0.00280,0.00272,0.00264,
		0.00256,0.00248,0.00240,0.00233,0.00226,0.00219,0.00212,0.00205,0.00199,0.00193,
		0.00187,0.00181,0.00175,0.00169,0.00164,0.00159,0.00154,0.00149,0.00144,0.00139,
		0.00135,0.00131,0.00126,0.00122,0.00118,0.00114,0.00111,0.00107,0.00104,0.00100,
		0.00097,0.00094,0.00090,0.00087,0.00084,0.00082,0.00079,0.00076,0.00074,0.00071,
		0.00069,0.00066,0.00064,0.00062,0.00060,0.00058,0.00056,0.00054,0.00052,0.00050,
		0.00048,0.00047,0.00045,0.00043,0.00042,0.00040,0.00039,0.00038,0.00036,0.00035,
		0.00034,0.00032,0.00031,0.00030,0.00029,0.00028,0.00027,0.00026,0.00025,0.00024,
		0.00023,0.00022,0.00022,0.00021,0.00020,0.00019,0.00019,0.00018,0.00017,0.00017,
		0.00016,0.00015,0.00015,0.00014,0.00014,0.00013,0.00013,0.00012,0.00012,0.00011,
		0.00011,0.00010,0.00010,0.00010,0.00009,0.00009,0.00008,0.00008,0.00008,0.00008,
		0.00007,0.00007,0.00007,0.00006,0.00006,0.00006,0.00006,0.00005,0.00005,0.00005,
		0.00005,0.00005,0.00004,0.00004,0.00004,0.00004,0.00004,0.00004,0.00003,0.00003,
		0.00003,0.00003,0.00003,0.00003,0.00003,0.00003,0.00002,0.00002,0.00002,0.00002
	};
	private static double getP(double z){
		if(z>=0){
			int i=(int)(z*100);
			return i<P.length?P[i]:0;
		}else{
			return 1-getP(-z);
		}
	}
	public static void trainLinkageClassifier() throws IOException{
		CsvParser parser=new CsvParser(Files.lines(new File(TRAIN_FILE).toPath(),StandardCharsets.ISO_8859_1),false);
		class Record{
			String type, link;
			boolean region, baseline, italic, bold;
			int width, height, parent, left, top, right, bottom;
		}
		int recordCount=688569+1;
		int fieldCount=6;
		ArrayList<Record> records=new ArrayList<>(recordCount);
		for(int i=0;i<recordCount;i++){
			records.add(null);
		}
		while(parser.hasNext()){
			List<String> row=parser.next();
			int index=Integer.parseInt(row.get(0));
			Record record=new Record();
			record.type=row.get(3);
			record.region=row.get(6).equals("text");
			record.baseline=row.get(7).equals("1");
			record.italic=row.get(8).equals("1");
			record.bold=row.get(9).equals("1");
			record.width=Integer.parseInt(row.get(11));
			record.height=Integer.parseInt(row.get(12));
			record.parent=Integer.parseInt(row.get(13));
			record.link=row.get(14).intern();
			record.left=Integer.parseInt(row.get(16));
			record.top=Integer.parseInt(row.get(17));
			record.right=Integer.parseInt(row.get(18));
			record.bottom=Integer.parseInt(row.get(19));
			records.set(index,record);
		}
		double[][] sum=new double[7][fieldCount];
		double[][] sqsum=new double[7][fieldCount];
		int[] count=new int[7];
		for(Record record:records){
			if(record==null){
				continue;
			}
			if(record.parent!=-1){
				Record parentRecord=records.get(record.parent);
				if(parentRecord==null){
					System.out.println(record.parent);
					continue;
				}
				int t=getLinkCode(record.link);
				++count[t];
				double scale=1.0/Math.max(parentRecord.width,parentRecord.height);
				double tmp=Math.max(record.width,record.height)*scale;
				sum[t][0]+=tmp;
				sqsum[t][0]+=tmp*tmp;
				tmp=(record.top-parentRecord.top)*scale;
				sum[t][1]+=tmp;
				sqsum[t][1]+=tmp*tmp;
				tmp=(record.bottom-parentRecord.bottom)*scale;
				sum[t][2]+=tmp;
				sqsum[t][2]+=tmp*tmp;
				tmp=(record.left-parentRecord.left)*scale;
				sum[t][3]+=tmp;
				sqsum[t][3]+=tmp*tmp;
				tmp=(record.top-parentRecord.bottom)*scale;
				sum[t][4]+=tmp;
				sqsum[t][4]+=tmp*tmp;
				tmp=(record.bottom-parentRecord.top)*scale;
				sum[t][5]+=tmp;
				sqsum[t][5]+=tmp*tmp;
			}
		}
		for(int j=0;j<fieldCount;j++){
			for(int i=0;i<7;i++){
				double mean=sum[i][j]/count[i];
				double var=Math.sqrt(sqsum[i][j]/count[i]-mean*mean);
				System.out.println(mean+"("+var+")\t");
			}
			System.out.println();
		}
	}
	public static void testLinkageClassifier() throws IOException{
		CsvParser parser=new CsvParser(Files.lines(new File(TRAIN_FILE).toPath(),StandardCharsets.ISO_8859_1),false);
		class Record{
			String type, link;
			boolean region, baseline, italic, bold;
			int width, height, parent, left, top, right, bottom;
		}
		int recordCount=688569+1;
		int fieldCount=6;
		ArrayList<Record> records=new ArrayList<>(recordCount);
		for(int i=0;i<recordCount;i++){
			records.add(null);
		}
		HashSet<String> ent=new HashSet<>();
		while(parser.hasNext()){
			List<String> row=parser.next();
			int index=Integer.parseInt(row.get(0));
			Record record=new Record();
			ent.add(row.get(5));
			record.type=row.get(3);
			record.region=row.get(6).equals("text");
			record.baseline=row.get(7).equals("1");
			record.italic=row.get(8).equals("1");
			record.bold=row.get(9).equals("1");
			record.width=Integer.parseInt(row.get(11));
			record.height=Integer.parseInt(row.get(12));
			record.parent=Integer.parseInt(row.get(13));
			record.link=row.get(14).intern();
			record.left=Integer.parseInt(row.get(16));
			record.top=Integer.parseInt(row.get(17));
			record.right=Integer.parseInt(row.get(18));
			record.bottom=Integer.parseInt(row.get(19));
			records.set(index,record);
		}
		System.out.println(ent);
		int[][] matrix=new int[8][8];
		for(Record record:records){
			if(record==null){
				continue;
			}
			if(record.parent!=-1){
				Record parentRecord=records.get(record.parent);
				if(parentRecord==null){
					System.out.println(record.parent);
					continue;
				}
				int actual=getLinkCode(record.link);
				BoundBox box=new BoundBox(record.left,record.right,-record.top,-record.bottom);
				BoundBox parentBox=new BoundBox(parentRecord.left,parentRecord.right,-parentRecord.top,-parentRecord.bottom);
				int detected=classifyLinkage(box,parentBox);
				++matrix[actual][detected];
			}
		}
		for(int i=0;i<8;i++){
			for(int j=0;j<8;j++){
				System.out.print(matrix[i][j]+"\t");
			}
			System.out.println();
		}
	}
	private static int getLinkCode(String link){
		switch(link){
			case "TOP":
				return TOP;
			case "HORIZONTAL":
				return HORIZONTAL;
			case "UPPER":
				return UPPER;
			case "UNDER":
				return UNDER;
			case "LSUP":
				return LSUP;
			case "LSUB":
				return LSUB;
			case "RSUB":
				return RSUB;
			case "RSUP":
				return RSUP;
			default:
				return -2;
		}
	}
	public static void main(String[] args) throws IOException{
		testLinkageClassifier();
		/*CsvParser parser=new CsvParser(Files.lines(new File(TRAIN_FILE).toPath(),StandardCharsets.ISO_8859_1),false);
		int count=-1;
		try{
			while(parser.hasNext()){
				int index=Integer.parseInt(parser.next().get(0));
				if(index>count){
					count=index;
				}
			}
		}catch(Exception ex){
			System.err.println(count);
		}
		System.out.println(count);*/
	}
	//Type :[Operator, Arrow, Script, Blackboard Bold, Accent, Latin  Ex, Roman, German, Point, Calligraphic, Numeric, Parenthesis, OtherSymbols, Latin, BigSymbol, Greek]
	//Quality :[, normal, touched, separate, touch_and_sep, doubleprint]
	//Link :[UPPER, LSUP, TOP, LSUB, RSUB, UNDER, RSUP, HORIZONTAL]
}
/*
HORIZONTAL(94.6107%)
0.1732299272532034(0.1096151041014052)
RSUB(2.6862
0.07893310741100201(0.022304634168095288)
RSUP(1.5943%)
0.13531677125005376(0.04139353195440977)
 */
