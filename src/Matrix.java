import java.io.*;
import java.util.*;

/*
* This Matrix object class takes in a file and generates an adjacency list
* representing crawled webpages of a particular domain and their inlinks
* to ultimately produce a pagerank.
* */
public class Matrix {
    private ArrayList<ArrayList<Integer>> adjInList = new ArrayList<>(); //adjacency list for inlinks
    private ArrayList<ArrayList<Integer>> adjOutList = new ArrayList<>(); //adjacency list for outlinks
    private File inlinkFile, outlinkFile;
    private ArrayList<String> recipeList = new ArrayList<>(); //list of unique recipes
    private int recipeCount, edgeCount; //number of recipes and node indexes

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

    //create adj matrix as an arraylist of lists that list the inlinks for
    private void generateInLinkList(){
        try{
            Scanner sc = new Scanner(inlinkFile);
            String line;
            String [] splitLine; // splitLine[0]: URL, splitLine[1]: outlink

            sc.nextLine(); //skip header
            while(sc.hasNextLine()){
                //ct++;
                line = sc.nextLine();
                splitLine = line.split(",");

                //find the vertex number for the given recipe

                int aIndex = recipeList.indexOf(splitLine[0]);
                int bIndex = recipeList.indexOf(splitLine[1]);

                //add to that arraylist in the list of lists
                if(!adjInList.get(aIndex).contains(bIndex)){
                    adjInList.get(aIndex).add(bIndex); //add edge between recipe1 (aIndex) and recipe2 (bIndex)
                }
            }
        }
        catch(FileNotFoundException f){
            System.out.println("Couldn't find file!");
        }
    }

    //create adj matrix as an arraylist of lists that list the inlinks for
    private void generateOutLinkList(){
        try{
            Scanner sc = new Scanner(outlinkFile);
            String line;
            String [] splitLine; // splitLine[0]: URL, splitLine[1]: outlink

            sc.nextLine(); //skip header
            while(sc.hasNextLine()){
                line = sc.nextLine();
                splitLine = line.split(",");

                //find the vertex number for the given recipe
                if(recipeList.contains(splitLine[0]) && recipeList.contains(splitLine[1])){
                    int aIndex = recipeList.indexOf(splitLine[0]);
                    int bIndex = recipeList.indexOf(splitLine[1]);

                    //add to that arraylist in the list of lists
                    if(!adjOutList.get(aIndex).contains(bIndex)){
                        adjOutList.get(aIndex).add(bIndex); //add edge between recipe1 (aIndex) and recipe2 (bIndex)
                    }
                }
            }
        }
        catch(FileNotFoundException f){
            System.out.println("Couldn't find file!");
        }
    }


    //add unique recipes to an arraylist -- index in recipeList = node in the list
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

    //to display the adjacency matrix
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

    public void PageRank() {
        final double initial = 1 / (double) recipeCount;
        System.out.println("Iteration 0 --> All PageRanks set to initial value (1/number of unique recipes): "+initial);
        double[] initRanks = new double[recipeCount];
        final int iter= 0;

        for(int i=0; i<initRanks.length; i++){
            initRanks[i]=initial;
        }

        double[] convergedPr = calcPR(0, initRanks, initial, false );
        sortPageRank(convergedPr);

          }//end PageRank

    //stop when average =1
    private double[] calcPR(int iteration, double[] lastCalculatedPR, double lastCalculatedSum, boolean conv){

            //base case
            if(iteration>1  && lastCalculatedSum>=1. && lastCalculatedSum<=1.000000000001 && conv==true){
                System.out.println("Converged at Iteration: "+iteration + " with PageRank sum of: " + lastCalculatedSum);
                return lastCalculatedPR;
            }
            else {
                //to hold new ranks
                final int iter = ++iteration;
                double[] ranks = new double[lastCalculatedPR.length];
                int numIn;
                final double lambda = 0.2;
                final double epsilon = .00001;
                double inPRsummation, sumPR=0;

                //num outlinks is the length of the list in the recipe list
                for (int j = 0; j < recipeList.size(); j++) { //for every recipe
                    numIn = adjInList.get(j).size(); //size of the array = num outlinks
                    inPRsummation = 0;
                        for (int k = 0; k < numIn; k++) { //for every inlink
                            int linkedIndex = adjOutList.get(j).get(k);
                            inPRsummation += (lastCalculatedPR[linkedIndex])/(adjOutList.get(linkedIndex).size());
                        }
                    ranks[j] = (lambda / (double) recipeCount) + (1 - lambda) * (inPRsummation);

                    }
                    //get sum of pageranks
                    sumPR=(Arrays.stream(ranks).sum());

                //check convergence
                Boolean converges = true;
                for(int b=0; b<ranks.length; b++){
                    //if values differ by more than .00001
                    if(Math.abs(ranks[b]-lastCalculatedPR[b])>=epsilon){
                        converges = false;
                        break; //exit loop, difference too large
                    }
                }

                System.out.println("\nIteration: " + iter + " --> Sum PageRank = " + sumPR );

                if(converges==true){
                    System.out.println("\n Converged!");
                    return calcPR(iter, ranks, sumPR, true);
                }

                else {
                    System.out.println("\nNot all values have converged.");
                    return calcPR(iter, ranks, sumPR, false);
                    }

                }//end else
            }//end calcPR

        //displays the top 100 recipes according to calculated pageranks
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
