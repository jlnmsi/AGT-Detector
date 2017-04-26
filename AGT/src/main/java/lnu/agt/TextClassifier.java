package lnu.agt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.DecimalFormat;
import java.util.Properties;

import org.codehaus.jackson.JsonNode;

import weka.classifiers.Classifier;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader.ArffReader;

public class TextClassifier {

	Instances data;
	JsonNode tweet;
	DecimalFormat formatter;
	Classifier cls;
	Properties agtProps;
	
	//model params
	String newModel = "";
	
	public static void main(String[] args){
		
	}
	
	
	public TextClassifier(JsonNode tweet) throws Exception {
		this.tweet = tweet;
		
		agtProps = AGTProperties.getAGTProperties();
		String path = agtProps.getProperty("dummy");
		
		//load dummy dataset to get correct format of Instances data
		BufferedReader reader = new BufferedReader(new FileReader(path));
		ArffReader arff = new ArffReader(reader);
		data = arff.getData();
		reader.close();
		
		formatter = new DecimalFormat("#0.00000");     

		Instance inst = new DenseInstance(data.numAttributes());
		TweetText tweetText = new TweetText(tweet);

		inst.setValue(data.attribute(0), data.attribute(0).addStringValue(tweetText.getCleanText()));
		data.add(0, inst);
		
	}
	
	public void setClassifier(String classifier) {
		newModel = classifier;
	}
	
	public double getClassification() throws Exception{
		String modelPath;
		if(newModel.equals("")){
			modelPath  = agtProps.getProperty("modelRF");
		} else {
			modelPath = newModel;
		}
		//load model
		cls = (Classifier) weka.core.SerializationHelper.read(modelPath);
		return cls.distributionForInstance(data.instance(0))[1];
	}
	
	public void printClassification() throws Exception{
		System.out.println(formatter.format(getClassification()));
	}
	
	public Classifier getClassifier(){
		return cls;
	}
	
	
}
