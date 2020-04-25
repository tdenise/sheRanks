import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

//import Jsoup library
// import Selenium for web driver capabilities

/**
 * Crawler object class.
 **/
public class Crawler {
    private String seed;
    private Set<String> recipeLinks; // map of recipes to their inlinks
    private Set<String> crawled; // set to keep track of the recipe pages
    private SetMultimap<String, String> inlinkMap; // multi map to keep track of recipe inlinks

    private WebDriver driver; // selenium web driver

    private File dir; // root directory for output
    private File inlinksFile; // csv file with crawl data for inlinks
    private File outlinksFile; // csv file with crawl data for outlinks
    private File detailsFile; // csv file with details about each site

    public Crawler(String url) {
        this.seed = url;
        this.recipeLinks = new HashSet<>();
        this.crawled = new HashSet<>();
        this.inlinkMap = HashMultimap.create();

        // chrome web driver settings (uncomment appropriate webdriver settings)
        System.setProperty("webdriver.chrome.driver", "src/resources/chromedriver"); // mac
//        System.setProperty("webdriver.chrome.driver", "src/resources/chromedriver.exe"); // windows

        // options for web driver
        ChromeOptions options = new ChromeOptions().addArguments("--headless"); // run a chrome browser in the background
        this.driver = new ChromeDriver(options);

        // create the output file
        this.dir = new File(System.getProperty("user.dir"));
        this.dir.mkdir();
        createOutFiles();

        // add seed URL to links arraylist
        this.recipeLinks.add(this.seed);
    }

    public void crawl() {
        // crawl recipes starting with the seed
        crawl(this.seed);

        // crawl the remaining links
        Set<String> remaining = Sets.difference(recipeLinks, crawled);
        remaining.forEach(this::crawl);

        writeInlinks();

        if (inlinksFile.length() > 21 && outlinksFile.length() > 23) {
            System.out.println("Done! Check out the " + inlinksFile.getPath() + " file for inlinks and " + outlinksFile.getPath() + " for outlinks.");
        } else {
            System.out.println("Oops! Looks like the " + inlinksFile.getPath() + " file is empty. Something went wrong :/");
        }

        // close the chrome window
        driver.quit();
    }

    private void crawl(String url) {
        try {
            crawled.add(url);

            // use automated chrome to open up the url and then grab the document
            driver.get(url);
            Document d = url.contains("/recipe") ? loadRecipe(url) : Jsoup.parse(driver.getPageSource());

            // stop crawling outlinks once we've collected at most 200 recipes
            if (inlinkMap.keySet().size() >= 200) {
                return;
            }

            // collect outlinks
            Set<String> outlinks = processLinks(d, url);
            writeOutlinks(url, outlinks);

            // crawl the outlinks
            for (String link : outlinks) {
                if (link.contains("/recipe") && !crawled.contains(link)) {
                    crawl(link);
                }
            }
        } catch (TimeoutException | NoSuchElementException e) {
            System.out.println("Couldn't load dynamic content for " + url);
        } catch (IndexOutOfBoundsException e) {
            System.out.println("Couldn't grab percentage for " + url);
        }
    }

    private void createOutFiles() {
        this.detailsFile = new File(dir.getPath() + "/details.csv");
        this.inlinksFile = new File(dir.getPath() + "/inlinks.csv");
        this.outlinksFile = new File(dir.getPath() + "/outlinks.csv");
        try (FileWriter dWriter = new FileWriter(this.detailsFile);
             FileWriter iWriter = new FileWriter(this.inlinksFile);
             FileWriter oWriter = new FileWriter(this.outlinksFile)
        ) {
            dWriter.write("Recipe Title,Recipe URL,\"Would Make Again\" %,Tips Count\n");
            iWriter.write("Recipe URL,Inlink URL\n");
            oWriter.write("Recipe URL,Outlink URL\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Document loadRecipe(String url) {
        // generate dynamic content by scrolling to bottom
        WebElement element = driver.findElement(By.className("recipe-submit-cta"));
        ((JavascriptExecutor) driver)
                .executeScript("arguments[0].scrollIntoView();", element);
        WebDriverWait wait = new WebDriverWait(driver, 5);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("related-recipes")));

        // parse the html after the related recipes have loaded
        Document d = Jsoup.parse(driver.getPageSource());

        // extract name of recipe
        String recipeName = d.title().replace(",", "-");

        // extract "would make again" percentage
        String percentage = grabPercentage(d);

        // extract number of tips
        String tipsCt = grabTipsCt(d);

        // add recipe, url, percentage, and inlink to csv
        addEntry(recipeName, url, percentage, tipsCt);

        return d;
    }

    private String grabPercentage(Document d) {
        Elements targets = d.getElementsByClass("tips-score-heading");
        String percentageStr = targets.get(0).text();
        return percentageStr.split("%")[0];
    }

    private String grabTipsCt(Document d) {
        Elements targets = d.getElementsByClass("tips-count-heading");
        String tipsCtStr = targets.get(0).text();
        return tipsCtStr.split(" ")[0];
    }

    private void addEntry(String... inputs) {
        try (FileWriter writer = new FileWriter(this.detailsFile, true)) {
            StringBuilder entry = new StringBuilder();
            for (int i = 0; i < inputs.length; i++) {
                String input = inputs[i];
                entry.append(input);
                if (i < inputs.length - 1) {
                    entry.append(",");
                } else {
                    entry.append("\n");
                }
            }
            writer.write(entry.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeOutlinks(String url, Set<String> outlinks) {
        try (FileWriter writer = new FileWriter(this.outlinksFile, true)) {
            StringBuilder entry = new StringBuilder();
            for (String outlink : outlinks) {
                if (outlink.contains("/recipe")) {
                    entry.append(url).append(",").append(outlink).append("\n");
                }
            }
            writer.write(entry.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeInlinks() {
        try (FileWriter writer = new FileWriter(this.inlinksFile, true)) {
            StringBuilder entry = new StringBuilder();
            this.inlinkMap.forEach((url, inlink) -> {
                if (url.contains("/recipe")) {
                    entry.append(url).append(",").append(inlink).append("\n");
                }
            });
            writer.write(entry.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Set<String> processLinks(Document d, String inlink) {
        Set<String> outlinks = new HashSet<>();
        try {
            Elements urls = d.select("a[href]");

            for (Element link : urls) {
                String href = link.attr("href").startsWith("/") ? "https://tasty.co" + link.attr("href") : link.attr("href");
                // check to see if link is a tasty.co link
                if (isLink(href)) {
                    recipeLinks.add(href);
                    outlinks.add(href);
                    inlinkMap.put(href, inlink);
                }
            }
        } catch (Exception e) {
            System.out.print("For '" + d.location() + "': " + e.getMessage());
        }

        return outlinks;
    }

    private boolean isLink(String href) {
        return href.contains("tasty.co") && (href.contains("/recipe") || href.contains("/topic"));
    }
} //end Crawler
