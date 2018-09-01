package com.github.chungkwong.mathocr;
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
//import java.io.*;
//import java.net.*;
//import java.util.*;
//import javax.xml.parsers.*;
//import javax.xml.xpath.*;
//import org.bytedeco.javacv.CanvasFrame;
//import org.bytedeco.javacv.OpenCVFrameConverter;
//import org.datavec.api.records.metadata.RecordMetaDataImageURI;
//import org.datavec.api.split.FileSplit;
//import org.datavec.image.loader.NativeImageLoader;
//import org.datavec.image.recordreader.objdetect.ObjectDetectionRecordReader;
//import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
//import org.deeplearning4j.nn.layers.objdetect.DetectedObject;
//import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
//import org.deeplearning4j.util.ModelSerializer;
//import org.nd4j.linalg.api.ndarray.INDArray;
//import org.nd4j.linalg.dataset.api.preprocessor.ImagePreProcessingScaler;
//import org.nd4j.linalg.factory.Nd4j;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import static org.bytedeco.javacpp.opencv_core.*;
//import static org.bytedeco.javacpp.opencv_imgproc.*;
//import org.datavec.api.io.filters.*;
//import org.datavec.api.split.*;
//import org.datavec.image.recordreader.objdetect.*;
//import org.datavec.image.recordreader.objdetect.impl.*;
//import org.deeplearning4j.nn.api.*;
//import org.deeplearning4j.nn.conf.*;
//import org.deeplearning4j.nn.conf.inputs.*;
//import org.deeplearning4j.nn.conf.layers.*;
//import org.deeplearning4j.nn.conf.layers.objdetect.*;
//import org.deeplearning4j.nn.graph.*;
//import org.deeplearning4j.nn.transferlearning.*;
//import org.deeplearning4j.nn.weights.*;
//import org.deeplearning4j.zoo.model.*;
//import org.nd4j.linalg.activations.*;
//import org.nd4j.linalg.learning.config.*;
//import org.w3c.dom.*;
//import org.xml.sax.*;
///**
// * Example transfer learning from a Tiny YOLO model pretrained on ImageNet and
// * Pascal VOC to perform object detection with bounding boxes on The Street View
// * House Numbers (SVHN) Dataset.
// * <p>
// * References: <br>
// * - YOLO: Real-Time Object Detection: https://pjreddie.com/darknet/yolo/ <br>
// * - The Street View House Numbers (SVHN) Dataset:
// * http://ufldl.stanford.edu/housenumbers/ <br>
// * <p>
// * Please note, cuDNN should be used to obtain reasonable performance:
// * https://deeplearning4j.org/cudnn
// *
// * @author saudet
// */
//public class HouseNumberDetection{
//	private static final Logger log=LoggerFactory.getLogger(HouseNumberDetection.class);
//	public static void main(String[] args) throws java.lang.Exception{
//		// parameters matching the pretrained TinyYOLO model
//		int width=416;
//		int height=416;
//		int nChannels=3;
//		int gridWidth=13;
//		int gridHeight=13;
//		// number classes for the red blood cells (RBC)
//		int nClasses=1;
//		// parameters for the Yolo2OutputLayer
//		int nBoxes=5;
//		double lambdaNoObj=0.5;
//		double lambdaCoord=5.0;
//		double[][] priorBoxes={{2,2},{2,2},{2,2},{2,2},{2,2}};
//		double detectionThreshold=0.3;
//		// parameters for the training phase
//		int batchSize=10;
//		int nEpochs=5;//0;
//		double learningRate=1e-3;
//		double lrMomentum=0.9;
//		int seed=123;
//		Random rng=new Random(seed);
//		String dataDir="/home/kwong/projects/SciOCR-data/dataset/";
//		File imageDir=new File(dataDir,"JPEGImages");
//		log.info("Load data...");
//		RandomPathFilter pathFilter=new RandomPathFilter(rng){
//			@Override
//			protected boolean accept(String name){
//				name=name.replace("/JPEGImages/","/Annotations/").replace(".jpg",".xml");
//				try{
//					return new File(new URI(name)).exists();
//				}catch(URISyntaxException ex){
//					throw new RuntimeException(ex);
//				}
//			}
//		};
//		InputSplit[] data=new FileSplit(imageDir,NativeImageLoader.ALLOWED_FORMATS,rng).sample(pathFilter,0.8,0.2);
//		InputSplit trainData=data[0];
//		InputSplit testData=data[1];
//		ObjectDetectionRecordReader recordReaderTrain=new ObjectDetectionRecordReader(height,width,nChannels,
//				gridHeight,gridWidth,new VocLabelProvider(dataDir));
//		recordReaderTrain.initialize(trainData);
//		ObjectDetectionRecordReader recordReaderTest=new ObjectDetectionRecordReader(height,width,nChannels,
//				gridHeight,gridWidth,new VocLabelProvider(dataDir));
//		recordReaderTest.initialize(testData);
//		// ObjectDetectionRecordReader performs regression, so we need to specify it here
//		RecordReaderDataSetIterator train=new RecordReaderDataSetIterator(recordReaderTrain,batchSize,1,1,true);
//		train.setPreProcessor(new ImagePreProcessingScaler(0,1));
//		RecordReaderDataSetIterator test=new RecordReaderDataSetIterator(recordReaderTest,1,1,1,true);
//		test.setPreProcessor(new ImagePreProcessingScaler(0,1));
//		ComputationGraph model;
//		String modelFilename="model_rbc.zip";
//		if(new File(modelFilename).exists()){
//			log.info("Load model...");
//			model=ModelSerializer.restoreComputationGraph(modelFilename);
//		}else{
//			log.info("Build model...");
//			ComputationGraph pretrained=(ComputationGraph)TinyYOLO.builder().build().initPretrained();
//			INDArray priors=Nd4j.create(priorBoxes);
//			FineTuneConfiguration fineTuneConf=new FineTuneConfiguration.Builder()
//					.seed(seed)
//					.optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
//					.gradientNormalization(GradientNormalization.RenormalizeL2PerLayer)
//					.gradientNormalizationThreshold(1.0)
//					.updater(new Adam.Builder().learningRate(learningRate).build())
//					//.updater(new Nesterovs.Builder().learningRate(learningRate).momentum(lrMomentum).build())
//					.activation(Activation.IDENTITY)
//					.trainingWorkspaceMode(WorkspaceMode.SEPARATE)
//					.inferenceWorkspaceMode(WorkspaceMode.SEPARATE)
//					.build();
//			model=new TransferLearning.GraphBuilder(pretrained)
//					.fineTuneConfiguration(fineTuneConf)
//					.removeVertexKeepConnections("conv2d_9")
//					.addLayer("convolution2d_9",
//							new ConvolutionLayer.Builder(1,1)
//									.nIn(1024)
//									.nOut(nBoxes*(5+nClasses))
//									.stride(1,1)
//									.convolutionMode(ConvolutionMode.Same)
//									.weightInit(WeightInit.UNIFORM)
//									.hasBias(false)
//									.activation(Activation.IDENTITY)
//									.build(),
//							"leaky_re_lu_8")
//					.addLayer("outputs",
//							new Yolo2OutputLayer.Builder()
//									.lambbaNoObj(lambdaNoObj)
//									.lambdaCoord(lambdaCoord)
//									.boundingBoxPriors(priors)
//									.build(),
//							"convolution2d_9")
//					.setOutputs("outputs")
//					.build();
//			System.out.println(model.summary(InputType.convolutional(height,width,nChannels)));
//			log.info("Train model...");
//			model.setListeners(new ScoreIterationListener(1));
//			for(int i=0;i<nEpochs;i++){
//				train.reset();
//				while(train.hasNext()){
//					model.fit(train.next());
//				}
//				System.err.println("*** Completed epoch "+i);
//			}
//			ModelSerializer.writeModel(model,modelFilename,true);
//		}
//		// visualize results on the test set
//		NativeImageLoader imageLoader=new NativeImageLoader();
//		CanvasFrame frame=new CanvasFrame("RedBloodCellDetection");
//		OpenCVFrameConverter.ToMat converter=new OpenCVFrameConverter.ToMat();
//		org.deeplearning4j.nn.layers.objdetect.Yolo2OutputLayer yout
//				=(org.deeplearning4j.nn.layers.objdetect.Yolo2OutputLayer)model.getOutputLayer(0);
//		List<String> labels=train.getLabels();
//		test.setCollectMetaData(true);
//		while(test.hasNext()&&frame.isVisible()){
//			org.nd4j.linalg.dataset.DataSet ds=test.next();
//			RecordMetaDataImageURI metadata=(RecordMetaDataImageURI)ds.getExampleMetaData().get(0);
//			INDArray features=ds.getFeatures();
//			INDArray results=model.outputSingle(features);
//			List<DetectedObject> objs=yout.getPredictedObjects(results,detectionThreshold);
//			File file=new File(metadata.getURI());
//			log.info(file.getName()+": "+objs);
//			Mat mat=imageLoader.asMat(features);
//			Mat convertedMat=new Mat();
//			mat.convertTo(convertedMat,CV_8U,255,0);
//			int w=metadata.getOrigW()*2;
//			int h=metadata.getOrigH()*2;
//			Mat image=new Mat();
//			resize(convertedMat,image,new Size(w,h));
//			for(DetectedObject obj:objs){
//				double[] xy1=obj.getTopLeftXY();
//				double[] xy2=obj.getBottomRightXY();
//				String label=labels.get(obj.getPredictedClass());
//				int x1=(int)Math.round(w*xy1[0]/gridWidth);
//				int y1=(int)Math.round(h*xy1[1]/gridHeight);
//				int x2=(int)Math.round(w*xy2[0]/gridWidth);
//				int y2=(int)Math.round(h*xy2[1]/gridHeight);
//				rectangle(image,new Point(x1,y1),new Point(x2,y2),Scalar.RED);
//				putText(image,label,new Point(x1+2,y2-2),FONT_HERSHEY_DUPLEX,1,Scalar.GREEN);
//			}
//			frame.setTitle(new File(metadata.getURI()).getName()+" - RedBloodCellDetection");
//			frame.setCanvasSize(w,h);
//			frame.showImage(converter.convert(image));
//			frame.waitKey();
//		}
//		frame.dispose();
//	}
//	private static class IcdarLabelProvider implements ImageObjectLabelProvider{
//		private final Document document;
//		public IcdarLabelProvider(File dir) throws ParserConfigurationException,IOException,SAXException,XPathExpressionException{
//			DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
//			factory.setIgnoringElementContentWhitespace(true);
//			document=factory.newDocumentBuilder().parse(new File(dir,"locations.xml"));
//		}
//		@Override
//		public List<ImageObject> getImageObjectsForPath(String string){
//			NodeList images=document.getElementsByTagName("image");
//			for(int i=0;i<images.getLength();i++){
//				String path=" ";
//				NodeList image=images.item(i).getChildNodes();
//				for(int j=0;j<image.getLength();j++){
//					if(image.item(j).getNodeName().equals("imageName")){
//						path=image.item(j).getTextContent();
//						break;
//					}
//				}
//				if(string.endsWith(path)){
//					NodeList rects=null;
//					for(int j=0;j<image.getLength();j++){
//						if(image.item(j).getNodeName().equals("taggedRectangles")){
//							rects=(NodeList)image.item(j);
//							break;
//						}
//					}
//					List<ImageObject> frames=new ArrayList<>(rects.getLength());
//					for(int j=0;j<rects.getLength();j++){
//						if(!(rects.item(j) instanceof Element)){
//							continue;
//						}
//						Element item=(Element)rects.item(j);
//						int x=(int)Double.parseDouble(item.getAttribute("x"));
//						int y=(int)Double.parseDouble(item.getAttribute("y"));
//						int width=(int)Double.parseDouble(item.getAttribute("width"));
//						int height=(int)Double.parseDouble(item.getAttribute("height"));
//						frames.add(new ImageObject(x,y,x+width,y+height,"text"));
//					}
//					return frames;
//				}
//			}
//			return Collections.emptyList();
//		}
//		@Override
//		public List<ImageObject> getImageObjectsForPath(URI uri){
//			return getImageObjectsForPath(uri.getPath());
//		}
//	}
//}
