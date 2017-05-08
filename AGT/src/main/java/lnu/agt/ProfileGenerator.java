/**
 * LockupUser.java
 * 19 apr 2017
 */
package lnu.agt;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author jlnmsi
 *
 */
public class ProfileGenerator {
	// Thread communication
	private final ConcurrentHashMap<Long,UserProfile> user2profile = new ConcurrentHashMap<Long, UserProfile>();
	private final LinkedBlockingQueue<Long> downloadQueue = new LinkedBlockingQueue<Long>();
	
	private UserProfile dontknow;
	private final int CountToStartDownload = 3; // Tweets to be seen before Tweet download start. 
	private final UserTweetDownloadThread downloadThread;
	private final boolean Download_Profile; 
	
	public ProfileGenerator(boolean downloadUserProfile) {
		Download_Profile = downloadUserProfile;
		
		// Init table by reading previously stored user-to-profile info 
		initUserToProfileMapping();
		
		// Check "Don't Know" user profile 
		if (dontknow == null)
			throw new RuntimeException("\"Don't Know\" profile not found!");
		
		// Start download thread
		if (Download_Profile) {
			downloadThread = new UserTweetDownloadThread(downloadQueue, user2profile, dontknow);
			downloadThread.start();
		}
		else
			downloadThread = null;
	}
	
	
	private HashMap<Long,Integer> user2count = new HashMap<Long,Integer>();
	int downloadCount = 0;
	int dontknownCount = 0;
	public UserProfile getUserProfile(long uID) {
		UserProfile user = user2profile.get(uID);
		Integer count = user2count.get(uID);
		
		if (user == null) { // No profile available ==> return "don't know" while processing
			dontknownCount++;
			if (count == null) {  // First contact with user
				count = 1;
			}
			else {  // Update count
				count++;
				
			}
			user2count.put(uID, count);
			
			if (Download_Profile && count == CountToStartDownload) {  // Start download user tweet
				downloadCount++;
				downloadQueue.add(uID);
				//user2profile.put(uID, dontknow);
				
			}
			
			//System.out.println("No profile available for user "+uID+", count: "+count);	
			return dontknow;
		}
		else 
			return user;
		
	}
	
	public int getDownloadCount() { return downloadCount; }
	public int getDontknowCount() { return dontknownCount; }
	public UserProfile getDontknowProfile() { return dontknow; }
	
	public ArrayList<UserProfile> getAvailableProfiles() {
		ArrayList<UserProfile> allProfiles = new ArrayList<UserProfile>();
		for (long uID : user2profile.keySet()) {
			UserProfile up = user2profile.get(uID);
			allProfiles.add(up);
		}
		return allProfiles;
	}
	
	
	private void initUserToProfileMapping() {
		Properties agtProps = AGTProperties.getAGTProperties();
		File userProfiles;
		if (Download_Profile)
			userProfiles = new File(agtProps.getProperty("allUserProfiles"));
		else 
			userProfiles = new File(agtProps.getProperty("userProfiles"));
		double[] average = null;
		int userCount = 0;
		
		Scanner scanner = null;
		try {
		    scanner = new Scanner(userProfiles);
		    while (scanner.hasNext()) {
		    	userCount++;
		    	String row = scanner.nextLine();
		    	String[] items = row.split("\t");
		    	long userID = Long.parseLong(items[0]);
		    	String userName = items[1];
		    	
		    	// Setup average
		    	if (average == null) {
		    		average = new double[items.length-2];
		    		Arrays.fill(average, 0.0);
		    	}
		        
		        double[] profileProps = new double[items.length-2];
		        for (int i = 0; i<profileProps.length; i++) {
		        	double prop = Double.parseDouble( items[i+2]);
		        	profileProps[i] = prop;
		        	average[i] += prop;
		        }
		        	
		        
		        UserProfile profile = new UserProfile(userID,userName,profileProps);
		        user2profile.put(userID, profile);
		        
		    }
		} catch (FileNotFoundException e) {
		    e.printStackTrace();
		} finally {
		    if (scanner != null) {
		        scanner.close();
		    }
		}
		
		// Compute averages ==> "Don't Know!"
		for (int i=0;i<average.length;i++) {
			average[i] = average[i]/userCount;
		}
		dontknow = new UserProfile(0,"Don't Know",average);
		dontknow.setDontknow(true);
		user2profile.put(0L, dontknow);
		//System.out.println("Don't Know: "+dontknow);
		
		System.out.println("ProfileGenerator: Initialized "+user2profile.size()+" user profiles from repository " +userProfiles.getAbsolutePath());
	}
	
	
	
	
	// Demonstrator
	public static void main(String[] args) {
		// Start non-downloading generator 
		ProfileGenerator profGen = new ProfileGenerator(false);
		
		long uID = 1002031308;
		UserProfile profile = profGen.getUserProfile(uID);
		System.out.println(profile);
		
		uID = 99942447;
		profile = profGen.getUserProfile(uID);
		System.out.println(profile);
		
		uID = 0;    // Don't Know
		profile = profGen.getUserProfile(uID);
		System.out.println(profile);
		
		ArrayList<UserProfile> allProfiles = profGen.getAvailableProfiles();
		System.out.println("Available profiles: "+ allProfiles.size());
		
		// Try to lookup all users in Random 1
		System.out.println("\nLookup users in unique users set 1");
		Properties local = AGTProperties.getLocalProperties();
		File uniqueUsers = new File( local.getProperty("uniqueUsers") );
		
		try {
			Scanner sc = new Scanner(uniqueUsers);
			while (sc.hasNext()) {
				long userID = Long.parseLong( sc.nextLine());
				UserProfile userProfile = profGen.getUserProfile(userID);
				//System.out.println(userProfile);
			}
			sc.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		
	}
	
	

}
