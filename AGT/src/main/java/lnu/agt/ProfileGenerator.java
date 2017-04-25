/**
 * LockupUser.java
 * 19 apr 2017
 */
package lnu.agt;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
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
	private final ConcurrentHashMap<Long,UserProfile>user2profile = new ConcurrentHashMap<Long, UserProfile>();
	private final LinkedBlockingQueue<Long> downloadQueue = new LinkedBlockingQueue<Long>();
	
	private UserProfile dontknow = null;
	private final int CountToStartDownload = 5; // Tweets to be seen before Tweet download start. 
	private final UserTweetDownloadThread downloadThread;
	private final boolean Download_Profile; 
	
	public ProfileGenerator(boolean downloadUserProfile) {
		Download_Profile = downloadUserProfile;
		
		// Init table by reading previously stored user-to-profile info 
		initUserToProfileMapping();
		
		// ToDo Create "Don't Know" user profile 
//		dontknow = new UserProfile(0,"Don't Know!");
		
		
		
		// Start download thread
		downloadThread = new UserTweetDownloadThread(downloadQueue);
		downloadThread.start();
	}
	
	
	private HashMap<Long,Integer> user2count = new HashMap<Long,Integer>();
	int downloadCount = 0;
	public UserProfile getUserProfile(long uID) {
		UserProfile user = user2profile.get(uID);
		Integer count = user2count.get(uID);
		
		if (user == null) { // No profile available ==> return "don't know" while processing
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
				
			}
			
			System.out.println("No profile available for "+uID+", count: "+count);	
			return dontknow;
		}
		else 
			return user;
		
	}
	
	public int getDownloadCount() { return downloadCount; }
	
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
		File userProfiles = new File(agtProps.getProperty("userProfiles"));
		
		
		Scanner scanner = null;
		try {
		    scanner = new Scanner(userProfiles);
		    while (scanner.hasNext()) {
		    	String row = scanner.nextLine();
		    	String[] items = row.split("\t");
		    	long userID = Long.parseLong(items[0]);
		    	String userName = items[1];
		        //System.out.println(userID+"\t"+userName);
		        
		        double[] profileProps = new double[items.length-2];
		        for (int i = 0; i<profileProps.length; i++)
		        	profileProps[i] = Double.parseDouble( items[i+2]);
		        
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
		
		System.out.println("Initialized "+user2profile.size()+" from repository " +userProfiles.getAbsolutePath());
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
		
		ArrayList<UserProfile> allProfiles = profGen.getAvailableProfiles();
		System.out.println("Available profiles: "+ allProfiles.size());
		
	}
	
	

}
