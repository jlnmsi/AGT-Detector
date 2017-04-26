package lnu.agt;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.math3.stat.inference.ChiSquareTest;

public class ChiSquared {
	
	private final int BIN_SIZE = 15;

	// p-value for minute, p-value for seconds
	private double[] pValues = new double[2];
	
	public ChiSquared(List<AGTStatus> tweets){
		calculatepValues(tweets);
	}
	
	private void calculatepValues(List<AGTStatus> tweets){
		
		long[] minutes = getMinutes(tweets);
		long[] seconds = getSeconds(tweets);
		
		int numOfTweets = tweets.size();

		/* putting the data into bins */
		long[] minutesBins = putIntoBins(minutes, 0, 60, BIN_SIZE);
		long[] secondsBins = putIntoBins(seconds, 0, 60, BIN_SIZE);

		/* getting the expected normal distributions */
		double[] expectedMinutes = expectedDistribution(numOfTweets, minutesBins);
		double[] expectedSeconds = expectedDistribution(numOfTweets, secondsBins);
		
		ChiSquareTest test = new ChiSquareTest();
		double pValMin = test.chiSquareTest(expectedMinutes, minutesBins);
		double pValSec = test.chiSquareTest(expectedSeconds, secondsBins); 
		
		pValues[0] = pValMin;
		pValues[1] = pValSec;
	}
	
	// returning the array with uniform distribution of seconds, minutes or hours
	private static double[] expectedDistribution(int numOfTweets, long[] timeUnitsInBins){
		double[] expected = new double[timeUnitsInBins.length];
		for(int j = 0; j < timeUnitsInBins.length; j++)
			expected[j] = (double) numOfTweets/timeUnitsInBins.length;
		return expected;
	}

	/* Organizing the data into bins, data are seconds, minutes, hours
	   max and min 0 and 60 or 0 and 24, numBins is the number of bins one wants to have */
	public static long[] putIntoBins(long[] data, int min, int max, int numBins) {
		final long[] result = new long[numBins];
		final double binSize = (max - min)/numBins;

		for (double d : data) {
			int bin = (int) ((d - min) / binSize);
			if (bin < 0) { /* this data is smaller than min */ }
			else if (bin >= numBins) { /* this data point is bigger than max */ }
			else {
				result[bin] += 1;
			}
		}
		return result;
	}

	private static Calendar calendar = Calendar.getInstance();
	private static long[] getSeconds(List<AGTStatus> tweets){
		long[] seconds = new long[tweets.size()];
		for(int i = 0; i < tweets.size(); i++){
			Date time = new Date(tweets.get(i).createdAt);
			calendar.setTime(time);
			int sec = calendar.get(Calendar.SECOND);
//			System.out.println(sec+"\t"+time);
			seconds[i] = sec;	//seconds
			//seconds[i] = Integer.parseInt(asString.substring(17, 19));	//seconds
		}
		return seconds;
	}

	private static long[] getMinutes(List<AGTStatus> tweets){
		long[] minutes = new long[tweets.size()];
		for(int i = 0; i < tweets.size(); i++){
			Date time = new Date(tweets.get(i).createdAt);
			calendar.setTime(time);
			int min = calendar.get(Calendar.MINUTE);
//			System.out.println(min+"\t"+time);
			minutes[i] = min;
//			minutes[i] = Integer.parseInt(asString.substring(14, 16));	//minutes
		}
		return minutes;
	}

	public double[] getpValues(){
		return pValues;
	}

}
