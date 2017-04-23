/**
 * AGTProperties.java
 * 20 apr 2017
 */
package lnu.agt;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * @author jlnmsi
 *
 */
public class AGTProperties {
	private static final String localProperties = "src/main/resources/local.properties";
	private static final String agtProperties = "src/main/resources/agt.properties";
	
	
	public static Properties getLocalProperties() {
		Properties props = new Properties();
		return getProperties(localProperties);
	}
	
	public static Properties getAGTProperties() {
		return getProperties(agtProperties);
	}
	
	public static Properties getProperties(String propertyFilePath) {
		Properties props = new Properties();
		try {
			props.load(new FileInputStream(propertyFilePath));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return props;
	}
	
	// Demonstrator 	
	public static void main(String[] args) {
		Properties agtProps = getAGTProperties();
		System.out.println(agtProps);
		System.out.println("Project name: "+agtProps.getProperty("project") );
		
		Properties localProps = getLocalProperties();
		System.out.println(localProps);
		
	}
}
