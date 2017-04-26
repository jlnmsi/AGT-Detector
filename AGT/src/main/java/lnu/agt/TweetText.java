package lnu.agt;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.regex.Pattern;

import org.codehaus.jackson.JsonNode;



public class TweetText {

	String id;
	String text;
	String lang;
	String cleanText;
	JsonNode tweet;
	ArrayList<Integer> userMentions;
	ArrayList<Integer> hashtagMentions;
	ArrayList<Integer> symbolMentions;
	ArrayList<Integer> urlMentions;
	ArrayList<Integer> mediaMentions;
	int classification;
	
	public TweetText(int classification, JsonNode tweet) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException{
		text = tweet.get("text").asText();
		id = tweet.get("id").asText();
		lang = tweet.get("lang").asText();
		this.tweet = tweet;
		this.classification = classification;
		setEntities();
		cleanTweet();
	}
	
	public TweetText(JsonNode tweet) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException{
		text = tweet.get("text").asText();
		id = tweet.get("id").asText();
		lang = tweet.get("lang").asText();
		this.tweet = tweet;
		setEntities();
		cleanTweet();
	}
	
	public void setEntities(){
		//get entities from Twitter
		hashtagMentions = null;
		urlMentions = null;
		userMentions = null;
		symbolMentions = null;
		mediaMentions = null;
		JsonNode rootNode = tweet.get("entities");
		Iterator<JsonNode> entityIterator = rootNode.getElements();
		int i = 0;
		while (entityIterator.hasNext()) {
		    JsonNode entnode = entityIterator.next();
		    int index = entnode.toString().indexOf("\"indices\":");
		    int indexEnd =-1;
		    String s;
		    ArrayList<Integer> list = new ArrayList<Integer>();
		    if(index>=0){indexEnd = entnode.toString().substring(index).indexOf("]");}
		    while (index >= 0) {
		        s = entnode.toString().substring(index+11, index+indexEnd);
		        int innerIndex = s.indexOf(",");
		        int start = Integer.parseInt(s.substring(0, innerIndex));
		        int end = Integer.parseInt(s.substring(innerIndex+1));
		        list.add(start);
		        list.add(end);
		        index = entnode.toString().indexOf("\"indices\":", index + 1);
		        if(index>=0){indexEnd = entnode.toString().substring(index).indexOf("]");}
		    }
		    if(i==0){
		    	hashtagMentions = list;
		    }else if (i==1){
		    	urlMentions = list;
		    }else if (i==2){
		    	userMentions = list;
		    }else if (i==3){
		    	symbolMentions = list;
		    }else if (i==4){
		    	mediaMentions = list;
		    }
		    i++;
		}	
	}
	

	public void cleanTweet() throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
		if(text.equals("")) {
			System.out.println("Empty string");
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
		//regex cleaning
		tmp = tmp.replaceAll("[()\\[\\]/%\\\\:#]", "")
					.replaceAll("^[(]?^[-+]?\\d+([.,][\\d]+)?\\w*", "number ")
					.replaceAll("[^a-zA-Z0-9_]+$", "")
					.replaceAll("[°'´\"`.,’!?]", "")
					.replaceAll("[0-9][(k|m)?mh?]?[ms]?", "")
					.replaceAll("\\s+",  " ").trim();		
		
		
		cleanText = tmp.toLowerCase();
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
