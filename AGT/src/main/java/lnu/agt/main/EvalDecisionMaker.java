/**
 * EvalDecisionMaker.java
 * 3 maj 2017
 */
package lnu.agt.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.Scanner;

import lnu.agt.AGTDetector;
import lnu.agt.AGTProperties;
import lnu.agt.DeviceProfiler;
import lnu.agt.ProfileGenerator;
import lnu.agt.ReadZipFiles;
import lnu.agt.TextClassifier;
import lnu.agt.UserProfile;

import org.codehaus.jackson.JsonNode;

/**
 * @author jlnmsi
 *
 */
public class EvalDecisionMaker {

	
	public static void main(String[] args) {
		// Read DM training data ==> setup tweetClassification mapping 
		HashMap<Long,Integer> tweetClassification = readClassification();

		// Read json tweets to setup tweetID-to-json mapping
		HashMap<Long,JsonNode> jsonTweets = readJsonTweets();
		
		AGTDetector agtDetector = new AGTDetector(false);		
		try {
			for (long tID : tweetClassification.keySet()) {
				int actual = tweetClassification.get(tID);
				JsonNode tweet = jsonTweets.get(tID);
				
				boolean predicted = agtDetector.isAGT(tweet);
				evalStat(tweet, tID, predicted, actual);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
		// Present summary
		System.out.println("\nInstances: "+tweetClassification.size()+", Correct: "+correct
				+", Incorrect: "+incorrect+", Dontknow: "+agtDetector.getDontknowCount());		
		printConfusionMatrix();
//		System.out.println("\nError Classifications \n"+misclassified);
		System.out.println("\nError Classifications \n"+out);
	}

	private static int humanHuman = 0, botBot = 0, predHumanActualBot = 0, predBotActualHuman = 0;
	private static int correct = 0, incorrect = 0;
	private static StringBuilder misclassified = new StringBuilder();
	public static void evalStat(JsonNode tweet, long tweetID, boolean isAgt, int actual) {
		int pred = isAgt?1:0;
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
			misclassified.append(actual + "\t" + pred + "\t" + tweetID + "\t" + text + "\n");
			logError(actual, pred, tweetID, tweet, text);
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
	
	private static ProfileGenerator profiler = new ProfileGenerator(false);
	private static TextClassifier textClassifier = new TextClassifier();
	private static StringBuilder out = new StringBuilder();
	private static void logError(int actual, int pred, long tweetID, JsonNode tweet, String text) {
		String itemSep = "\t";
		out.append(actual).append(itemSep);
		out.append(pred).append(itemSep);
		out.append(tweetID).append(itemSep);
		
		// Start collecting account properties
		String source = tweet.get("source").asText();
		int deviceType = DeviceProfiler.classifyDevice(source);
		double textProbability = textClassifier.getClassification(tweet);
		String textProb = String.format("%.3f",textProbability);
		long userID = tweet.get("user").get("id").asLong();
		UserProfile profile = profiler.getUserProfile(userID);
		boolean isDontknow = profile.userID == 0?true:false;
		double[] userProperties = profile.getProperties();


		out.append(userID).append(itemSep);
		out.append(isDontknow).append(itemSep);
		out.append(deviceType).append(itemSep);
		out.append(textProb).append(itemSep);
		for (double d : userProperties) {
			String dString = String.format("%.3f", d);
			out.append( dString ).append(itemSep);
		}

		out.append(text).append("\n");
	}
	

	private static HashMap<Long,Integer> readClassification() {
		Properties agtProps = AGTProperties.getAGTProperties();
		File trainingData = new File(agtProps.getProperty("trainingData10k"));
		
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
