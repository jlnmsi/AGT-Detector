/**
 * TCArff.java
 * 2 maj 2017
 */
package lnu.agt;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;
import org.codehaus.jackson.JsonNode;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

/**
 * @author jlnmsi
 *
 */
public class TCArff {

	private final Instances dummy;
	
	public TCArff() {
		dummy = setupAttributes();
	}
	
	public Instances setupAttributes() {
		// Configure attribute set
		ArrayList<Attribute> attributes = new ArrayList<Attribute> ();
		List<String> values = new ArrayList<String>();
		values.add("0"); values.add("1");
		Attribute clz = new Attribute("class_attr",values) ;
		Attribute text = new Attribute("text",(ArrayList<String>) null);
		attributes.add( text ); attributes.add( clz );   

		Instances data = new Instances("tcTraining", attributes, 1);
		data.setClass( clz );
		return data;
	}
	
	public Instances getDummyArff() { return new Instances(dummy); }  // Deep copy?
	
	@Override
	public String toString() { 
		return "TCArrf, Name: "+dummy.relationName()+", Attributes: "+dummy.numAttributes();
	}
	
	
	public Instances buildTrainingArff() {
		// Read TC training data ==> setup tweetClassification mapping 
		HashMap<Long,Integer> tweetClassification = readClassification();
						
		// Read json tweets to setup tweetID-to-json mapping
		HashMap<Long,JsonNode> jsonTweets = readJsonTweets();
		
		// Initialize empty instance
		Instances data = new Instances(dummy);
		
		int agt = 0, hgt = 0;
		try {
			for (long tID : tweetClassification.keySet()) {
				String clazz = ""+tweetClassification.get(tID);
				JsonNode tweet = jsonTweets.get(tID);

				TweetText tweetText = new TweetText(tweet);
				String cleanText = tweetText.getCleanText();
				//System.out.println(clazz+"\t"+cleanText);
				
				Instance inst = new DenseInstance(2);
				inst.setValue(data.attribute(0), cleanText);
				inst.setValue(data.attribute(1), clazz);
				data.add(inst);
				
				if (clazz.equals("1"))
					agt++;
				else hgt++;

			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
		System.out.println("TCArff: Added Instances: "+data.numInstances()+", AGT Count: "+agt+", HGT Count: "+hgt);
		
		return data;
	}
	
	private HashMap<Long,JsonNode> readJsonTweets() {
		
		Properties localProps = AGTProperties.getLocalProperties();
		File zipFile = new File(localProps.getProperty("random1JsonZip"));
		ArrayList<JsonNode> tweets = ReadZipFiles.readZipFile(zipFile);

		HashMap<Long,JsonNode> jsonTweets = new HashMap<Long,JsonNode>();
		for (JsonNode tweet : tweets) {
			long tweetID = tweet.get("id").asLong();
			jsonTweets.put(tweetID,tweet);
		}
	
		System.out.println("TCArff: Read "+jsonTweets.size()+
				" Json tweets from file "+zipFile.getAbsolutePath());
		
		return jsonTweets;
	}
	
	private HashMap<Long,Integer> readClassification() {
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
		System.out.println("TCArff: Read "+tweetClassification.size()+
				" tweet classifications from file "+trainingData.getAbsolutePath());
		return tweetClassification;
	}
	
	
	
	

	// Demonstrator + Arff generator
	public static void main(String[] args) {
		TCArff tc = new TCArff();
		System.out.println(tc);
		System.out.println(tc.getDummyArff());
		
		Instances training = tc.buildTrainingArff();
		System.out.println(training.numInstances());
		saveArff(training);
		
	}
	
	private static void saveArff(Instances data) {
		try {
			File out = new File("config/tcTraining10k.arff");
			String text = data.toString();
			
			FileUtils.writeStringToFile(out,text,(Charset) null);
			System.out.println("Saved arff file as "+out.getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
