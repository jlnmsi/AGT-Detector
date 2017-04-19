/**
 * 
 */
package lnu.agt.main;

import java.io.File;
import java.util.ArrayList;

import lnu.agt.ReadZipFiles;

/**
 * @author jlnmsi
 *
 */
public class ReadZipMain {
	
	private static File ntsZipDir = new File("/Users/jlnmsi/Software/1.8/EngDep/EntireTweetsStatistics/dailyTweets/");

	public static void main(String[] args) {
		System.out.println("Working Directory: "+System.getProperty("user.dir"));
		
		ArrayList<File> entireFiles = ReadZipFiles.findZipFiles(ntsZipDir);

	}

}
