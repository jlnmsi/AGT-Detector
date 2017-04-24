/**
 * DeviceMain.java
 * 22 apr 2017
 */
package lnu.agt.trying_things;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import lnu.agt.AGTProperties;
import lnu.agt.ReadZipFiles;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;

/**
 * @author jlnmsi
 *
 */
public class DeviceMain {


	public static void main(String[] args) {
		System.out.println("Working Directory: "+System.getProperty("user.dir"));
		
		Properties props = AGTProperties.getLocalProperties();
		File ntsDir = new File( props.getProperty("userTweets1") );
		
		
		ArrayList<File> ntsFiles = ReadZipFiles.findZipFiles( ntsDir );
		HashMap<String,Integer> device2count = new HashMap<String,Integer>();
		for (File f : ntsFiles) {
			if (!f.getName().startsWith("10"))   // A single zip file
				continue;
			
			ArrayList<JsonNode> tweets = ReadZipFiles.readZipFile(f);
			
			for (JsonNode tweet : tweets) {
				String source = tweet.get("source").asText();
				String deviceName = getDeviceName(source);
				
				Integer count = device2count.get(deviceName);
				if (count == null) 
					device2count.put(deviceName, 1);
				else {
					count++;
					device2count.put(deviceName, count);
				}
				
			}
			
		}
		
		// Print result
		System.out.println("\nDevice Count: "+device2count.size());
		for (String name : device2count.keySet()) {
			int count = device2count.get(name);
			System.out.println(name+"\t"+count);
		}
		
		System.out.println("\nDevice parse problems: "+device2count.get("DeviceParseProblem"));

	}
	
	public static String getDeviceName(String sourceUrlTag) {
		// Parse device name from string of type
		// <a href="http://twitter.com" rel="nofollow">Twitter Web Client</a>
		
		try {
		//System.out.println(sourceUrlTag);
		int firstGT = sourceUrlTag.indexOf(">");
		String name = sourceUrlTag.substring(firstGT+1);
		//System.out.println(name);
		
		int firstLT = name.indexOf("<");
		name = name.substring(0,firstLT);
		//System.out.println(name);
		return name.toString();
		}
		catch(Exception e) {
			//System.err.println("Problem to parse name from "+sourceUrlTag);
			return "DeviceParseProblem"; 
		}
		
	}

}
