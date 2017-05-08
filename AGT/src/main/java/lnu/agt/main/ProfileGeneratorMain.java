/**
 * ProfileGeneratorMain.java
 * 23 apr 2017
 */
package lnu.agt.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Properties;

import lnu.agt.AGTProperties;
import lnu.agt.AGTStatus;
import lnu.agt.ReadZipFiles;
import lnu.agt.UserProfile;

import org.codehaus.jackson.JsonNode;

/**
 * @author jlnmsi
 *
 */
public class ProfileGeneratorMain {

	public static void main(String[] args) throws FileNotFoundException {
		System.out.println("Working Directory: "+System.getProperty("user.dir"));
		
		// Read user tweets from local repository
		Properties localProps = AGTProperties.getLocalProperties();
		File userTweetsDir = new File( localProps.getProperty("userTweets1") );
		
		// Prepare to store profiles in config
		Properties agtProps = AGTProperties.getAGTProperties();
		File profileFile = new File( agtProps.getProperty("userProfiles1") );
		PrintWriter output = new PrintWriter(profileFile);
		System.out.println(profileFile.getAbsolutePath());
		
		
		ArrayList<File> userTimelines = ReadZipFiles.findZipFiles( userTweetsDir );
		long before = System.currentTimeMillis();
		int userCount = 0;
		for (File f : userTimelines) {
//			if (!f.getName().startsWith("10"))   // A few zip files
//				continue;
			
			userCount++;
			ArrayList<JsonNode> tweets = ReadZipFiles.readZipFile(f);
			
			// Convert from JsonNode to AGTStatus
			ArrayList<AGTStatus> statuses = new ArrayList<AGTStatus>();
			for (JsonNode json : tweets) {
				try {
					AGTStatus aStatus = AGTStatus.createFromJson4J(json);
					statuses.add(aStatus);
				} catch (Exception e) {
					//System.err.println("Unable to convert json tweet "+json);
					e.printStackTrace();
					System.exit(-1);
				}
			}
			
			// Compute profile and print
			if (statuses.size() > 0){
				UserProfile user = new UserProfile(statuses);
				String row = user.asRow('\t');
				output.println(row);
				System.out.println(f.getName());
			}
		}
		output.close();
		long ellapsed = System.currentTimeMillis() - before;
		System.out.println("User profiles saved in: "+profileFile.getAbsolutePath());
		System.out.println("User Count: " + userCount+", Minutes: " + (0.0+ellapsed)/(60*1000));

	}
	
	

}
