package lnu.agt;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;

import weka.core.stemmers.SnowballStemmer;



public class TweetText {

	String id;
	String text;
	String lang;
	String cleanText;
	ArrayList<Integer> userMentions;
	ArrayList<Integer> hashtagMentions;
	ArrayList<Integer> symbolMentions;
	ArrayList<Integer> urlMentions;
	ArrayList<Integer> mediaMentions;
	int classification;
	
	public TweetText(String text, String id, String lang, int classification, ArrayList<Integer> hashtagMentions, ArrayList<Integer> urlMentions, ArrayList<Integer> userMentions, ArrayList<Integer> symbolMentions, ArrayList<Integer> mediaMentions) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException{
		this.text = text;
		this.id = id;
		this.lang = lang;
		this.classification = classification;
		this.userMentions = userMentions;
		this.hashtagMentions = hashtagMentions;
		this.symbolMentions = symbolMentions;
		this.urlMentions = urlMentions;
		this.mediaMentions = mediaMentions;
		cleanTweet();
	}
	
	public TweetText(String text, String id, String lang, ArrayList<Integer> hashtagMentions, ArrayList<Integer> urlMentions, ArrayList<Integer> userMentions, ArrayList<Integer> symbolMentions, ArrayList<Integer> mediaMentions) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException{
		this.text = text;
		this.id = id;
		this.lang = lang;
		this.userMentions = userMentions;
		this.hashtagMentions = hashtagMentions;
		this.symbolMentions = symbolMentions;
		this.urlMentions = urlMentions;
		this.mediaMentions = mediaMentions;
		cleanTweet();
	}

	public void cleanTweet() throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
		if(text.equals("")) {
			System.out.println("No text to clean");
			return;
		}
		String tmp = text;

		//use the supplied entity markers from Twitter to map #... -> hashtag, @.. -> user, etc.
		byte[] arr32 = tmp.getBytes("UTF-32");
	    byte[] arrPart;
	    String newString;
	    Charset.forName("UTF-32").encode(tmp);
		if(hashtagMentions != null) {
			for(int i = 0; i < hashtagMentions.size(); i = i + 2){
				arrPart = Arrays.copyOfRange(arr32, 4*hashtagMentions.get(i), 4*hashtagMentions.get(i+1));
				newString = new String(arrPart, "UTF-32");
				tmp = tmp.replaceAll(Pattern.quote(newString), " HASHTAG ");
			}
		}
		if(urlMentions != null) {
			for(int i = 0; i < urlMentions.size(); i = i + 2){
				arrPart = Arrays.copyOfRange(arr32, 4*urlMentions.get(i), 4*urlMentions.get(i+1));
				newString = new String(arrPart, "UTF-32");
				tmp = tmp.replaceAll(Pattern.quote(newString), " URL ");
			}
		}
		if(userMentions != null) {
			for(int i = 0; i < userMentions.size(); i = i + 2){
				arrPart = Arrays.copyOfRange(arr32, 4*userMentions.get(i), 4*userMentions.get(i+1));
				newString = new String(arrPart, "UTF-32");
				tmp = tmp.replaceAll(Pattern.quote(newString), " USER ");
			}
		}
		if(symbolMentions != null) {
			for(int i = 0; i < symbolMentions.size(); i = i + 2){
				arrPart = Arrays.copyOfRange(arr32, 4*symbolMentions.get(i), 4*symbolMentions.get(i+1));
				newString = new String(arrPart, "UTF-32");
				tmp = tmp.replaceAll(Pattern.quote(newString), " SYMBOL ");
			}
		}
		if(mediaMentions != null) {
			for(int i = 0; i < mediaMentions.size(); i = i + 2){
				arrPart = Arrays.copyOfRange(arr32, 4*mediaMentions.get(i), 4*mediaMentions.get(i+1));
				newString = new String(arrPart, "UTF-32");
				tmp = tmp.replaceAll(newString, " MEDIA ");
			}
		}
		//RegEx cleaning
		tmp = tmp.replaceAll("[()\\[\\]/%\\\\:#]", "")
					.replaceAll("^[(]?^[-+]?\\d+([.,][\\d]+)?\\w*", "number ")
					.replaceAll("[^a-zA-Z0-9_]+$", "")
					.replaceAll("[°'´\"`.,’!?]", "")
					.replaceAll("[0-9][(k|m)?mh?]?[ms]?", "");		
		
		
		cleanText = tmp;
	}

	
	public void printTweet() {
		System.out.println("id: " + id 
				+ "\ntext: " + text 
				+ "\ncleanText: " + cleanText 
				+ "\nclassification: " + classification 
				+ "\nlang: " + lang);
	}
	
	
	public ArrayList<Integer> getUserMentions() {
		return userMentions;
	}

	public void setUserMentions(ArrayList<Integer> userMentions) {
		this.userMentions = userMentions;
	}

	public ArrayList<Integer> getHashtagMentions() {
		return hashtagMentions;
	}

	public void setHashtagMentions(ArrayList<Integer> hashtagMentions) {
		this.hashtagMentions = hashtagMentions;
	}

	public ArrayList<Integer> getSymbolMentions() {
		return symbolMentions;
	}

	public void setSymbolMentions(ArrayList<Integer> symbolMentions) {
		this.symbolMentions = symbolMentions;
	}

	public ArrayList<Integer> getUrlMentions() {
		return urlMentions;
	}

	public void setUrlMentions(ArrayList<Integer> urlMentions) {
		this.urlMentions = urlMentions;
	}

	public ArrayList<Integer> getMediaMentions() {
		return mediaMentions;
	}

	public void setMediaMentions(ArrayList<Integer> mediaMentions) {
		this.mediaMentions = mediaMentions;
	}

	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public int getClassification() {
		return classification;
	}

	public void setClassification(int classification) {
		this.classification = classification;
	}

	public String getCleanText() {
		return cleanText;
	}

	public void setCleanText(String cleanText) {
		this.cleanText = cleanText;
	}

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}
	

	
}
