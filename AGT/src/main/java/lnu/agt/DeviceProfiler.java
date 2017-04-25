/**
 * DeviceProfile.java
 * 23 apr 2017
 */
package lnu.agt;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

/**
 * @author jlnmsi
 *
 */
public class DeviceProfiler {

	private static HashMap<String,Integer> deviceClassifier = new  HashMap<String,Integer>();
	
	// Initialize classifier 
	static {
		Properties agtProps = AGTProperties.getAGTProperties();
		File path = new File( agtProps.getProperty("deviceClassification") );
			
		try{
			Scanner scan = new Scanner(path);
			int n = 1;
			while(scan.hasNext()){
				String line = scan.nextLine();	
				if (line.length() <= 2) {
					continue;
				}
				
				String parts[] = line.split("\t");
				deviceClassifier.put(parts[0], Integer.parseInt(parts[1]));
			}
			scan.close();
			System.out.println("Initialized DeviceProfiler with "+deviceClassifier.size()+" new entries");
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	// Demonstrator
	public static void main(String[] args) {		
		String device = "TweetDeck";
		System.out.println(device+" --> "+deviceClassifier.get(device));
		device = "Twitter Web Client";
		System.out.println(device+" --> "+deviceClassifier.get(device));
		device = "Twitter for iPhone";
		System.out.println(device+" --> "+deviceClassifier.get(device));

	}
	
	public static double[] getDeviceProfile(List<AGTStatus> tweets) {
		double[] ratios = new double[6];
		int[] counts = {0,0,0,0,0,0};
		
		for (AGTStatus tweet : tweets) {		
			String deviceName = getDeviceName( tweet.source );
			Integer code = deviceClassifier.get(deviceName);
			if (code == null)  { // Unknown
				counts[5]++;
				//System.out.println(deviceName + " --> "+code);
			}
			else
				counts[code-1]++;	
		}
		int tweetCount = tweets.size();
		for (int i=0;i<counts.length;i++) {
			ratios[i] = (0.0+counts[i])/tweetCount;
		}		
//		System.out.println(Arrays.toString(counts)+" --> "+Arrays.toString(ratios));
//		System.exit(-1);
		return ratios;
	}
	
	public static String getDeviceName(String sourceUrlTag) {
		// Parse device name from string of type
		// <a href="http://twitter.com" rel="nofollow">Twitter Web Client</a>

		try {
			//System.out.println(sourceUrlTag);
			int firstGT = sourceUrlTag.indexOf(">");
			int lastLT = sourceUrlTag.lastIndexOf("<");
			String name = sourceUrlTag.substring(firstGT+1,lastLT);
			//System.out.println(name);
			return name.trim();
		}
		catch(Exception e) {
			System.err.println("Problem to parse name from "+sourceUrlTag);
			return "DeviceParseProblem"; 
		}

	}

}
