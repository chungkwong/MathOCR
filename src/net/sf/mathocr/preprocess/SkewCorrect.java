/* SkewCorrect.java
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
package net.sf.mathocr.preprocess;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.util.*;
import net.sf.mathocr.common.*;
import static net.sf.mathocr.Environment.env;
/**
 * A Preprocessor used to correct skew
 */
public final class SkewCorrect implements Preprocessor{
	double angle;
	/**
	 * Construct a SkewCorrect
	 * @param angle the angle from current x-axis to document baseline(clockwise)
	 */
	public SkewCorrect(double angle){
		this.angle=angle;
	}
	/**
	 * Rotate image anti-clockwise by skew angle
	 * @param src the document image
	 */
	public BufferedImage preprocess(BufferedImage src){
		double s=Math.sin(angle),c=Math.cos(angle);
		int w=src.getWidth(),h=src.getHeight();
		int left=(int)Math.floor(Math.min(Math.min(0,h*s),Math.min(w*c,w*c+h*s)));
		int right=(int)Math.ceil(Math.max(Math.max(0,h*s),Math.max(w*c,w*c+h*s)));
		int top=(int)Math.floor(Math.min(Math.min(0,h*c),Math.min(-w*s,-w*s+h*c)));
		int bottom=(int)Math.ceil(Math.max(Math.max(0,h*c),Math.max(-w*s,-w*s+h*c)));
		int width=right-left+1,height=bottom-top+1;
		int type=src.getType();
		if(type==BufferedImage.TYPE_CUSTOM)
			type=BufferedImage.TYPE_INT_ARGB;
		BufferedImage dst=new BufferedImage(width,height,type);
		int[] pixels=new int[width*height];
		//for(int i=0;i<pixels.length;i++)
		//	pixels[i]=0xffffffff;
		//dst.setRGB(0,0,width,height,pixels,0,width);
		Graphics2D g2d=(Graphics2D)dst.getGraphics();
		g2d.setColor(Color.WHITE);
		g2d.fillRect(0,0,width,height);
		g2d.drawImage(src,new AffineTransformOp(new AffineTransform(c,-s,s,c,-left,-top),AffineTransformOp.TYPE_BILINEAR),0,0);
		//AffineTransformOp op=;
		//op.filter(src,dst);
		return dst;
	}
	/**
	 * Detect skew using default method
	 * @param image estimated skew angle
	 */
	public static double detectSkew(BufferedImage image){
		return detectSkew(image,env.getString("SKEW_DETECT_METHOD"));
	}
	/**
	 * Detect skew
	 * @param image the document image
	 * @param method skew detection method, including NONE,PPA,PCP,PP,TC,HT,CC,NN
	 * @return skew angle
	 */
	public static double detectSkew(BufferedImage image,String method){
		/*if(angle==Double.NaN)
			angle=detectSkewTC(image);
		System.out.println("PPA:"+angle);
		System.out.println(System.currentTimeMillis());
		//System.out.println("CC:"+detectSkewCC(image));
		//System.out.println(System.currentTimeMillis());
		System.out.println("PP:"+detectSkewPP(image));
		System.out.println(System.currentTimeMillis());
		System.out.println("TC:"+detectSkewTC(image));
		System.out.println(System.currentTimeMillis());
		System.out.println("PCP:"+detectSkewPCP(image));
		System.out.println(System.currentTimeMillis());
		System.out.println("HT:"+detectSkewHT(image));
		System.out.println(System.currentTimeMillis());
		System.out.println("NN:"+detectSkewNN(image));
		System.out.println(System.currentTimeMillis());*/
		if(method.equals("PP"))
			return detectSkewPP(image);
		else if(method.equals("PPA"))
			return detectSkewPPA(image);
		else if(method.equals("PCP"))
			return detectSkewPCP(image);
		else if(method.equals("TC"))
			return detectSkewTC(image);
		else if(method.equals("HT"))
			return detectSkewHT(image);
		else if(method.equals("CC"))
			return detectSkewCC(image);
		else if(method.equals("NN"))
			return detectSkewNN(image);
		return 0.0;
	}
	/**
	 * Detect skew by piecewise painting algorithm
	 * @param image the document image
	 * @return skew angle
	 */
	public static double detectSkewPPA(BufferedImage image){
		int width=image.getWidth(),height=image.getHeight(),len=width*height;
		int[] pixels=image.getRGB(0,0,width,height,null,0,width);
		ComponentPool pool=new ComponentPool(pixels,width,height);
		int dx=pool.getAverageWidth(),dy=pool.getAverageHeight();
		pool=null;
		return detectSkewPPA(pixels,width,height,dx,dy);
	}
	/**
	 * Detect skew by piecewise painting algorithm
	 * @param pixels pixel array of the image
	 * @param width the width of the image
	 * @param height the height of the image
	 * @param dx the width of a normal vertical stripe
	 * @param dy the height of a normal horizontal stripe
	 * @return skew angle(NaN if detection failed)
	 */
	public static double detectSkewPPA(int[] pixels,int width,int height,int dx,int dy){
		//for(int i=0;i<pixels.length;i++)
			//pixels[i]&=0x1;
		int columns=(width-1)/dx+1,rows=(height-1)/dy+1,len=width*height;
		int[][] ppaHor=new int[5][rows];
		int[] lacc=new int[Math.max(columns,rows)];
		int nonblanklines=-1;
		for(int row=0;row<rows;row++){
			boolean notfound=true;
			for(int x=0;x<width;x++){
				int count=0,j=0;
				for(int ind=width*row*dy+x;j<dy&&ind<len;j++,ind+=width)
					count+=pixels[ind];
				if(count*2<j){
					if(notfound){
						++nonblanklines;
						ppaHor[0][nonblanklines]=row*dy;
						ppaHor[1][nonblanklines]=x;
						notfound=false;
					}
					ppaHor[3][nonblanklines]=x;
				}
			}
			if(!notfound){
				ppaHor[2][nonblanklines]=(ppaHor[1][nonblanklines]+ppaHor[3][nonblanklines])/2;
				ppaHor[4][nonblanklines]=(ppaHor[3][nonblanklines]-ppaHor[1][nonblanklines])/dx;
				++lacc[ppaHor[4][nonblanklines]];
			}
		}
		int max=0,maxind=0;
		for(int i=0;i<columns;i++)
			if(lacc[i]>max){
				max=lacc[i];
				maxind=i;
			}
		int selected=-1;
		for(int i=0;i<=nonblanklines;i++)
			if(ppaHor[4][i]==maxind){
				++selected;
				ppaHor[0][selected]=ppaHor[0][i];
				ppaHor[1][selected]=ppaHor[1][i];
				ppaHor[2][selected]=ppaHor[2][i];
			}
		if(selected<3)
			return Double.NaN;
		double HLR=Math.atan(-linearRegression(ppaHor[0],ppaHor[1],selected+1));
		double HLD=Math.atan(-(ppaHor[1][selected]-ppaHor[1][0]+0.0)/(ppaHor[0][selected]-ppaHor[0][0]));
		double HMR=Math.atan(-linearRegression(ppaHor[0],ppaHor[2],selected+1));
		ppaHor=null;
		for(int i=0;i<lacc.length;i++)
			lacc[i]=0;
		int[][] ppaVert=new int[5][columns];
		nonblanklines=-1;
		for(int col=0;col<60;col++){
			boolean notfound=true;
			for(int y=0;y<height;y++){
				int count=0,j=0;
				for(int x=col*dx,ind=y*width+x;j<dx&&x<width;j++,ind++,x++)
					count+=pixels[ind];
				if(count*2<j){
					if(notfound){
						++nonblanklines;
						ppaVert[0][nonblanklines]=col*dx;
						ppaVert[1][nonblanklines]=y;
						notfound=false;
					}
					ppaVert[3][nonblanklines]=y;
				}
			}
			if(!notfound){
				ppaVert[2][nonblanklines]=(ppaVert[1][nonblanklines]+ppaVert[3][nonblanklines])/2;
				ppaVert[4][nonblanklines]=(ppaVert[3][nonblanklines]-ppaVert[1][nonblanklines])/dy;
				++lacc[ppaVert[4][nonblanklines]];
			}
		}
		max=0;
		maxind=0;
		for(int i=0;i<rows;i++)
			if(lacc[i]>max){
				max=lacc[i];
				maxind=i;
			}
		selected=-1;
		for(int i=0;i<=nonblanklines;i++)
			if(ppaVert[4][i]==maxind){
				++selected;
				ppaVert[0][selected]=ppaVert[0][i];
				ppaVert[1][selected]=ppaVert[1][i];
				ppaVert[2][selected]=ppaVert[2][i];
			}
		if(selected<3)
			return Double.NaN;
		double VTR=Math.atan(linearRegression(ppaVert[0],ppaVert[1],selected+1));
		double VTD=Math.atan((ppaVert[1][selected]-ppaVert[1][0]+0.0)/(ppaVert[0][selected]-ppaVert[0][0]));
		double VMR=Math.atan(linearRegression(ppaVert[0],ppaVert[2],selected+1));
		double VMD=Math.atan((ppaVert[2][selected]-ppaVert[2][0]+0.0)/(ppaVert[0][selected]-ppaVert[0][0]));
		double[] angles=new double[]{HLR,HLD,HMR,VTR,VTD,VMR,VMD};
		Arrays.sort(angles);
		double SM=angles[3];
		return SM;
	}
	/**
	 * Calculate the slope of the linear regression line of point (ptx[0],pty[0]),...,(ptx[len-1],pty[len-1]).
	 * @param ptx x coordinate of the points
	 * @param pty y coordinate of the points
	 * @param len the number of points
	 * @return the slope
	 */
	static double linearRegression(int[] ptx,int[] pty,int len){
		double sumX=0,sumY=0,sumXX=0,sumXY=0;
		for(int i=0;i<len;i++){
			sumX+=ptx[i];
			sumXX+=ptx[i]*ptx[i];
			sumXY+=ptx[i]*pty[i];
			sumY+=pty[i];
		}
		return (len*sumXY-sumX*sumY)/(len*sumXX-sumX*sumX);
	}
	/**
	 * Detect skew by Cross-corrlation method
	 * @param image the document image
	 * @return skew angle
	 */
	public static double detectSkewCC(BufferedImage image){
		int width=image.getWidth(),height=image.getHeight(),len=width*height;
		int[] pixels=image.getRGB(0,0,width,height,null,0,width);
		for(int i=0;i<pixels.length;i++)
			pixels[i]=(~pixels[i])&0x1;
		int d=width/2;
		double maxR=0;
		int S=0,ds=(int)((width*Math.PI/1800)+1);
		for(int s=-height;s<height;s+=ds){
			int R=0;
			for(int x=0;x<width-d;x++){
				int start=Math.max(0,-s);
				for(int ind1=start*width+x,ind2=(start+s)*width+x+d;ind1<len&&ind2<len;ind1+=width,ind2+=width)
					R+=pixels[ind1]*pixels[ind2];
			}
			if(R>maxR){
				maxR=R;
				S=s;
			}
		}
		return Math.atan((S+0.0)/d);
	}
	/**
	 * Detect skew by projection profile
	 * @param image the document image
	 * @return skew angle
	 */
	public static double detectSkewPP(BufferedImage image){
		int width=image.getWidth(),height=image.getHeight();
		int[] pixels=image.getRGB(0,0,width,height,null,0,width);
		double max=0,angle1=0,angle2=0,degree=Math.PI/180,dt1=degree*3,dt2=degree/5;
		for(double t=-12*degree;t<13*degree;t+=dt1){
			double cost=PPcost(pixels,width,height,t);
			if(cost>max){
				max=cost;
				angle1=t;
			}
		}
		max=0;
		for(double t=angle1-2.8*degree;t<angle1+2.9*degree;t+=dt2){
			double cost=PPcost(pixels,width,height,t);
			if(cost>max){
				max=cost;
				angle2=t;
			}
		}
		return angle2;
	}
	static double PPcost(int[] pixels,int width,int height,double theta){
		int len=width*height;
		double k=Math.tan(theta),sum=0,sqsum=0;
		int[] offset=new int[width];
		for(int j=0;j<width;j++)
			offset[j]=(int)(j*k+0.5);
		for(int i=0;i<height;i++){
			int count=0;
			for(int j=0;j<width;j++){
				int ind=(i+offset[j])*width+j;
				if(ind>=0&&ind<len&&pixels[ind]==0xff000000)
					++count;
			}
			sum+=count;
			sqsum+=count*count;
		}
		return (sqsum-sum*sum/height)/height;
	}
	/**
	 * Detect skew by Transition Counts
	 * @param image the document image
	 * @return skew angle
	 */
	public static double detectSkewTC(BufferedImage image){
		int width=image.getWidth(),height=image.getHeight();
		int[] pixels=image.getRGB(0,0,width,height,null,0,width);
		double max=0,angle1=0,angle2=0,degree=Math.PI/180,dt1=degree*3,dt2=degree/5;
		for(double t=-12*degree;t<13*degree;t+=dt1){
			double cost=TCcost(pixels,width,height,t);
			if(cost>max){
				max=cost;
				angle1=t;
			}
		}
		max=0;
		for(double t=angle1-2.8*degree;t<angle1+2.9*degree;t+=dt2){
			double cost=TCcost(pixels,width,height,t);
			if(cost>max){
				max=cost;
				angle2=t;
			}
		}
		return angle2;
	}
	static double TCcost(int[] pixels,int width,int height,double theta){
		int len=width*height;
		double k=Math.tan(theta),sum=0,sqsum=0;
		int[] offset=new int[width];
		for(int j=0;j<width;j++)
			offset[j]=(int)(j*k+0.5);
		for(int i=0;i<height;i++){
			int count=0;
			int prev=0xffffffff;
			for(int j=0;j<width;j++){
				int ind=(i+offset[j])*width+j;
				if(ind>=0&&ind<len&&pixels[ind]!=prev){
					prev=pixels[ind];
					++count;
				}
			}
			sum+=count;
			sqsum+=count*count;
		}
		return (sqsum-sum*sum/height)/height;
	}
	/**
	 * Detect skew by Hough transform
	 * @param image the document image
	 * @return skew angle
	 */
	public static double detectSkewHT(BufferedImage image){
		int width=image.getWidth(),height=image.getHeight();
		int[] pixels=image.getRGB(0,0,width,height,null,0,width);
		double max=0,angle1=0,angle2=0,degree=Math.PI/180,dt1=degree*3,dt2=degree/5;
		for(double t=-12*degree;t<13*degree;t+=dt1){
			double cost=HTcost(pixels,width,height,t);
			if(cost>max){
				max=cost;
				angle1=t;
			}
		}
		max=0;
		for(double t=angle1-2.8*degree;t<angle1+2.9*degree;t+=dt2){
			double cost=HTcost(pixels,width,height,t);
			if(cost>max){
				max=cost;
				angle2=t;
			}
		}
		return angle2;
	}
	static double HTcost(int[] pixels,int width,int height,double theta){
		double c=Math.cos(Math.PI/2-theta),s=Math.sin(Math.PI/2-theta);
		int[] acc=new int[((int)Math.hypot(width,height)+1)/20];
		for(int i=0,ind=0;i<height;++i)
			for(int j=0;j<width;++j,++ind)
				if(pixels[ind]!=0xffffffff)
					++acc[(int)(Math.abs(j*c-i*s))/20];
		double sum=0,sqsum=0;
		int count=0;
		for(int i=0;i<acc.length;i++)
			if(acc[i]!=0){
				sum+=acc[i];
				sqsum+=acc[i]*acc[i];
				count=i;
			}
		/*int max=0;
		for(int p=0;p<acc.length;p++)
			if(acc[p]>max)
				max=acc[p];*/
		return sqsum-sum*sum/count;
	}
	/**
	 * Detect skew by Piecewise covering by parallelograms
	 * @param image the document image
	 * @return skew angle
	 */
	public static double detectSkewPCP(BufferedImage image){
		int width=image.getWidth(),height=image.getHeight();
		int[] pixels=image.getRGB(0,0,width,height,null,0,width);
		double max=0,angle1=0,angle2=0,degree=Math.PI/180,dt1=degree*3,dt2=degree/5;
		for(double t=-12*degree;t<13*degree;t+=dt1){
			double cost=PCPcost(pixels,width,height,t);
			if(cost>max){
				max=cost;
				angle1=t;
			}
		}
		max=0;
		for(double t=angle1-2.8*degree;t<angle1+2.9*degree;t+=dt2){
			double cost=PCPcost(pixels,width,height,t);
			if(cost>max){
				max=cost;
				angle2=t;
			}
		}
		return angle2;
	}
	static double PCPcost(int[] pixels,int width,int height,double theta){
		int len=width*height,area=0,dx=width/20;
		double k=Math.tan(theta);
		int[] offset=new int[width];
		for(int j=0;j<width;j++)
			offset[j]=(int)(j*k+0.5);
		for(int i=0;i<height;i++){
			int count=0,next=dx-1;
			for(int j=0;j<width;j++){
				int ind=(i+offset[j])*width+j;
				if(ind>=0&&ind<len&&pixels[ind]!=0xffffffff)
					++count;
				if(j==next){
					if(count>0){
						area+=dx;
						count=0;
					}
					next+=dx;
				}else if(j==width-1&&count>0){
					area+=width%dx;
					count=0;
				}
			}
		}
		int row=k>0?0:height-1;
		for(int i=0;i<width;i++){
			int count=0,next=i+dx-1;
			for(int j=i,l=0;j<width;j++,l++){
				int ind=(row+offset[l])*width+j;
				if(ind>=0&&ind<len&&pixels[ind]!=0xffffffff)
					++count;
				if(j==next){
					if(count>0){
						area+=dx;
						count=0;
					}
					next+=dx;
				}else if(j==width-1&&count>0){
					area+=width%dx;
					count=0;
				}
			}
		}
		return len-area;
	}
	/**
	 * Detect skew by Nearest Neighbors Clustering
	 * @param image the document image
	 * @return skew angle
	 */
	public static double detectSkewNN(BufferedImage image){
		int width=image.getWidth(),height=image.getHeight();
		int[] pixels=image.getRGB(0,0,width,height,null,0,width);
		ComponentPool pool=new ComponentPool(pixels,width,height);
		int[] hist=new int[450];
		for(ConnectedComponent ele:pool.getComponents()){
			int dx=width,dy=height;
			for(ConnectedComponent ele2:pool.getComponents())
				if(ele!=ele2){
					int dx1=ele.getLeft()+ele.getRight()-ele2.getLeft()-ele2.getRight();
					int dy1=ele.getBottom()+ele.getTop()-ele2.getBottom()-ele2.getTop();
					if(dx1*dx1+dy1*dy1<dx*dx+dy*dy&&dx1*dx1>dy1*dy1){
						dx=dx1;
						dy=dy1;
						//Math.min(Math.min(ele.getWidth(),ele.getHeight()),Math.min(ele2.getWidth(),ele2.getHeight()))*Math.signum(dx1);
					}
				}
			++hist[(int)(Math.atan((dy+0.0)/dx)*900/Math.PI+225)];
		}
		int max=0,t=225;
		for(int i=0;i<450;i++)
			if(hist[i]>max){
				max=hist[i];
				t=i;
			}
		return (t-225)*Math.PI/900;
	}
}