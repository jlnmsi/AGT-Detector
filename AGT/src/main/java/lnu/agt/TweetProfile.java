/**
 * MetadataProfile.java
 * 6 maj 2017
 */
package lnu.agt;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

import org.codehaus.jackson.JsonNode;

import twitter4j.User;

/**
 * @author jlnmsi
 *
 */
public class TweetProfile {

	private double[] profile = new double[9];
	private AGTStatus status;

	public TweetProfile(AGTStatus s){
		status = s;
		profile[0] = this.getDevice();
		profile[1] = this.isReply();
		profile[2] = this.isRetweet();
		profile[3] = this.getHashtagDensity();
		profile[4] = this.getURLDensity();
		profile[5] = this.getUserMentionDensity();
		profile[6] = this.accountReputation();
		profile[7] = this.averageTweetsPublishedPerDay();
		profile[8] = this.averageTweetsFavoritedPerDay();
	}
	
	public double[] getProfile() { return profile; }


	private int getDevice(){
		String sourceURLTag = status.source;
		return DeviceProfiler.classifyDevice(sourceURLTag);		
	}

	private int isReply(){
		return status.isReply?1:0;
	}


	private int isRetweet(){
		return status.isRetweet?1:0;
	}

	
	private String[] getCleanTweet(){
		return status.cleanText.split(" ");
	}

	private double getHashtagDensity(){
		int hashtagNum = status.hashtagCount;
		return (double) hashtagNum/this.getCleanTweet().length;		//here I divide by number of words	
	}

	private double getURLDensity(){
		int URLNum = status.urlCount;
		return (double) URLNum/this.getCleanTweet().length;		//here I divide by number of words	
	}

	private double getUserMentionDensity(){
		int userMentionNum = status.mentionCount;
		return (double) userMentionNum/this.getCleanTweet().length;		//here I divide by number of words	
	}

	private double averageTweetsPublishedPerDay(){
		int days = this.daysSinceCreation();
		int totalTweets = status.statusCount;
		return (0.0+totalTweets)/days;
	}

	private double averageTweetsFavoritedPerDay(){
		int totalFavorited = status.favoritesCount;
		int days = this.daysSinceCreation();
		return (0.0+totalFavorited)/days;
	}

	private double accountReputation(){
		return (double) status.followersCount/(status.followersCount + status.friendsCount);
	}

	private int daysSinceCreation(){
		int days = (int) ((status.createdAt - status.accountCreatedAt)/(1000 * 60 * 60 * 24));
		return (days != 0)?days:1;   // account created today, return that it exists 1 day so we avoid dividing by 0

	}


	public String toString(){
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < profile.length; i++){
			sb.append(profile[i] + "  ");
		}
		return sb.toString();
	}

	public static void main(String[] args) {
		System.out.println("Working Directory: "+System.getProperty("user.dir"));

		Properties props = AGTProperties.getLocalProperties();
		File zipDir = new File( props.getProperty("random1JsonZip") );
		ArrayList<JsonNode> tweets = ReadZipFiles.readZipFile( zipDir);

		int count = 0;
		for (JsonNode tweet : tweets) {
			// Select a few users
			JsonNode user = tweet.get("user");
			long userID = user.get("id").asLong();
			if ( !(""+userID).startsWith("10") ) continue;


			// Print metadata profiles 
			AGTStatus aStatus = AGTStatus.createFromJson(tweet);
			//System.out.println(aStatus);
			TweetProfile mProfile = new TweetProfile(aStatus);			
			System.out.println(mProfile);

			count++;
		}
		System.out.println("Count: "+count);
	}

}
