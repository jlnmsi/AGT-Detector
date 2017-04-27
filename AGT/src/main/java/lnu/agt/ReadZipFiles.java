/**
 * ReadZipFiles.java
 * 19 apr 2017
 */
package lnu.agt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
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
//			else
//				System.out.println("Dropping file "+f);
		}
		System.out.println("Zip files: "+toReturn.size());
		return toReturn;
	}
	
	private static SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
	public static ArrayList<JsonNode> readZipFile(File f)  {
		ArrayList<JsonNode> toReturn = null;
		try {
			InputStream input= new FileInputStream(f);
			ZipInputStream zip = new ZipInputStream(input);
			zip.getNextEntry();

			Scanner sc = new Scanner(zip);
			toReturn = new ArrayList<JsonNode>();
			ObjectMapper mapper = new ObjectMapper();
			while (sc.hasNextLine()) {
				String tweet = sc.nextLine();
				JsonNode jsonTweet = null;
				try {
					jsonTweet = mapper.readTree(tweet);
				}
				catch (JsonParseException e) {
					System.err.println("Unable to parse Json string in file: "+f.getName());
					System.err.println(tweet);
					System.err.println("Drops it!!");
					continue;
				}
				toReturn.add(jsonTweet);
			}
			sc.close();

			// Prepare summary print
			String fName = f.getName();
			System.out.println("\t"+fName+", Count: "+toReturn.size());
		}catch(IOException e) {
			e.printStackTrace();
		}

		return toReturn;
	}
	
	public static File saveTweetsZip(List<JsonNode> collectedTweets, File outDir, String fileName) {		
		// Build output string		
		StringBuilder buf = new StringBuilder();   
		String lineSep = System.getProperty("line.separator");
		for (JsonNode tweet : collectedTweets) {
			buf.append( tweet.toString() );   
			buf.append(lineSep);
		}
		
		File outFile = new File(outDir,fileName+".zip");       
		ZipOutputStream out = null;
		try {
			out = new ZipOutputStream(new FileOutputStream(outFile));
			ZipEntry e = new ZipEntry(fileName+".json");
			out.putNextEntry(e);
			byte[] data = buf.toString().getBytes();
			out.write(data, 0, data.length);
			out.closeEntry();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally	{ 
			try{
				if(out!=null)
					out.close();
			}catch(Exception ex){
				System.out.println("Error in closing the BufferedWriter"+ex);
			}
		}
		return outFile;
	}
	
//	public static long getTimeStamp(JsonNode tweet) {
//		String text = tweet.get("timestamp_ms").asText();
//		return Long.parseLong(text);
//	}
}
