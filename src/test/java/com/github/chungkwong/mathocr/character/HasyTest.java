package com.github.chungkwong.mathocr.character;
import com.github.chungkwong.mathocr.character.*;
import com.github.chungkwong.mathocr.character.classifier.*;
import com.github.chungkwong.mathocr.common.*;
import com.github.chungkwong.mathocr.preprocess.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.logging.*;
import javax.imageio.*;
/**
 * Assume that the dataset(https://doi.org/10.5281/zenodo.259444) is unpacked at
 * <code>../datasets</code>
 *
 * @author Chan Chung Kwong
 */
public class HasyTest{
	private static final File DATASETS=new File("../datasets");
	public static void main(String[] args) throws IOException{
		File root=new File(DATASETS,"HASYv2/classification-task");
		for(File fold:root.listFiles()){
			testHasy2Fold(fold,new LinearClassifier(1));
		}
	}
	private static void testHasy2Fold(File fold,CharacterRecognizer recognizer) throws IOException{
		System.out.println(fold.getName());
		CombinedPreprocessor preprocessor=CombinedPreprocessor.getDefaultCombinedPreprocessor();
		DataSet dataSet=new DataSet();
		System.out.println("training");
		int[] counter=new int[]{0};
		Files.lines(new File(fold,"train.csv").toPath()).skip(1).forEach((line)->{
			String[] row=line.split(",");
			try{
				++counter[0];
				if(counter[0]%1000==0){
					System.out.println(counter[0]);
				}
				ConnectedComponent image=new ConnectedComponent(preprocessor.apply(ImageIO.read(new File(fold,row[0])),true));
				int codePoint=Integer.parseInt(row[1]);
				CharacterPrototype prototype=new CharacterPrototype(codePoint,image.getBox(),"",32,0);
				dataSet.addSample(prototype,image);
			}catch(IOException ex){
				Logger.getLogger(SingleCharacterTest.class.getName()).log(Level.SEVERE,new File(fold,row[0]).toString(),ex);
			}
		});
		System.out.println("testing");
		int[] result=new int[]{0,0};
		Object model=ModelTypes.REGISTRY.get(recognizer.getModelType()).build(dataSet);
		CharacterList list=dataSet.getCharacterList();
		Files.lines(new File(fold,"test.csv").toPath()).skip(1).forEach((line)->{
			String[] row=line.split(",");
			try{
				ConnectedComponent image=new ConnectedComponent(preprocessor.apply(ImageIO.read(new File(fold,row[0])),true));
				int codePoint=Integer.parseInt(row[1]);
				NavigableSet<CharacterCandidate> geuss=recognizer.recognize(image,model,list);
				if(!geuss.isEmpty()&&geuss.first().getCodePoint()==codePoint){
					++result[0];
				}
				++result[1];
			}catch(IOException ex){
				Logger.getLogger(SingleCharacterTest.class.getName()).log(Level.SEVERE,null,ex);
			}
		});
		System.out.println(result[0]+"/"+result[1]+"="+(result[0]*1.0/result[1]));
	}
}
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

package com.github.chungkwong.mathocr;
import com.github.chungkwong.mathocr.character.classifier.CnnClassifier;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.logging.*;
import java.util.stream.*;
import org.datavec.api.io.labels.*;
import org.datavec.api.split.*;
import org.datavec.api.writable.*;
import org.datavec.image.recordreader.*;
import org.deeplearning4j.datasets.datavec.*;
import org.deeplearning4j.eval.*;
import org.deeplearning4j.nn.conf.*;
import org.deeplearning4j.nn.conf.inputs.*;
import org.deeplearning4j.nn.conf.layers.*;
import org.deeplearning4j.nn.multilayer.*;
import org.deeplearning4j.nn.weights.*;
import org.deeplearning4j.optimize.listeners.*;
import org.deeplearning4j.util.*;
import org.nd4j.linalg.activations.*;
import org.nd4j.linalg.dataset.api.iterator.*;
import org.nd4j.linalg.dataset.api.preprocessor.*;
import org.nd4j.linalg.learning.config.*;
import org.nd4j.linalg.lossfunctions.*;
import org.nd4j.linalg.schedule.*;

public class HasyTest{
	private static final Logger log=Logger.getLogger(CnnClassifier.class.getName());
	private static final String basePath="/home/kwong/projects/SciOCR-data/HASYv2";
	private static final String trainFile=basePath+"/classification-task/fold-1/train.csv";
	private static final String testFile=basePath+"/classification-task/fold-1/test.csv";
	private static final String samples=basePath+"/hasy-data";
	public static void main(String[] args) throws Exception{
		int batchSize=54;
		int nEpochs=3;
		int iterations=1;
		int seed=1234;
		Random randNumGen=new Random(seed);
		log.info("Network configuration and training...");
		Map<Integer,Double> lrSchedule=new HashMap<>();
		lrSchedule.put(0,0.06);
		lrSchedule.put(200,0.05);
		lrSchedule.put(600,0.028);
		lrSchedule.put(800,0.0060);
		lrSchedule.put(1000,0.001);
		MultiLayerConfiguration conf=new NeuralNetConfiguration.Builder()
				.seed(seed)
				.l2(0.0005)
				.updater(new Nesterovs(new MapSchedule(ScheduleType.ITERATION,lrSchedule)))
				.weightInit(WeightInit.XAVIER)
				.list()
				.layer(0,new ConvolutionLayer.Builder(5,5)
						.nIn(1)
						.stride(1,1)
						.nOut(32)
						.activation(Activation.IDENTITY)
						.build())
				.layer(1,new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
						.kernelSize(4,4)
						.stride(4,4)
						.build())
				.layer(2,new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
						.nOut(369)
						.activation(Activation.SOFTMAX)
						.build())
				.setInputType(InputType.convolutionalFlat(32,32,1)) // InputType.convolutional for normal image
				.backprop(true).pretrain(false).build();
		MultiLayerNetwork net=new MultiLayerNetwork(conf);
		net.init();
		net.setListeners(new ScoreIterationListener(10),new TimeIterationListener(1));
		log.log(Level.INFO,"Total num of params: {}",net.numParams());
		// evaluation while training (the score should go down)
		DataSetIterator trainIter=getTrainIterator(trainFile);
		DataSetIterator testIter=getTrainIterator(testFile);
		for(int i=0;i<nEpochs;i++){
			net.fit(trainIter);
			log.log(Level.INFO,"Completed epoch {}",i);
			Evaluation eval=net.evaluate(testIter);
			log.info(eval.stats());
			trainIter.reset();
			testIter.reset();
		}
		ModelSerializer.writeModel(net,new File(basePath+"/alphanum.zip"),true);
	}
	private static DataSetIterator getTrainIterator(String file) throws IOException{
		Map<URI,Integer> map=Files.lines(new File(file).toPath()).skip(1).map((line)->line.split(",")).
				collect(Collectors.toMap((r)->new File(samples,r[0].substring(16)).toURI(),(r)->Integer.parseInt(r[1])));
		CollectionInputSplit split=new CollectionInputSplit(map.keySet());
		ImageRecordReader reader=new ImageRecordReader(32,32,1,new PathLabelGenerator(){
			@Override
			public Writable getLabelForPath(String string){
				return getLabelForPath(new File(string).toURI());
			}
			@Override
			public Writable getLabelForPath(URI uri){
				return new IntWritable(map.getOrDefault(uri,0));
			}
			@Override
			public boolean inferLabelClasses(){
				return true;
			}
		});
		reader.initialize(split);
		RecordReaderDataSetIterator iterator=new RecordReaderDataSetIterator(reader,1000);
		ImagePreProcessingScaler scaler=new ImagePreProcessingScaler(0,1);
		scaler.fit(iterator);
		iterator.setPreProcessor(scaler);
		return iterator;
	}
}
 */
