/**
 * 
 */
package lnu.agt.main;

import java.io.File;
import java.util.ArrayList;
import java.util.Properties;

import lnu.agt.AGTProperties;
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
		File ntsDir = new File( props.getProperty("ntsDir") );
		
		
		ArrayList<File> ntsFiles = ReadZipFiles.findZipFiles( ntsDir );
		for (File f : ntsFiles) {
			if (!f.getName().startsWith("2017-02-1"))   // Ten days in February
				continue;
			
			ArrayList<JsonNode> tweets = ReadZipFiles.readZipFile(f);
			
		}

	}

}
