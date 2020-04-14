/*
  Crawler object class.
 */
import java.io.*;
import java.util.*;

//import Jsoup library
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class Crawler {
    private String seed;
    private Set<String> links; //extracted links
    private int exportedCt; //number of URLs exported
    private File dir; // root directory for output
    private File recipesFile; // csv file with crawl data

    private static int crawlCt; // number of completed crawls

    // constructor takes in seed URL
    public Crawler(String url) {
        this.seed = url;
        this.links = new HashSet<>();
        this.exportedCt = 0;

        // create the output file
        this.dir = new File(System.getProperty("user.dir"));
        this.dir.mkdir();
        createOutFile();

        // add seed URL to links arraylist
        this.links.add(this.seed);

        crawlCt = 0;
    }

    public void crawl() {
        crawl(this.seed);
    }

    private void crawl(String url) {
        try {
            // stop crawling after 100 links have been crawled
            if(crawlCt > 100) {
                return;
            }

            Document d = Jsoup.connect(url).get();
            String entry = "";

            String recipeName = grabRecipeName(d);
            // extract "would make again" percentage (tips-score-heading)
            int percentage = grabPercentage(d);
            // extract tip count (tips-count-heading)
            int tipsCt = grabTipsCt(d);

            // add recipe, percentage, tips ct to csv
            entry += recipeName + "," + percentage + "," + tipsCt;
            addCsvEntry(entry);

            // extract page outlinks
            Set<String> outlinks = getPageLinks(d);

            // crawl each outlink
            outlinks.forEach(link -> {
                crawlCt++;
                crawl(link);
            });

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void createOutFile() {
        try {
            this.recipesFile = new File(dir.getPath() + "recipes.csv");
            this.recipesFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Set<String> getPageLinks(Document d) {
        Set<String> outlinks = new HashSet<>();
        try {
            Elements urls = d.select("a[href]");

            for (Element url : urls) {
                String href = url.attr("href");
                // is actually a link
                if (isLink(href)) {
                    links.add(href);
                    outlinks.add(href);
                }

            }
        } catch (Exception e) {
            System.out.print("For '" + d.location() + "': " + e.getMessage());
        }

        return outlinks;
    }

    private boolean isLink(String href) {
        return href.startsWith("http");
    }
} //end Crawler