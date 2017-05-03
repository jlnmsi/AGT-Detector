/**
 * CreateTCArff.java
 * 30 apr 2017
 */
package lnu.agt.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import lnu.agt.AGTProperties;
import lnu.agt.ReadZipFiles;
import lnu.agt.TweetText;

import org.apache.commons.io.FileUtils;
import org.codehaus.jackson.JsonNode;

import weka.classifiers.Classifier;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.RandomForest;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

/**
 * Generate a Weka arff file tcTraining.arff for our text classifier 
 * based on the training set tcTrainingData.
 * 
 * @author jlnmsi
 *
 */
public class CreateTCArff {
	
	
	private static final int FOLDS = 10;
	private static final Classifier CLASSIFIER = new RandomForest();

	public static void main(String[] args) {
		// Read TC training data ==> setup tweetClassification mapping 
		HashMap<Long,Integer> tweetClassification = readClassification();
				
		// Read json tweets to setup tweetID-to-json mapping
		HashMap<Long,JsonNode> jsonTweets = readJsonTweets();
		
		Instances data = buildDataSet(tweetClassification,jsonTweets);
		
		System.out.println(data.toSummaryString());
		saveARFF(data);
	}
	
	private static void saveARFF(Instances data) {
		try {
			File out = new File("config/tcTraining.arff");
			String text = data.toString();
			
			FileUtils.writeStringToFile(out,text,(Charset) null);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public static FilteredClassifier setupFilteredClassifier( Classifier classifier  ) {
		FilteredClassifier fc = new FilteredClassifier();
		// making a classifier and taking care of instance ID
		Filter filter = new StringToWordVector();

		// meta-classifier
		fc.setFilter(filter);
		fc.setClassifier(classifier);		

		return fc;
	}
	
	private static Instances buildDataSet(HashMap<Long,Integer> tweetClassification, 
											HashMap<Long,JsonNode> jsonTweets) {
//		// Configure data set
//		ArrayList<Attribute> attributes = new ArrayList<Attribute> ();
//		List<String> values = new ArrayList<String>();
//		values.add("0"); values.add("1");
//		Attribute clz = new Attribute("class_attr",values) ;
//		Attribute text = new Attribute("text",(ArrayList<String>) null);
//		attributes.add( text ); attributes.add( clz );   
//		
//		Instances data = new Instances("tcTraining", attributes, 1);
//		data.setClass( clz );
		int agt = 0, hgt = 0;
		try {
			for (long tID : tweetClassification.keySet()) {
				String clazz = ""+tweetClassification.get(tID);
				JsonNode tweet = jsonTweets.get(tID);

				TweetText tweetText = new TweetText(tweet);
				String cleanText = tweetText.getCleanText();
				//System.out.println(clazz+"\t"+cleanText);
				
				Instance inst = new DenseInstance(2);
				inst.setValue(text, cleanText);
				inst.setValue(clz, clazz);
				data.add(inst);
				
				if (clazz.equals("1"))
					agt++;
				else hgt++;

			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
		System.out.println("AGT Count: "+agt+", HGT Count: "+hgt);
		return data;
	}

	private static HashMap<Long,JsonNode> readJsonTweets() {
		
		Properties localProps = AGTProperties.getLocalProperties();
		File zipFile = new File(localProps.getProperty("random1JsonZip"));
		ArrayList<JsonNode> tweets = ReadZipFiles.readZipFile(zipFile);

		HashMap<Long,JsonNode> jsonTweets = new HashMap<Long,JsonNode>();
		for (JsonNode tweet : tweets) {
			long tweetID = tweet.get("id").asLong();
			jsonTweets.put(tweetID,tweet);
		}
	
		System.out.println("Read "+jsonTweets.size()+
				" Json tweets from file "+zipFile.getAbsolutePath());
		
		return jsonTweets;
	}
	
	private static HashMap<Long,Integer> readClassification() {
		Properties agtProps = AGTProperties.getAGTProperties();
		File trainingData = new File(agtProps.getProperty("tcTrainingData"));
		
		HashMap<Long,Integer> tweetClassification = new HashMap<Long,Integer>();
		Scanner scanner = null;
		try {
		    scanner = new Scanner(trainingData);
		    while (scanner.hasNext()) {
		    	String row = scanner.nextLine();
		    	String[] items = row.split(" ");
		    	int classification = Integer.parseInt(items[0]);
		    	long tweetID = Long.parseLong(items[1]);
		    	tweetClassification.put(tweetID, classification);
		    }
		    
		} catch (FileNotFoundException e) {
		    e.printStackTrace();
		} finally {
		    if (scanner != null) {
		        scanner.close();
		    }
		}
		System.out.println("Read "+tweetClassification.size()+
				" tweet classifications from file "+trainingData.getAbsolutePath());
		return tweetClassification;
	}
	


}
