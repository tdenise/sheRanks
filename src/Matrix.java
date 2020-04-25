import java.io.*;
import java.util.*;

/*
* This Matrix object class takes in a file and generates an adjacency list
* representing crawled webpages of a particular domain and their inlinks
* to ultimately produce a pagerank.
* */
public class Matrix {
    private ArrayList<ArrayList<Integer>> adjList = new ArrayList<>(); //adjacency list
    private File inFile;
    private ArrayList<String> recipeList = new ArrayList<>(); //list of unique recipes
    private int recipeCount, edgeCount; //number of recipes and node indexes

    //test Matrix
    public static void main(String[] args) {
        Matrix m = new Matrix("inlinks.csv");
        System.out.print("\nOur crawl found " + m.getRecipeCount() + " unique recipes\n");
        //m.showAdjList(m);
        m.calcPageRank(0.15);
    }

    //pagerank obj that can return the list of the top recipes
    public Matrix(String fileName){
        File f = new File(fileName);
        this.inFile = f;
        getRecipes(); //create arraylist for recipes (node # = recipeList index)
        generateList(); //create adjacency list to store edges
    }

    public int getRecipeCount(){
        return recipeCount;
    }

    //create adj matrix as an arraylist of lists that list the outlinks for
    private void generateList(){
        int ct = 0;
        try{
            Scanner sc = new Scanner(inFile);
            String line;
            String [] splitLine; // splitLine[0]: URL, splitLine[1]: outlink

            sc.nextLine(); //skip header
            while(sc.hasNextLine()){
                ct++;
                line = sc.nextLine();
                splitLine = line.split(",");

                //find the vertex number for the given recipe
                int aIndex = recipeList.indexOf(splitLine[0]);
                int bIndex = recipeList.indexOf(splitLine[1]);

                //add to that arraylist in the list of lists
                if(!adjList.get(aIndex).contains(bIndex)){
                    adjList.get(aIndex).add(bIndex); //add edge between recipe1 (aIndex) and recipe2 (bIndex)
                }
            }
            this.edgeCount=ct; //set number of edges
        }
        catch(FileNotFoundException f){
            System.out.println("Couldn't find file!");
        }
    }

    //add unique recipes to an arraylist -- index in recipeList = node in the list
    private void getRecipes(){
        String str;
        String[] sp;
        int count=-1;

        try {
            Scanner sc = new Scanner(inFile);
            sc.nextLine(); //skip header line
            while (sc.hasNextLine()) {
                str = sc.nextLine();
                sp = str.split(",");
                if (!(recipeList.contains(sp[0]))) {
                    recipeList.add(sp[0]); //add r to the recipe list
                    ArrayList<Integer> vertexList1 = new ArrayList<>();
                    adjList.add(vertexList1); //add new list according to recipe index
                    count++;
                }
                if (!(recipeList.contains(sp[1]))) {
                    recipeList.add(sp[1]); //add r to the recipe list
                    ArrayList<Integer> vertexList2 = new ArrayList<>();
                    adjList.add(vertexList2); //add new list according to recipe index
                    count++;
                }//endif
            } //end while
            this.recipeCount = count;
        }//end try
        catch(FileNotFoundException f){
            System.out.println("Couldn't find file!");
        }
    }

    //to display the adjacency matrix
    private void showAdjList(Matrix pr){
        System.out.println("Generated Adjacency List");
        System.out.print("========================");
        for(int n=0; n<recipeList.size(); n++){ //iterate through recipe list
            System.out.print("\n" + n + ": ");
            for(int d=0; d<adjList.get(n).size(); d++){ //match recipe index in list to adjList
                System.out.print(adjList.get(n).get(d) + " " );
            }
        }
    }

    /*
    * Goal: implement iterative version that includes surfer model
    * Stores PageRank in final array.
    * To do:
    * 1. define function to figure out convergence to 1 (the sum of all PRs)
    * 2. sort the page ranks to get the top 100
    * */
    public void calcPageRank(double l) {

        //int probabilitySum=0, iteration=0;
        final double lambda = l;
        final double initial = 1 / (double) recipeCount;
        System.out.println(initial);
        double[] ranks = new double[recipeCount];

        //initialize ranks array
        for (int i = 0; i < recipeCount; i++) {
            ranks[i] = initial; //initialize at first iteration (init to 0)
            // System.out.println(i + " :" + ranks[i]);
        }

        int inlinks;
        //num outlinks is the length of the list in the recipe list
              for (int j = 0; j < ranks.length; j++) {
                  inlinks = adjList.get(j).size(); //size of the array = num inlinks
                  if (inlinks > 0) {
                      for (int k = 0; k < inlinks; k++) {
                          ranks[j] += (lambda / (double) recipeCount) + (1 - lambda) * (ranks[k] / inlinks);
                      }
                  }
                  System.out.println((ranks[j]));
              }
          }//end calcPageRank
    }//end Matrix
