/**
 * ReadProperties.java
 * 19 apr 2017
 */
package lnu.agt.trying_things;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * @author jlnmsi
 *
 */
public class ReadProperties {


	public static void main(String[] args) throws FileNotFoundException, IOException {
		System.out.println("Working Directory: "+System.getProperty("user.dir"));
		
		Properties props = new Properties();
		props.load(new FileInputStream("src/main/resources/agt.properties"));

	}

}
