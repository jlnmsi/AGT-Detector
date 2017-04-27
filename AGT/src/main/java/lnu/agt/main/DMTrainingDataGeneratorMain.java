/**
 * DMTrainingDataGeneratorMain.java
 * 26 apr 2017
 */
package lnu.agt.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.Scanner;

import lnu.agt.AGTProperties;
import lnu.agt.DeviceProfiler;
import lnu.agt.ProfileGenerator;
import lnu.agt.ReadZipFiles;
import lnu.agt.TextClassifier;
import lnu.agt.UserProfile;

import org.codehaus.jackson.JsonNode;

/**
 * The aim of this program is to generate training data for the Decision Maker.
 * The input is a classification list (tweetIDs,classification) and the output
 * is a decision making model
 * - config/decisionMakingModel.txt
 * containing, for each tweet, a text row with
 *    tweetID, deviceType, textProbability, ...numerical profile properties ..., classification
 * 
 * @author jlnmsi
 *
 */
public class DMTrainingDataGeneratorMain {


	public static void main(String[] args) {
		
		// Read DM training data ==> setup tweetClassification mapping 
		HashMap<Long,Integer> tweetClassification = readClassification();
		
		// Read json tweets to setup tweetID-to-json mapping
		HashMap<Long,JsonNode> jsonTweets = readJsonTweets();
		
		// Start building training data
		ProfileGenerator profiler = new ProfileGenerator(false);
		TextClassifier textClassifier = new TextClassifier();
		ArrayList<String> outputRows = new ArrayList<String>();
		for (long tweetID : tweetClassification.keySet()) {
			int classification = tweetClassification.get(tweetID);
			JsonNode tweet = jsonTweets.get(tweetID);
			
			String source = tweet.get("source").asText();
			int deviceType = DeviceProfiler.classifyDevice(source);
			
			double textProbability = textClassifier.getClassification(tweet);
			
			long userID = tweet.get("user").get("id").asLong();
			UserProfile profile = profiler.getUserProfile(userID);
			double[] userProperties = profile.getProperties();
			
			String row = buildOutputRow(classification,tweetID,deviceType,textProbability,userProperties);
			outputRows.add(row);
		}
		System.out.println("Don't know profile used for "+profiler.getUnknownCount()+" tweets");
		
		File outFile = new File("config/decisionMakingModel.txt");
		try {
			PrintWriter writer = new PrintWriter( outFile );
			for (String row : outputRows) {
				writer.println(row);
			}
			writer.close();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		System.out.println("Saved Decion Maker model for "+outputRows.size()+" tweet in "+outFile.getAbsolutePath());
			
		

	}
	
	private static String buildOutputRow(int classification, long tweetID, int deviceType, 
											double textProbability, double[] userProperties) {
		StringBuilder buf = new StringBuilder();
		String itemSep = " ";
		buf.append(tweetID);              // tweetID as first item
		buf.append(itemSep).append(deviceType);
		buf.append(itemSep).append(textProbability);
		for (double uProp : userProperties)
			buf.append(itemSep).append(uProp);
		buf.append(classification);     // classification as last item
		return buf.toString();
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

}
