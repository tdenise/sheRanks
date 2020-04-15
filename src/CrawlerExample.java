import java.io.*;

/**
 Main driver for the crawler.
 **/
public class CrawlerExample {

    public static void main(String[] args) throws IOException {
        String URL = "https://tasty.co/recipe/easter-savory-pie-pizza-rustica";
        Crawler crawler = new Crawler (URL);
        crawler.crawl();
    }
}