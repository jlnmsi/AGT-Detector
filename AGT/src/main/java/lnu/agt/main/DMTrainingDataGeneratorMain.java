/**
 * DMTrainingDataGeneratorMain.java
 * 26 apr 2017
 */
package lnu.agt.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Properties;
import java.util.Scanner;

import lnu.agt.AGTProperties;

/**
 * @author jlnmsi
 *
 */
public class DMTrainingDataGeneratorMain {
	
	private static HashMap<Long,Integer> tweetClassification = new HashMap<Long,Integer>();

	public static void main(String[] args) {
		// Read DM training data ==> setup tweetClassification mapping 
		readClassification();

	}
	
	private static void readClassification() {
		Properties agtProps = AGTProperties.getAGTProperties();
		File trainingData = new File(agtProps.getProperty("dmTrainingData"));
		double[] average = null;
		int tweetCount = 0;
		
		Scanner scanner = null;
		try {
		    scanner = new Scanner(trainingData);
		    while (scanner.hasNext()) {
		    	tweetCount++;
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
	}
	
	

}
