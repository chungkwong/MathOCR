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
import com.github.chungkwong.mathocr.*;
import com.github.chungkwong.mathocr.common.*;
import java.awt.image.*;
import java.util.*;
/**
 *
 * @author Chan Chung Kwong
 */
public abstract class FormulaTest{
	private final LineRecognizer recognizer;
	public FormulaTest(){
		this.recognizer=LineRecognizers.REGISTRY.get();
	}
	public FormulaTest(LineRecognizer recognizer){
		this.recognizer=recognizer;
	}
	private static final int N=4;
	public void evaluate(){
		HashMap<String,Pair<Counter,Counter>> freq=new HashMap<>();
		int count=0;
		int exact=0;
		int editDistanceSum=0;
		int[] nGramCount=new int[N];
		int[] matchedNGramCount=new int[N];
		int actualLength=0;
		while(next()){
			BufferedImage image=getImage();
			if(image==null){
				continue;
			}
			++count;
			String[] predicted=Util.normalize(Util.tokenize(PipeLine.recognizeLatexFormula(image)));
			String[] actual=Util.normalize(Util.tokenize(getText()));
			for(String token:predicted){
				if(!freq.containsKey(token)){
					freq.put(token,new Pair<>(new Counter(),new Counter()));
				}
				freq.get(token).getKey().advance();
			}
			for(String token:actual){
				if(!freq.containsKey(token)){
					freq.put(token,new Pair<>(new Counter(),new Counter()));
				}
				freq.get(token).getValue().advance();
			}
			if(Arrays.equals(predicted,actual)){
				++exact;
			}else{
				System.out.println(count+Arrays.toString(actual));
				System.out.println(" => "+Arrays.toString(predicted));
			}
			editDistanceSum+=Util.getLevenshteinDistance(predicted,actual);
			updateBleu(predicted,actual,nGramCount,matchedNGramCount);
			actualLength+=actual.length;
		}
		freq.forEach((k,v)->System.out.println(k+":"+v.getKey()+"/"+v.getValue()));
		System.out.println("Count:"+count);
		System.out.println("Length:"+nGramCount[0]+"/"+actualLength);
		System.out.println("N-gram:"+Arrays.toString(nGramCount));
		System.out.println("BLEU:"+getBleu(nGramCount,matchedNGramCount,actualLength));
		System.out.println("Edit distance(per formula):"+editDistanceSum*1.0/count);
		System.out.println("Edit distance(per predicted token):"+editDistanceSum*1.0/nGramCount[0]);
		System.out.println("Exact:"+exact*1.0/count);
	}
	protected abstract boolean next();
	protected abstract BufferedImage getImage();
	protected abstract String getText();
	private static void updateBleu(String[] predicted,String[] actual,int[] nGramCount,int[] matchedNGramCount){
		for(int i=0;i<N;i++){
			int found=Math.max(0,predicted.length-i);
			int expected=Math.max(0,actual.length-i);
			nGramCount[i]+=found;
			BitSet used=new BitSet(expected);
			for(int j=0;j<found;j++){
				out:
				for(int k=0;k<expected;k++){
					if(!used.get(k)){
						for(int l=0;l<=i;l++){
							if(!Objects.equals(predicted[j+l],actual[k+l])){
								continue out;
							}
						}
						used.set(k);
						++matchedNGramCount[i];
						break;
					}
				}
			}
		}
	}
	private static double getBleu(int[] nGramCount,int[] matchedNGramCount,int actualLength){
		double[] p=new double[N];
		for(int i=0;i<N;i++){
			p[i]=matchedNGramCount[i]*1.0/nGramCount[i];
			System.out.println("p_"+i+"="+p[i]);
		}
		double penalty=nGramCount[0]>=actualLength?1:Math.exp(1-actualLength*1.0/nGramCount[0]);
		return penalty*Math.exp(Arrays.stream(p).map((q)->Math.log(q)).sum()/N);
	}
	public static void main(String[] args) throws Exception{
		System.out.println(Util.tokenize("1+8\\pi\\sqrt[1+x^2]%hello\n\\(\\)%hell"));
		System.out.println(Util.getLevenshteinDistance("gg","aaaaa"));
	}
}
