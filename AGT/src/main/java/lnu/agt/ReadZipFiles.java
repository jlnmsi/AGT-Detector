/**
 * ReadZipFiles.java
 * 19 apr 2017
 */
package lnu.agt;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.zip.ZipInputStream;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * @author jlnmsi
 *
 */
public class ReadZipFiles {

	public static ArrayList<File>  findZipFiles(File zipDir) {
		System.out.println("Reading from directory: "+zipDir.getAbsolutePath());
		File[] files = zipDir.listFiles();
		ArrayList<File> toReturn = new ArrayList<File>();
		for (File f : files) {
			if (f.getName().endsWith(".zip"))
				toReturn.add(f);
			else
				System.out.println("Dropping file "+f);
		}
		System.out.println("Zip files: "+toReturn.size());
		return toReturn;
	}
	
	private static SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
	public static ArrayList<JsonNode> readZipFile(File f) throws IOException {
		int duplicates = 0;
		InputStream input= new FileInputStream(f);
        ZipInputStream zip = new ZipInputStream(input);
        zip.getNextEntry();
        
        Scanner sc = new Scanner(zip);
        ArrayList<JsonNode> toReturn = new ArrayList<JsonNode>();
//        System.out.println("\n"+f.getAbsolutePath());
//        duplicateSearch = new HashSet<String>();
//        int rowCount = 0;
        ObjectMapper mapper = new ObjectMapper();
        while (sc.hasNextLine()) {
        	String tweet = sc.nextLine();
        	//System.out.println(rowCount+"\t"+tweet);
        	JsonNode jsonTweet = mapper.readTree(tweet);
        	//rowCount++;
    		
    		toReturn.add(jsonTweet);
        }
        sc.close();
        
        // Prepare summary print
        String fName = f.getName();
        
        JsonNode first = toReturn.get(0);
        long timeStamp = getTimeStamp(first);
        String fTime = format.format(timeStamp);
        
        JsonNode last = toReturn.get(toReturn.size()-1);
        timeStamp = getTimeStamp(last);
        String lTime = format.format(timeStamp);
        
        System.out.println("\t"+fName+", Count: "+toReturn.size()+", First: "+ fTime+", Last: "+ lTime);
        
        return toReturn;
	}
	
	public static long getTimeStamp(JsonNode tweet) {
		String text = tweet.get("timestamp_ms").asText();
		return Long.parseLong(text);
	}
}
