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
import com.github.chungkwong.mathocr.text.structure.Line;
import com.github.chungkwong.json.*;
import com.github.chungkwong.mathocr.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.logging.*;
import java.util.stream.*;
import javax.imageio.*;
/**
 * Baidu cloud OCR service
 *
 * @author Chan Chung Kwong
 */
public class BaiduLineRecognizer implements LineRecognizer{
	private static final String OCR_URL="https://aip.baidubce.com/rest/2.0/ocr/v1/general_basic";
	private static final String TOKEN_URL="https://aip.baidubce.com/oauth/2.0/token";
	private static String accessToken;
	/**
	 * Create a line recognizer
	 *
	 */
	public BaiduLineRecognizer(){
	}
	@Override
	public Line recognize(TextLine block,BufferedImage input){
		if(accessToken==null){
			getAccessToken();
		}
		try{
			ByteArrayOutputStream lineImage=new ByteArrayOutputStream();
			ImageIO.write(input.getSubimage(block.getBox().getLeft(),block.getBox().getTop(),block.getBox().getRight()-block.getBox().getLeft()+1,block.getBox().getBottom()-block.getBox().getTop()+1),"png",lineImage);
			String image=Base64.getEncoder().encodeToString(lineImage.toByteArray());
			URL url=new URL(OCR_URL+"?access_token="+accessToken);
			HttpURLConnection connection=(HttpURLConnection)url.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
			connection.setDoOutput(true);
			connection.connect();
			OutputStreamWriter out=new OutputStreamWriter(connection.getOutputStream());
			out.write("image=");
			out.write(URLEncoder.encode(image,"UTF-8"));
			out.close();
			Map<String,Object> result=(Map<String,Object>)JSONDecoder.decode(new BufferedReader(new InputStreamReader(connection.getInputStream())));
			connection.disconnect();
			String text=((List<Map<String,String>>)result.get("words_result")).stream().map((word)->word.get("words")).collect(Collectors.joining(" "));
			return Line.fromLine(text,block.getBox());
		}catch(Exception ex){
			Logger.getGlobal().log(Level.SEVERE,"",ex);
			return new Line(Collections.emptyList());
		}
	}
	private static void getAccessToken(){
		try{
			URL url=new URL(TOKEN_URL+"?grant_type=client_credentials&client_id="
					+Environment.ENVIRONMENT.getString("BAIDU_KEY")
					+"&client_secret="+Environment.ENVIRONMENT.getString("BAIDU_SECRET"));
			HttpURLConnection connection=(HttpURLConnection)url.openConnection();
			connection.setRequestMethod("GET");
			connection.connect();
			Map<String,List<String>> map=connection.getHeaderFields();
			Map<String,String> result=(Map<String,String>)JSONDecoder.decode(new BufferedReader(new InputStreamReader(connection.getInputStream())));
			accessToken=result.get("access_token");
			connection.disconnect();
			System.err.println(accessToken);
		}catch(Exception e){
			System.err.printf("获取token失败！");
			e.printStackTrace(System.err);
		}
	}
	public static void main(String[] args) throws IOException,SyntaxException{
		getAccessToken();
		File file=new File("/home/kwong/图片/abstract_en.png");
		String image=Base64.getEncoder().encodeToString(Files.readAllBytes(file.toPath()));
		URL url=new URL(OCR_URL+"?access_token="+accessToken);
		HttpURLConnection connection=(HttpURLConnection)url.openConnection();
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
		connection.setDoOutput(true);
		connection.connect();
		OutputStreamWriter out=new OutputStreamWriter(connection.getOutputStream());
		out.write("image=");
		out.write(URLEncoder.encode(image,"UTF-8"));
		out.close();
		Map<String,Object> result=(Map<String,Object>)JSONDecoder.decode(new BufferedReader(new InputStreamReader(connection.getInputStream())));
		connection.disconnect();
		System.out.println(result);
		String text=((List<Map<String,String>>)result.get("words_result")).stream().map((word)->word.get("words")).collect(Collectors.joining(" "));
		System.out.println(text);
	}
}
