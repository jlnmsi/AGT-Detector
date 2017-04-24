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
		File profileFile = new File( agtProps.getProperty("userProfiles") );
		PrintWriter output = new PrintWriter(profileFile);
		System.out.println(profileFile.getAbsolutePath());
		
		
		ArrayList<File> ntsFiles = ReadZipFiles.findZipFiles( userTweetsDir );
		long before = System.currentTimeMillis();
		for (File f : ntsFiles) {
			if (!f.getName().startsWith("1002"))   // A few zip files
				continue;
			
			ArrayList<JsonNode> tweets = ReadZipFiles.readZipFile(f);
			
			// Convert from JsonNode to AGTStatus
			ArrayList<AGTStatus> statuses = new ArrayList<AGTStatus>();
			for (JsonNode json : tweets) {
				try {
					AGTStatus aStatus = AGTStatus.createFromJson4J(json);
					statuses.add(aStatus);
				} catch (Exception e) {
					System.err.println("Unable to convert json tweet "+json);
				}
			}
			
			// Compute profile and print
			if (statuses.size() > 0){
				UserProfile user = new UserProfile(statuses);
				String row = user.asRow('\t');
				output.println(row);
				//System.out.println(user.getUserProfile());
			}
		}
		output.close();
		long ellapsed = System.currentTimeMillis() - before;
		System.out.println("Minutes: " + (0.0+ellapsed)/(60*1000));

	}
	
	

}
