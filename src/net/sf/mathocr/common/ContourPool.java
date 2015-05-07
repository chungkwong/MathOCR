package net.sf.mathocr.common;
import java.util.*;
public final class ContourPool{
	List<Contour> contours=new ArrayList<Contour>();
	public ContourPool(int[] pixels,int width,int height){
		ContourAnalysis(pixels,width,height);
	}
	public ContourPool(java.awt.image.BufferedImage image){
		int width=image.getWidth(),height=image.getHeight();
		ContourAnalysis(image.getRGB(0,0,width,height,null,0,width),width,height);
	}
	int curr=-1;
	Partition partition;
	final void makeContour(Contour c){
		contours.add(c);
		partition.makeSet();
		++curr;
	}
	private void ContourAnalysis(int[] pixels,int width,int height){
		int curr=-1;
		//Integer tmp;
		partition=new Partition(new Linkable(){
			public void link(int m,int n){
				contours.get(n).combineWith(contours.get(m));
				contours.set(m,null);
			}
		});
		int[] last=new int[width+1];
		int prev=-1;
		for(int i=0;i<pixels.length;i++)
			pixels[i]&=0x1;
		for(int i=0;i<=height;i++){
			for(int j=0;j<=width;j++){
				boolean tl=i!=0&&j!=0&&pixels[(i-1)*width+(j-1)]==0;
				boolean tr=i!=0&&j!=width&&pixels[(i-1)*width+j]==0;
				boolean bl=i!=height&&j!=0&&pixels[i*width+(j-1)]==0;
				boolean br=i!=height&&j!=width&&pixels[i*width+j]==0;
				//System.out.println(tl+","+tr+","+bl+","+br+",");
				if(tl){
					if(tr){
						if(bl){
							if(br){
								/* bb
								   bb */
								last[j]=-1;
							}else{
								/* bb
								   bw */
								contours.add(new Contour(i,j));
								partition.makeSet();
								prev=last[j]=++curr;
							}
						}else{
							if(br){
								/* bb
								   wb */
								contours.get(partition.findRoot(prev)).add(i,j);
								last[j]=prev;
								prev=-1;
							}else{
								/* bb
								   ww */
								contours.get(partition.findRoot(prev)).add(i,j);
								last[j]=-1;
							}
						}
					}else{
						if(bl){
							if(br){
								/* bw
								   bb */
								contours.get(partition.findRoot(last[j])).add(i,j);
								prev=last[j];
								last[j]=-1;
							}else{
								/* bw
								   bw */
								contours.get(partition.findRoot(last[j])).add(i,j);
								prev=-1;
							}
						}else{
							if(br){
								/* bw
								   wb */
								contours.get(partition.findRoot(last[j])).add(i,j);
								contours.get(partition.findRoot(prev)).add(i,j);
								int tmp=last[j];
								last[j]=prev;
								prev=tmp;
							}else{
								/* bw
								   ww */
								contours.get(partition.findRoot(prev)).add(i,j);
								partition.union(last[j],prev);
								prev=last[j]=-1;
							}
						}
					}
				}else{
					if(tr){
						if(bl){
							if(br){
								/* wb
								   bb */
								contours.get(partition.findRoot(last[j])).add(i,j);
								partition.union(last[j],prev);
								prev=last[j]=-1;
							}else{
								/* wb
								   bw */
								contours.get(partition.findRoot(last[j])).add(i,j);
								partition.union(last[j],prev);
								contours.add(new Contour(i,j));
								partition.makeSet();
								prev=last[j]=++curr;
							}
						}else{
							if(br){
								/* wb
								   wb */
								contours.get(partition.findRoot(last[j])).add(i,j);
								prev=-1;
							}else{
								/* wb
								   ww */
								contours.get(partition.findRoot(last[j])).add(i,j);
								prev=last[j];
								last[j]=-1;
							}
						}
					}else{
						if(bl){
							if(br){
								/* ww
								   bb */
								contours.get(partition.findRoot(prev)).add(i,j);
								last[j]=-1;
							}else{
								/* ww
								   bw */
								contours.get(partition.findRoot(prev)).add(i,j);
								last[j]=prev;
								prev=-1;
							}
						}else{
							if(br){
								/* ww
								   wb */
								contours.add(new Contour(i,j));
								partition.makeSet();
								prev=last[j]=++curr;
							}else{
								/* ww
								   ww */
								last[j]=-1;
							}
						}
					}
				}
			}
		}
		partition=null;
		ListIterator<Contour> iter=contours.listIterator();
		while(iter.hasNext())
			if(iter.next()==null)
				iter.remove();
	}
	public List<Contour> getContours(){
		return contours;
	}
	public static void main(String[] args)throws Exception{
		java.awt.image.BufferedImage image=javax.imageio.ImageIO.read(new java.io.File("/home/kwong/图片/sss3.png"));
		image=new net.sf.mathocr.preprocess.Grayscale().preprocess(image);
		image=new net.sf.mathocr.preprocess.ThreholdSauvola(0.2,15).preprocess(image);
		for(Contour c:new ContourPool(image).getContours())
			if(!c.isClosed())
				System.out.println(c);
	}
}
class Contour{
	LinkedList<Integer> is,js,split;
	public Contour(int i,int j){
		is=new LinkedList<Integer>();
		js=new LinkedList<Integer>();
		is.addLast(i);
		js.addLast(j);
	}
	public Contour(LinkedList<Integer> is,LinkedList<Integer> js){
		this.is=is;
		this.js=js;
	}
	public void add(int i,int j){
		if(isAdjoint(is.getFirst(),js.getFirst(),i,j)){
			is.addFirst(i);
			js.addFirst(j);
		}else if(isAdjoint(is.getLast(),js.getLast(),i,j)){
			is.addLast(i);
			js.addLast(j);
		}else{
			ListIterator<Integer> iteri=is.listIterator(),iterj=js.listIterator();
			while(iteri.hasNext())
				if(isAdjoint(iteri.next(),iterj.next(),i,j)){
					int k=iteri.previousIndex();
					if(isClosed()){
						Collections.rotate(is,-k);
						Collections.rotate(js,-k);
						add(i,j);
					}else{
						;
					}
					return;
				}
			System.out.println(toString());
			System.out.println("error:"+i+","+j);
		}
	}
	static final boolean isAdjoint(int i1,int j1,int i2,int j2){
		return (i1==i2&&Math.abs(j1-j2)==1)||(j1==j2&&Math.abs(i1-i2)==1);
	}
	public void combineWith(Contour c){
		if(c!=this){
			if(isAdjoint(is.getLast(),js.getLast(),c.is.getFirst(),c.js.getFirst())){
				is.addAll(c.is);
				js.addAll(c.js);
			}else if(isAdjoint(is.getLast(),js.getLast(),c.is.getLast(),c.js.getLast())){
				Collections.reverse(c.is);
				Collections.reverse(c.js);
				is.addAll(c.is);
				js.addAll(c.js);
			}else if(isAdjoint(is.getFirst(),js.getFirst(),c.is.getFirst(),c.js.getFirst())){
				Collections.reverse(is);
				Collections.reverse(js);
				is.addAll(c.is);
				js.addAll(c.js);
			}else if(isAdjoint(is.getFirst(),js.getFirst(),c.is.getLast(),c.js.getLast())){
				c.is.addAll(is);
				c.js.addAll(js);
				is=c.is;
				js=c.js;
			}
		}
	}
	public boolean isClosed(){
		return isAdjoint(is.getFirst(),js.getFirst(),is.getLast(),js.getLast());
		//return is.getFirst()==is.getLast()&&js.getFirst()==js.getLast();
	}
	public String toString(){
		StringBuilder str=new StringBuilder();
		ListIterator<Integer> iteri=is.listIterator(),iterj=js.listIterator();
		while(iteri.hasNext()){
			str.append(iteri.next());
			str.append(",");
			str.append(iterj.next());
			str.append(";");
		}
		return str.toString();
	}
}
class LineSegment{

}