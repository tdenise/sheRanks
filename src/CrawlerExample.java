import java.io.*;

/**
 Main driver for the crawler.
 **/
public class CrawlerExample {

    public static void main(String[] args) {
//        String URL = "https://tasty.co/recipe/easter-savory-pie-pizza-rustica";
        String URL = "https://tasty.co";
        Crawler crawler = new Crawler (URL);
        crawler.crawl();
    }
}
