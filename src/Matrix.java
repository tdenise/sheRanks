package src;
import java.io.*;
import java.util.*;

/**
 * Matrix obj class that generates an adjacency matrix
 * as a 2d array based on inter-linkage of recipes.
 * The goal is to use the output matrix to calculate PageRank.
 */
public class Matrix {
    private final int webPages=0;
    private int[][] adjMatrix ;
    private File inFile = null;
    private ArrayList<String> recipeList = new ArrayList<>();
    private HashMap<String, Integer> recipeMap = new HashMap<>(); //ie Easter Savory, 1
    private int recipeCount;

    public Matrix(String fileName){
        File f = new File(fileName);
        this.inFile = f;
        recipeCount = setNumRecipes(); //set arraylist for recipes and get unique list of recipes
        initMatrix(); //initialize matrix to 0
    }

    //initialize matrix to 0's
    public void initMatrix(){
        adjMatrix = new int[recipeCount][recipeCount];
        for(int r=0; r<adjMatrix.length; r++){
            for(int c=0; c<adjMatrix.length; c++){
                adjMatrix[r][c]=0;
            }
        }
    }

    //public void displayFull() //display full graph with nodes and values

    //build the matrix
    public void buildMatrix(){
        String a;
        String b;
        try{
            Scanner sc = new Scanner(inFile);
            while(sc.hasNextLine()){
                //parse the line for first value and second value
                //create edge between them
                a = sc.next();
                b = sc.next();
                create_edge(a,b,1); //create edge in the matrix
            }
        }
        catch(FileNotFoundException f){
            System.out.println("Couldn't find file!");
        }
    }

    //get unique list of recipes (number of nodes) from the file
    public int setNumRecipes(){
        String r = null;
        int count =0;
        try{
            Scanner sc = new Scanner(inFile);
            while(sc.hasNext()){
                r = sc.next();
                if(!recipeList.contains(r)) {
                    recipeList.add(r); //add r to the recipe list
                    count++; //incr num recipes
                    //ie node 1 is Easter Savory Pie
                    recipeMap.put(r, count); //enter r (recipe name) and node into hashmap
                }
            }
        }
        catch(FileNotFoundException f){
            System.out.println("Couldn't find file!");
        }
        return count;
    }

    //create edge between two recipes
    public void create_edge(String a, String b, int edge){
        //if recipes (key) exists, then we get their node number (key)
        int aIndex = recipeMap.get(a)-1; //get node number of a
        int bIndex = recipeMap.get(b)-1; //get the node number of b
        adjMatrix[aIndex][bIndex]=edge; //place edge between them
    }
}