package com.example;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        ConnectionString connectionString = new ConnectionString("mongodb+srv://doodlesearchengine:Ff8KgnvPX35ufw7@cluster0.zhk66.mongodb.net/myFirstDatabase?retryWrites=true&w=majority&connectTimeoutMS=30000&socketTimeoutMS=30000");
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .build();
        MongoClient mongoClient = MongoClients.create(settings);
        MongoDatabase database = mongoClient.getDatabase("sample_training");
        MongoCollection<Document> collection = database.getCollection("companies");
        Document query = new Document("_id", new ObjectId("52cdef7c4bab8bd675297d8a"));
        Document result = collection.find(query).iterator().next();
        System.out.println(result.getString("name"));
    }
}
