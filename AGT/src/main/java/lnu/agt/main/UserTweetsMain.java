/**
 * UserTweetsMain.java
 * 19 apr 2017
 */
package lnu.agt.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Properties;

import lnu.agt.AGTProperties;
import lnu.agt.ProfileGenerator;
import lnu.agt.ReadZipFiles;
import lnu.agt.UserProfile;

import org.codehaus.jackson.JsonNode;

/**
 * @author jlnmsi
 *
 */
public class UserTweetsMain {


	public static void main(String[] args) throws FileNotFoundException {
		System.out.println("Working Directory: "+System.getProperty("user.dir"));
		
		Properties props = AGTProperties.getLocalProperties();
		File ntsDir = new File( props.getProperty("ntsDir") );
		
		
		ArrayList<File> ntsFiles = ReadZipFiles.findZipFiles( ntsDir );
		ProfileGenerator profileGenerator = new ProfileGenerator(true);
		for (File f : ntsFiles) {
			if (!f.getName().startsWith("2017-03-15"))   
				continue;
			
			ArrayList<JsonNode> tweets = ReadZipFiles.readZipFile(f);
			
			for (JsonNode tweet : tweets) {
				// Select users from English tweets
				JsonNode user = tweet.get("user");
				long userID = user.get("id").asLong();
				String lang = tweet.get("lang").asText();
				if ( lang.equals("en")) {
					UserProfile uProfile = profileGenerator.getUserProfile(userID);	
				}
				//System.out.println(userID);
			}
			System.out.println("DownloadCount: "+profileGenerator.getDownloadCount());
			
		}

	}

}
