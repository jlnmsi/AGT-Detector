/**
 * UserProfile.java
 * 19 apr 2017
 */
package lnu.agt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author jlnmsi
 *
 */
public class UserProfile {
//	private StringBuilder userProperties = new StringBuilder();
	
	private static int MAX_TWEETS = 3200;  // Used to normalize tweet count
	
	private long userID;
	private String userName;
	private double[] userProperties;
	
	
	
	public UserProfile(long uID, String uName, double[] props) {
		userID = uID;
		userName = uName;
		userProperties = props;
	}

	public UserProfile(List<AGTStatus> tweets){
		AGTStatus latestTweet = tweets.get(0);
		userID = latestTweet.userId;
		userName = latestTweet.userName;
		
		//Start collecting numeric profile properties
		ArrayList<Double> profile = new ArrayList<Double>();
		
		// Profile tweet count
		double tweetCount = (0.0+tweets.size())/MAX_TWEETS;
		profile.add(tweetCount);

		// Appending all the other properties
		ChiSquared chiSquared = new ChiSquared(tweets);
		double[] pValues = chiSquared.getpValues(); // minutes followed by seconds
		append(profile,pValues);
		
		Entropy entropy = new Entropy(tweets);
		double[] entropyData = entropy.getEntropies();
		append(profile,entropyData);
		
		AccountProperties accProp = new AccountProperties(tweets);
		double[] props = accProp.getProperties();
		append(profile,props);
		
		double[] deviceProfile = DeviceProfiler.getDeviceProfile(tweets);
		append(profile,deviceProfile);
	
		// Convert list to double array
		userProperties = new double[profile.size()];
		for (int i=0;i<profile.size();i++)
			userProperties[i] = profile.get(i);
		
		
//		System.out.println(this);
//		System.out.println(asRow('\t'));
//		System.exit(-1);

	}
	
	public void append(List<Double> list, double[] data) {
		for( double d: data)
			list.add(d);
	}
	
	@Override
	public String toString() {
		return userID+", "+userName+" "+Arrays.toString(userProperties);
	}
	
	public String asRow(char itemSeparator) {
		StringBuilder buf = new StringBuilder();
		buf.append(userID);
		buf.append(itemSeparator).append(userName);
		for (double d : userProperties)
			buf.append(itemSeparator).append(d);
		
		return buf.toString();
	}

}
