/**
 * UserTweetDownloadThread.java
 * 20 apr 2017
 */
package lnu.agt;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.conf.ConfigurationBuilder;

/**
 * @author jlnmsi
 *
 */
public class UserTweetDownloadThread extends Thread {
	private final LinkedBlockingQueue<Long> queue;
	private final ConcurrentHashMap<Long,UserProfile> user2profile;
	private int downloadCount = 0;
	private final UserProfile dontknow;
	private final Twitter twitter;
	private final FileWriter outWriter; 
	
	 public UserTweetDownloadThread(LinkedBlockingQueue<Long> tweetQueue,
			 						ConcurrentHashMap<Long,UserProfile> user2prof,
			 						UserProfile dontknow) {
		 queue = tweetQueue;
		 user2profile = user2prof;
		 this.dontknow = dontknow;
		 
		// Setup Twitter4j connection 
		twitter = connectTwitter4j();
		
		// Connect to user_profiles.txt to append more profiles
		outWriter = setupProfileStorage();
		
	 }
	
	public void run() {
		try {
			while ( true ) {
				long userID = queue.take();
				User user = lookupUser(twitter,userID);
				if (user != null) {   // A new user found
					
					try {
						List<Status> statuses4j = getAvailableStatuses(twitter,user);
						List<AGTStatus> tweets = new ArrayList<AGTStatus>();
						for (Status s : statuses4j) {
							AGTStatus aStatus = AGTStatus.createFromStatus(s);
							tweets.add(aStatus);
						}
						downloadCount++;
						System.out.println(downloadCount+"\tDownload completed for user "+ 
											user.getId()+"\t"+user.getScreenName()+", tweets: "+tweets.size());
						UserProfile uProf = new UserProfile(tweets);
						saveUserProfile(userID,uProf);
						
						
					} catch (TwitterException e) {
						String msg = e.getMessage();
						
						if (msg.contains("Authentication credentials")) {  // Access private account?
							System.err.println("Dropping private account "+userID+". Adds DontKnow as replacer");
							saveUserProfile(userID,dontknow);
						}
						else if (msg.contains("Something is broken")) {  // Temporary download problem
							System.err.println("Temporary problem for account "
									+userID+", "+user.getName()+", enqueues it again and sleeps for 1 minute");			
							// Add to queue again and wait 1 minute if queue is almost empty
							if (queue.size() < 3)
								try { Thread.sleep(1*60*1000); } catch (InterruptedException e1) {e1.printStackTrace();}
							queue.add(userID);
							
						}
						else {
							System.err.println("Unkown error: "+msg+" for account "
									+userID+", "+user.getName()+", enqueues it again and sleeps for 1 minute");
							// Add to queue again and wait 1 minute
							queue.add(userID);
							try { Thread.sleep(1*60*1000); } catch (InterruptedException e1) {e1.printStackTrace();}
						}
					}
				}
			}

		} catch (InterruptedException ex) { ex.printStackTrace();}
		
	}
	
	private Twitter connectTwitter4j() {
		Properties localProps = AGTProperties.getLocalProperties();
		String keysPropFile = localProps.getProperty("twitterKeys");
		Properties keyProps = AGTProperties.getProperties(keysPropFile);
		
		String consumerKey = keyProps.getProperty("consumerKey");
		String consumerSecret = keyProps.getProperty("consumerSecret");
		String accessToken = keyProps.getProperty("accessToken");
		String accessTokenSecret = keyProps.getProperty("accessTokenSecret");
		
		// Connect to Twitter
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true)
		.setOAuthConsumerKey(consumerKey)
		.setOAuthConsumerSecret(consumerSecret)
		.setOAuthAccessToken(accessToken)
		.setOAuthAccessTokenSecret(accessTokenSecret);
		cb.setJSONStoreEnabled(true);
		TwitterFactory tf = new TwitterFactory(cb.build());
		Twitter twitter = tf.getInstance();
		
		System.out.println("TweetDownload: Connected to Twitter using account "+keyProps.getProperty("accountName"));
		
		return twitter;
	}
	
	private User lookupUser(Twitter twitter, long accountID) {
		ResponseList<User> users = null;
		User user = null;
		try {
			users = twitter.lookupUsers(accountID);
			user = users.get(0);
			if (users.size()>1)
				System.err.println(accountID+"\t"+users.size());
		} catch (TwitterException e) {
			//System.err.println(e.getMessage());
			System.err.println("No user matches for specified terms: "+accountID+". Adds DontKnow as replacer");
			saveUserProfile(accountID,dontknow);
			//e.printStackTrace();
		}
		return user;
	}
	
	private int pageCount = 0;
	private List<Status> getAvailableStatuses(Twitter twitter,User u) throws TwitterException {
		ArrayList<Status> allStatuses = new ArrayList<Status>();
		
		long id = u.getId();
    	int sz = 0; 
    	int page = 0;
    	int pageSize = 200;
    	do {
    		page++;
    		pageCount++;
    		
    		Paging paging = new Paging(page, pageSize);
    		List<Status> statuses = twitter.getUserTimeline(id,paging);
    		
//    		System.out.print(" "+pageCount);
    		sz = statuses.size();
    		if (sz > 0) {
    			allStatuses.addAll(statuses);
    		}
    		
    		if (pageCount == 895) {
    			System.err.print("\nPage max reached at "+new Date());
				System.err.print(". Restarts again in 10 minutes");
				System.err.println(". Current queue size: "+queue.size());
//System.exit(-1);
				long sleep = 10*60*1000;
				try { Thread.sleep(sleep); } catch (InterruptedException e1) {e1.printStackTrace();}
				pageCount = 0;
    		}
    		
    		
    	} while(sz > 0);
    	//System.out.println();
    	//System.out.println("Found statuses: "+allStatuses.size());
		return allStatuses;
	}
	
	private FileWriter setupProfileStorage() {
		Properties props = AGTProperties.getAGTProperties();
		File outFile =  new File( props.getProperty("allUserProfiles") );

		FileWriter fw = null;
		try{
			fw = new FileWriter(outFile,true);
			System.out.println("Data successfully appended at the end of file");

		}catch(IOException ioe){
			ioe.printStackTrace();
		}
		return fw;
	}
	
	private void saveUserProfile(long userID, UserProfile uProf) {
		user2profile.put(userID,uProf);
		try {
			String asRow = uProf.asRow('\t');
			outWriter.write( asRow +"\n" );
			outWriter.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
