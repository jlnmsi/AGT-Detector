package lnu.agt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Random;

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
	String newModel = "";
	
	public static void main(String[] args) throws Exception {
		System.out.println("Working Directory: "+System.getProperty("user.dir"));
		
		Properties props = AGTProperties.getLocalProperties();
		File ntsDir = new File( props.getProperty("ntsDir") );
		TextClassifier tc;
		Random rand = new Random();
		
		ArrayList<File> ntsFiles = ReadZipFiles.findZipFiles( ntsDir );
		ArrayList<JsonNode> tweets = ReadZipFiles.readZipFile(ntsFiles.get(rand.nextInt(ntsFiles.size())));
		
		int i = 0;
		while(i < 3) {		
			int r  = rand.nextInt(tweets.size());
			tc = new TextClassifier(tweets.get(r));
			//tc.setClassifier("modelNB"); uncomment for Naive Bayesian instead
			//tc.setClassifier("modelSMO"); uncomment for support vector machine instead
			TweetText tweetText = new TweetText(tweets.get(r));
			if(tweetText.getLang().equals("en")) {
				i++;
				System.out.println(tweetText.getText()
					+"\n"+tweetText.getCleanText() 
					+"\nP(AGT) = "+tc.getClassification()
					);
			}
		}

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
			modelPath = agtProps.getProperty(newModel);
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
