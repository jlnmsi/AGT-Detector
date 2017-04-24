/**
 * UserTweetDownloadThread.java
 * 20 apr 2017
 */
package lnu.agt;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
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
	private int downloadCount = 0;
	private final Twitter twitter;
	
	 public UserTweetDownloadThread(LinkedBlockingQueue<Long> tweetQueue) {
		 queue = tweetQueue;
		 
		// Setup Twitter4j connection 
		twitter = connectTwitter4j();
	 }
	
	public void run() {
		try {
			while ( true ) {
				long userID = queue.take();
				User user = lookupUser(twitter,userID);
				if (user != null) {
					List<Status> tweets = null;
					try {
						tweets = getAvailableStatuses(twitter,user);
						
						for (Status s : tweets) {
							AGTStatus aStatus = AGTStatus.createFromStatus(s);
							System.out.println(aStatus);
						}
						
					} catch (TwitterException e) {
						String msg = e.getMessage();
						
						if (msg.contains("Authentication credentials")) {  // Access private account?
							System.err.println("Dropping private account "+userID+". Add substitution");
						}
						else if (msg.contains("Something is broken")) {  // Temporary download problem
							System.err.println("Temporary problem for account "
									+userID+", "+user.getName()+", enqueues it again and sleeps for 1 minute");
							
							// Add to queue again and wait 1 minute
							queue.add(userID);
							try { Thread.sleep(1*60*1000); } catch (InterruptedException e1) {e1.printStackTrace();}
						}
						else
							System.err.println("Unkown error: "+msg+" for account "
									+userID+", "+user.getName()+", enqueues it again and sleeps for 1 minute");
					}
					
					if (tweets != null) {
						downloadCount++;
						System.out.println(downloadCount+"\tDownload completed for user "+ 
											user.getId()+"\t"+user.getName()+", tweets: "+tweets.size());
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
		
		System.out.println("Connected to Twitter using account "+keyProps.getProperty("accountName"));
		
		return twitter;
	}
	
	private static User lookupUser(Twitter twitter, long accountID) {
		ResponseList<User> users = null;
		User user = null;
		try {
			users = twitter.lookupUsers(accountID);
			user = users.get(0);
			if (users.size()>1)
				System.err.println(accountID+"\t"+users.size());
		} catch (TwitterException e) {
			System.err.println(e.getMessage());
			System.err.println("No user matches for specified terms: "+accountID);
			//e.printStackTrace();
		}
		return user;
	}
	
	private static int pageCount = 0;
	private static List<Status> getAvailableStatuses(Twitter twitter,User u) throws TwitterException {
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
    		
    		System.out.print(" "+pageCount);
    		sz = statuses.size();
    		if (sz > 0) {
    			allStatuses.addAll(statuses);
    		}
    		
    		if (pageCount == 895) {
    			System.err.print("\nPage max reached at "+new Date());
				System.err.println(". Restarts again in 15 minutes");
//System.exit(-1);
				long sleep = 15*60*1000;
				try { Thread.sleep(sleep); } catch (InterruptedException e1) {e1.printStackTrace();}
				pageCount = 0;
    		}
    		
    		
    	} while(sz > 0);
    	//System.out.println();
    	//System.out.println("Found statuses: "+allStatuses.size());
		return allStatuses;
	}
}
