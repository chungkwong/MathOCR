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
import com.github.chungkwong.mathocr.character.*;
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
public class LinkageTest{
	public static final int TOP=-1, HORIZONTAL=0, UPPER=1, UNDER=2, LSUP=5, LSUB=6, RSUB=3, RSUP=4;
	private static final String TRAIN_FILE="../datasets/InftyCDB-1-6152/InftyCDB-1/InftyCDB-1/InftyCDB-1.csv";
	private static final String LINK_MODEL="src/main/resources/com/github/chungkwong/mathocr/resources/linkage.svm";
	private static svm_model model;
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
	public static void trainLinkageClassifier() throws IOException{
		int fieldCount=2;
		ArrayList<Record> records=loadRecords();
		double[][] sum=new double[7][fieldCount];
		double[][][] sqsum=new double[7][fieldCount][fieldCount];
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
				double[] fields=getVector(record,parentRecord);
				for(int i=0;i<fieldCount;i++){
					sum[t][i]+=fields[i];
					for(int j=0;j<fieldCount;j++){
						sqsum[t][i][j]+=fields[i]*fields[j];
					}
				}
			}
		}
		for(int t=0;t<7;t++){
			for(int i=0;i<fieldCount;i++){
				sum[t][i]/=count[t];
			}
			System.out.println(t+":"+Arrays.toString(sum[t])+count[t]);
			double[][] matrix=new double[fieldCount][fieldCount];
			for(int i=0;i<fieldCount;i++){
				for(int j=0;j<fieldCount;j++){
					matrix[i][j]=sqsum[t][i][j]/count[t]-sum[t][i]*sum[t][j];
					System.out.print(matrix[i][j]+"\t");
				}
				System.out.println();
			}
			double det=matrix[0][0]*matrix[1][1]-matrix[0][1]*matrix[1][0];
			System.out.println(Math.sqrt(det));
			System.out.println(matrix[1][1]/det+"\t"+(-matrix[0][1]/det));
			System.out.println(-matrix[1][0]/det+"\t"+(matrix[0][0]/det));
		}
	}
	public static void testLinkageClassifier() throws IOException{
		//ConfusionMatrix confusionMatrix=new ConfusionMatrix();
		ArrayList<Record> records=loadRecords();
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
				int detected=geuss(record,parentRecord);
				/*if(actual!=detected){
					confusionMatrix.advanceFrequency(parentRecord.codepoint,record.codepoint);
				}*/
				++matrix[actual][detected];
			}
		}
		//System.out.println(confusionMatrix);
		double match=0;
		for(int i=0;i<8;i++){
			match+=matrix[i][i];
			for(int j=0;j<8;j++){
				System.out.print(matrix[i][j]+"\t");
			}
			System.out.println();
		}
		System.out.println(match/(528348+1624+4568+15001+8903+10+2));
	}
	private static ArrayList<Record> loadRecords() throws IOException{
		Map<String,Integer> name2code=new HashMap<>();
		new BufferedReader(new InputStreamReader(LinkageTest.class.getResourceAsStream("inftyCDB1.map"),StandardCharsets.UTF_8)).
				lines().forEach((line)->{
					int i=line.indexOf('\t');
					if(i!=-1){
						name2code.put(line.substring(0,i),Integer.parseInt(line.substring(i+1),16));
					}
				});
		CsvParser parser=new CsvParser(Files.lines(new File(TRAIN_FILE).toPath(),StandardCharsets.ISO_8859_1),false);
		int recordCount=688569+1;
		ArrayList<Record> records=new ArrayList<>(recordCount);
		for(int i=0;i<recordCount;i++){
			records.add(null);
		}
		while(parser.hasNext()){
			List<String> row=parser.next();
			int index=Integer.parseInt(row.get(0));
			Record record=new Record();
			record.type=row.get(3);
			record.codepoint=name2code.getOrDefault(row.get(5),0);
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
		return records;
	}
	private static double[] getVector(Record record,Record parentRecord){
		double scale=1.0/getHeight(parentRecord);
		return new double[]{
			getHeight(record)*scale,
			(getBaseline(record)-getBaseline(parentRecord))*scale};
	}
	private static double getHeight(Record record){
		CharacterPrototype character=ModelManager.getCharacterList().getCharacter(record.codepoint);
		if(character!=null){
			return ((double)character.getFontSize())*Math.max(record.width,record.height)/1024;
		}else{
			return Math.max(record.width,record.height);
		}
	}
	private static double getBaseline(Record record){
		CharacterPrototype character=ModelManager.getCharacterList().getCharacter(record.codepoint);
		if(character!=null){
			return record.bottom-record.height*character.getBox().getBottom()/character.getBox().getHeight();
		}else{
			return record.bottom;
		}
	}
	private static class Record{
		String type, link;
		boolean region, baseline, italic, bold;
		int codepoint, width, height, parent, left, top, right, bottom;
	}
	private static final double[] PROBABILITY={
		528348/0.009861529660392148,
		1624/0.1040702141517769,
		4568/0.13751945042954178,
		15001/0.013195586738944665,
		8903/0.027787839624836656,
		10/0.03218247825547289,
		2/2.3352062410536323E-11};
	private static final double[][] MEAN={
		{1.0135851617645684,8.383093131682563E-4},
		{0.6981082097177692,-0.5600019555303543},
		{0.7803256850738456,0.06626289511917687},
		{0.6747105237471085,0.14521376635106567},
		{0.7515797205037746,-0.23817089113652665},
		{0.5826839009682183,-0.24853120309597915},
		{0.5716590297356976,0.1294603524457424}
	};
	private static final double[][][] INVERSE={
		{{24.691897478823577,-39.318012662812706},{-39.318012662812706,479.0521704647488}},
		{{14.696752648571493,15.418378773373924},{15.418378773373924,22.4578394462599}},
		{{5.954351567308771,-0.610403675236202},{-0.610403675236202,8.943071816596268}},
		{{61.03638849122157,-38.84997225490616},{-38.84997225490616,118.8204349130987}},
		{{34.5467574161527,-29.8233989085476},{-29.8233989085476,63.233047170290405}},
		{{96.91875428112918,-1.8057101215910305},{-1.8057101215910305,9.9957955033575}},
		{{9.635430802333482E15,7.194030500933896E16},{7.194030500933896E16,5.3712258341216954E17}}
	};
	private static int geuss(Record record,Record parentRecord){
		if(Math.min(record.right,parentRecord.right)-Math.max(record.left,parentRecord.left)>Math.min(record.width,parentRecord.width)*0.9){
			if(record.top>parentRecord.bottom){
				return UNDER;
			}else if(record.bottom<parentRecord.top){
				return UPPER;
			}
		}
		if(record.left<=parentRecord.right&&parentRecord.left<=record.right){
			if(isPunct(record)){
				return UPPER;
			}else if(isPunct(parentRecord)){
				return UNDER;
			}
		}
		if(isPunct(record)||isPunct(parentRecord)||"−-=".indexOf(parentRecord.codepoint)!=-1||record.codepoint=='='){
			return HORIZONTAL;
		}
		double scale=1.0/getHeight(parentRecord);
		return geuss(getVector(record,parentRecord));
	}
	private static boolean isPunct(Record record){
		return "¯′″˜".indexOf(record.codepoint)!=-1;
//return ModelManager.getCharacterList().getCharacter(record.codepoint)==null;
	}
	private static int geuss(double[] record){
		int bestIndex=0;
		double bestValue=-1;
		for(int t=0;t<PROBABILITY.length;t++){
			if(t!=HORIZONTAL&&t!=RSUB&&t!=RSUP){
				continue;
			}
			double tmp=0.0;
			for(int i=0;i<record.length;i++){
				for(int j=0;j<record.length;j++){
					tmp+=(record[i]-MEAN[t][i])*INVERSE[t][i][j]*(record[j]-MEAN[t][j]);
				}
			}
			double value=PROBABILITY[t]*Math.exp(-0.5*tmp);
			if(value>bestValue){
				bestIndex=t;
				bestValue=value;
			}
		}
		return bestIndex;
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
		//testLinkageClassifier();
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
 /*
0:[1.0135851617645684, 8.383093131682563E-4]528348
0.04658771207485213	0.0038236675799077652
0.0038236675799077652	0.0024012812825985265
0.009861529660392148
24.691897478823577	-39.318012662812706
-39.318012662812706	479.0521704647488
1:[0.6981082097177692, -0.5600019555303543]1624
0.24323208866317625	-0.16699043921040596
-0.16699043921040596	0.1591747884667259
0.1040702141517769
14.696752648571493	15.418378773373924
15.418378773373924	22.4578394462599
2:[0.7803256850738456, 0.06626289511917687]4568
0.16912779022762936	0.011543709684623114
0.011543709684623114	0.11260631061337441
0.13751945042954178
5.954351567308771	-0.610403675236202
-0.610403675236202	8.943071816596268
3:[0.6747105237471085, 0.14521376635106567]15001
0.02068943111372218	0.006764693508534619
0.006764693508534619	0.010627870164278474
0.013195586738944665
61.03638849122157	-38.84997225490616
-38.84997225490616	118.8204349130987
4:[0.7515797205037746, -0.23817089113652665]8903
0.048826284596413694	0.023028555919811622
0.023028555919811622	0.026675763464976007
0.027787839624836656
34.5467574161527	-29.8233989085476
-29.8233989085476	63.233047170290405
5:[0.5826839009682183, -0.24853120309597915]10
0.010352764419405691	0.0018701954729155035
0.0018701954729155035	0.10037990778800662
0.03218247825547289
96.91875428112918	-1.8057101215910305
-1.8057101215910305	9.9957955033575
6:[0.5716590297356976, 0.1294603524457424]2
2.9290305275087025E-4	-3.923040215364493E-5
-3.923040215364493E-5	5.2543817440041385E-6
2.3352062410536323E-11
9.635430802333482E15	7.194030500933896E16
7.194030500933896E16	5.3712258341216954E17
 */
