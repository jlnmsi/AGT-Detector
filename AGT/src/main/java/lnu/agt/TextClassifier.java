package lnu.agt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Random;

import org.codehaus.jackson.JsonNode;

import weka.classifiers.Classifier;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader.ArffReader;

/**
 * @author jofrab
 *
 */

public class TextClassifier {

	Instances data;
	Classifier cls;
	Properties agtProps;
	String newModel = "";
	
	public static void main(String[] args) throws Exception {
		System.out.println("Working Directory: "+System.getProperty("user.dir"));
		
		Properties props = AGTProperties.getLocalProperties();
		File ntsDir = new File( props.getProperty("ntsDir") );
		TextClassifier tc = new TextClassifier();
		Random rand = new Random();
		
		ArrayList<File> ntsFiles = ReadZipFiles.findZipFiles( ntsDir );
		ArrayList<JsonNode> tweets = ReadZipFiles.readZipFile(ntsFiles.get(rand.nextInt(ntsFiles.size())));
		
		int i = 0;
		while(i < 3) {		
			int r  = rand.nextInt(tweets.size());
			//tc.setClassifier("modelNB"); uncomment for Naive Bayesian instead
			//tc.setClassifier("modelSMO"); uncomment for support vector machine instead
			TweetText tweetText = new TweetText(tweets.get(r));
			if(tweetText.getLang().equals("en")) {
				i++;
				System.out.println(tweetText.getText()
					+"\n"+tweetText.getCleanText() 
					+"\nP(AGT) = "+tc.getClassification(tweets.get(r))
					);
			}
		}

	}
	
	public TextClassifier()  {
		try {
		agtProps = AGTProperties.getAGTProperties();
		//String path = agtProps.getProperty("dummy");        // Jonas N
		String path = agtProps.getProperty("tcDummy");        // Jonas L
		
		//load dummy dataset to get correct format of Instances data
		BufferedReader reader = new BufferedReader(new FileReader(path));
		ArffReader arff = new ArffReader(reader);
		data = arff.getData();
		reader.close();
		
		//initilize classifier
		//String modelPath = agtProps.getProperty("modelRF");       // Jonas N
		String modelPath = agtProps.getProperty("tcModel");         // Jonas L
		cls = (Classifier) weka.core.SerializationHelper.read(modelPath);
		System.out.println("Used Classifier: "+cls.getClass().getName());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public TextClassifier(String model) throws Exception {
		agtProps = AGTProperties.getAGTProperties();
		//String path = agtProps.getProperty("dummy");	// Jonas N
		String path = agtProps.getProperty("tcDummy");    // Jonas L
		
		//load dummy dataset to get correct format of Instances data
		BufferedReader reader = new BufferedReader(new FileReader(path));
		ArffReader arff = new ArffReader(reader);
		data = arff.getData();
		reader.close();
		
		//initilize classifier
		String modelPath = agtProps.getProperty(model);;
		cls = (Classifier) weka.core.SerializationHelper.read(modelPath);
	}
	
	public void setClassifier(String model) throws Exception {
		cls = (Classifier) weka.core.SerializationHelper.read(model);
	}
	
	public double getClassification(JsonNode tweet) {
		try {
			TweetText tweetText = new TweetText(tweet);
			Instance inst = new DenseInstance(data.numAttributes());
			inst.setValue(data.attribute(0), data.attribute(0).addStringValue(tweetText.getCleanText()));
			data.add(0, inst);

			return cls.distributionForInstance(data.instance(0))[1];
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return -1.0;
	}

	public Classifier getClassifier(){
		return cls;
	}
	
	
}
