package lnu.agt;

import java.util.List;

import twitter4j.Status;

public class AccountProperties {

	/*acc. reputation, URL ratio, hashtag ratio, mention ratio, retweet ratio*/
	private double[] accountProperties = new double[6];
	
	public AccountProperties(List<AGTStatus> tweets){
		accountReputation(tweets);
		computeRatios(tweets);
		
	}
	
	private void accountReputation(List<AGTStatus> tweets){
		int friends = tweets.get(0).friendsCount;
		int followers = tweets.get(0).followersCount;
		double accountReputation = (double) followers/(friends + followers);
		
		accountProperties[0] = accountReputation;
	}
	
	private void computeRatios(List<AGTStatus> tweets){
		double retweetRatio = 0;
		double hashRatio = 0;
		double mentionRatio = 0;
		double URLRatio = 0;
		double replyRatio = 0;
		int ownTweets = 0;
		

		for(AGTStatus s : tweets){
			if (s.isReply)
				replyRatio++;
			if(s.isRetweet){
				retweetRatio++;
			}
			else{
				hashRatio += s.hashtagCount;
				mentionRatio += s.mentionCount;
				URLRatio += s.urlCount;
				ownTweets++;
			}
		}
		
		
		
		retweetRatio = retweetRatio/tweets.size();
		replyRatio = replyRatio/tweets.size();
		if (ownTweets > 0) {			
			hashRatio = hashRatio/ownTweets;
			mentionRatio = mentionRatio/ownTweets;
			URLRatio = URLRatio/ownTweets;
		}
		else {
			hashRatio = 0;
			mentionRatio = 0;
			URLRatio = 0;
		}
		
		accountProperties[1] = URLRatio;
		accountProperties[2] = hashRatio;
		accountProperties[3] = mentionRatio;
		accountProperties[4] = retweetRatio;
		accountProperties[5] = replyRatio;

	}
	
	public double[] getProperties(){
		return accountProperties;
	}
	
}
