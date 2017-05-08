/**
 * 
 */
package lnu.agt.main;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

import lnu.agt.AGTProperties;
import lnu.agt.AGTStatus;
import lnu.agt.ReadZipFiles;

import org.codehaus.jackson.JsonNode;

/**
 * @author jlnmsi
 *
 */
public class ReadZipMain {

	public static void main(String[] args) {
		System.out.println("Working Directory: "+System.getProperty("user.dir"));
		
		Properties props = AGTProperties.getLocalProperties();
		File file = new File( props.getProperty("random234JsonZip") );
		
		
		//ArrayList<File> ntsFiles = ReadZipFiles.findZipFiles( ntsDir );
		//for (File f : ntsFiles) {
		
			
			ArrayList<JsonNode> tweets = ReadZipFiles.readZipFile(file);

			
			for (JsonNode json : tweets) {
				JsonNode user = json.get("user");
				int count = user.get("favourites_count").asInt();
				System.out.println(user.get("screen_name").asText()+"\t"+count);
			}

		//}

	}

}
