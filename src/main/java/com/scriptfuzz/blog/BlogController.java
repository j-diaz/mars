package com.scriptfuzz.blog;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import spark.ModelAndView;
import spark.template.freemarker.FreeMarkerEngine;
import spark.utils.IOUtils;


import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static spark.Spark.*;
/**
 * Created by J. Diaz on 08-04-15.
 */
public class BlogController {
    private static final int CONNECTION_POOLS = 100;
    public static final Logger log = Logger.getLogger(BlogController.class.getName());
    private final FreemarkerConfiguration cfg;
    private final ArticleDAO articleDAO;

    //Todo: Use actual username password combo
    // Right now is being ignored
   public BlogController(String username, String password, String host, int port){
       log.info("Server running on port: "+port);
       MongoClientOptions options = MongoClientOptions.builder().connectionsPerHost(CONNECTION_POOLS).build();

       final MongoClient mongoClient = new MongoClient(new ServerAddress(host, port), options);
       //final MongoDatabase blogDatabase = mongoClient.getDatabase("blog");
       log.info("Connecting to mongo: "+mongoClient);
       final MongoDatabase blogDatabase = mongoClient.getDatabase("test");
       log.info("Got database: "+blogDatabase);
       articleDAO = new ArticleDAO(blogDatabase);

       cfg = new FreemarkerConfiguration();
       // Serve the static files
       externalStaticFileLocation("src/main/resources/");

       // Enable CORS
       enableCORS();

       // Initialize roots
       bootstrapRoutes();

       // Load memory cache
       loadCache();
   }

    //Todo: Actually use credentials
    public static void main(String[] args){

        String mode = System.getProperty("mode");
        log.info("Running on mode: "+mode);
        if("production".equalsIgnoreCase(mode)) {
            String username = "Thatguy";
            String password = "thisguy";
            new BlogController(username, password, "localhost", 3000);
        }else{
            String username = "Thatguy";
            String password = "thisguy";
            new BlogController(username, password, "localhost", 27017);
        }
    }

    /**
     * Loads recent articles from DB to memory
     * Todo Fix the hardcoded date
     * Todo Preferably provide a range
     */
    private void loadCache(){
        log.info("Loading article memory cache");
        Map<String,String> params = new HashMap<>();
        //Todo The year should not be hardcoded
        //Fix this crap
        params.put("date", "2015-08-15");
        List<Document> recentArticles = articleDAO.findArticlesByFilter(params);
        recentArticles.stream().forEach(a -> ArticleCache.addToCache(a));
    }

    private void bootstrapRoutes() {
        //--------------------------------------------------------------//
        //                          API ROUTES                          //
        //--------------------------------------------------------------//
        before("/", (req, res) -> {
           try( InputStream stream = getClass().getResourceAsStream("/index.html")){
               halt(200, IOUtils.toString(stream));
           }catch (IOException e){
               log.severe("Error serving index: "+e);
           }
        });


        /**
         * Displays the api
         */
        get("/api", (req, res) -> cfg.getHtmlApi());

        /**
         * Upon hitting root of the page load articles
         * from memory cache instead of going to DB
         * Get recent articles
         * Todo Decide what this will return
         */
        get("/api/article/load", (req, res) -> {
            List<Document> recent = ArticleCache.getRecentArticles();
            //String json = toJsonStr(recent);
            int count = recent.size();
            log.info("Loaded " + count + " articles from cache");
            return count;
        });

//        get("/", (req, res) -> {
//            return new ModelAndView(new HashMap<>(), "index.html");
//        }, new FreeMarkerEngine(cfg.getConfiguration()) );

        get("/api/articles/recent", (req, res) -> toJsonStr(ArticleCache.getRecentArticles()));

        /**
         * Test out clearing the cache
         * Fix this crap
         */
        get("/article/clear", (req, res) -> {
            ArticleCache.clearCache();
            return res;
        });

        /**
         * Returns all articles in db
         */
        get("/api/articles/all", (req, res) -> {
            String jsonStr = toJsonStr(articleDAO.findAllArticles());
            log.info("Response: " + jsonStr);
            res.status(200);
            res.type("application/json");
            res.body(jsonStr);
            return jsonStr;
        });

        /**
         * Returns all articles by year
         */
        get("/api/articles/year/:year", (req, res) -> {
            String year = req.params("year");
            Map params = new HashMap<String, String>();

            params.put("year", year);
            String jsonStr = toJsonStr(articleDAO.findArticlesByFilter(params));
            log.info("Response: " + jsonStr);
            res.status(200);
            res.type("application/json");
            res.body(jsonStr);
            return jsonStr;
        });

        /**
         * Find article by title
         */
        get("/api/articles/title/:title", (req, res) -> {
            String title = req.params("title");
            Map params = new HashMap<String, String>();
            params.put("title", title);

            String jsonStr = toJsonStr(articleDAO.findArticlesByFilter(params));
            log.info("Response: " + jsonStr);
            res.status(200);
            res.type("application/json");
            res.body(jsonStr);
            return jsonStr;
        });

        /**
         * Find article by author
         */
        get("/api/articles/author/:author", (req, res) -> {
            String author = req.params("author");
            Map params = new HashMap<String, String>();
            params.put("author", author);

            String jsonStr = toJsonStr(articleDAO.findArticlesByFilter(params));
            log.info("Response: " + jsonStr);
            res.status(200);
            res.type("application/json");
            res.body(jsonStr);
            return res;
        });

        get("/api/articles/:year/:title", (req, res) -> {
            String year = req.params(":year");
            String title = req.params(":title");

            Map<String, String> params = new HashMap<>();

            params.put("year", year);
            params.put("title", title);
            String jsonStr = toJsonStr(articleDAO.findArticlesByFilter(params));
            log.info("Response: " + jsonStr);
            res.status(200);
            res.type("application/json");
            res.body(jsonStr);
            return jsonStr;
        });

        /**
         * Add a new article.
         */
        post("/api/articles/add", (req, res) -> {
            log.info("Request: " + req);
            String markdownArticle = req.body();
            log.fine("Markdown received: " + markdownArticle);

            log.fine("Attempting to parse markdown and add to cache");
            try {
                // Add to DB
                Document articleAdded = articleDAO.addNewMarkdownArticle(markdownArticle);
                // Add to
                ArticleCache.addToCache(articleAdded);
                res.status(200);
                res.type("application/json");
                res.body("{\"success\":true}");
            } catch (Exception e) {
                log.severe("Error adding an article: " +e);
                res.status(200);
                res.type("application/json");
                res.body("{\"success\":false}");
            }
            log.info("Response: " + req.body());
            return res.body();
        });

//        get("*", (req, res) -> {
//            try( InputStream stream = getClass().getResourceAsStream("/index.html")){
//                halt(200, IOUtils.toString(stream));
//            }catch (IOException e){
//                log.severe("Error serving index: "+e);
//            }
//            return 0;
//        });
    }

    private void enableCORS(){
        before((req, res) -> {
            log.info(req.host() + " " +req.contentType() + " "+req.ip() + " " +req.headers());
            res.header("Access-Control-Allow-Origin", "*");
            res.header("Access-Control-Allow-Methods", "X-Requested-With, Content-Type, Content-Length, Authorization");
            res.header("Access-Control-Allow-Headers", "GET,PUT,POST,DELETE,OPTIONS");
        });
    }

    private static String toJsonStr(List<Document> in){
       Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
       return gson.toJson(in);
    }
}
