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
	private DMArff dmArff;
	
	public AGTDetector(boolean downloadUsers) {
		this.downloadUsers = downloadUsers;
		
		dmArff = new DMArff();
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

		AGTStatus status = AGTStatus.createFromJson(tweet);
		
		int classification = -99; // Placeholder, will not be used

		// Tweet specific properties
		int deviceType = DeviceProfiler.classifyDevice( status.source );
		double textProbability = textClassifier.getClassification(tweet);
		int days = (int) ((status.createdAt - status.accountCreatedAt)/(1000 * 60 * 60 * 24));
		days = (days!=0)?days:1;   // account created today, return that it exists 1 day so we avoid dividing by 0
		double tweetsPerDay = (0.0+status.statusCount)/days;
		double favoritesPerDay = (0.0+status.favoritesCount)/days;

		// User profile properties
		UserProfile profile = profiler.getUserProfile( status.userId);
		double[] userProperties = profile.getProperties();
		if (profile.isDontKnow()) dontknowCount++;
		
		// Build instance and make classification
		try {
			Instance inst = dmArff.addInstance(data, classification,
                    deviceType,textProbability,tweetsPerDay,favoritesPerDay,
                    userProperties);
			inst.setDataset(data);
			long clz= Math.round( classifier.classifyInstance(inst) );
			return clz ==1;    // 1 ==> AFT
		} catch (Exception e) {
			e.printStackTrace();
		}
		throw new RuntimeException("Failed to make a classification!");
	}
	
	public int getDontknowCount() { return dontknowCount; }

	
	
	/*
	 * Decision maker  
	 */
	private Classifier setupClassifier() {
		Classifier cls = null;
		try {
			data = dmArff.getDummyArff();

			// initilize classifier		
			Properties agtProps = AGTProperties.getAGTProperties();
			String modelPath = agtProps.getProperty("dmModel");  
			cls = (Classifier) weka.core.SerializationHelper.read(modelPath);
			
			System.out.println("AGTDetector: Model: "+modelPath+", Used Classifier: "+cls.getClass().getName());
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
		File zipFile = new File(localProps.getProperty("random234JsonZip"));
		ArrayList<JsonNode> tweets = ReadZipFiles.readZipFile(zipFile);

		int nAgt = 0, nHgt = 0;
		for (JsonNode tweet : tweets) {
			String text = tweet.get("text").asText();
			long tID = tweet.get("id").asLong();
			if (agt.isAGT(tweet) ) {
				nAgt++;
			}
			else
				nHgt++;
		}
		System.out.println("\nAGT: "+(0.0+nAgt)/tweets.size()+", HGT: "+(0.0+nHgt)/tweets.size());
		System.out.println("DontknowCount: "+agt.getDontknowCount() );
	}
}
