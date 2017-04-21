/**
 * ConfigureTwitter.java
 * 20 apr 2017
 */
package lnu.agt.trying_things;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import lnu.agt.AGTProperties;
import twitter4j.ResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.conf.ConfigurationBuilder;

/**
 * @author jlnmsi
 *
 */
public class ConfigureTwitter {

	
	public static void main(String[] args) throws IllegalStateException, TwitterException {
		Properties localProps = AGTProperties.getLocalProperties();
		System.out.println(localProps);
		
		String keysPropFile = localProps.getProperty("twitterKeys");
		System.out.println(keysPropFile);
		
		Properties keyProps = AGTProperties.getProperties(keysPropFile);
		System.out.println(keyProps);
		
		String consumerKey = keyProps.getProperty("consumerKey");
		String consumerSecret = keyProps.getProperty("consumerSecret");
		String accessToken = keyProps.getProperty("accessToken");
		String accessTokenSecret = keyProps.getProperty("accessTokenSecret");
		
		
//		System.out.println(consumerKey);
//		System.out.println(consumerSecret);
//		System.out.println(accessToken);
//		System.out.println(accessTokenSecret);
		
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

		long userID = 1219865652L;      // Carl Bildt
		User user = lookupUser(twitter,userID);
		System.out.println(user);
		
		


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
			System.out.println("\n\t"+accountID+" ***** No user matches for specified terms (inside lookupUser)");
			e.printStackTrace();
		}
		return user;
	}
	

}
