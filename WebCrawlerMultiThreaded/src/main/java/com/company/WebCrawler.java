package com.company;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

// import static com.company.Main.bots;

class Pair {
  public String first;
  public String second;
    public int popularity;
    public String description;
    public String title;

  public Pair(String first, String second,String desc,String tit) {
    this.first = first;
    this.second = second;
    this.popularity = 0;
    this.description=desc;
    this.title = tit;

  }
}

public class WebCrawler implements Runnable {
  private static final Object visitedLockObj1 = new Object();
  private static final Object visitedLockObj2 = new Object();
  private static final Object robootLockObj = new Object();
  private Thread thread;
  private String first_link;
  private static int currentDoc = 0;
  public static ArrayList<Pair> visitedLinks = new ArrayList<>();
  public static ConcurrentHashMap<String, ArrayList<String>> linksOfVisitedLinks = new ConcurrentHashMap<String, ArrayList<String>>();
  public static ArrayList<String> docs = new ArrayList<String>();
  public static HashMap<String, ArrayList<String>> robots = new HashMap<String, ArrayList<String>>();
  private int ID;

  public static ArrayList<String> robotSafe(URL url) {
    String strHost = url.getHost();
    // System.out.println(strHost);
    String strRobot = "https://" + strHost + "/robots.txt";

    if (robots.get(strRobot) != null) return robots.get(strRobot);

    URL urlRobot;
    try {
      urlRobot = new URL(strRobot);
    } catch (Exception e) {
      System.out.println("NULL2");
      return null;
    }
    System.out.println(urlRobot);
    ArrayList<String> robotRules = new ArrayList<String>();
    robots.put(strRobot, robotRules);

    String strCommands;
    try {
      InputStream urlRobotStream = urlRobot.openStream();
      // System.out.println(urlRobotStream);
      byte b[] = new byte[1000];
      // System.out.println(b);
      int numRead = urlRobotStream.read(b);
      // System.out.println(numRead);
      // numRead = 1000;
      if (numRead == -1) return null;

      strCommands = new String(b, 0, numRead);
      // System.out.println(strCommands);
      numRead = urlRobotStream.read(b);
      while (numRead != -1) {
        String newCommands = new String(b, 0, numRead);
        strCommands += newCommands;
        numRead = urlRobotStream.read(b);
      }
      // System.out.println("CLOSED");
      urlRobotStream.close();
    } catch (Exception e) {
      System.out.println(e);
      return null; // if there is no robots.txt file, it is OK to search
    }

    if (
      strCommands.contains("Disallow")
    ) { // if there are no "disallow" values, then they are not blocking anything.
      String[] split = strCommands.split("\n");
      String mostRecentUserAgent = null;
      boolean disallow = false;
      for (String s : split) {
        String line = s.trim();
        if (line.toLowerCase().startsWith("user-agent")) {
          int start = line.indexOf(":") + 1;
          int end = line.length();
          mostRecentUserAgent = line.substring(start, end).trim();
          disallow = mostRecentUserAgent.equals("*");
        } else if (line.startsWith("Disallow")) {
          if (mostRecentUserAgent != null && disallow) {
            int start = line.indexOf(":") + 1;
            int end = line.length();
            String path = url.getHost();
            String newRule = path + line.substring(start, end).trim();
            robotRules.add(newRule);
          }
        }
      }

      synchronized (robootLockObj) {
        robots.put(strRobot, robotRules);
      }
      // for (String notAllowedUrl : robotRules) {
      //     // System.out.println(notAllowedUrl);
      // }
    }
    return robotRules;
  }

  public WebCrawler(String link, int num) {
    first_link = link;
    ID = num;
    System.out.println("WebCrawler Created: " + ID + link);

    thread = new Thread(this);
    thread.start();
  }

  @Override
  public void run() {
    try {
      crawl(1, first_link);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void crawl(int level, String url) throws Exception {
    if (currentDoc < 5000) {
      Document doc = request(url);
      if (doc != null) {
        for (Element link : doc.select("a[href]")) {
          String next_link = link.absUrl("href");
          synchronized (this) {
            try {
              boolean checkRepeat = true;
              for (Pair visitedLink : visitedLinks) {
                if (visitedLink.first.equals(next_link)) {
                  synchronized (visitedLockObj2) {
                    visitedLink.popularity++;
                  }
                  checkRepeat = false;
                  break;
                }
              }
              if (checkRepeat) {
                crawl(level + 1, next_link);
                // if(level == 1){

                //     System.out.println(ID + " finished round one");
                //     // crawl(level+1, next_link);
                // }
              }
            } catch (Exception e) {
              System.out.println("ssssssssssssssssssssssss");
              e.printStackTrace();
            }
          }
        }
      }
    }
    if (level == 1) {
      System.out.println(ID + " finished round one");
      // crawl(level+1, next_link);
    }
  }

  private Document request(String url) {
    try {
      URL urlRobot = new URL(url);
      ArrayList<String> notAllowedList = robotSafe(urlRobot);
      // System.out.println("RobotRuleSize = " + notAllowedList.size());
      if (notAllowedList != null) {
        for (String rRole : notAllowedList) {
          if (rRole.contains(url)) {
            return null;
          }
        }
      }
      Connection con = Jsoup.connect(url);
      Document doc = con.get();
      String description=null;
      String title=null;
      title=doc.title();
        Elements elements = doc.select("meta[name=description]");
        for (Element element : elements) {
            description= element.attr("content");
            if (description != null) break;
        }
      if (con.response().statusCode() == 200) {
        // System.out.println(String.format("Bot: %d - URL: %s", ID, url));

        String docContent = doc.text();
        String[] docContentArr = docContent.trim().split("\\s");
        int docContentLen = docContentArr.length;
        StringBuilder docContentBuffer = new StringBuilder();
        for (int i = 0; i < (Math.min(docContentLen, 50)); i++) {
          if (docContentArr[i].length() > 0) {
            docContentBuffer.append(docContentArr[i].charAt(0));
          }
        }
        String docContentFirst50 = docContentBuffer.toString();

        for (Pair link : visitedLinks) {
          if (link.second.equals(docContentFirst50)) {
            synchronized (visitedLockObj2) {
              link.popularity++;
            }
            return null;
          }
        }

        ArrayList<String> linksOfVisitedLinksArr = new ArrayList<String>();
        for (Element link : doc.select("a[href]")) {
          linksOfVisitedLinksArr.add(link.absUrl("href"));
        }
        
        linksOfVisitedLinks.put(url, linksOfVisitedLinksArr);
        
        Pair newPair = new Pair(url, docContentFirst50,description,title);
        synchronized (visitedLockObj1) {
            String docText=doc.text();
          // System.out.println(String.format("%d : %s : %s", ID, url, docContentFirst50));
          visitedLinks.add(newPair);
          docs.add((docText));
          currentDoc++;
          if (currentDoc % 100 == 0) System.out.println(currentDoc);
        }

        return doc;
      }
      return null;
    } catch (Exception e) {
      return null;
    }
  }

  public Thread getThread() {
    return thread;
  }
}
