/**
 * AGTStatus.java
 * 23 apr 2017
 */
package lnu.agt;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;

import twitter4j.Status;

/**
 * We have a problem! Antonio's statuses are platform dependent, and Jonas' 
 * platform independent json statuses are not compatible! In short, when 
 * trying to convert Jonas' json to a twitter4j Staus certain information (e.g.
 * cretaedAt is missing. 
 * 
 *  This class is an attempt to create a platform independent status that is compatible 
 *  with both status types. 
 *  
 *  Note: It is not complete, a lot of attributes are missing. We will add them on demand.
 * 
 * @author jlnmsi
 *
 */
public class AGTStatus {
	public final long tweetId;
	public final long userId;
	public final long createdAt;
	public final String userName;
	public final String text;
	public final String source;
	public final boolean isRetweet;
	public final int hashtagCount;
	public final int mentionCount;
	public final int urlCount;
	public final int friendsCount;
	public final int followersCount;
	public final int favoritesCount;
	
	private AGTStatus(long tweetID, long userID, long createdAt, String userName,
						String text, String source, boolean isRetweet,
						int hashtagCount, int mentionCount, int urlCount,
						int friendsCount, int followersCount,int favoritesCount) {
		this.tweetId = tweetID; this.userId = userID; this.createdAt = createdAt; this.userName = userName;
		this.text = text; this.source = source; this.isRetweet = isRetweet; 
		this.hashtagCount = hashtagCount; this.mentionCount = mentionCount;  this.urlCount = urlCount; 
		this.friendsCount = friendsCount; this.followersCount = followersCount; this.favoritesCount = favoritesCount;
	}
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append(tweetId).append(", ");
		buf.append(userId).append(", ");
		buf.append(userName).append(", ");
		buf.append(createdAt).append(", \n");
		buf.append(source).append(", ");
		buf.append(text).append(", \n");
		buf.append(isRetweet).append(", ");
		buf.append(hashtagCount).append(", ");
		buf.append(mentionCount).append(", ");
		buf.append(urlCount).append(", ");
		buf.append(friendsCount).append(", ");
		buf.append(followersCount).append(", ");
		buf.append(favoritesCount).append(", ");

		return buf.toString();
	}
	
	
	public static  AGTStatus createFromStatus(Status s) {
		long tID = s.getId(); 
		long uID = s.getUser().getId();
		long time = s.getCreatedAt().getTime();
		String userName = s.getUser().getScreenName();
		String text = s.getText();
		String source = s.getSource();
		boolean isRT = s.isRetweet();
		int hashtags = s.getHashtagEntities().length;
		int mentions = s.getUserMentionEntities().length;
		int urls = s.getURLEntities().length;
		int friends = s.getUser().getFriendsCount();
		int followers = s.getUser().getFollowersCount();
		int favorites = s.getUser().getFavouritesCount();
		
		return new AGTStatus(tID,uID,time,userName,text,source,isRT,hashtags,mentions,urls,friends,followers,favorites);		
	}
	
	public static  AGTStatus createFromJson4J(JsonNode json) {
		long tID = json.get("id").asLong(); 
		long uID = json.get("user").get("id").asLong();
		long time = json.get("createdAt").asLong();
		String name = json.get("user").get("screenName").asText();
		String text = json.get("text").asText();
		String source = json.get("source").asText();
		boolean isRT = json.get("retweet").asBoolean();
		ArrayNode ht = (ArrayNode) json.get("hashtagEntities");
		int hashtags = ht.size();
		ArrayNode men = (ArrayNode) json.get("userMentionEntities");
		int mentions = men.size();
		ArrayNode u = (ArrayNode) json.get("urlentities");
		int urls = u.size();
		int friends = json.get("user").get("friendsCount").asInt();
		int followers = json.get("user").get("followersCount").asInt();
		int favorites = json.get("user").get("favoritesCount").asInt();
		System.out.println(favorites);
		
		return new AGTStatus(tID,uID,time,name,text,source,isRT,hashtags,mentions,urls,friends,followers, favorites);		
	}
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
