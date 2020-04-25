# sheRanks
A PageRank analysis of Buzzfeed Tasty (tasty.co) - Popular Recipes	

## Table of Contents
- [Running the Crawler](#running-the-crawler-)
  * [Step One - Get the Source Code](#step-one---get-the-source-code)
  * [Step Two - Install Dependencies](#step-two---install-dependencies)
  * [Step Three - Run The Crawler Locally](#step-three---run-the-crawler-locally)
- [Crawler Results](#crawler-results-)
  * [Details](#detailscsv)
  * [Inlinks](#inlinkscsv)
  * [Outlinks](#outlinkscsv)
- [Built With](#built-with-)
- [Special Thanks To](#special-thanks-to-)

## Running the Crawler 💻
### Step One - Get the Source Code
Clone this repository by opening up the command line and running the following:
```shell script
mkdir sheRanks
git clone https://github.com/tdenise/sheRanks.git
cd sheRanks
```

### Step Two - Install Dependencies
Make sure you have the following libraries installed for this project
* [JSoup v1.13.1](https://jsoup.org/packages/jsoup-1.13.1.jar)
* [Selenium WebDriver Bindings](https://www.selenium.dev/downloads/) - Check under the _Selenium Client & WebDriver Language Bindings_ section

### Step Three - Run The Crawler Locally
Now that you've installed the dependencies, you can run the sheRanks crawler locally! To do so, go ahead run the `CrawlerExample::main` method.

## Crawler Results 📊
The sheRanks crawler produces three nice CSV files:

#### details.csv
This file contains details about each recipe that has been crawled. The number of lines in this file is the same as the number of unique recipes that have been crawled.

Recipe Name | Recipe URL | "Would Make Again" % / Number of Tips | Inlink
------ | ------------ | ---------------------------- | ------
The title of the recipe | The link to the recipe | !["Would Make Again" % and Number of Tips](https://user-images.githubusercontent.com/5050718/79623571-e6632980-80d1-11ea-9d6e-844a63c43d45.png)
 | The link that led the crawler to this recipe

#### inlinks.csv
This file contains recipes and their inlinks. This can be used to build a graph or adjacency list for page rank calculation.

#### outlinks.csv
This file contains recipes and their outlinks. This can be used to build a graph or adjacency list for page rank calculation.

## Built With 🛠
[JSoup](https://jsoup.org/)

[Selenium](https://www.selenium.dev/)


## Special Thanks To 💖
Dr. Ben Steichen

The country of Luxembourg
