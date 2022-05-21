import com.mongodb.client.model.Projections;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import org.bson.Document;

import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import org.bson.conversions.Bson;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.tartarus.snowball.ext.PorterStemmer;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import jakarta.servlet.http.*;
import java.io.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;


public class NameGender extends  HttpServlet {
    public static String X;
    public static String OriginalPhrase;
    public static PorterStemmer stem = new PorterStemmer();
    public static List<String> stop_words;

    public static ConnectionString connectionString ;
    public static MongoClientSettings settings ;
    public static MongoClient mongoClient ;
    public static MongoDatabase database ;
    public static MongoCollection<Document>collection ;
    public static MongoCursor<Document> cursor;

    public static HashMap<String, Object> crawledUrls = new HashMap<String, Object>();          // from crawler database
    public static HashMap<String, Double> outputHM = new HashMap<String, Double>();                 // final output

    public static HashMap<String,Integer> LinkInd=new HashMap<String,Integer>();

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        ArrayList<String> suggestions = new ArrayList<String>();

        connectionString = new ConnectionString("mongodb://localhost:27017");
        settings = MongoClientSettings.builder().applyConnectionString(connectionString).build();
        mongoClient = MongoClients.create(settings);
        database = mongoClient.getDatabase("Suggestion");
        collection = database.getCollection("Suggestion");

        cursor = collection.find().iterator();
        while(cursor.hasNext()) {
            suggestions.add(cursor.next().get("query").toString());
        }

        request.setAttribute("suggestions", suggestions);
        request.getRequestDispatcher("search.jsp").forward(request, response);
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        String result=null;
        boolean isphrase = false;

        X = request.getParameter("WORD_Query");

        connectionString = new ConnectionString("mongodb://localhost:27017");
        settings = MongoClientSettings.builder().applyConnectionString(connectionString).build();
        mongoClient = MongoClients.create(settings);
        database = mongoClient.getDatabase("Suggestion");
        collection = database.getCollection("Suggestion");

        Document newQuerySuggestion = new Document();
        newQuerySuggestion.append("query", X);
        collection.insertOne(newQuerySuggestion);

        try {
            stop_words = Files.readAllLines(Paths.get("C:\\Users\\fathi\\OneDrive\\Desktop\\s.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        database = mongoClient.getDatabase("Crawler");
        collection = database.getCollection("Link_Popularity");

        cursor = collection.find().iterator();
        while(cursor.hasNext()) {

            Document currentCursor = cursor.next();

            HashMap<String ,Object> CraweledUrl=new HashMap<>() ;
            CraweledUrl.put("popularity", currentCursor.get("popularity"));
            CraweledUrl.put("insideLinks", currentCursor.get("insideLinks"));
            CraweledUrl.put("title", currentCursor.get("title"));
            CraweledUrl.put("description", currentCursor.get("description"));
            CraweledUrl.put("doc", currentCursor.get("doc"));

            crawledUrls.put(currentCursor.get("url").toString(), CraweledUrl);
        }

        if (X.charAt(0) == '"' && X.charAt(X.length() - 1) == '"') {
            isphrase = true;
            OriginalPhrase = X.substring(1, X.length() - 1);
        }

        X = " " + X + " ";
        X = X.replaceAll("[^a-zA-Z0-9]", " ");
        X = X.trim().toLowerCase(Locale.ROOT);
        X = X.replaceAll("\\s+", " ");

        String[] q = X.split("[\\s,]+");
        StringBuilder queryBuilder = new StringBuilder();
        for (String word : q) {
            if (!stop_words.contains(word)) {
                stem.setCurrent(word);
                stem.stem();
                word=stem.getCurrent();
                queryBuilder.append(word);
                queryBuilder.append(' ');
            }
        }

        result = queryBuilder.toString().trim();
        String[] queryWords = result.split(" ");

        database = mongoClient.getDatabase("Indexer");
        collection = database.getCollection("Word_vs_URL");

        cursor = collection.find(Filters.in("word", queryWords)).iterator();

        ArrayList<ArrayList<HashMap<String, Object>>> words = new ArrayList<ArrayList<HashMap<String, Object>>>();
        while (cursor.hasNext()) {
            ArrayList<HashMap<String, Object>> urls = new ArrayList<HashMap<String, Object>>();
            Document currentCursor = cursor.next();
            System.out.println(currentCursor);
            ArrayList<Document> currentUrls = (ArrayList<Document>) currentCursor.get("urls");
            for (Document currentUrl : currentUrls) {
                HashMap<String, Object> url = new HashMap<String, Object>();
                url.put("url", currentUrl.get("url"));
                url.put("tf", currentUrl.get("TF"));
                url.put("importance", currentUrl.get("importance"));
                url.put("word", "word");
                urls.add(url);
            }
            words.add(urls);
        }

        Set<String> LinksSet = new HashSet<String>();
        HashMap<Integer,Integer> Edges = new HashMap<Integer,Integer>();

        int numWords = words.size();
        double IDF[] = new double[numWords];
        int wordIndex = 0;

//        for (ArrayList<HashMap<String, Object>> urlsOfWord : words) {
//            IDF[wordIndex] = Math.log(urlsOfWord.size() / 5000.0);
//            for (HashMap<String, Object> urlOfWord : urlsOfWord) {
//                LinksSet.add(urlOfWord.get("url").toString());
//            }
//            wordIndex++;
//        }

//        final int[] i = {0};
//
//        LinksSet.stream().forEach((Link1) -> {
//            LinkInd.put(Link1,i[0]);
//            final int[] j = {0};
//            LinksSet.stream().forEach((Link2) -> {
//                if(Link1.equals(Link2) == false) {
////                     if(crawledUrls.get(Link1) != null) {
//                        if(((ArrayList<String>)((HashMap<String,Object>)crawledUrls.get(Link1)).get("insideLinks")).contains(Link2)) {
//                            Edges.put(i[0], j[0]);
//                        }
////                     }else{
////                         System.out.println(Link1);
////                     }
//                }
//                j[0]++;
//            });
//            i[0]++;
//        });

//        StringBuilder pageRankerBuilder = new StringBuilder();
//        pageRankerBuilder.append(LinksSet.size());
//        pageRankerBuilder.append(" ");gr
//        pageRankerBuilder.append(Edges.size());
//
//        for (HashMap.Entry<Integer,Integer> Map : Edges.entrySet()) {
//            pageRankerBuilder.append("\n");
//            pageRankerBuilder.append(Map.getKey());
//            pageRankerBuilder.append(" ");
//            pageRankerBuilder.append(Map.getValue());
//        }

//        String rankerInput = pageRankerBuilder.toString();
//        pgrk9169 ranker = new pgrk9169();
//        ranker.rank(rankerInput,1,1)/;

//        wordIndex=0;
        for (ArrayList<HashMap<String, Object>> urlsOfWord : words) {
            IDF[wordIndex] = Math.log(urlsOfWord.size() / 5000.0);
            for (HashMap<String, Object> urlOfWord : urlsOfWord) {
//                Rank(urlOfWord, IDF[wordIndex], ranker.pageRank);
                Rank(urlOfWord, IDF[wordIndex]);
            }
            wordIndex++;
        }

        outputHM = sortByValue(outputHM);

        if (isphrase == true) {
            try {
                filterLinks();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        ArrayList<String> sortedLinks = new ArrayList<String>();
        ArrayList<String> sortedTitles = new ArrayList<String>();
        ArrayList<String> sortedDescriptions = new ArrayList<String>();
        for (Map.Entry<String,Double> ma : outputHM.entrySet()) {
            sortedLinks.add("\""+ma.getKey()+"\"");

            String title = ((HashMap<String, Object>) crawledUrls.get(ma.getKey())).get("title").toString();
            if(title.equals("")) title = ma.getKey();
            sortedTitles.add("\""+title+"\"");

//            int indexOfWord = ((HashMap<String, Object>) crawledUrls.get(ma.getKey())).get("doc").toString().indexOf("word");
            int docLength = ((HashMap<String, Object>) crawledUrls.get(ma.getKey())).get("doc").toString().length();
            int endIndex = 100;
            if(docLength < 100) endIndex = docLength;
            String desc = ((HashMap<String, Object>) crawledUrls.get(ma.getKey())).get("doc").toString().substring(0, 150).replace("\n", "").replace("\r", "").replace("//", "").replace("\"", "");
            sortedDescriptions.add("\""+desc+"\"");
        }
        Collections.reverse(sortedLinks);
        Collections.reverse(sortedTitles);
        Collections.reverse(sortedDescriptions);

//        request.setAttribute("results",outputHM);
//        String nextHTML = "results.html";
//        RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(nextHTML);
//        dispatcher.forward(request, response);
        // request.setAttribute("links", outputHM);                                     // This will be available as ${links}
        // request.getRequestDispatcher("/hello.jsp").forward(request, response);
        request.setAttribute("links", sortedLinks);
        request.setAttribute("titles", sortedTitles);
        request.setAttribute("descriptions", sortedDescriptions);
        request.getRequestDispatcher("home.jsp").forward(request, response);

//        response.sendRedirect("home.jsp");
        /*
        response.setContentType("text/html");

        StringBuilder stb1 = new StringBuilder();
        StringBuilder stb2 = new StringBuilder();
        StringBuilder stb3 = new StringBuilder();

        for (Map.Entry<String,Double> ma : outputHM.entrySet()) {
            // if(crawledUrls.get(ma.getKey())!=null) {
            String title = ((HashMap<String, Object>) crawledUrls.get(ma.getKey())).get("title").toString();
            if(title.equals("")) title = ma.getKey();
            stb1.append(title);
            stb1.append(" ^@# ");
            stb2.append(ma.getKey());
            stb2.append(" ^@# ");
//            int indexOfWord = ((HashMap<String, Object>) crawledUrls.get(ma.getKey())).get("doc").toString().indexOf("word");
            int docLength = ((HashMap<String, Object>) crawledUrls.get(ma.getKey())).get("doc").toString().length();
            int endIndex = 100;
            if(docLength < 100) endIndex = docLength;
            String desc = ((HashMap<String, Object>) crawledUrls.get(ma.getKey())).get("doc").toString().substring(0, endIndex);
            stb3.append(desc);
            stb3.append(" ^@# ");
            // }
        }

        String page = "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "\n" +
                "<head>\n" +
                "    <meta charset=\"utf-8\">\n" +
                "    <title>Autocomplete Search Box</title>\n" +
                "    <link rel=\"stylesheet\" href=\"style2.css\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <link rel=\"stylesheet\" href=\"https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.3/css/all.min.css\" />\n" +
                "    <script src=\"https://kit.fontawesome.com/afc97c838a.js\" crossorigin=\"anonymous\"></script>\n" +
                "    <title>results</title>\n" +
                "    <style>\n" +
                "        .hide {\n" +
                "            display: none;\n" +
                "        }\n" +
                "        \n" +
                "        .link_container {\n" +
                "            margin: 10px;\n" +
                "            padding: 5px;\n" +
                "        }\n" +
                "        \n" +
                "        .link_container .url_container {\n" +
                "            padding: 10px;\n" +
                "            font-size: bold;\n" +
                "            font-weight: 20px;\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "\n" +
                "<body>\n" +
                "    <div class=\"wrapper\">\n" +
                "        <div class=\"search-input\">\n" +
                "            <a href=\"\" target=\"_blank\" hidden></a>\n" +
                "            <input type=\"text\" placeholder=\"Type to search..\">\n" +
                "            <div class=\"autocom-box\">\n" +
                "                <!-- here list are inserted from javascript -->\n" +
                "            </div>\n" +
                "            <div class=\"icon\" onclick=\"runSpeechRecognition()\"><i class=\"fa-solid fa-microphone\"></i></div>\n" +
                "        </div>\n" +
                "        <div id=\"output\" class=\"hide\"></div>\n" +
                "        <div id=\"output\" class=\"hide\"></div>\n" +
                "    </div>\n" +
                "    <div id=\"link\" class=\"link_container hide\">\n" +
                "        <div class=\"url_container\">\n" +
                "            <a class=\"url\">\n" +
                "\n" +
                "            </a>\n" +
                "        </div>\n" +
                "        <div class=\"description_container\">\n" +
                "            <p class=\"description\">\n" +
                "\n" +
                "            </p>\n" +
                "        </div>\n" +
                "\n" +
                "\n" +
                "\n" +
                "    </div>\n" +
                "    <div id=\"resultsContainer\">\n" +
                "\n" +
                "    </div>\n" +
                "    <div class=\"pageination_container\">\n" +
                "        <button id=\"nextPage\">F</button>\n" +
                "        <button id=\"prevPage\">B</button>\n" +
                "    </div>\n" +
                "    <input type=\"text\" value=\""+ stb2.toString() +"\" id=\"links\" class=\"hide\" >\n" +
                "    <input type=\"text\" value=\""+ stb1.toString() +"\" id=\"titles\" class=\"hide\" >\n" +
                "    <input type=\"text\" value=\""+ stb3.toString() +"\" id=\"descriptions\" class=\"hide\">\n" +
                "    \n" +
                "\n" +
                "</body>\n" +
                "\n" +
                "<script>\n" +
                "    let links = document.getElementById('links').value.split(' ^@# ');\n" +
                "\n" +
                "    let titles = document.getElementById('titles').value.split(' ^@# ');\n" +
                "\n" +
                "    let descriptions = document.getElementById('descriptions').value.split(' ^@# ');\n" +
                "\n" +
                "    const resultsContainer = document.getElementById('resultsContainer');\n" +
                "\n" +
                "    let currentPage = 0;\n" +
                "    let numLinks = descriptions.length;\n" +
                "    let numPages = numLinks / 10;\n" +
                "\n" +
                "    let limit = 10 > numLinks ? numLinks : 10;\n" +
                "    for (let i = 0; i < limit; i++) {\n" +
                "        let newlink = document.getElementById(\"link\").cloneNode(true);\n" +
                "        newlink.setAttribute(\"id\", `link-${i}`);\n" +
                "        \n" +
                "        newlink.classList.remove(\"hide\");\n" +
                "        newlink.children[0].children[0].textContent = titles[i];\n" +
                "        newlink.children[0].children[0].setAttribute(\"href\", links[i]);\n" +
                "        newlink.children[1].children[0].textContent = descriptions[i];\n" +
                "        resultsContainer.appendChild(newlink);\n" +
                "    }\n" +
                "\n" +
                "    document.getElementById('nextPage').onclick = () => {\n" +
                "        if(currentPage+1 == numPages) return;\n" +
                "        \n" +
                "        currentPage++;\n" +
                "        resultsContainer.innerHTML = '';\n" +
                "        let limit = ((currentPage+1)*10) >= numLinks ? numLinks : (currentPage+1)*10;\n" +
                "        for (let i = currentPage*10; i < limit; i++) {\n" +
                "            let newlink = document.getElementById(\"link\").cloneNode(true);\n" +
                "            newlink.setAttribute(\"id\", `link-${i}`);\n" +
                "            \n" +
                "            newlink.classList.remove(\"hide\");\n" +
                "            newlink.children[0].children[0].textContent = titles[i];\n" +
                "            newlink.children[0].children[0].setAttribute(\"href\", links[i]);\n" +
                "            newlink.children[1].children[0].textContent = descriptions[i];\n" +
                "            resultsContainer.appendChild(newlink);\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    document.getElementById('prevPage').onclick = () => {\n" +
                "        if(currentPage-1 == -1) return;\n" +
                "        \n" +
                "        currentPage--;\n" +
                "        resultsContainer.innerHTML = '';\n" +
                "        let limit = ((currentPage+1)*10) >= numLinks ? numLinks : (currentPage+1)*10;\n" +
                "        for (let i = currentPage*10; i < limit; i++) {\n" +
                "            let newlink = document.getElementById(\"link\").cloneNode(true);\n" +
                "            newlink.setAttribute(\"id\", `link-${i}`);\n" +
                "            \n" +
                "            newlink.classList.remove(\"hide\");\n" +
                "            newlink.children[0].children[0].textContent = titles[i];\n" +
                "            newlink.children[0].children[0].setAttribute(\"href\", links[i]);\n" +
                "            newlink.children[1].children[0].textContent = descriptions[i];\n" +
                "            resultsContainer.appendChild(newlink);\n" +
                "        }\n" +
                "    }\n" +
                "</script>\n" +
                "\n" +
                "<script src=\"/main.js\"></script>\n" +
                "<script src=\"/s.js\"></script>\n" +
                "<script src=\"/results.js\"></script>\n" +
                "\n" +
                "</html>";
        response.getWriter().println(page);
*/
    }

    public static void filterLinks() throws Exception {
        for (HashMap.Entry<String, Double> link : outputHM.entrySet()) {
            Connection con = Jsoup.connect(link.getKey());
            org.jsoup.nodes.Document doc = con.get();
            String docContent = ((HashMap<String, Object>)crawledUrls.get(link.getKey())).get("doc").toString();

            if(docContent.contains(OriginalPhrase) == false) {
                outputHM.remove(link.getKey());
            }
        }
    }


//    public static void Rank(HashMap<String,Object> HM,double IDF,double pgrank[] ){

    public static void Rank(HashMap<String,Object> HM,double IDF){
        double TF = (Double)HM.get("tf");
        double tf_idf = TF*IDF;
        int importance = (Integer)HM.get("importance");
        int popularity=0;
        // if ( crawledUrls.get(HM.get("url"))!= null) {
        popularity = (int) ((HashMap<String, Object>) crawledUrls.get(HM.get("url"))).get("popularity");
        // }
        double rank = 0;
//        rank = tf_idf*200 + (10-importance)*200 + popularity*50 + 700*pgrank[LinkInd.get(HM.get("url"))];
        rank = tf_idf*1000000 + (10-importance)*200 + popularity*50;
        if(outputHM.get(HM.get("url")) == null){
            outputHM.put((String)HM.get("url"), rank);
        }else{
            double newRank = outputHM.get(HM.get("url"))+rank*10;
            outputHM.put((String)HM.get("url"), newRank);
        }
    }

    public static HashMap<String, Double> sortByValue(HashMap<String, Double> hm) {
        List<Map.Entry<String, Double> > list = new LinkedList<Map.Entry<String, Double> >(hm.entrySet());

        Collections.sort(
                list,
                new Comparator<Map.Entry<String, Double> >() {
                    public int compare(
                            Map.Entry<String, Double> object1,
                            Map.Entry<String, Double> object2)
                    {
                        return (object1.getValue())
                                .compareTo(object2.getValue());
                    }
                });

        HashMap<String, Double> result
                = new LinkedHashMap<String, Double>();
        for (Map.Entry<String, Double> me : list) {
            result.put(me.getKey(), me.getValue());
        }

        return result;
    }
}

class pgrk9169 {
    private static List<ArrayList<Integer>> adjacencyList;
    // public static double[] pageRank;
    public static double[] pageRank;
    private static double[] contribution;
    private static double[] old_pageRank;
    private static int nodeCount;
    private static int edgeCount;
    private static int iterations;
    private static final double dampingFactor = 0.85;
    private static double offset = 0;
    /**
     //* @param args
     */
    public static void rank(String  input,int initialValue,int iteration ) {


        //Read the argument
        iterations =iteration ;

        //Get the input


        //Split the input as rows of nodes
        String[] rows = input.split("\n");

        //Check the boundary condition
        if (rows.length < 2) {
            System.out.println("Invalid input");
            return;
        }

        try {
            //Get the header row and its details
            int[] headerArray = extractNodeValues(rows[0]);

            //Get the number of nodes and edges
            nodeCount = headerArray[0];
            edgeCount = headerArray[1];

            if (edgeCount < rows.length -1) {
                System.out.println("Inconsistent data!");
                return;
            }

            if (nodeCount > 10) {
                iterations = 0;
                initialValue = -1;
            }

            //Initialize Variables
            initializeVariables(initialValue);

            //Create adjacency List
            createAdjacencyList(rows);

            //Calculate the contribution
            calculateContribution();

            int count =0;
            printPageRank(count);
            //Loop until the values converge
            do {
                //Calculate the page rank
                calculatePageRank();


                ++count;

                //Print the page rank
                if (nodeCount <= 10) {
                    printPageRank(count);
                }

            } while (!didConverge(count));


            if (nodeCount > 10) {
                printPageRankForHigherNodes(count);
            }

        } catch (Exception e) {
            // Auto-generated catch block
            System.out.println(e.getMessage());
            e.printStackTrace();
        }


    }

    /**
     * Reads a file and  returns a Strings Array
     * @param filePath
     * @return
     * @throws IOException
     */
    public static String readFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        List<String> inputFileList =  Files.readAllLines(path, StandardCharsets.US_ASCII);
        String inputString = "";
        for (String string : inputFileList) {
            inputString = inputString.concat(string) + "\n";
        }

        return inputString;
    }


    /**
     * Initializes the adjacencyList, hub and authority
     * @param initialValue
     */
    public static void initializeVariables(int initialValue) {
        // Initialize the values
        adjacencyList = new ArrayList<ArrayList<Integer>>(nodeCount);
        pageRank = new double[nodeCount];
        contribution = new double[nodeCount];
        old_pageRank= new double[nodeCount];
        offset = (1 - dampingFactor)/nodeCount;

        double init = 0;
        switch (initialValue) {
            case 0:
            case 1:
                init = initialValue;
                break;

            case -1:
                init = 1 / (double)nodeCount;
                break;

            case -2:
                init = 1 / Math.sqrt(nodeCount);
                break;
            default:
                init = initialValue;
                break;
        }

        for (int i = 0; i < nodeCount; i++) {
            adjacencyList.add(new ArrayList<>());
            pageRank[i] = init;
            old_pageRank[i] = init;
        }
    }

    /**
     * Creates an adjacency list from the input.
     * @param rows
     * @throws Exception
     */
    public static void createAdjacencyList(String[] rows) throws Exception {
        //Create an adjacency list of the edges
        int[] row;
        int node1, node2;
        for (int index = 1; index <= edgeCount; index++) {

            row = extractNodeValues(rows[index]);
            node1 = row[0];
            node2 = row[1];

            if (node1 >= nodeCount || node2 >= nodeCount) {
                System.out.println("Invalid input in rows 2: "+node1+" "+node2+ " "+ adjacencyList.size());
                return;
            }
            adjacencyList.get(node1).add(node2);
        }
    }

    /**
     * Prints the adjacency list.
     */
    public static void printAdjacencyList() {
        for (List<Integer> list : adjacencyList) {
            for (Integer integer : list) {
                System.out.print(integer + " ");
            }
            System.out.println();
        }
    }

    /**
     * Extracts the Node Values
     * @param line
     * @return An array of integers with 2 values.
     * @throws Exception
     */
    public static int[] extractNodeValues(String line) throws Exception {
        String []row = line.split(" ");

        if (row.length != 2) {
            throw new Exception("Invalid Input found in row:"+row);
        }

        int[] result = new int[2];
        result[0] = Integer.parseInt(row[0]);
        result[1] = Integer.parseInt(row[1]);

        return result;
    }


    /**
     * Calculate the Contribution or the out degree
     */
    public static void calculateContribution() {
        for (int i = 0; i < contribution.length; i++) {
            contribution[i] = adjacencyList.get(i).size();
        }
    }

    /**
     * Calculate the page rank
     */
    public static void calculatePageRank() {

        double[] newPageRankArray = new double[nodeCount];
        double intermediateCalculation;
        for (int i = 0; i < pageRank.length; i++) {
            intermediateCalculation = 0;
            for (int j = 0; j < adjacencyList.size(); j++) {
                if (adjacencyList.get(j).contains(i)) {
                    intermediateCalculation += pageRank[j] / contribution[j];
                }
            }
            newPageRankArray[i] = offset + dampingFactor * intermediateCalculation;
        }

        old_pageRank = pageRank;
        pageRank = newPageRankArray;
    }

    /**
     * Check if the values of page rank converged
     * @return True if the converge is successful
     */
    public static boolean didConverge(int current_iteration) {
        double multiplicationFactor = 0;
        if (iterations > 0) {
            return current_iteration == iterations;
        }
        else {
            if (iterations == 0) {
                multiplicationFactor = 100000;
            }
            else  {
                multiplicationFactor = Math.pow(10, (iterations * -1));
            }

            for (int i = 0; i < pageRank.length; i++) {
                if ((int)Math.floor(pageRank[i]*multiplicationFactor) != (int)Math.floor(old_pageRank[i]*multiplicationFactor)) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * Print the Page Rank Values
     */
    public static void printPageRank(int iteration) {

        if (iteration == 0) {
            System.out.print("Base : "+ iteration + " : ");
        }
        else {
            System.out.print("Iterat : "+ iteration + " : ");
        }

        DecimalFormat numberFormat = new DecimalFormat("0.0000000");

        for (int i = 0; i < pageRank.length; i++) {
            System.out.print("PR["+ i + "] = "+numberFormat.format(pageRank[i]) + " ");
        }
        System.out.println();
    }

    /**
     * Prints the Pagerank if higher than 10 nodes.
     * @param iteration
     */
    public static void printPageRankForHigherNodes(int iteration) {

//        System.out.println("Iterat : "+ iteration + " : ");
        DecimalFormat numberFormat = new DecimalFormat("0.0000000");

        for (int i = 0; i < pageRank.length; i++) {
//            System.out.println("PR["+ i + "] = "+numberFormat.format(pageRank[i]) + " ");
        }
//        System.out.println();
    }
}
