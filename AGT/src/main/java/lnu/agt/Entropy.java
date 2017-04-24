package lnu.agt;

import java.util.Date;
import java.util.List;

import twitter4j.Status;

public class Entropy {

	// entropy for hours, entropy for minutes, entropy for seconds
	private double[] listOfEntropies = new double[3];
	
	public Entropy(List<AGTStatus> tweets){
		calculateAllEntropies(tweets);
	}

	// calculating all entropy quantities
	public void calculateAllEntropies(List<AGTStatus> tweets){

		long[] timeDelaysH = new long[tweets.size() - 1];
		long[] timeDelaysM = new long[tweets.size() - 1];
		long[] timeDelaysS = new long[tweets.size() - 1];

		timeDelays(tweets, timeDelaysH, timeDelaysM, timeDelaysS);

		int binNumH = 500;
		int[] binsH = new int[binNumH];
		int numOfDelaysH = fillingBins(binsH, binNumH, timeDelaysH);

		int binNumM = 60;
		int[] binsM = new int[60];
		int numOfDelaysM = fillingBins(binsM, binNumM, timeDelaysM);

		int binNumS = 60;
		int[] binsS = new int[60];
		int numOfDelaysS = fillingBins(binsS, binNumM, timeDelaysS);

		double entropyH = computeEntropy(binsH, binNumH, numOfDelaysH);
		double entropyM = computeEntropy(binsM, binNumM, numOfDelaysM);
		double entropyS = computeEntropy(binsS, binNumS, numOfDelaysS);

		listOfEntropies[0] = entropyH;
		listOfEntropies[1] = entropyM;
		listOfEntropies[2] = entropyS;
	}


	// computing one entropy quantity
	private static double computeEntropy(int[] bins, int binNum, int numOfDelays){
		double entropy = 0;
		for (int k = 0; k < bins.length; k++){
			if (bins[k] != 0){
				double p = (double) bins[k]/numOfDelays;
				entropy +=  p*(Math.log(p)/Math.log(2));
			}
			else if (bins[k] == 0){
				entropy += 0;
			}
		}
		
		if(entropy != 0){
			entropy = (entropy*(-1))/(Math.log(binNum)/Math.log(2)); // denominator is the maximum possible entropy, so we divide with it to get a value in [0,1]
		}
		
		return entropy;
	}

	// orginizing the data into the bins
	private static int fillingBins(int[] bins, int binNum, long[] timeDelays){
		int numOfDelays = 0;

		for(int i = 0; i < timeDelays.length; i++){
			if(timeDelays[i] < binNum && timeDelays[i] >= 0){
				bins[Math.toIntExact(timeDelays[i])] += 1;
				numOfDelays++;
			}
		}
		return numOfDelays;
	}

	// calculating time delays for hours, minutes and seconds
	private static void timeDelays(List<AGTStatus> tweets, long[] timeDelaysH, long[] timeDelaysM, long[] timeDelaysS){
		for(int j = 0; j < tweets.size() - 1; j++){
//			Date date1 = tweets.get(j + 1).getCreatedAt();
//			Date date2 = tweets.get(j).getCreatedAt();
//			long diff = date2.getTime() - date1.getTime();
			long date1 = tweets.get(j + 1).createdAt;
			long date2 = tweets.get(j).createdAt;
			long diff = date2 - date1;
			long diffSeconds = diff / 1000 % 60;
			long diffMinutes = diff / (60 * 1000) % 60;
			long diffHours = diff / (60 * 60 * 1000);

			timeDelaysH[j] = diffHours;
			timeDelaysM[j] = diffMinutes;
			timeDelaysS[j] = diffSeconds;
		}
	}
	
	public double[] getEntropies(){
		return listOfEntropies;
	}
}