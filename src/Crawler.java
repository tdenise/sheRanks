import java.io.*;
import java.util.*;

//import Jsoup library
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

// import Selenium for web driver capabilities
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Crawler object class.
 **/
public class Crawler {
    private String seed;
    private Map<String, String> recipeLinks; // map of recipes to their inlinks
    private Set<String> pages; // set to keep track of the recipe pages
    private int droppedCt; // counter for recipes that didn't load properly
    private WebDriver driver; // selenium web driver

    private File dir; // root directory for output
    private File recipesFile; // csv file with crawl data

    // TODO: Keep track of all inlinks

    public Crawler(String url) {
        this.seed = url;
        this.recipeLinks = new HashMap<>();
        this.pages = new HashSet<>();
        this.droppedCt = 0;

        // chrome web driver settings
        this.driver = new ChromeDriver();

        // create the output file
        this.dir = new File(System.getProperty("user.dir"));
        this.dir.mkdir();
        createOutFile();

        // add seed URL to links arraylist
        this.recipeLinks.put(this.seed, this.seed);
    }

    public void crawl() {
        // collect recipes starting with the seed
        crawl(this.seed);

        System.out.println("Number of dropped links: " + droppedCt);

        if (recipesFile.length() > 106) {
            System.out.println("Done! Check out the " + recipesFile.getPath() + " file for results.");
        } else {
            System.out.println("Oops! Looks like the " + recipesFile.getPath() + " file is empty. Something went wrong :/");
        }

        // close the chrome window
        driver.quit();
    }

    private void crawl(String url) {
        // crawl until we collect at most 300 recipes
        if (pages.size() >= 300) {
            return;
        }

        try {
            // use automated chrome to open up the url
            driver.get(url);

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

            // get the inlink for the current link
            String inlink = recipeLinks.get(url);

            // add recipe, url, percentage, and inlink to csv
            addCsvEntry(recipeName, url, percentage, tipsCt, inlink);

            // extract page outlinks
            Set<String> outlinks = getPageLinks(d, url);
            pages.add(url);

            // crawl the outlinks of the outlinks
            for (String link : outlinks) {
                crawl(link);
            }
        } catch (TimeoutException | NoSuchElementException e) {
            droppedCt++;
            System.out.println("Couldn't load dynamic content for " + url);
        } catch (IndexOutOfBoundsException e) {
            droppedCt++;
            System.out.println("Couldn't grab percentage for " + url);
        }
    }

    private void createOutFile() {
        this.recipesFile = new File(dir.getPath() + "/recipes.csv");
        try (FileWriter writer = new FileWriter(this.recipesFile)) {
            writer.write("Recipe Title,Recipe URL,\"Would Make Again\" %,Tips Count,Inlink\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    private void addCsvEntry(String... inputs) {
        try (FileWriter writer = new FileWriter(this.recipesFile, true)) {
            StringBuilder entry = new StringBuilder();
            for (int i = 0; i < inputs.length; i++) {
                String input = inputs[i];
                entry.append(input);
                if (i < inputs.length - 1) {
                    entry.append(",");
                }
            }
            entry.append("\n");
            writer.write(entry.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Set<String> getPageLinks(Document d, String inlink) {
        Set<String> outlinks = new HashSet<>();
        try {
            Elements urls = d.select("a[href]");

            for (Element link : urls) {
                String href = link.attr("href").startsWith("/") ? "https://tasty.co" + link.attr("href") : link.attr("href");
                // check to see if link is a tasty.co recipe link
                if (isLink(href) && !recipeLinks.containsKey(href)) {
                    recipeLinks.put(href, inlink);
                    outlinks.add(href);
                }
            }
        } catch (Exception e) {
            System.out.print("For '" + d.location() + "': " + e.getMessage());
        }

        return outlinks;
    }

    private boolean isLink(String href) {
        return href.contains("tasty.co") && href.contains("/recipe");
    }
} //end Crawler
