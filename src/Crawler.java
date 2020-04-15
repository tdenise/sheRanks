import java.io.*;
import java.util.*;

//import Jsoup library
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Crawler object class.
 **/
public class Crawler {
    private String seed;
    private Set<String> links; //extracted links
    private File dir; // root directory for output
    private File recipesFile; // csv file with crawl data

    private static int crawlCt; // number of completed crawls

    // TODO: Keep track of all inlinks
    // TODO: Crawl more links

    public Crawler(String url) {
        this.seed = url;
        this.links = new HashSet<>();

        // create the output file
        this.dir = new File(System.getProperty("user.dir"));
        this.dir.mkdir();
        createOutFile();

        // add seed URL to links arraylist
        this.links.add(this.seed);

        // initialize counter for universal number of crawls
        crawlCt = 0;
    }

    public void crawl() {
        crawl(this.seed, "");
        System.out.println("Done! Check out the " + recipesFile.getPath() + " file for results.");
    }

    private void crawl(String url, String inlink) {
        try {
            Document d = Jsoup.connect(url).maxBodySize(0).get();

            // extract page outlinks
            Set<String> outlinks = getPageLinks(d);

            if (url.contains("/recipe")) {
                // stop crawling after 100 recipes have been crawled
                if (crawlCt > 100) {
                    return;
                }

                // extract name of recipe
                String recipeName = d.title();

                // extract "would make again" percentage
                int percentage = grabPercentage(d);

                // add recipe, url, and percentage to csv
                addCsvEntry(recipeName, url, percentage, inlink);
            }

            // crawl each outlink
            for (String link : outlinks) {
                    crawl(link, url);
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void createOutFile() {
        try {
            this.recipesFile = new File(dir.getPath() + "/recipes.csv");
            this.recipesFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int grabPercentage(Document d) {
        Elements targets = d.getElementsByClass("tips-score-heading");
        String percentageStr = targets.get(0).text();
        percentageStr = percentageStr.split("%")[0];
        return Integer.parseInt(percentageStr);
    }

    private void addCsvEntry(String recipeName, String url, int percentage, String inlink) {
        try (FileWriter writer = new FileWriter(this.recipesFile, true)) {
            String entry = recipeName + "," + url + "," + percentage + "," + inlink + "\n";
            writer.write(entry);
            crawlCt++;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Set<String> getPageLinks(Document d) {
        Set<String> outlinks = new HashSet<>();
        try {
            Elements urls = d.select("a[href]");

            for (Element link : urls) {
                String href = link.attr("href").startsWith("/") ? "https://tasty.co" + link.attr("href") : link.attr("href");
                // check to see if link is a tasty.co recipe link
                if (isLink(href) && !links.contains(href)) {
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
        return href.contains("tasty.co/recipe") || href.contains("/topic");
    }
} //end Crawler
