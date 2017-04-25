package lnu.agt;

import java.io.BufferedReader;
import java.io.FileReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;

import org.codehaus.jackson.JsonNode;

import weka.classifiers.Classifier;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader.ArffReader;

public class TextClassifier {

	Instances data;
	JsonNode tweet;
	DecimalFormat formatter;
	Classifier cls;
	
	//model params
	String path = "models/";
	String newModel = "";
	
	public TextClassifier(JsonNode tweet) throws Exception {
		this.tweet = tweet;
		
		//load dataset to get correct format of input data
		BufferedReader reader = new BufferedReader(new FileReader("dummy.arff"));
		ArffReader arff = new ArffReader(reader);
		data = arff.getData();
		reader.close();
		//ghjg
		
		formatter = new DecimalFormat("#0.00000");     


		//get entities from Twitter
		ArrayList<Integer> hashtag = null;
		ArrayList<Integer> url = null;
		ArrayList<Integer> user = null;
		ArrayList<Integer> symbols = null;
		ArrayList<Integer> media = null;
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
		    	hashtag = list;
		    }else if (i==1){
		    	url = list;
		    }else if (i==2){
		    	user = list;
		    }else if (i==3){
		    	symbols = list;
		    }else if (i==4){
		    	media = list;
		    }
		    i++;
		}
		Instance inst = new DenseInstance(data.numAttributes());
		TweetText t = new TweetText(tweet.get("text").asText(), tweet.get("id").asText(), tweet.get("lang").asText(), -1, hashtag, url, user, symbols, media);

		inst.setValue(data.attribute(0), data.attribute(0).addStringValue(t.getCleanText()));
		data.add(0, inst);
		
	}
	
	public void setClassifier(String classifier) {
		newModel = classifier;
	}
	
	public double getClassification() throws Exception{
		String model;
		if(newModel.equals("")){
			model  = "filtrandfor1.0.model";
		} else {
			model = newModel;
		}
		//load model
		cls = (Classifier) weka.core.SerializationHelper.read(path + model);
		return cls.distributionForInstance(data.instance(0))[1];
	}
	
	public void printClassification() throws Exception{
		System.out.println(formatter.format(getClassification()));
	}
	
	public Classifier getClassifier(){
		return cls;
	}
	
	
}
