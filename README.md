# sheRanks
A PageRank analysis of Buzzfeed Tasty (tasty.co) - Popular Recipes	

## Table of Contents
- [Running the Crawler](#running-the-crawler-)
  * [Step One - Get the Source Code](#step-one---get-the-source-code)
  * [Step Two - Install Dependencies](#step-two---install-dependencies)
  * [Step Three - Run The Crawler Locally](#step-three---run-the-crawler-locally)
- [Crawler Results](#crawler-results-)
  * [Recipe Name](#recipe-name)
  * [Recipe URL](#recipe-url)
  * ["Would Make Again" % and Number of Tips](#would-make-again--and-number-of-tips)
  * [Inlink](#inlink)
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
The sheRanks crawler produces a nice CSV file with the following format:

Recipe Name | Recipe URL | "Would Make Again" % | Number of Tips | Inlink
------ | ------------ | ---------------------- | ------ | ------
Southwestern Sweet Potato Toast Recipe by Tasty | https://tasty.co/recipe/southwestern-sweet-potato-toast | 80 | 6 | https://tasty.co/topic/game-day

#### Recipe Name
The title of the recipe

#### Recipe URL
The link to the recipe

#### "Would Make Again" % and Number of Tips


### Inlink
The link that led the crawler to this recipe

## Built With 🛠
[JSoup](https://jsoup.org/)

[Selenium](https://www.selenium.dev/)


## Special Thanks To 💖
Dr. Ben Steichen

The country of Luxembourg
