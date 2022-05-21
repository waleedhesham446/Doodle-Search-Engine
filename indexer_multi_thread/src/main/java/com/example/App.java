package com.example;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Projections;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.nio.file.Files.readAllLines;

class Adapter {
    public org.bson.Document MogoDocument;
    public org.jsoup.nodes.Document JsoupDocument;
}

class  WordInPage {
    public int importance;
    public int repetition;
    public int pageLength;
    public double tf;

    public WordInPage(int pl) {
        this.importance = 7;
        this.repetition = 1;
        this.pageLength = pl;
    }

    public void calcTF() {
        tf =  1.0*repetition/pageLength;
    }

    @Override
    public String toString() {
        return "{ R: " + repetition + " | I: " + importance + " }";
    }
}

public class App 
{
    public static ArrayList<org.jsoup.nodes.Document> docs = new ArrayList<org.jsoup.nodes.Document>();
    public static ConcurrentHashMap<String,ConcurrentHashMap<String,WordInPage>> WWord =new ConcurrentHashMap<String,ConcurrentHashMap<String,WordInPage>>();
    public static String Ls[];
    public static List<String> stop_words;
    public static ArrayList<IndexerThread> bots = new ArrayList<IndexerThread>();
    public static void main( String[] args )  throws IOException {

        stop_words = readAllLines(Paths.get("s.txt"));
        Ls = new String[5005];
        int urlIndex = 0;
        ConnectionString connectionString = new ConnectionString("mongodb+srv://doodlesearchengine:Ff8KgnvPX35ufw7@cluster0.zhk66.mongodb.net/myFirstDatabase?retryWrites=true&w=majority&connectTimeoutMS=30000&socketTimeoutMS=30000");
        MongoClientSettings settings = MongoClientSettings.builder().applyConnectionString(connectionString).build();
        MongoClient mongoClient = MongoClients.create(settings);
        MongoDatabase database = mongoClient.getDatabase("Crawler");
        MongoCollection<Document> collection = database.getCollection("Link_Popularity");
        
        Bson projection = Projections.fields(Projections.include("url"), Projections.excludeId());
        MongoCursor<Document> cursor = collection.find().projection(projection).iterator();
        
        while(cursor.hasNext()) {
            Ls[urlIndex++] = cursor.next().get("url").toString();
        }
        
        int botsSize = 10;
        for (int i = 0; i < botsSize; i++) {
            bots.add(new IndexerThread(i));
        }
        for (int i = 0; i < botsSize; i++) {
            try {
                bots.get(i).getThread().join();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        System.out.println("UPLOADING");
        database = mongoClient.getDatabase("Indexer");
        collection = database.getCollection("Word_vs_URL");
        collection.deleteMany(new Document());
        
        Map<String, Object> url;
        ArrayList<Document> documents = new ArrayList<Document>();

        for (ConcurrentHashMap.Entry<String,ConcurrentHashMap<String,WordInPage>> entry : WWord.entrySet()) {
            
            Document newRecord = new Document();
            ArrayList<Map<String, Object>> urls = new ArrayList<Map<String, Object>>();
            
            for(ConcurrentHashMap.Entry<String,WordInPage> entry2 : entry.getValue().entrySet()) {
                url = new HashMap<String, Object>();
                url.put("url", entry2.getKey());
                url.put("importance", entry2.getValue().importance);
                url.put("repetition", entry2.getValue().repetition);
                url.put("pageLength", entry2.getValue().pageLength);
                entry2.getValue().calcTF();
                url.put("TF", entry2.getValue().tf);
                urls.add(url);
            }
            newRecord.append("word", entry.getKey());
            newRecord.append("urls", urls);
            documents.add(newRecord);
        }
        collection.insertMany(documents);
        System.out.println("DONE");
    }
}
