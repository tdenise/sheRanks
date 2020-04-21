package src;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

/*
* This Matrix object class takes in a file and generates an adjacency list
* representing crawled webpages of a particular domain and their inlinks
* to ultimately produce a pagerank
* */
public class Matrix {
    private ArrayList<ArrayList<Integer>> adjList = new ArrayList<>(); //adjacency list
    private File inFile;
    private ArrayList<String> recipeList = new ArrayList<>(); //list of unique recipes
   // private HashMap<String, Integer> recipeMap = new HashMap<>(); //to map recipe to
    private int recipeCount, edgeCount; //number of recipes and node indexes

    //simple main to test matrix
    public static void main(String[] args) {
        Matrix m = new Matrix("src/recipes.csv");
        System.out.print("\nOur crawl found " + m.getRecipeCount() + " unique recipes\n");
        m.showAdjList(m);
    }

    //pagerank obj that can return the list of the top recipes
    public Matrix(String fileName){
        File f = new File(fileName);
        this.inFile = f;
        getRecipes(); //set arraylist for recipes and get unique list of recipes
        generateList();
        System.out.println("Edge count: " + edgeCount);
    }

    public int getRecipeCount(){
        return recipeCount;
    }

    public ArrayList<String> getRecipeList(){
        return recipeList;
    }

    //build the matrix, pass in initialized matrix
    //we have: array list of lists for each vertex, recipe list with indices 0...1080 and recipe hashmap that maps it to the node
    private void generateList(){
        int ct = 0;
        try{
            Scanner sc = new Scanner(inFile);
            String line;
            String [] splitLine; // 0: title, [1:URL], 2:would make again 3:tips [4:INLINK]
            //pass in array every time

            sc.nextLine(); //skip header
            while(sc.hasNextLine()){
                ct++;
                line = sc.nextLine(); //line is next line
                //parse the line for first value and second value
                splitLine = line.split(",");

                //find the vertex number for the given recipe
                int aIndex = recipeList.indexOf(splitLine[1]); //get node of recipe -- index of the arraylist
                int bIndex = recipeList.indexOf(splitLine[4]); //get node of inlink -- index of inlink

                //find the vertext number of
                //add to that arraylist in the list of lists
                adjList.get(aIndex).add(bIndex); //add the inlink to the recipe list
                //System.out.print("Printing this list: " + adjList.get(aIndex) + " ");
                System.out.println();
            }
            this.edgeCount=ct;

        }
        catch(FileNotFoundException f){
            System.out.println("Couldn't find file!");
        }
    }

    //add unique recipes to an arraylist, map node number to URL
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
                if (!(recipeList.contains(sp[1]))) {
                    recipeList.add(sp[1]); //add r to the recipe list
                    ArrayList<Integer> vertexList1 = new ArrayList<>();
                    adjList.add(vertexList1); //add new list according to recipe index
                    //System.out.println(sp[0]);
                    count++; //will start with 0
                   // recipeMap.put(sp[1], count); //enter r (recipe name) and node into hashmap
                }
                if (!(recipeList.contains(sp[4]))) {
                    recipeList.add(sp[4]); //add r to the recipe list
                    ArrayList<Integer> vertexList2 = new ArrayList<>();
                    adjList.add(vertexList2); //add new list according to recipe index
                    count++;
                }//endif
            } //end while
            this.recipeCount = count;
            System.out.println("adj list size:" + adjList.size());
        }//end try
        catch(FileNotFoundException f){
            System.out.println("Couldn't find file!");
        }
    }

    private void showAdjList(Matrix pr){
        System.out.println("Generated Adjacency List:");
        for(int n=0; n<recipeList.size(); n++){ //iterate through recipe list
            for(int d=0; d<adjList.get(n).size(); d++){ //match recipe index in list to adjList
                System.out.print("List for node " + n + ": "+adjList.get(n).get(d) + " ");
                System.out.println();
            }
        }
        System.out.println("Expected num edges: " + edgeCount + "\nEdges found in matrix: " + adjList.size());
    }
}
