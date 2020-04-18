import java.io.*;
import java.util.*;

import com.google.common.collect.Sets;

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
    private Set<String> crawled; // set to keep track of the recipe pages

    private WebDriver driver; // selenium web driver

    private File dir; // root directory for output
    private File recipesFile; // csv file with crawl data

    public Crawler(String url) {
        this.seed = url;
        this.recipeLinks = new HashMap<>();
        this.crawled = new HashSet<>();

        // chrome web driver settings (uncomment appropriate webdriver settings)
        System.setProperty("webdriver.chrome.driver", "src/resources/chromedriver"); // mac
//        System.setProperty("webdriver.chrome.driver", "src/resources/chromedriver.exe"); // windows

        // options for web driver
        ChromeOptions options = new ChromeOptions().addArguments("--headless"); // run a chrome browser in the background
        this.driver = new ChromeDriver(options);

        // create the output file
        this.dir = new File(System.getProperty("user.dir"));
        this.dir.mkdir();
        createOutFile();

        // add seed URL to links arraylist
        this.recipeLinks.put(this.seed, this.seed);
    }

    public void crawl() {
        // crawl recipes starting with the seed
        crawl(this.seed);

        // crawl the remaining links
        Sets.difference(recipeLinks.keySet(), crawled).forEach(link -> {
            if (!crawled.contains(link)) {
                crawl(link);
            }
        });

        if (recipesFile.length() > 110) {
            System.out.println("Done! Check out the " + recipesFile.getPath() + " file for results.");
        } else {
            System.out.println("Oops! Looks like the " + recipesFile.getPath() + " file is empty. Something went wrong :/");
        }

        // close the chrome window
        driver.quit();
    }

    private void crawl(String url) {
        try {
            // use automated chrome to open up the url and then grab the document
            driver.get(url);
            Document d = url.contains("/recipe") ? crawlRecipe(url) : Jsoup.parse(driver.getPageSource());

            // stop crawling outlinks once we've collected at most 200 recipes
            if (recipeLinks.size() >= 200) {
                return;
            }

            // collect outlinks
            Set<String> outlinks = getOutlinks(d, url);

            // crawl the outlinks
            for (String link : outlinks) {
                crawl(link);
            }
        } catch (TimeoutException | NoSuchElementException e) {
            System.out.println("Couldn't load dynamic content for " + url);
        } catch (IndexOutOfBoundsException e) {
            System.out.println("Couldn't grab percentage for " + url);
        }
    }

    private void createOutFile() {
        this.recipesFile = new File(dir.getPath() + "/recipes.csv");
        try (FileWriter writer = new FileWriter(this.recipesFile)) {
            writer.write("Recipe Title,Recipe URL,\"Would Make Again\" %,Tips Count,Inlink");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Document crawlRecipe(String url) {
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
        crawled.add(url);

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

    private void addCsvEntry(String... inputs) {
        try (FileWriter writer = new FileWriter(this.recipesFile, true)) {
            StringBuilder entry = new StringBuilder().append("\n");
            for (int i = 0; i < inputs.length; i++) {
                String input = inputs[i];
                entry.append(input);
                if (i < inputs.length - 1) {
                    entry.append(",");
                }
            }
            writer.write(entry.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Set<String> getOutlinks(Document d, String inlink) {
        Set<String> outlinks = new HashSet<>();
        try {
            Elements urls = d.select("a[href]");

            for (Element link : urls) {
                String href = link.attr("href").startsWith("/") ? "https://tasty.co" + link.attr("href") : link.attr("href");
                // check to see if link is a tasty.co link
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
        return href.contains("tasty.co") && (href.contains("/recipe") || href.contains("/topic"));
    }
} //end Crawler
