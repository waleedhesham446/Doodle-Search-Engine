package com.example;

import static com.example.App.Ls;
import static com.example.App.stop_words;
import static com.example.App.WWord;

import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import ca.rmen.porterstemmer.PorterStemmer;

public class IndexerThread implements Runnable {
    
    private int ID;
    private Thread thread;
    private int startIndex;
    private int endIndex;

    public static Elements h1;
    public static Elements h2;
    public static Elements h3;
    public static Elements h4;
    public static Elements h5;
    public static Elements h6;
    public static String title;
    
    IndexerThread(int threadIndex) {
        this.ID = threadIndex;
        this.startIndex = threadIndex * 500;
        this.endIndex = (threadIndex + 1) * 500;
        
        thread = new Thread(this);
        thread.start();
    }
    
    @Override
    public void run() {
        PorterStemmer stem = new PorterStemmer();

        int currentDocumentIndex = 1;
        urlsLoop:
        for (int k = startIndex; k < endIndex; k++) {
            System.out.println(ID + " - " + currentDocumentIndex++);
            String url = Ls[k];
            
            Connection con = null;
            org.jsoup.nodes.Document doc;
            try {
                con = Jsoup.connect(url);
                doc = con.get();
            } catch (Exception e) {
                System.out.println("ERROR: " + e);
                continue urlsLoop;
            }

            String X = doc.text();
            title = doc.title();
            Elements hTags = doc.select("h1, h2, h3, h4, h5, h6");
            h1 = hTags.select("h1");
            h2 = hTags.select("h2");
            h3 = hTags.select("h3");
            h4 = hTags.select("h4");
            h5 = hTags.select("h5");
            h6 = hTags.select("h6");

            X = " " + X + " ";
            X = X.replaceAll("[^a-zA-Z0-9]", " ");
            X = X.trim().toLowerCase(Locale.ROOT);
            X = X.replaceAll("\\s+", " ");
            
            String[] q = X.split("[\\s,]+");
            StringBuilder builder = new StringBuilder();
            for (String word : q) {
                if (!stop_words.contains(word.toLowerCase(Locale.ROOT)) && Pattern.matches("[a-zA-Z]+",word) && word.length() > 2) {
                    word = stem.stemWord(word);
                    builder.append(word);
                    builder.append(' ');
                }
            }
            String result = builder.toString().trim();
            String[] Index_Words = result.split("[\\s,]+");
            int pageLength = Index_Words.length;
            for (int i = 0; i < Index_Words.length; i++) {

                if (WWord.get(Index_Words[i]) != null) {
                    if (WWord.get(Index_Words[i]).get(url) != null) {
                        WWord.get(Index_Words[i]).get(url).repetition++;
                        WWord.get(Index_Words[i]).put(url, WWord.get(Index_Words[i]).get(url));
                    }
                    else {
                        WordInPage wp = new WordInPage(pageLength);
                        determinImportance(wp, Index_Words[i]);
                        WWord.get(Index_Words[i]).put(url, wp);
                    }
                } else {
                    ConcurrentHashMap<String, WordInPage> TEMPO = new ConcurrentHashMap<String, WordInPage>();
                    WordInPage wp = new WordInPage(pageLength);
                    determinImportance(wp, Index_Words[i]);
                    TEMPO.put(url, wp);
                    WWord.put(Index_Words[i], TEMPO);
                }
            }
        }
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
    
    public Thread getThread() {
        return thread;
    }
}
