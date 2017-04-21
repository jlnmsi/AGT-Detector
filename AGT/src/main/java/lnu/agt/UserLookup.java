/**
 * LockupUser.java
 * 19 apr 2017
 */
package lnu.agt;

import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.conf.ConfigurationBuilder;

/**
 * @author jlnmsi
 *
 */
public class UserLookup {
	// Thread communication
	private final ConcurrentHashMap<Long,UserProfile>user2profile = new ConcurrentHashMap<Long, UserProfile>();
	private final LinkedBlockingQueue<Long> downloadQueue = new LinkedBlockingQueue<Long>();
	
	private final UserProfile dontknow;
	private final int CountToStartDownload = 5; // Tweets to be seen before Tweet download start. 
	private final UserTweetDownloadThread downloadThread;
	
	public UserLookup() {
		/*
		 * 1. init table by reading previously stored user-to-profile info 
		 * 2. Create "Don't know" user profile
		 * 
		 */
		dontknow = new UserProfile(0,"Don't Know!");
		
		
		
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
			
			if (count == CountToStartDownload) {  // Start download user tweet
				downloadCount++;
				downloadQueue.add(uID);
				
			}
			else {
				//System.out.println(uID+"\t"+count);
			}	
			return dontknow;
		}
		else 
			return user;
		
	}
	
	public int getDownloadCount() { return downloadCount; }
	
	

}
