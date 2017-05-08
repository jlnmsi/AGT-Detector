/**
 * AGTStatus.java
 * 23 apr 2017
 */
package lnu.agt;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

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
	public final long createdAt;        // Tweet creation
	public final long accountCreatedAt;
	public final String userName;
	public final String text;
	public final String cleanText;         // Provided by TweetText
	public final String source;
	public final boolean isRetweet;
	public final boolean isReply;
	public final int hashtagCount;
	public final int mentionCount;
	public final int urlCount;
	public final int friendsCount;
	public final int followersCount;
	public final int favoritesCount;
	public final int statusCount;
	
	private AGTStatus(long tweetID, long userID, long createdAt, String userName,
						String text, String source, boolean isRetweet, boolean isReply,
						int hashtagCount, int mentionCount, int urlCount,
						int friendsCount, int followersCount,int favoritesCount, int statusCount, 
						long accountCreatedAt, String cleanText) {
		this.tweetId = tweetID; this.userId = userID; this.createdAt = createdAt; this.userName = userName;
		this.text = text; this.source = source; this.isRetweet = isRetweet;  this.isReply = isReply;
		this.hashtagCount = hashtagCount; this.mentionCount = mentionCount;  this.urlCount = urlCount; this.accountCreatedAt = accountCreatedAt;
		this.friendsCount = friendsCount; this.followersCount = followersCount; this.favoritesCount = favoritesCount;
		this.statusCount = statusCount; this.cleanText = cleanText;
	}
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append(tweetId).append(", ");
		buf.append(userId).append(", ");
		buf.append(userName).append(", ");
		buf.append(createdAt).append(", ");
		buf.append(accountCreatedAt).append(", \n");
		buf.append(source).append(", ");
		buf.append(text.replaceAll("\n"," ")).append(", \n");
		buf.append(isRetweet).append(", ");
		buf.append(isReply).append(", ");
		buf.append(hashtagCount).append(", ");
		buf.append(mentionCount).append(", ");
		buf.append(urlCount).append(", ");
		buf.append(friendsCount).append(", ");
		buf.append(followersCount).append(", ");
		buf.append(favoritesCount).append(", ");
		buf.append(statusCount).append("\n");
		buf.append(cleanText);

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
		boolean isRep = s.getInReplyToScreenName() != null;
		int hashtags = s.getHashtagEntities().length;
		int mentions = s.getUserMentionEntities().length;
		int urls = s.getURLEntities().length;
		int friends = s.getUser().getFriendsCount();
		int followers = s.getUser().getFollowersCount();
		int favorites = s.getUser().getFavouritesCount();
		int statuses = s.getUser().getStatusesCount();
		long accountCreatedAt = s.getUser().getCreatedAt().getTime();
		
		// Clean text not implemented
		
		return new AGTStatus(tID,uID,time,userName,text,source,isRT,isRep,hashtags,mentions,urls,friends,followers,favorites,statuses, accountCreatedAt,null);		
	}
	
	/*
	 * The json format provided by Twitter4j is not the same as 
	 * the json format provided by Twitter Streaming API.
	 * {"createdAt":1491934290000,"id":851860272239304704,"text":"RT @AntMwah: Nothing like a mid-afternoon whooosh. #GOLD vs. #USDJPY https://t.co/Dex1pPEFd4","displayTextRangeStart":-1,"displayTextRangeEnd":-1,"source":"<a href=\"http://twitter.com/download/android\" rel=\"nofollow\">Twitter for Android</a>","inReplyToStatusId":-1,"inReplyToUserId":-1,"favoriteCount":0,"inReplyToScreenName":null,"geoLocation":null,"place":null,"retweetCount":4,"lang":"en","retweetedStatus":{"createdAt":1491925415000,"id":851823046763696128,"text":"Nothing like a mid-afternoon whooosh. #GOLD vs. #USDJPY https://t.co/Dex1pPEFd4","displayTextRangeStart":-1,"displayTextRangeEnd":-1,"source":"<a href=\"http://twitter.com/#!/download/ipad\" rel=\"nofollow\">Twitter for iPad</a>","inReplyToStatusId":-1,"inReplyToUserId":-1,"favoriteCount":1,"inReplyToScreenName":null,"geoLocation":null,"place":null,"retweetCount":4,"lang":"en","retweetedStatus":null,"userMentionEntities":[],"hashtagEntities":[{"start":38,"end":43,"text":"GOLD"},{"start":48,"end":55,"text":"USDJPY"}],"mediaEntities":[{"start":56,"end":79,"id":851823044268105729,"url":"https://t.co/Dex1pPEFd4","mediaURL":"http://pbs.twimg.com/media/C9JIi60XoAEQR49.jpg","mediaURLHttps":"https://pbs.twimg.com/media/C9JIi60XoAEQR49.jpg","expandedURL":"https://twitter.com/AntMwah/status/851823046763696128/photo/1","displayURL":"pic.twitter.com/Dex1pPEFd4","sizes":{"0":{"width":150,"height":150,"resize":101},"1":{"width":680,"height":432,"resize":100},"2":{"width":1024,"height":651,"resize":100},"3":{"width":1024,"height":651,"resize":100}},"type":"photo","videoAspectRatioWidth":0,"videoAspectRatioHeight":0,"videoDurationMillis":0,"videoVariants":[],"extAltText":null,"text":"https://t.co/Dex1pPEFd4"}],"symbolEntities":[],"currentUserRetweetId":-1,"scopes":null,
	 * "user":{"id":3978413296,"name":"AntMwah","email":null,"screenName":"AntMwah","location":"UK","description":"◼️Commodities◾️Equities▪️Forex","descriptionURLEntities":[],"url":"https://t.co/WPHMzWDKmO","followersCount":1728,"status":null,"profileBackgroundColor":"C0DEED","profileTextColor":"333333","profileLinkColor":"1DA1F2","profileSidebarFillColor":"DDEEF6","profileSidebarBorderColor":"C0DEED","profileUseBackgroundImage":true,"showAllInlineMedia":false,"friendsCount":51,"createdAt":1445072422000,"favouritesCount":5232,"utcOffset":-1,"timeZone":null,"profileBackgroundImageUrlHttps":"https://abs.twimg.com/images/themes/theme1/bg.png","profileBackgroundTiled":false,"lang":"en-GB","statusesCount":7357,"translator":false,"listedCount":87,"withheldInCountries":null,"protected":false,"verified":false,"contributorsEnabled":false,"profileImageURL":"http://pbs.twimg.com/profile_images/819866787277864960/T3f_Dwsd_normal.jpg","biggerProfileImageURL":"http://pbs.twimg.com/profile_images/819866787277864960/T3f_Dwsd_bigger.jpg","miniProfileImageURL":"http://pbs.twimg.com/profile_images/819866787277864960/T3f_Dwsd_mini.jpg","originalProfileImageURL":"http://pbs.twimg.com/profile_images/819866787277864960/T3f_Dwsd.jpg","profileImageURLHttps":"https://pbs.twimg.com/profile_images/819866787277864960/T3f_Dwsd_normal.jpg","biggerProfileImageURLHttps":"https://pbs.twimg.com/profile_images/819866787277864960/T3f_Dwsd_bigger.jpg","miniProfileImageURLHttps":"https://pbs.twimg.com/profile_images/819866787277864960/T3f_Dwsd_mini.jpg","originalProfileImageURLHttps":"https://pbs.twimg.com/profile_images/819866787277864960/T3f_Dwsd.jpg","defaultProfileImage":false,"defaultProfile":true,"profileBackgroundImageURL":"http://abs.twimg.com/images/themes/theme1/bg.png","profileBannerURL":"https://pbs.twimg.com/profile_banners/3978413296/1484407942/web","profileBannerRetinaURL":"https://pbs.twimg.com/profile_banners/3978413296/1484407942/web_retina","profileBannerIPadURL":"https://pbs.twimg.com/profile_banners/3978413296/1484407942/ipad","profileBannerIPadRetinaURL":"https://pbs.twimg.com/profile_banners/3978413296/1484407942/ipad_retina","profileBannerMobileURL":"https://pbs.twimg.com/profile_banners/3978413296/1484407942/mobile","profileBannerMobileRetinaURL":"https://pbs.twimg.com/profile_banners/3978413296/1484407942/mobile_retina","geoEnabled":false,"followRequestSent":false,"urlentity":{"start":0,"end":23,"url":"https://t.co/WPHMzWDKmO","expandedURL":"https://uk.tradingview.com/u/AntMwah/","displayURL":"uk.tradingview.com/u/AntMwah/","text":"https://t.co/WPHMzWDKmO"},"rateLimitStatus":null,"accessLevel":0},"withheldInCountries":null,"quotedStatus":null,"quotedStatusId":-1,"contributors":[],"favorited":false,"retweeted":false,"retweet":false,"retweetedByMe":false,"possiblySensitive":false,"truncated":false,"urlentities":[],"rateLimitStatus":null,"accessLevel":0},"userMentionEntities":[{"start":3,"end":11,"name":"AntMwah","screenName":"AntMwah","id":3978413296,"text":"AntMwah"}],"hashtagEntities":[{"start":51,"end":56,"text":"GOLD"},{"start":61,"end":68,"text":"USDJPY"}],"mediaEntities":[{"start":69,"end":92,"id":851823044268105729,"url":"https://t.co/Dex1pPEFd4","mediaURL":"http://pbs.twimg.com/media/C9JIi60XoAEQR49.jpg","mediaURLHttps":"https://pbs.twimg.com/media/C9JIi60XoAEQR49.jpg","expandedURL":"https://twitter.com/AntMwah/status/851823046763696128/photo/1","displayURL":"pic.twitter.com/Dex1pPEFd4","sizes":{"0":{"width":150,"height":150,"resize":101},"1":{"width":680,"height":432,"resize":100},"2":{"width":1024,"height":651,"resize":100},"3":{"width":1024,"height":651,"resize":100}},"type":"photo","videoAspectRatioWidth":0,"videoAspectRatioHeight":0,"videoDurationMillis":0,"videoVariants":[],"extAltText":null,"text":"https://t.co/Dex1pPEFd4"}],"symbolEntities":[],"currentUserRetweetId":-1,"scopes":null,"user":{"id":1002031308,"name":"Mikael Koskela","email":null,"screenName":"Maxknew","location":"Helsinki, Finland","description":"Doosy trader by boustrophedon patterns, collywobbles hedged by bumbershoot. Catercornered folderol. Hemidemisemiquaver markettimer. Vomitory charts.","descriptionURLEntities":[],"url":"https://t.co/SrDsRXp5hM","followersCount":977,"status":null,"profileBackgroundColor":"C0DEED","profileTextColor":"333333","profileLinkColor":"0084B4","profileSidebarFillColor":"DDEEF6","profileSidebarBorderColor":"FFFFFF","profileUseBackgroundImage":true,"showAllInlineMedia":false,"friendsCount":3200,"createdAt":1355158056000,"favouritesCount":6705,"utcOffset":-1,"timeZone":null,"profileBackgroundImageUrlHttps":"https://pbs.twimg.com/profile_background_images/858589350/83a6adb90436cce49c57f30c2fd7f305.jpeg","profileBackgroundTiled":false,"lang":"fi","statusesCount":7632,"translator":false,"listedCount":52,"withheldInCountries":null,"protected":false,"verified":false,"contributorsEnabled":false,"profileImageURL":"http://pbs.twimg.com/profile_images/534759604589047808/RJAGvN8b_normal.png","biggerProfileImageURL":"http://pbs.twimg.com/profile_images/534759604589047808/RJAGvN8b_bigger.png","miniProfileImageURL":"http://pbs.twimg.com/profile_images/534759604589047808/RJAGvN8b_mini.png","originalProfileImageURL":"http://pbs.twimg.com/profile_images/534759604589047808/RJAGvN8b.png","profileImageURLHttps":"https://pbs.twimg.com/profile_images/534759604589047808/RJAGvN8b_normal.png","biggerProfileImageURLHttps":"https://pbs.twimg.com/profile_images/534759604589047808/RJAGvN8b_bigger.png","miniProfileImageURLHttps":"https://pbs.twimg.com/profile_images/534759604589047808/RJAGvN8b_mini.png","originalProfileImageURLHttps":"https://pbs.twimg.com/profile_images/534759604589047808/RJAGvN8b.png","defaultProfileImage":false,"defaultProfile":false,"profileBackgroundImageURL":"http://pbs.twimg.com/profile_background_images/858589350/83a6adb90436cce49c57f30c2fd7f305.jpeg","profileBannerURL":"https://pbs.twimg.com/profile_banners/1002031308/1418619018/web","profileBannerRetinaURL":"https://pbs.twimg.com/profile_banners/1002031308/1418619018/web_retina","profileBannerIPadURL":"https://pbs.twimg.com/profile_banners/1002031308/1418619018/ipad","profileBannerIPadRetinaURL":"https://pbs.twimg.com/profile_banners/1002031308/1418619018/ipad_retina","profileBannerMobileURL":"https://pbs.twimg.com/profile_banners/1002031308/1418619018/mobile","profileBannerMobileRetinaURL":"https://pbs.twimg.com/profiljava.lang.NullPointerException
	 */
	public static  AGTStatus createFromJson4J(JsonNode json) {
		//System.out.println(json);
		long tID = json.get("id").asLong(); 
		long uID = json.get("user").get("id").asLong();
		long time = json.get("createdAt").asLong();
		String name = json.get("user").get("screenName").asText();
		String text = json.get("text").asText();
		String source = json.get("source").asText();
		boolean isRT = json.get("retweet").asBoolean();
		boolean isRep = !json.get("inReplyToScreenName").isNull();
//		System.out.println(isRep+"\t"+json.get("inReplyToScreenName"));
		ArrayNode ht = (ArrayNode) json.get("hashtagEntities");
		int hashtags = ht.size();
		ArrayNode men = (ArrayNode) json.get("userMentionEntities");
		int mentions = men.size();
		ArrayNode u = (ArrayNode) json.get("urlentities");
		int urls = u.size();
		int friends = json.get("user").get("friendsCount").asInt();
		int followers = json.get("user").get("followersCount").asInt();
		int favorites = json.get("user").get("favouritesCount").asInt();
		int statuses = json.get("user").get("statusesCount").asInt();
		long accountCreation = json.get("user").get("createdAt").asLong();
		
		String cleanText = null;   // Not implemented for twitter4j json objects.
		
		return new AGTStatus(tID,uID,time,name,text,source,isRT,isRep,hashtags,mentions,urls,friends,followers, favorites, statuses,accountCreation, null);		
	}
	
	/*
	 * {"created_at":"Sun Jan 01 22:30:37 +0000 2017","id":815686695253397505,"id_str":"815686695253397505",
	 * "text":"Goodnight world 🌎🌍🌏\n\n#losangeles #model #blogger #inspo https://t.co/Sia4pHD2TI","display_text_range":[0,55],
	 * "source":"<a href=\"http://twitter.com/download/iphone\" rel=\"nofollow\">Twitter for iPhone</a>","truncated":false,"in_reply_to_status_id":null,"in_reply_to_status_id_str":null,"in_reply_to_user_id":null,"in_reply_to_user_id_str":null,"in_reply_to_screen_name":null,
	 * "user":{"id":1009154784,"id_str":"1009154784","name":"Ebba Ericsson","screen_name":"EricssonEbba","location":null,"url":"http://nouw.com/ericssonebba","description":"Swedish Entrepreneur, Author, Blogger And Founder Of My Own Business ÄSKEBEÅT STUDIO STOCKHOLM.","protected":false,"verified":false,"followers_count":90,"friends_count":155,"listed_count":140,"favourites_count":266,"statuses_count":1154,"created_at":"Thu Dec 13 16:15:24 +0000 2012","utc_offset":null,"time_zone":null,"geo_enabled":true,"lang":"sv","contributors_enabled":false,"is_translator":false,"profile_background_color":"C0DEED","profile_background_image_url":"http://abs.twimg.com/images/themes/theme1/bg.png","profile_background_image_url_https":"https://abs.twimg.com/images/themes/theme1/bg.png","profile_background_tile":false,"profile_link_color":"1DA1F2","profile_sidebar_border_color":"C0DEED","profile_sidebar_fill_color":"DDEEF6","profile_text_color":"333333","profile_use_background_image":true,"profile_image_url":"http://pbs.twimg.com/profile_images/810925341128749057/QnLEhk3x_normal.jpg","profile_image_url_https":"https://pbs.twimg.com/profile_images/810925341128749057/QnLEhk3x_normal.jpg","profile_banner_url":"https://pbs.twimg.com/profile_banners/1009154784/1388857143","default_profile":true,"default_profile_image":false,"following":null,"follow_request_sent":null,"notifications":null},
	 * "geo":null,"coordinates":null,"place":{"id":"82b141af443cb1b8","url":"https://api.twitter.com/1.1/geo/id/82b141af443cb1b8.json","place_type":"country","name":"Sverige","full_name":"Sverige","country_code":"SE","country":"Sverige","bounding_box":{"type":"Polygon","coordinates":[[[10.592263,55.136571],[10.592263,69.060024],[24.17731,69.060024],[24.17731,55.136571]]]},"attributes":{}},"contributors":null,"is_quote_status":false,"retweet_count":0,"favorite_count":0,"entities":{"hashtags":[{"text":"losangeles","indices":[21,32]},{"text":"model","indices":[33,39]},{"text":"blogger","indices":[40,48]},{"text":"inspo","indices":[49,55]}],"urls":[],"user_mentions":[],"symbols":[],"media":[{"id":815686684381683712,"id_str":"815686684381683712","indices":[56,79],"media_url":"http://pbs.twimg.com/media/C1Hmt76WIAALjct.jpg","media_url_https":"https://pbs.twimg.com/media/C1Hmt76WIAALjct.jpg","url":"https://t.co/Sia4pHD2TI","display_url":"pic.twitter.com/Sia4pHD2TI","expanded_url":"https://twitter.com/EricssonEbba/status/815686695253397505/photo/1","type":"photo","sizes":{"small":{"w":551,"h":680,"resize":"fit"},"large":{"w":1242,"h":1533,"resize":"fit"},"thumb":{"w":150,"h":150,"resize":"crop"},"medium":{"w":972,"h":1200,"resize":"fit"}}}]},
	 * "extended_entities":{"media":[{"id":815686684381683712,"id_str":"815686684381683712","indices":[56,79],"media_url":"http://pbs.twimg.com/media/C1Hmt76WIAALjct.jpg","media_url_https":"https://pbs.twimg.com/media/C1Hmt76WIAALjct.jpg","url":"https://t.co/Sia4pHD2TI","display_url":"pic.twitter.com/Sia4pHD2TI","expanded_url":"https://twitter.com/EricssonEbba/status/815686695253397505/photo/1","type":"photo","sizes":{"small":{"w":551,"h":680,"resize":"fit"},"large":{"w":1242,"h":1533,"resize":"fit"},"thumb":{"w":150,"h":150,"resize":"crop"},"medium":{"w":972,"h":1200,"resize":"fit"}}}]},
	 * "favorited":false,"retweeted":false,"possibly_sensitive":false,"filter_level":"low","lang":"en","timestamp_ms":"1483309837531"}
	 */
	public static  AGTStatus createFromJson(JsonNode json) {
		JsonNode user = json.get("user");
		
		long tID = json.get("id").asLong(); 
		long uID = user.get("id").asLong();
		long time = json.get("timestamp_ms").asLong();
		String name = user.get("screen_name").asText();
		String text = json.get("text").asText();
		String source = json.get("source").asText();
		
		JsonNode rt = json.get("retweeted_status");
		boolean isRT = rt != null;
		JsonNode rep = json.get("in_reply_to_screen_name");  // How about in_reply_to_status and other replies
		boolean isRep = !rep.isNull();
		
		JsonNode entities = json.get("entities");
		ArrayNode ht = (ArrayNode) entities.get("hashtags");
		int hashtags = ht.size();
		ArrayNode men = (ArrayNode) entities.get("user_mentions");
		int mentions = men.size();
		ArrayNode u = (ArrayNode) entities.get("urls");
		int urls = u.size();
		
		
		//System.out.println(user);
		int friends = user.get("friends_count").asInt();
		int followers = user.get("followers_count").asInt();
		int favorites = user.get("favourites_count").asInt();
		int statuses = user.get("statuses_count").asInt();
		String date = user.get("created_at").asText();
		long createdAt = new Date(date).getTime();
		
		String cleanText = null;
		try {
			TweetText tText = new TweetText(json);
			cleanText = tText.getCleanText();
		}
		catch(Exception e) { e.printStackTrace(); }
		
		return new AGTStatus(tID,uID,time,name,text,source,isRT,isRep,hashtags,mentions,urls,friends,followers, favorites, statuses, createdAt,cleanText);		
	}
	


	// Demonstrator 
	public static void main(String[] args) {
		System.out.println("Working Directory: "+System.getProperty("user.dir"));

		Properties props = AGTProperties.getLocalProperties();  
		File zipDir = new File( props.getProperty("random1JsonZip") );      // No retweets!
		//File zipDir = new File( props.getProperty("swedishTweetsZip") );  // Plenty of retwets!
		ArrayList<JsonNode> tweets = ReadZipFiles.readZipFile( zipDir);

		int count = 0;
		for (JsonNode tweet : tweets) {
			// Select a few users
			JsonNode user = tweet.get("user");
			long userID = user.get("id").asLong();
			if ( !(""+userID).startsWith("10") ) continue;


			// Print metadata profiles 
			AGTStatus aStatus = AGTStatus.createFromJson(tweet);
			System.out.println("\n"+aStatus);

			count++;
		}
		System.out.println("Count: "+count);
	}


}
