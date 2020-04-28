import java.io.*;
import java.util.*;
/*
* This Matrix object class takes 2 files, inlinks.csv and outlinks.csv,
* and generates two adjacency matrices. This class is based on our web crawler for
* Tasty.co. The list of unique recipes is created from the inlinks.csv file.
* The final PageRank is calculated using the Random Surfer Model and displays the top 100 recipes
* based on PageRank.
* */
public class Matrix {
    private ArrayList<ArrayList<Integer>> adjInList = new ArrayList<>(); //adjacency list for inlinks
    private ArrayList<ArrayList<Integer>> adjOutList = new ArrayList<>(); //adjacency list for outlinks
    private File inlinkFile, outlinkFile;
    private ArrayList<String> recipeList = new ArrayList<>(); //list of unique recipes
    private int recipeCount; //number of recipes and node indexes

    //test Matrix
    public static void main(String[] args) {
        Matrix m = new Matrix("inlinks.csv", "outlinks.csv");
        System.out.print("\nOur crawl found " + m.getRecipeCount() + " unique recipes\n");
        //m.showInAdjList();
        //m.showOutAdjList();
        m.PageRank();
    }

    //pagerank obj that can return the list of the top recipes
    public Matrix(String inlinkFileName, String outlinkFileName){
        File f = new File(inlinkFileName);
        File f2 = new File(outlinkFileName);
        this.inlinkFile = f;
        this.outlinkFile=f2;
        getRecipes(); //create arraylist for recipes (node # = recipeList index)
        generateInLinkList(); //create adjacency list to store edges
        generateOutLinkList();
    }

    public int getRecipeCount(){
        return recipeCount;
    }

    //create adj matrix as an arraylist of lists based on inlinks
    private void generateInLinkList(){
        try{
            Scanner sc = new Scanner(inlinkFile);
            String line;
            String [] splitLine; // splitLine[0]: URL, splitLine[1]: outlink
            sc.nextLine(); //skip header
            while(sc.hasNextLine()){
                line = sc.nextLine();
                splitLine = line.split(",");
                int aIndex = recipeList.indexOf(splitLine[0]);
                int bIndex = recipeList.indexOf(splitLine[1]);

                //add "edge" between recipes based on inklinks
                if(!adjInList.get(aIndex).contains(bIndex)){
                    adjInList.get(aIndex).add(bIndex);
                }
            }
        }
        catch(FileNotFoundException f){
            System.out.println("Couldn't find file!");
        }
    }

    //create adj matrix as an arraylist of lists based on outlinks
    private void generateOutLinkList(){
        try{
            Scanner sc = new Scanner(outlinkFile);
            String line;
            String [] splitLine; // splitLine[0]: URL, splitLine[1]: outlink
            sc.nextLine(); //skip header
            while(sc.hasNextLine()){
                line = sc.nextLine();
                splitLine = line.split(",");

                //check that outlink recipes are in our unique list of recipes
                if(recipeList.contains(splitLine[0]) && recipeList.contains(splitLine[1])){
                    int aIndex = recipeList.indexOf(splitLine[0]);
                    int bIndex = recipeList.indexOf(splitLine[1]);

                    //add to that arraylist in the list of lists
                    if(!adjOutList.get(aIndex).contains(bIndex)){
                        adjOutList.get(aIndex).add(bIndex);
                    }
                }
            }
        }
        catch(FileNotFoundException f){
            System.out.println("Couldn't find file!");
        }
    }

    //add unique recipes to an arraylist --> index in recipeList = node in the list
    private void getRecipes(){
        String str;
        String[] sp;

        try {
            Scanner sc = new Scanner(inlinkFile);
            sc.nextLine(); //skip header line
            while (sc.hasNextLine()) {
                str = sc.nextLine();
                sp = str.split(",");
                if (!(recipeList.contains(sp[0]))) {
                    recipeList.add(sp[0]); //add r to the recipe list
                    ArrayList<Integer> vertexList1 = new ArrayList<>();
                    adjInList.add(vertexList1); //add new list according to recipe index
                    adjOutList.add(vertexList1);
                }
                if (!(recipeList.contains(sp[1]))) {
                    recipeList.add(sp[1]); //add r to the recipe list
                    ArrayList<Integer> vertexList2 = new ArrayList<>();
                    adjInList.add(vertexList2); //add new list according to recipe index
                    adjOutList.add(vertexList2);
                }//endif
            } //end while
            this.recipeCount = recipeList.size();
        }//end try
        catch(FileNotFoundException f){
            System.out.println("Couldn't find file!");
        }
    }

    //to display the inlink adjacency list
    public void showInAdjList(){
        System.out.println("Generated Adjacency List for Inlinks");
        System.out.print("========================");
        for(int n=0; n<recipeList.size(); n++){ //iterate through recipe list
            System.out.print("\n" + n + ": ");
            for(int d=0; d<adjInList.get(n).size(); d++){ //match recipe index in list to adjList
                System.out.print(adjInList.get(n).get(d) + " " );
            }
        }
    }

    //to display the outlink adjacency list
    public void showOutAdjList(){
        System.out.println("Generated Adjacency List for Outlinks");
        System.out.print("========================");
        for(int n=0; n<recipeList.size(); n++){ //iterate through recipe list
            System.out.print("\n" + n + ": ");
            for(int d=0; d<adjOutList.get(n).size(); d++){ //match recipe index in list to adjList
                System.out.print(adjOutList.get(n).get(d) + " " );
            }
        }
    }

    //public method retrieves the final array of ordered pagerank
    public void PageRank() {
        final double initial = 1 / (double) recipeCount;
        System.out.println("Iteration 0 --> All PageRanks set to initial value (1/number of unique recipes): "+initial);
        double[] initRanks = new double[recipeCount];
        final int iter= 0;

        //initialize ranks to 1/recipeCount at iteration 0
        for(int i=0; i<initRanks.length; i++){
            initRanks[i]=initial;
        }

        //get final ordered array of PageRanks
        double[] convergedPr = calcPR(0, initRanks, initial, false );

        //call sortPageRank to display top 100 PRs
        sortPageRank(convergedPr);
          }//end PageRank

    /*
    * Method: calcPR() implements the heart of the algorithm for PageRank based on the Random Surfer Model
    *
    * Parameters:
    * -iteration = iteration number for the number of times PageRank has been calculated
    * -lastCalculatedPR = array storing last computed PageRanks
    * -lastCalculatedSum = sum of the last array of PageRanks - to check if 1.0 has been reached
    * -conv = if conv is true, then the values have stopped changing
    *
    * */
    private double[] calcPR(int iteration, double[] lastCalculatedPR, double lastCalculatedSum, boolean conv){

            //base case: iteration passed 1, the sum of the PageRanks is 1, and the PageRank values have converged
            if(iteration>1  && lastCalculatedSum>=1. && lastCalculatedSum<=1.000000000001 && conv==true){
                System.out.println("Converged at Iteration: "+iteration + " with PageRank sum of: " + lastCalculatedSum);
                return lastCalculatedPR;
            }

            else {
                final int iter = ++iteration; //iteration number
                double[] ranks = new double[lastCalculatedPR.length]; //hold calculated ranks
                int numIn; //number of inlinks
                final double lambda = 0.2; //for Random Surfer Model
                final double errorMargin = .00001; //for convergence
                double inPRsummation, sumPR=0; //summation for Random Surfer Model

                //num outlinks is the length of the list in the recipe list
                for (int j = 0; j < recipeList.size(); j++) { //for every recipe
                    numIn = adjInList.get(j).size(); //get num for this recipe, j
                    inPRsummation = 0; //to sum PageRanks of inlinks
                        for (int k = 0; k < numIn; k++) { //for every inlink
                            int linkedIndex = adjOutList.get(j).get(k); //get index for outlink

                            //sum every page rank for each inlink: PageRank of inlink from last iteration/num outlinks
                            inPRsummation += (lastCalculatedPR[linkedIndex])/(adjOutList.get(linkedIndex).size());
                        }
                    //calculate rank based on Random Surfer Model
                    ranks[j] = (lambda / (double) recipeCount) + (1 - lambda) * (inPRsummation);
                    }
                    //get sum of pageranks to check if they sum to 1
                    sumPR=(Arrays.stream(ranks).sum());

                //check convergence
                Boolean converges = true;
                for(int b=0; b<ranks.length; b++){
                    //if values differ by more than .00001
                    if(Math.abs(ranks[b]-lastCalculatedPR[b])>=errorMargin){
                        converges = false; //the values are still changing drastically from one iteration to next
                        break; //exit loop
                    }
                }

                //print status: iteration and sum of PageRank array at this iteration
                System.out.println("\nIteration: " + iter + " --> Sum PageRank = " + sumPR );

                //if the values have converged
                if(converges==true){
                    System.out.println("\nConverged!");
                    //this call will reach the base case
                    return calcPR(iter, ranks, sumPR, true);
                }

                //if the values have not converged
                else {
                    System.out.println("\nNot all values have converged.");
                    //run PageRank again
                    return calcPR(iter, ranks, sumPR, false);
                    }

                }//end else
            }//end calcPR

        //displays the top 100 recipes according to calculated PageRanks
        public void sortPageRank(double[] ranks){
            HashMap<String, Double > calculatedRanks = new LinkedHashMap<>(); //map recipe and pagerank
            final HashMap<String, Double> sortedRanks = new LinkedHashMap<>(); //for sorted ranks and recipes

            for(int i=0; i<ranks.length; i++){
                calculatedRanks.put(recipeList.get(i), ranks[i]); //add to map
            }

            //sort the unsorted map using stream class, in descending order
            calculatedRanks.entrySet().stream()
                    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())) //sort in descending order
                    .forEachOrdered(x -> sortedRanks.put(x.getKey(), x.getValue())); //placed sorted in sortedRanks map

            System.out.println("================================= Displaying Top 100 Recipes Based on PageRank ========================================");
            //print all the ranks to test

            //print top 100
            int count =1;
            Set<String> recipeKeys = sortedRanks.keySet();
            for(String rKey: recipeKeys){
                if(count<=100){
                    System.out.println("("+count+") PageRank: " + sortedRanks.get(rKey) + "\tRecipe Link: "+ rKey);
                    count++;
                }
            }//end loop to print top 100
        }//end sortPageRank
    }//end Matrix
