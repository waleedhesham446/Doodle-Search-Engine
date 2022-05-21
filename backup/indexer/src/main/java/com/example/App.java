package com.example;

import org.bson.Document;                //  collision
// import org.bson.types.ObjectId;

import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;


import org.jsoup.Connection;
import org.jsoup.Jsoup;
// import org.jsoup.nodes.Document;         //  collision
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
// import org.tartarus.snowball.ext.PorterStemmer;
// import org.nocrala.tools.database.tartarus.*;
import ca.rmen.porterstemmer.PorterStemmer;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

import static java.nio.file.Files.readAllLines;

class Adapter {
    public org.bson.Document MogoDocument;
    public org.jsoup.nodes.Document JsoupDocument;
}

class  WordInPage {
    public int importance;
    public int repetition;

    public WordInPage() {
        this.importance = 7;
        this.repetition = 1;
    }

    @Override
    public String toString() {
        return "{ R: " + repetition + " | I: " + importance + " }";
    }
}

public class App 
{
    public static ArrayList<org.jsoup.nodes.Document> docs = new ArrayList<org.jsoup.nodes.Document>();
    //static Map<String,Integer> mp_All_file=new TreeMap<>();
    //static Map<String,Integer> FreqOfEachWordPerAllFiles=new TreeMap<>();
    //static Map<String,List<String>> HTMLL=new TreeMap<>();
    // public static HashMap<String,HashMap<String,Integer>> WWord =new HashMap<String,HashMap<String,Integer>>();
    public static HashMap<String,HashMap<String,WordInPage>> WWord =new HashMap<String,HashMap<String,WordInPage>>();
    public static String title;
    public static Elements h1;
    public static Elements h2;
    public static Elements h3;
    public static Elements h4;
    public static Elements h5;
    public static Elements h6;
    public static void main( String[] args )  throws IOException {
        
        PorterStemmer stem = new PorterStemmer();
        List<String> stop_words = readAllLines(Paths.get("s.txt"));

        String Ls[]=new String[3];
        Ls[0]="https://www.bbc.com";
        Ls[1]="https://www.javatpoint.com/java-map#:~:text=A%20map%20contains%20values%20on,the%20basis%20of%20a%20key.";
        Ls[2]="https://www.facebook.com/abdelrhman.fathi.735/";
        for (int k = 0; k < 1; k++) {
            String url = Ls[k];

            Connection con = Jsoup.connect(url);
            org.jsoup.nodes.Document doc = con.get();
            String X = doc.text();
            title = doc.title();
            h1 = doc.select("h1");
            h2 = doc.select("h2");
            h3 = doc.select("h3");
            h4 = doc.select("h4");
            h5 = doc.select("h5");
            h6 = doc.select("h6");
            String[] q = X.split("[\\s,]+");
            StringBuilder builder = new StringBuilder();
            for (String word : q) {
                if (!stop_words.contains(word.toLowerCase(Locale.ROOT))) {
                    //////////////////
                    // stem.setCurrent(word);
                    // stem.stem();
                    // word = stem.getCurrent();
                    word = stem.stemWord(word);
                    ////////////////////////
                    builder.append(word);
                    builder.append(' ');
                }
            }
            String result = builder.toString().trim();
            // System.out.println(result);
            // Map<String,Integer> mp_one_file=new TreeMap<>();
            String[] Index_Words = result.split("[\\s,]+");
            for (int i = 0; i < Index_Words.length; i++) {

                if (WWord.get(Index_Words[i])!=null) {
                    if (WWord.get(Index_Words[i]).get(url)!=null) {
                        WWord.get(Index_Words[i]).get(url).repetition++;
                        WWord.get(Index_Words[i]).put(url, WWord.get(Index_Words[i]).get(url));
                    }
                    else {
                        // WWord.get(Index_Words[i]).put(url, 1);
                        WordInPage wp = new WordInPage();
                        determinImportance(wp, Index_Words[i]);
                        WWord.get(Index_Words[i]).put(url, wp);
                    }
                } else {
                    // HashMap<String, Integer> TEMPO = new HashMap<String, Integer>();
                    HashMap<String, WordInPage> TEMPO = new HashMap<String, WordInPage>();
                    WordInPage wp = new WordInPage();
                    determinImportance(wp, Index_Words[i]);
                    // TEMPO.put(url, 1);
                    TEMPO.put(url, wp);
                    WWord.put(Index_Words[i], TEMPO);
                    // System.out.println(WWord);
                }
            }
        }

        ConnectionString connectionString = new ConnectionString("mongodb+srv://doodlesearchengine:Ff8KgnvPX35ufw7@cluster0.zhk66.mongodb.net/myFirstDatabase?retryWrites=true&w=majority&connectTimeoutMS=30000&socketTimeoutMS=30000");
        MongoClientSettings settings = MongoClientSettings.builder()
            .applyConnectionString(connectionString)
            .build();
        MongoClient mongoClient = MongoClients.create(settings);
        MongoDatabase database = mongoClient.getDatabase("Indexer");
        MongoCollection<Document> collection = database.getCollection("Word_vs_URL");
        
        HashMap<String, Integer> wp = new HashMap<String, Integer>();
        Map<String, Object> url = new HashMap<String, Object>();
        
        for (Map.Entry<String,HashMap<String,WordInPage>> entry : WWord.entrySet()) {
            
            Document newRecord = new Document();
            ArrayList<Map<String, Object>> urls = new ArrayList<Map<String, Object>>();
            
            for(Map.Entry<String,WordInPage> entry2 : entry.getValue().entrySet()) {
                wp.put("importance", entry2.getValue().importance);
                wp.put("repetition", entry2.getValue().repetition);
                url.put("url", entry2.getKey());
                url.put("wp", wp);
                urls.add(url);
            }
            newRecord.append("word", entry.getKey());
            newRecord.append("urls", urls);
            System.out.println(entry);
            collection.insertOne(newRecord);
        }
        System.out.println("DONE");
        
//        for(Map.Entry<String,Integer> entry:
//                mp_one_file.entrySet())
//        {
//            FreqOfEachWordPerAllFiles.put(entry.getKey(),1);
//        }
//        for(Map.Entry<String,Integer> entry:
//                FreqOfEachWordPerAllFiles.entrySet())
//        {
//            System.out.println(entry.getKey()+
//                    " - "+entry.getValue());
//        }

//        Elements elts = doc.select("*:containsOwn(BBC)");
//
//        for(Element e : elts) {
//            System.out.println(e.tagName());
//        }
    }

    public static void determinImportance(WordInPage wp, String word){
        if(title.toLowerCase().contains(word)){
            wp.importance = 0;
        }
        if(wp.importance > 1) {
            for (Element h1Tag : h1) {
                String h1TagContent = h1Tag.text();
                if(h1TagContent.contains(word)){
                    wp.importance = 1;
                    break;
                }
            }
        }
        if(wp.importance > 2) {
            for (Element h2Tag : h2) {
                String h2TagContent = h2Tag.text();
                if(h2TagContent.contains(word)){
                    wp.importance = 2;
                    break;
                }
            }
        }
        if(wp.importance > 3) {
            for (Element h3Tag : h3) {
                String h3TagContent = h3Tag.text();
                if(h3TagContent.contains(word)){
                    wp.importance = 3;
                    break;
                }
            }
        }
        if(wp.importance > 4) {
            for (Element h4Tag : h4) {
                String h4TagContent = h4Tag.text();
                if(h4TagContent.contains(word)){
                    wp.importance = 4;
                    break;
                }
            }
        }
        if(wp.importance > 5) {
            for (Element h5Tag : h5) {
                String h5TagContent = h5Tag.text();
                if(h5TagContent.contains(word)){
                    wp.importance = 5;
                    break;
                }
            }
        }
        if(wp.importance > 6) {
            for (Element h6Tag : h6) {
                String h6TagContent = h6Tag.text();
                if(h6TagContent.contains(word)){
                    wp.importance = 6;
                    break;
                }
            }
        }
    }
}
