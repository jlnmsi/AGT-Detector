/**
 * EvalTextClassifier.java
 * 30 apr 2017
 */
package lnu.agt.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.Scanner;

import lnu.agt.AGTProperties;
import lnu.agt.ReadZipFiles;
import lnu.agt.TextClassifier;
import lnu.agt.TweetText;

import org.codehaus.jackson.JsonNode;

import weka.classifiers.Classifier;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader.ArffReader;

/**
 * Goal: Evaluate my text classifier (constructed in TrainTextClassifier, 
 * using tcTraining) using tweets from dmTraining. I am using Jonas N's
 * idea for how to recreate a text classifier and certain parts of 
 * Antonio's ideas to present the evaluation result. 
 * 
 * 
 * @author jlnmsi
 *
 */
public class EvalTextClassifier {


	public static void main(String[] args) {
		// Read DM training data ==> setup tweetClassification mapping 
		HashMap<Long,Integer> tweetClassification = readClassification();
				
		// Read json tweets to setup tweetID-to-json mapping
		HashMap<Long,JsonNode> jsonTweets = readJsonTweets();
		
		// Restore classifier from arff file
		Classifier cls = setupClassifier();
		
		try {
			for (long tID : tweetClassification.keySet()) {
				int actual = tweetClassification.get(tID);
				JsonNode tweet = jsonTweets.get(tID);
				
				double predicted = getClassification(cls,tweet);
				
				evalStat(tweet, tID, predicted, actual);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
		// Present summary
		System.out.println("\nInstances: "+tweetClassification.size()+", Correct: "+correct+", Incorrect: "+incorrect);		
		printConfusionMatrix();
		System.out.println("\nError Classifications \n"+misclassified);

	}
	
	private static double getClassification(Classifier cls, JsonNode tweet) {
		try {
			TweetText tweetText = new TweetText(tweet);
			String cleanText = tweetText.getCleanText();
			Instance inst = new DenseInstance(2);
			inst.setDataset(data);
			inst.setValue(0,cleanText);

			return cls.distributionForInstance(inst)[1];
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(-1);
		return -1.0;
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
	
	private static Instances data; 
	private static Classifier setupClassifier() {
		Classifier cls = null;
		try {
			//load dummy dataset to get correct format of Instances data
			Properties agtProps = AGTProperties.getAGTProperties();
			String path = agtProps.getProperty("tcDummy");
			BufferedReader reader = new BufferedReader(new FileReader(path));
			ArffReader arff = new ArffReader(reader);
			data = arff.getData();
			reader.close();

			//initilize classifier		
			String modelPath = agtProps.getProperty("tcModel");   // Model by Jonas L
			cls = (Classifier) weka.core.SerializationHelper.read(modelPath);
			
			System.out.println("Dummy: "+path+", Model: "+modelPath);
			System.out.println("Used Classifier: "+cls.getClass().getName());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return cls;
	}
	
	private static HashMap<Long,Integer> readClassification() {
		Properties agtProps = AGTProperties.getAGTProperties();
		File trainingData = new File(agtProps.getProperty("dmTrainingData"));
		
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

	
	private static int humanHuman = 0, botBot = 0, predHumanActualBot = 0, predBotActualHuman = 0;
	private static int correct = 0, incorrect = 0;
	private static StringBuilder misclassified = new StringBuilder();
	public static void evalStat(JsonNode tweet, long tweetID, double predicted, int actual) {
		int pred = (int) Math.round(predicted);
		String text = tweet.get("text").asText();
		text = text.replaceAll("\n", " ").replaceAll("\t", " ");
		if (pred == actual){
			correct++;
			if( pred == 0 ){
				humanHuman++;
			}
			else {
				botBot++;
			}
		}
		else {  // Classification error
			misclassified.append(actual + "\t" + predicted + "\t" + tweetID + "\t" + text + "\n");
			incorrect++;
			
			if(pred == 1){
				predBotActualHuman++;
			}
			else 
				predHumanActualBot++;
		}
	}
	
	public static void printConfusionMatrix(){
		System.out.println("\n***** Confusion Matrix *******");
		final Object[][] table = new String[3][];
		table[0] = new String[] { "\t", "actual human", "actual AGT" };
		table[1] = new String[] { "predicted human", Integer.toString(humanHuman), Integer.toString(predHumanActualBot) };
		table[2] = new String[] { "predicted AGT", Integer.toString(predBotActualHuman), Integer.toString(botBot) };

		for (final Object[] row : table) {
		    System.out.format("%15s%15s%15s\n", row);
		}
	}

}
