/**
 * DMArff.java
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
 * 
@attribute deviceType {1,2,3,4,5,6}
@attribute textProbability numeric
@attribute tweetsPerDay numeric         // New!
@attribute favoritesPerDay numeric		// New!
@attribute tweetRatio numeric
@attribute chiSquaredMin numeric
@attribute chiSquaredSec numeric
@attribute entropyHours numeric
@attribute entropyMin numeric
@attribute entropySec numeric
@attribute accountRep numeric
@attribute urlRatio numeric
@attribute hashtagRatio numeric
@attribute mentionRatio numeric
@attribute retweetRatio numeric
@attribute mobileDevice numeric
@attribute webDevice numeric
@attribute appDevice numeric
@attribute miscDevice numeric
@attribute botDevice numeric
@attribute otherDevice numeric

@attribute class_attr {0,1}                        // class
 * 
 * @author jlnmsi
 *
 */
public class DMArff {

private final Instances dummy;
	
	public DMArff() {
		dummy = setupAttributes();
	}
	
	public Instances setupAttributes() {
		ArrayList<Attribute> attributes = new ArrayList<Attribute> ();

		// Add tweet specific data
		List<String> devices = new ArrayList<String>();
		devices.add("1"); devices.add("2");devices.add("3"); devices.add("4");devices.add("5"); devices.add("6"); 
		attributes.add( new Attribute("deviceType",devices) );
		attributes.add( new Attribute("textProbability") );
		attributes.add( new Attribute("tweetsPerDay") );
		attributes.add( new Attribute("favoritesPerDay") );
		
		// Add numeric user profile attributes
		attributes.add( new Attribute("tweetRatio") );
		attributes.add( new Attribute("chiSquaredMin") );
		attributes.add( new Attribute("chiSquaredSec") );
		attributes.add( new Attribute("entropyHours") );
		attributes.add( new Attribute("entropyMin") );
		attributes.add( new Attribute("entropySec") );
		attributes.add( new Attribute("accountRep") );
		attributes.add( new Attribute("urlRatio") );
		attributes.add( new Attribute("hashtagRatio") );
		attributes.add( new Attribute("mentionRatio") );
		attributes.add( new Attribute("retweetRatio") );
		attributes.add( new Attribute("replyRatio") );
		attributes.add( new Attribute("mobileDevice") );
		attributes.add( new Attribute("webDevice") );
		attributes.add( new Attribute("appDevice") );
		attributes.add( new Attribute("miscDevice") );
		attributes.add( new Attribute("botDevice") );
		attributes.add( new Attribute("otherDevice") );
		
		// Add class attribute
		List<String> classes = new ArrayList<String>();
		classes.add("0"); classes.add("1");
		Attribute clz = new Attribute("class_attr",classes) ; // number 21?
		attributes.add( clz);

		Instances data = new Instances("dmTraining", attributes, 1);
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

		// Start adding training data
		ProfileGenerator profiler = new ProfileGenerator(false);
		TextClassifier textClassifier = new TextClassifier();
		for (long tweetID : tweetClassification.keySet()) {

			JsonNode tweet = jsonTweets.get(tweetID);
			AGTStatus status = AGTStatus.createFromJson(tweet);
			
			int classification = tweetClassification.get(tweetID);

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


			Instance inst = addInstance(data, classification,
					                    deviceType,textProbability,tweetsPerDay,favoritesPerDay,
					                    userProperties);
		}
		System.out.println("DMArff: Don't know profile used for "+profiler.getDontknowCount()+" tweets");

		return data;
	}
	
	public Instance addInstance(Instances dataset, int classification,
								int deviceType, double textProbability, double tweetsPerDay, double favoritesPerDay,
								double[] userProperties) {

		Instance inst = new DenseInstance( dataset.numAttributes());
		inst.setValue( dataset.attribute(0), ""+ deviceType );
		inst.setValue( dataset.attribute(1), textProbability );
		inst.setValue( dataset.attribute(2), tweetsPerDay );
		inst.setValue( dataset.attribute(3), favoritesPerDay );
		for (int i=4; i<4+userProperties.length;i++) 
			inst.setValue( dataset.attribute(i), userProperties[i-4] );
		if (classification >= 0)
			inst.setValue( dataset.attribute(dataset.numAttributes()-1), ""+ classification );
		dataset.add(inst);
		return inst;
	}
	
	private HashMap<Long,Integer> readClassification() {
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
		System.out.println("DMArff: Read "+tweetClassification.size()+
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
	
		System.out.println("DMArff: Read "+jsonTweets.size()+
				" Json tweets from file "+zipFile.getAbsolutePath());
		
		return jsonTweets;
	}

	
	
	
	
	// Demonstrator
	public static void main(String[] args) {
		DMArff dm = new DMArff();
		System.out.println(dm);
		System.out.println(dm.getDummyArff());
		
		Instances training = dm.buildTrainingArff();
		saveArff(training);
		
	}
	
	private static void saveArff(Instances data) {
		try {
			File out = new File("config/dmTraining.arff");
			String text = data.toString();
			
			FileUtils.writeStringToFile(out,text,(Charset) null);
			System.out.println("Saved arff file as "+out.getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
