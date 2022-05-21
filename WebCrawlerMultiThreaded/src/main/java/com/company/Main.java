package com.company;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.*;
import org.bson.Document;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Scanner;

import static com.company.WebCrawler.*;

class RobotRule {
    public String userAgent;
    public ArrayList<String> rules;

    RobotRule() {
        rules = new ArrayList<String>();
    }
}
public class Main {
    public static ArrayList<WebCrawler> bots = new ArrayList<WebCrawler>();
    public static String[] seeds = new String[20];

    public Main() throws MalformedURLException {
    }

    public static void main(String[] args) throws Exception {
        File seedsObj = new File("seeds.txt");
        Scanner seedsReader = new Scanner(seedsObj);
        int seedIndex = 0;
        while (seedsReader.hasNextLine()) {
            seeds[seedIndex++] = seedsReader.nextLine();
        }
        seedsReader.close();

        int botsSize = 10;
        for (int i = 0; i < botsSize; i++) {
            bots.add(new WebCrawler(seeds[i], i+1));
        }
        for (int i = 0; i < botsSize; i++) {
            try {
                bots.get(i).getThread().join();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        ConnectionString connectionString = new ConnectionString("mongodb+srv://doodlesearchengine:Ff8KgnvPX35ufw7@cluster0.zhk66.mongodb.net/myFirstDatabase?retryWrites=true&w=majority&connectTimeoutMS=30000&socketTimeoutMS=30000");
        MongoClientSettings settings = MongoClientSettings.builder().applyConnectionString(connectionString).build();
        MongoClient mongoClient = MongoClients.create(settings);
        MongoDatabase database = mongoClient.getDatabase("Crawler");
        MongoCollection<Document> collection = database.getCollection("Link_Popularity");
        ArrayList<Document> documents = new ArrayList<Document>();
        collection.deleteMany(new Document());
        int i = 0;
        for (Pair link : visitedLinks) {
            System.out.println(link.first);
            // System.out.println(link.second);
            Document newRecord = new Document();
            newRecord.append("url", link.first);
            // newRecord.append("first50Word", link.second);
            newRecord.append("popularity", link.popularity);
            newRecord.append("insideLinks", linksOfVisitedLinks.get(link.first));
            newRecord.append("doc", docs.get(i));
            newRecord.append("title", link.title);
            newRecord.append("description", link.description);
            i++;
            // collection.insertOne(newRecord);
            documents.add(newRecord);
        }
        collection.insertMany(documents);

        System.out.println("DONE");
    }
}