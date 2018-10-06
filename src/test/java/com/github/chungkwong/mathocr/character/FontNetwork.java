package com.github.chungkwong.mathocr.character;
///*
// * Copyright (C) 2018 Chan Chung Kwong
// *
// * This program is free software: you can redistribute it and/or modify
// * it under the terms of the GNU Affero General Public License as published by
// * the Free Software Foundation, either version 3 of the License, or
// * (at your option) any later version.
// *
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU Affero General Public License for more details.
// *
// * You should have received a copy of the GNU Affero General Public License
// * along with this program.  If not, see <http://www.gnu.org/licenses/>.
// */
//package com.github.chungkwong.mathocr;
//import com.clearspring.analytics.util.*;
//import java.awt.*;
//import java.awt.font.*;
//import java.awt.geom.*;
//import java.awt.image.*;
//import java.io.*;
//import java.net.*;
//import java.util.*;
//import java.util.List;
//import java.util.logging.*;
//import java.util.stream.*;
//import javax.imageio.*;
//import org.datavec.api.conf.*;
//import org.datavec.api.io.labels.*;
//import org.datavec.api.records.*;
//import org.datavec.api.records.metadata.*;
//import org.datavec.api.records.reader.*;
//import org.datavec.api.split.*;
//import org.datavec.api.util.ndarray.*;
//import org.datavec.api.writable.*;
//import org.datavec.api.writable.batch.*;
//import org.datavec.image.loader.*;
//import org.datavec.image.transform.*;
//import org.deeplearning4j.datasets.datavec.*;
//import org.deeplearning4j.eval.*;
//import org.deeplearning4j.nn.conf.*;
//import org.deeplearning4j.nn.conf.inputs.*;
//import org.deeplearning4j.nn.conf.layers.*;
//import org.deeplearning4j.nn.multilayer.*;
//import org.deeplearning4j.nn.weights.*;
//import org.deeplearning4j.optimize.listeners.*;
//import org.deeplearning4j.util.*;
//import org.nd4j.linalg.activations.*;
//import org.nd4j.linalg.api.concurrency.*;
//import org.nd4j.linalg.dataset.api.iterator.*;
//import org.nd4j.linalg.dataset.api.preprocessor.*;
//import org.nd4j.linalg.learning.config.*;
//import org.nd4j.linalg.lossfunctions.*;
//import org.nd4j.linalg.schedule.*;
//import org.nd4j.linalg.api.ndarray.INDArray;
//import org.nd4j.linalg.factory.Nd4j;
///**
// *
// * @author Chan Chung Kwong
// */
//public class FontNetwork{
//	private static final String basePath="/home/kwong/projects/SciOCR-data/fonts";
//	private static final Font[] base=new Font[]{Font.decode("FreeMono"),Font.decode("FreeSans"),Font.decode("FreeSerif")};
//	private static final float[] size=new float[]{12,24,48};
//	private static final int[] style=new int[]{Font.PLAIN,Font.ITALIC,Font.BOLD};
//	private static final Font[] fonts=getFontCombination(base,size,style,new AffineTransform[]{AffineTransform.getScaleInstance(2,2)});
//	private static final int[] codePoints="abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".codePoints().toArray();
//	public static void main(String[] args) throws IOException{
//		int batchSize=54;
//		int nEpochs=3;
//		int iterations=1;
//		int seed=1234;
//		Random randNumGen=new Random(seed);
//		Logger.getGlobal().info("Network configuration and training...");
//		Map<Integer,Double> lrSchedule=new HashMap<>();
//		lrSchedule.put(0,0.06);
//		lrSchedule.put(200,0.05);
//		lrSchedule.put(600,0.028);
//		lrSchedule.put(800,0.0060);
//		lrSchedule.put(1000,0.001);
//		MultiLayerConfiguration conf=new NeuralNetConfiguration.Builder()
//				.seed(seed)
//				.l2(0.0005)
//				.updater(new Nesterovs(new MapSchedule(ScheduleType.ITERATION,lrSchedule)))
//				.weightInit(WeightInit.XAVIER)
//				.list()
//				.layer(0,new ConvolutionLayer.Builder(5,5)
//						.nIn(1)
//						.stride(1,1)
//						.nOut(32)
//						.activation(Activation.IDENTITY)
//						.build())
//				.layer(1,new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
//						.kernelSize(4,4)
//						.stride(4,4)
//						.build())
//				.layer(2,new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
//						.nOut(codePoints.length)
//						.activation(Activation.SOFTMAX)
//						.build())
//				.setInputType(InputType.convolutionalFlat(64,64,1)) // InputType.convolutional for normal image
//				.backprop(true).pretrain(false).build();
//		MultiLayerNetwork net=new MultiLayerNetwork(conf);
//		net.init();
//		net.setListeners(new ScoreIterationListener(10),new TimeIterationListener(1));
//		Logger.getGlobal().log(Level.INFO,"Total num of params: {}",net.numParams());
//		// evaluation while training (the score should go down)
//		DataSetIterator[] trainIter=Arrays.stream(fonts).map((f)->getTrainIterator(f,codePoints)).toArray(DataSetIterator[]::new);
//		DataSetIterator[] testIter=Arrays.stream(fonts).map((f)->getTrainIterator(f,codePoints)).toArray(DataSetIterator[]::new);
//		for(int i=0;i<nEpochs;i++){
//			Arrays.stream(trainIter).forEach((iter)->{
//				net.fit(iter);
//				iter.reset();
//			});
//			Logger.getGlobal().log(Level.INFO,"Completed epoch {}",i);
//			Evaluation eval=new Evaluation();
//			Arrays.stream(testIter).forEach((iter)->{
//				eval.merge(net.evaluate(iter));
//				iter.reset();
//			});
//			Logger.getGlobal().info(eval.stats());
//		}
//		ModelSerializer.writeModel(net,new File(basePath+"/alphanum.zip"),true);
//	}
//	private static Font[] getFontCombination(Font[] base,float[] size,int[] style,AffineTransform[] transform){
//		Font[] fonts=new Font[base.length*size.length*style.length];
//		int i=0;
//		for(Font font:base){
//			for(float f:size){
//				for(int j:style){
//					for(AffineTransform trans:transform){
//						fonts[i++]=font.deriveFont(j,f).deriveFont(trans);
//					}
//				}
//			}
//		}
//		return fonts;
//	}
//	private static DataSetIterator getTrainIterator(Font font,int[] codePoints){
//		InputSplit split=new FontSplit(font,codePoints);
//		ImageRecordReader reader=new ImageRecordReader(64,64,1,new PathLabelGenerator(){
//			@Override
//			public Writable getLabelForPath(String string){
//				return new IntWritable(Integer.parseInt(string.substring(7)));
//			}
//			@Override
//			public Writable getLabelForPath(URI uri){
//				return getLabelForPath(uri.toString());
//			}
//			@Override
//			public boolean inferLabelClasses(){
//				return true;
//			}
//		});
//		try{
//			reader.initialize(split);
//		}catch(IOException ex){
//			Logger.getLogger(FontNetwork.class.getName()).log(Level.SEVERE,null,ex);
//		}
//		RecordReaderDataSetIterator iterator=new RecordReaderDataSetIterator(reader,1000);
//		ImagePreProcessingScaler scaler=new ImagePreProcessingScaler(0,1);
//		scaler.fit(iterator);
//		iterator.setPreProcessor(scaler);
//		return iterator;
//	}
//	private static class FontSplit extends BaseInputSplit{
//		private final Font font;
//		public FontSplit(Font font,int[] codePoints){
//			this.font=font;
//			super.uriStrings=Arrays.stream(codePoints).filter((c)->font.canDisplay(c)).
//					mapToObj((c)->"char://"+Integer.toString(c)).collect(Collectors.toList());
//			super.length=super.uriStrings.size();
//		}
//		@Override
//		public void updateSplitLocations(boolean bln){
//		}
//		@Override
//		public boolean needsBootstrapForWrite(){
//			return false;
//		}
//		@Override
//		public void bootStrapForWrite(){
//		}
//		@Override
//		public OutputStream openOutputStreamFor(String string) throws Exception{
//			return null;
//		}
//		@Override
//		public InputStream openInputStreamFor(String string) throws Exception{
//			PipedInputStream in=new PipedInputStream();
//			PipedOutputStream out=new PipedOutputStream(in);
//			ImageIO.write(getImage(font,Integer.parseInt(string.substring(7))),"png",out);
//			out.close();
//			return in;
//		}
//		@Override
//		public void reset(){
//		}
//		@Override
//		public boolean resetSupported(){
//			return true;
//		}
//		private BufferedImage getImage(Font font,int codePoint){
//			FontRenderContext context=new FontRenderContext(null,false,true);
//			GlyphVector glyphVector=font.createGlyphVector(context,new String(new int[]{codePoint},0,1));
//			float x=(float)glyphVector.getVisualBounds().getX();
//			float y=(float)glyphVector.getVisualBounds().getY();
//			int width=(int)(glyphVector.getVisualBounds().getWidth()+0.5);
//			int height=(int)(glyphVector.getVisualBounds().getHeight()+0.5);
//			if(width==0||height==0){
//				return new BufferedImage(10,10,BufferedImage.TYPE_BYTE_BINARY);
//			}
//			BufferedImage bi=new BufferedImage(width,height,BufferedImage.TYPE_BYTE_BINARY);
//			Graphics2D g2d=bi.createGraphics();
//			g2d.setColor(Color.WHITE);
//			g2d.fillRect(0,0,width,height);
//			g2d.setColor(Color.BLACK);
//			g2d.drawGlyphVector(glyphVector,-x,-y);
//			return bi;
//		}
//	}
//	private static class ImageRecordReader extends BaseRecordReader{
//		protected boolean finishedInputStreamSplit;
//		protected Iterator<String> iter;
//		protected Configuration conf;
//		protected String currentFile;
//		protected PathLabelGenerator labelGenerator=null;
//		protected List<String> labels=new ArrayList<>();
//		protected boolean appendLabel=false;
//		protected boolean writeLabel=false;
//		protected List<Writable> record;
//		protected boolean hitImage=false;
//		protected int height=28, width=28, channels=1;
//		protected boolean cropImage=false;
//		protected BaseImageLoader imageLoader;
//		protected InputSplit inputSplit;
//		protected Map<String,String> fileNameMap=new LinkedHashMap<>();
//		protected String pattern; // Pattern to split and segment file name, pass in regex
//		protected int patternPosition=0;
//		protected boolean logLabelCountOnInit=true;
//		public final static String HEIGHT=NAME_SPACE+".height";
//		public final static String WIDTH=NAME_SPACE+".width";
//		public final static String CHANNELS=NAME_SPACE+".channels";
//		public final static String CROP_IMAGE=NAME_SPACE+".cropimage";
//		public final static String IMAGE_LOADER=NAME_SPACE+".imageloader";
//		public ImageRecordReader(int height,int width,int channels,PathLabelGenerator labelGenerator){
//			this.height=height;
//			this.width=width;
//			this.channels=channels;
//			this.labelGenerator=labelGenerator;
//			this.appendLabel=labelGenerator!=null;
//		}
//		protected boolean containsFormat(String format){
//			for(String format2:imageLoader.getAllowedFormats()){
//				if(format.endsWith("."+format2)){
//					return true;
//				}
//			}
//			return false;
//		}
//		@Override
//		public void initialize(InputSplit split) throws IOException{
//			if(imageLoader==null){
//				imageLoader=new NativeImageLoader(height,width,channels,(ImageTransform)null);
//			}
//			if(split instanceof InputStreamInputSplit){
//				this.inputSplit=split;
//				this.finishedInputStreamSplit=false;
//				return;
//			}
//			inputSplit=split;
//			URI[] locations=split.locations();
//			if(locations!=null&&locations.length>=1){
//				if(appendLabel&&labelGenerator!=null&&labelGenerator.inferLabelClasses()){
//					Set<String> labelsSet=new HashSet<>();
//					for(URI location:locations){
//						String name=labelGenerator.getLabelForPath(location).toString();
//						labelsSet.add(name);
//						if(pattern!=null){
//							String label=name.split(pattern)[patternPosition];
//							fileNameMap.put(location.toString(),label);
//						}
//					}
//					labels.clear();
//					labels.addAll(labelsSet);
//					if(logLabelCountOnInit){
//						Logger.getGlobal().log(Level.INFO,"ImageRecordReader: {} label classes inferred using label generator {}",new Object[]{labelsSet.size(),labelGenerator.getClass().getSimpleName()});
//					}
//				}
//				iter=inputSplit.locationsPathIterator();
//			}else{
//				throw new IllegalArgumentException("No path locations found in the split.");
//			}
//			//To ensure consistent order for label assignment (irrespective of file iteration order), we want to sort the list of labels
//			Collections.sort(labels);
//		}
//		@Override
//		public void initialize(Configuration conf,InputSplit split) throws IOException,InterruptedException{
//			this.appendLabel=conf.getBoolean(APPEND_LABEL,appendLabel);
//			this.labels=new ArrayList<>(conf.getStringCollection(LABELS));
//			this.height=conf.getInt(HEIGHT,height);
//			this.width=conf.getInt(WIDTH,width);
//			this.channels=conf.getInt(CHANNELS,channels);
//			this.cropImage=conf.getBoolean(CROP_IMAGE,cropImage);
//			if("imageio".equals(conf.get(IMAGE_LOADER))){
//				this.imageLoader=new ImageLoader(height,width,channels,cropImage);
//			}else{
//				this.imageLoader=new NativeImageLoader(height,width,channels,(ImageTransform)null);
//			}
//			this.conf=conf;
//			initialize(split);
//		}
//		@Override
//		public List<Writable> next(){
//			if(inputSplit instanceof InputStreamInputSplit){
//				InputStreamInputSplit inputStreamInputSplit=(InputStreamInputSplit)inputSplit;
//				try{
//					NDArrayWritable ndArrayWritable=new NDArrayWritable(imageLoader.asMatrix(inputStreamInputSplit.getIs()));
//					finishedInputStreamSplit=true;
//					return Arrays.<Writable>asList(ndArrayWritable);
//				}catch(IOException e){
//					e.printStackTrace();
//				}
//			}
//			if(iter!=null){
//				List<Writable> ret;
//				String image=iter.next();
//				currentFile=image;
//				try{
//					invokeListeners(image);
//					INDArray row=imageLoader.asMatrix(inputSplit.openInputStreamFor(image));
//					Nd4j.getAffinityManager().ensureLocation(row,AffinityManager.Location.DEVICE);
//					ret=RecordConverter.toRecord(row);
//					if(appendLabel||writeLabel){
//						{
//							if(labelGenerator.inferLabelClasses()){
//								//Standard classification use case (i.e., handle String -> integer conversion
//								ret.add(new IntWritable(labels.indexOf(getLabel(image))));
//							}else{
//								//Regression use cases, and PathLabelGenerator instances that already map to integers
//								ret.add(labelGenerator.getLabelForPath(image));
//							}
//						}
//					}
//				}catch(Exception e){
//					throw new RuntimeException(e);
//				}
//				return ret;
//			}else if(record!=null){
//				hitImage=true;
//				invokeListeners(record);
//				return record;
//			}
//			throw new IllegalStateException("No more elements");
//		}
//		@Override
//		public boolean hasNext(){
//			if(inputSplit instanceof InputStreamInputSplit){
//				return finishedInputStreamSplit;
//			}
//			if(iter!=null){
//				return iter.hasNext();
//			}else if(record!=null){
//				return !hitImage;
//			}
//			throw new IllegalStateException("Indeterminant state: record must not be null, or a file iterator must exist");
//		}
//		@Override
//		public boolean batchesSupported(){
//			return (imageLoader instanceof NativeImageLoader);
//		}
//		@Override
//		public List<List<Writable>> next(int num){
//			Preconditions.checkArgument(num>0,"Number of examples must be > 0: got "+num);
//			if(imageLoader==null){
//				imageLoader=new NativeImageLoader(height,width,channels,(ImageTransform)null);
//			}
//			List<String> currBatch=new ArrayList<>();
//			int cnt=0;
//			int numCategories=(appendLabel||writeLabel)?labels.size():0;
//			List<Integer> currLabels=null;
//			List<Writable> currLabelsWritable=null;
//			List<List<Writable>> multiGenLabels=null;
//			while(cnt<num&&iter.hasNext()){
//				currentFile=iter.next();
//				currBatch.add(currentFile);
//				invokeListeners(currentFile);
//				if(appendLabel||writeLabel){
//					//Collect the label Writables from the label generators
//					{
//						if(labelGenerator.inferLabelClasses()){
//							if(currLabels==null){
//								currLabels=new ArrayList<>();
//							}
//							currLabels.add(labels.indexOf(getLabel(currentFile)));
//						}else{
//							if(currLabelsWritable==null){
//								currLabelsWritable=new ArrayList<>();
//							}
//							currLabelsWritable.add(labelGenerator.getLabelForPath(currentFile));
//						}
//					}
//				}
//				cnt++;
//			}
//			INDArray features=Nd4j.createUninitialized(new int[]{cnt,channels,height,width},'c');
//			Nd4j.getAffinityManager().tagLocation(features,AffinityManager.Location.HOST);
//			for(int i=0;i<cnt;i++){
//				try{
//					((NativeImageLoader)imageLoader).asMatrixView(inputSplit.openInputStreamFor(currBatch.get(i)),
//							features.tensorAlongDimension(i,1,2,3));
//				}catch(Exception e){
//					System.out.println("Image file failed during load: "+currBatch.get(i));
//					throw new RuntimeException(e);
//				}
//			}
//			Nd4j.getAffinityManager().ensureLocation(features,AffinityManager.Location.DEVICE);
//			List<INDArray> ret=new ArrayList<>();
//			ret.add(features);
//			if(appendLabel||writeLabel){
//				//And convert the previously collected label Writables from the label generators
//				{
//					INDArray labels;
//					if(labelGenerator.inferLabelClasses()){
//						//Standard classification use case (i.e., handle String -> integer conversion)
//						labels=Nd4j.create(cnt,numCategories,'c');
//						Nd4j.getAffinityManager().tagLocation(labels,AffinityManager.Location.HOST);
//						for(int i=0;i<currLabels.size();i++){
//							labels.putScalar(i,currLabels.get(i),1.0f);
//						}
//					}else{
//						//Regression use cases, and PathLabelGenerator instances that already map to integers
//						if(currLabelsWritable.get(0) instanceof NDArrayWritable){
//							List<INDArray> arr=new ArrayList<>();
//							for(Writable w:currLabelsWritable){
//								arr.add(((NDArrayWritable)w).get());
//							}
//							labels=Nd4j.concat(0,arr.toArray(new INDArray[arr.size()]));
//						}else{
//							labels=RecordConverter.toMinibatchArray(currLabelsWritable);
//						}
//					}
//					ret.add(labels);
//				}
//			}
//			return new NDArrayRecordBatch(ret);
//		}
//		@Override
//		public void close() throws IOException{
//			//No op
//		}
//		@Override
//		public void setConf(Configuration conf){
//			this.conf=conf;
//		}
//		@Override
//		public Configuration getConf(){
//			return conf;
//		}
//		/**
//		 * Get the label from the given path
//		 *
//		 * @param path the path to get the label from
//		 * @return the label for the given path
//		 */
//		public String getLabel(String path){
//			if(labelGenerator!=null){
//				return labelGenerator.getLabelForPath(path).toString();
//			}
//			if(fileNameMap!=null&&fileNameMap.containsKey(path)){
//				return fileNameMap.get(path);
//			}
//			return path;
//		}
//		/**
//		 * Accumulate the label from the path
//		 *
//		 * @param path the path to get the label from
//		 */
//		protected void accumulateLabel(String path){
//			String name=getLabel(path);
//			if(!labels.contains(name)){
//				labels.add(name);
//			}
//		}
//		/**
//		 * Returns the file loaded last by {@link #next()}.
//		 */
//		public String getCurrentFile(){
//			return currentFile;
//		}
//		/**
//		 * Sets manually the file returned by {@link #getCurrentFile()}.
//		 */
//		public void setCurrentFile(String currentFile){
//			this.currentFile=currentFile;
//		}
//		@Override
//		public List<String> getLabels(){
//			return labels;
//		}
//		public void setLabels(List<String> labels){
//			this.labels=labels;
//			this.writeLabel=true;
//		}
//		@Override
//		public void reset(){
//			if(inputSplit==null){
//				throw new UnsupportedOperationException("Cannot reset without first initializing");
//			}
//			inputSplit.reset();
//			if(iter!=null){
//				iter=inputSplit.locationsPathIterator();
//			}else if(record!=null){
//				hitImage=false;
//			}
//		}
//		@Override
//		public boolean resetSupported(){
//			if(inputSplit==null){
//				return false;
//			}
//			return inputSplit.resetSupported();
//		}
//		/**
//		 * Returns {@code getLabels().size()}.
//		 */
//		public int numLabels(){
//			return labels.size();
//		}
//		@Override
//		public List<Writable> record(URI uri,DataInputStream dataInputStream) throws IOException{
//			invokeListeners(uri);
//			if(imageLoader==null){
//				imageLoader=new NativeImageLoader(height,width,channels,(ImageTransform)null);
//			}
//			INDArray row=imageLoader.asMatrix(dataInputStream);
//			List<Writable> ret=RecordConverter.toRecord(row);
//			if(appendLabel){
//				ret.add(new IntWritable(labels.indexOf(getLabel(uri.getPath()))));
//			}
//			return ret;
//		}
//		@Override
//		public Record nextRecord(){
//			List<Writable> list=next();
//			URI uri;
//			try{
//				uri=new URI(currentFile);
//			}catch(URISyntaxException ex){
//				Logger.getLogger(FontNetwork.class.getName()).log(Level.SEVERE,null,ex);
//				uri=null;
//			}
//			return new org.datavec.api.records.impl.Record(list,new RecordMetaDataURI(uri,ImageRecordReader.class));
//		}
//		@Override
//		public Record loadFromMetaData(RecordMetaData recordMetaData) throws IOException{
//			return loadFromMetaData(Collections.singletonList(recordMetaData)).get(0);
//		}
//		@Override
//		public List<Record> loadFromMetaData(List<RecordMetaData> recordMetaDatas) throws IOException{
//			List<Record> out=new ArrayList<>();
//			for(RecordMetaData meta:recordMetaDatas){
//				URI uri=meta.getURI();
//				List<Writable> next;
//				try(DataInputStream dis=new DataInputStream(new BufferedInputStream(inputSplit.openInputStreamFor(uri.toString())))){
//					next=record(uri,dis);
//					out.add(new org.datavec.api.records.impl.Record(next,meta));
//				}catch(Exception ex){
//					Logger.getLogger(FontNetwork.class.getName()).log(Level.SEVERE,null,ex);
//				}
//			}
//			return out;
//		}
//	}
//}
