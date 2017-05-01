/**
 * AGTDetector.java
 * 1 maj 2017
 */
package lnu.agt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;

import org.codehaus.jackson.JsonNode;

import weka.classifiers.Classifier;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader.ArffReader;

/**
 * The primary goal of this project. A class that can predict whether a 
 * given JsonNode tweet is AGT or not
 * 
 * 
 * @author jlnmsi
 *
 */
public class AGTDetector {
	private final boolean downloadUsers;
	private Instances data; 
	private Classifier classifier;
	
	public AGTDetector(boolean downloadUsers) {
		this.downloadUsers = downloadUsers;
		
		classifier = setupClassifier();
	}
	
	/**
	 * Returns a tweet classification.
	 */
	private ProfileGenerator profiler = null;
	private TextClassifier textClassifier = null;
	private int dontknowCount = 0;
	public boolean isAGT(JsonNode tweet) {
		// Initialize help classes
		if (textClassifier == null) {
			profiler = new ProfileGenerator(downloadUsers);
			textClassifier = new TextClassifier();
		}
		
		// Start building training data
		long tID = tweet.get("id").asLong();
		String source = tweet.get("source").asText();
		int deviceType = DeviceProfiler.classifyDevice(source);

		double textProbability = textClassifier.getClassification(tweet);

		long userID = tweet.get("user").get("id").asLong();
		UserProfile profile = profiler.getUserProfile(userID);
		boolean isDontKnow = profile.userID == 0?true:false;
		if (isDontKnow) dontknowCount++;
		double[] userProperties = profile.getProperties();
		
		boolean clz = classify(tID,isDontKnow, deviceType, textProbability, userProperties);
//		System.out.println(clz);
		return clz;
	}
	
	public int getDontknowCount() { return dontknowCount; }
	
	private boolean classify(long tweetID, boolean isDontKnow, int deviceType, 
			double textProbability, double[] userProperties) {
		try {

			Instance inst = new DenseInstance(19);
//			inst.setValue( data.attribute(0), ""+tweetID);
//			inst.setValue( data.attribute(1), ""+ (isDontKnow?1:0) );
			inst.setValue( data.attribute(0), ""+ deviceType );
			inst.setValue( data.attribute(1), textProbability );
			for (int i=2; i<2+userProperties.length;i++) 
				inst.setValue( data.attribute(i), userProperties[i-2] );

			//inst.setValue( data.attribute(data.numAttributes()-1), ""+ classification );
			data.add(inst);
			inst.setDataset(data);

//			System.out.println(inst);
			//double prob = classifier.distributionForInstance(inst)[1];
			long clz = Math.round( classifier.classifyInstance(inst) );
			//System.out.println(prob + " ==> "+clz);
			return clz ==1;    // 1 ==> AFT
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;    // Should never happen!
	}
	
	
	/*
	 * Decision maker  
	 */
	private Classifier setupClassifier() {
		Classifier cls = null;
		try {
			// Load empty dataset to get correct format of Instances data
			DecisionProfile decisionProfile = new DecisionProfile();
			data = decisionProfile.getDataset();

			// initilize classifier		
			Properties agtProps = AGTProperties.getAGTProperties();
			String modelPath = agtProps.getProperty("dmModel");   // Model by Jonas L
			cls = (Classifier) weka.core.SerializationHelper.read(modelPath);
			
			System.out.println("Model: "+modelPath+", Used Classifier: "+cls.getClass().getName());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return cls;
	}

	// Demonstrator
	public static void main(String[] args) {
		AGTDetector agt = new AGTDetector(false);
		
		Properties localProps = AGTProperties.getLocalProperties();
		File zipFile = new File(localProps.getProperty("random1JsonZip"));
		ArrayList<JsonNode> tweets = ReadZipFiles.readZipFile(zipFile);

		int nAgt = 0, nHgt = 0;
		for (JsonNode tweet : tweets) {
			String text = tweet.get("text").asText();
			long tID = tweet.get("id").asLong();
//			if (tID == 819676709536628737L) {
			if (agt.isAGT(tweet) ) {
				nAgt++;
			}
			else
				nHgt++;
				//System.out.println(tID+"\t"+text.replaceAll("\n", " "));
//			}
		}
		System.out.println("AGT: "+(0.0+nAgt)/tweets.size()+", HGT: "+(0.0+nHgt)/tweets.size());
		System.out.println("DontknowCount: "+agt.getDontknowCount() );
	}
}
