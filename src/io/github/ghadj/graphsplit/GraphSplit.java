package io.github.ghadj.graphsplit;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

/**
 * The program models the problem of a graph to be split into exactly three
 * sets, based on problem constraints, to a boolean satisfiability problem (SAT)
 * and uses SAT solver to find and present a solution, if exists.
 * 
 * There are the following options to be given as arguments to the program:
 * 
 * In case the first argument equals 0, the program promts user to give the path
 * to the graph.txt file containing the following parameters, in the order shown
 * below, each ending with newline: Number of nodes, Percentage of
 * negative-value edges, Percentage of positive-value edges, Graph density,
 * Adjacency Matrix, Adjacency Matrix with sign of each edge.
 * 
 * In case the first argument equals 1, the program generates a random graph
 * based on the number of nodes, density of graph, precentage of positive and
 * negative edges, that the user will be promted to enter
 * 
 * The program converts the problem into CNF (Conjunctive normal form) in order
 * to be given in a SAT problem solver. Presents the solution from the SAT
 * problem solver.
 * 
 * Compile: javac -d ./bin src/io/github/ghadj/graphsplit/*.java
 * 
 * Run: java -cp ./bin io.github.ghadj.graphsplit.GraphSplit <0 or 1>
 * 
 * @author Georgios Hadjiantonis
 * @since 18-10-2019
 */
public class GraphSplit {
    static final String PATH_SAT_SOLVER = "../lingeling_solver/lingeling"; // SAT solver outside Graph-Split-SAT-Solver/
    static final String GENERATED_GRAPH = "graph_out.txt";
    static final String CNF_OUT = "output.cnf";
    private int numberOfNodes = 0;
    private double negativeEdgesPercent = 0;
    private double positiveEdgesPercent = 0;
    private double density = 0;
    private char[][] adjacencyMatrix = null;
    private char[][] signEdges = null;
    private int[][] variables = null;
    private String cnf = null;
    private int numClauses = 0;

    /**
     * Constructor of GraphSplit. Generates a random graph based on the parameters
     * given. Writes the following parameters of the generated graph in txt file:
     * Number of nodes, Percentage of negative-value edges, Percentage of
     * positive-value edges, Graph density, Adjacency Matrix, Adjacency Matrix with
     * sign of each edge.
     * 
     * The name of the txt is determined by the constant {@link #GENERATED_GRAPH}.
     * 
     * Assume that all parameters given are valid.
     * 
     * @param numberOfNodes        number of nodes.
     * @param negativeEdgesPercent percentage of negative edges.
     * @param positiveEdgesPercent percentage of positive edges.
     * @param density              of the graph.
     * @throws IOException
     */
    public GraphSplit(int numberOfNodes, double negativeEdgesPercent, double positiveEdgesPercent, double density)
            throws IOException {
        // number of nodes
        this.numberOfNodes = numberOfNodes;

        // percentage of negative edges
        this.negativeEdgesPercent = negativeEdgesPercent;

        // percentage of positive edges
        this.positiveEdgesPercent = positiveEdgesPercent;

        // density
        this.density = density;

        // Adjacency matrix & sign of edges matrix
        generateGraph();
        writeGraph();
        setVariables();
    }

    /**
     * Constructor of GraphSplit. Reads from the file given the following parameters
     * about the graph: Number of nodes, Percentage of negative-value edges,
     * Percentage of positive-value edges, Graph density, Adjacency Matrix,
     * Adjacency Matrix with sign of each edge. The parameters must be given in the
     * above order, each ending with newline.
     * 
     * Assume that all parameters given are valid.
     * 
     * @param filename path to file containing the paameters of the graph.
     * @throws FileNotFoundException
     * @throws IOException
     */
    public GraphSplit(String filename) throws FileNotFoundException, IOException {
        File file = new File(filename);
        BufferedReader br = new BufferedReader(new FileReader(file));
        // number of nodes
        numberOfNodes = Integer.parseInt(br.readLine());

        // percentage of negative edges
        negativeEdgesPercent = Double.parseDouble(br.readLine());

        // percentage of positive edges
        positiveEdgesPercent = Double.parseDouble(br.readLine());

        // density
        density = Double.parseDouble(br.readLine());

        // Adjacency matrix
        adjacencyMatrix = new char[numberOfNodes][];
        for (int i = 0; i < numberOfNodes; i++)
            adjacencyMatrix[i] = br.readLine().replaceAll(" ", "").toCharArray();
        br.readLine(); // empty line
        // sign of edges matrix
        signEdges = new char[numberOfNodes][];
        for (int i = 0; i < numberOfNodes; i++)
            signEdges[i] = br.readLine().replaceAll(" ", "").toCharArray();

        br.close();
        writeGraph();
        setVariables();
    }

    /**
     * Creates the clauses in CNF so that the nodes given x and y, which are
     * connected via a positive edge, to be included in the same set.
     * 
     * @param x base case node to be included in the first set.
     * @param y base case node to be included in the first set.
     * @return string representation of clauses in CNF.
     */
    public static String positiveEdge(int x, int y) {
        // example: assign x and y to the same set
        // Set - #1 #2 #3
        // node x 1 2 3
        // node y 4 5 6
        return new String((x + 0) + " " + (x + 1) + " " + (x + 2) + " 0\n" + // (1 or 2 or 3)
                (x + 0) + " " + (x + 1) + " " + (y + 2) + " 0\n" + // (1 or 2 or 6)
                (x + 0) + " " + (y + 1) + " " + (x + 2) + " 0\n" + // (1 or 5 or 3)
                (x + 0) + " " + (y + 1) + " " + (y + 2) + " 0\n" + // (1 or 5 or 6)
                (y + 0) + " " + (x + 1) + " " + (x + 2) + " 0\n" + // (4 or 2 or 3)
                (y + 0) + " " + (x + 1) + " " + (y + 2) + " 0\n" + // (4 or 2 or 6)
                (y + 0) + " " + (y + 1) + " " + (x + 2) + " 0\n" + // (4 or 5 or 3)
                (y + 0) + " " + (y + 1) + " " + (y + 2) + " 0\n"); // (4 or 5 or 6)
    }

    /**
     * Creates the clauses in CNF so that the nodes given x and y, which are
     * connected via a negative edge, NOT to be included in the same set.
     * 
     * not (x and y) = not(x) or not(y)
     * 
     * @param x base case node to be included in the first set.
     * @param y base case node to be included in the first set.
     * @return string representation of clauses in CNF.
     */
    public static String negativeEdge(int x, int y) {
        // example: assign x and y to the different set
        // Set - #1 #2 #3
        // node x 1 2 3
        // node y 4 5 6
        return new String(-(x + 0) + " " + -(y + 0) + " 0\n" + // (not(1) or not(4))
                -(x + 1) + " " + -(y + 1) + " 0\n" + // (not(2) or not(5))
                -(x + 2) + " " + -(y + 2) + " 0\n"); // (not(3) or not(6))
    }

    /**
     * Creates the clauses in CNF so that the node given x to be included in exactly
     * one set.
     * 
     * @param x base case node to be included in the first set.
     * @return string representation of clauses in CNF.
     */
    public static String assignedToOneSetOnly(int x) {
        // example: assign x to one set only
        // Set - #1 #2 #3
        // node x 1 2 3
        return new String((x + 0) + " " + (x + 1) + " " + (x + 2) + " 0\n" + // (1 or 2 or 3))
                -(x + 0) + " " + -(x + 1) + " 0\n" + // (not(1) or not(2))
                -(x + 0) + " " + -(x + 2) + " 0\n" + // (not(1) or not(3))
                -(x + 1) + " " + -(x + 2) + " 0\n"); // (not(2) or not(3))
    }

    /**
     * Creates the clauses in CNF so that each set to have at least one node of the
     * graph.
     * 
     * @param variables (3 per node).
     * @return string representation of clauses in CNF.
     */
    public static String atLeastOneElement(int[][] variables) {
        // example: assign at least one element to each set
        // Set - #1 #2 #3
        // node x 1 2 3
        // node y 4 5 6
        // node z 7 8 9
        StringBuilder str = new StringBuilder();
        for (int j = 0; j < 3; j++) {
            for (int i = 0; i < variables.length; i++)
                str.append(variables[i][j] + " "); // (1 or 4 or 7) and (...) ...
            str.append("0\n");
        }
        return str.toString();
    }

    /**
     * Reads the name of file which includes the graph parameters.
     * 
     * @return filename.
     * @throws IOException
     */
    public static String readFilename() throws IOException {
        BufferedReader br = null;
        String filename = null;

        br = new BufferedReader(new InputStreamReader(System.in));

        System.out.print("Give path to file containing graph info: ");
        filename = br.readLine();

        if (br != null)
            br.close();

        return filename;
    }

    /**
     * Initializes array {@link #variables}. Each row of the array corresponds to
     * one node, represents the case that the specific node belongs to the
     * corresponding set (column number).
     */
    private void setVariables() {
        // example:
        // Set - #1 #2 #3
        // node x 1 2 3
        // node y 4 5 6
        // node z 7 8 9
        // ...
        variables = new int[numberOfNodes][3];
        int var = 1;
        for (int i = 0; i < numberOfNodes; i++)
            for (int j = 0; j < 3; j++)
                variables[i][j] = var++;
    }

    /**
     * Returns the variable of the given node, in the case that it belongs to the
     * first set.
     * 
     * @param x node.
     * @return the variable of the given node, in the case that it belongs to the
     *         first set.
     */
    private int getFirstVariable(int x) {
        return variables[x][0];
    }

    /**
     * Generates CNF clauses and sets the {@link #cnf} attribute, so that all the
     * constraints of the problem are met.
     */
    private void generateCNF() {
        StringBuilder str = new StringBuilder();
        str.append(atLeastOneElement(this.variables));
        numClauses += 3;
        for (int i = 0; i < numberOfNodes; i++) {
            str.append(assignedToOneSetOnly(getFirstVariable(i)));
            numClauses += 4;
            for (int j = 0; j < numberOfNodes; j++)
                if (j >= i)
                    if (signEdges[i][j] == '+') {
                        str.append(positiveEdge(getFirstVariable(i), getFirstVariable(j)));
                        numClauses += 8;
                    } else if (signEdges[i][j] == '-') {
                        str.append(negativeEdge(getFirstVariable(i), getFirstVariable(j)));
                        numClauses += 3;
                    }
        }
        this.cnf = "p cnf " + (numberOfNodes * 3) + " " + numClauses + "\n" + str.toString();
    }

    /**
     * Generates the cnf clauses, writes the clauses in DIMACS CNF former file named
     * accoring to the constant {@link #CNF_OUT} and calls the SAT solver.
     * 
     * Prints the solution given by the SAT solver, if exists and returns true,
     * otherwise returns false.
     * 
     * @return true if solutoin found, otherwise false.
     * @throws IOException
     */
    public boolean solve() throws IOException {
        this.generateCNF();
        writeCNF(this.cnf, CNF_OUT);
        String result = execCmd(PATH_SAT_SOLVER + " " + CNF_OUT);

        // solution line starts with 's'
        String s = result.substring(result.indexOf("\ns ") + 3, result.indexOf("\n", result.indexOf("\ns ") + 3));
        System.out.println("\n" + s);

        if (s.equals("UNSATISFIABLE"))
            return false;

        // assignment to the variables that satisfies SAT start with 'v'
        String v = result.substring(result.indexOf("\nv ") + 1, result.indexOf(" 0\n", result.indexOf("\nv ")));
        printSolution(v.replaceAll("v", ""));
        return true;
    }

    /**
     * Prints solution from the parameter given.
     * 
     * @param v solution, assignment of true(positive variable
     *          number)/false(negative variable number) values to variables.
     */
    private void printSolution(String v) {
        Scanner scanner = new Scanner(v);
        for (int i = 0; i < numberOfNodes && scanner.hasNext(); i++)
            for (int j = 0; j < 3 && scanner.hasNext(); j++)
                variables[i][j] = scanner.nextInt();
        scanner.close();
        System.out.println("\nThe three (3) sets that can be generated based on the graph given are:");
        for (int i = 0; i < 3; i++) {
            System.out.print("{ ");
            for (int j = 0; j < numberOfNodes; j++)
                // positive number of variable => assignment of True value
                if (variables[j][i] >= 0)
                    System.out.print((j + 1) + " ");
            System.out.print("} ");
        }
        System.out.println();
    }

    /**
     * Writes string given to file.
     * 
     * @param str      string.
     * @param filename name of file, where the string to be writen.
     * @throws IOException
     */
    public static void writeCNF(String str, String filename) throws IOException {
        Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), "utf-8"));
        writer.write(str);
        writer.close();
    }

    /**
     * Executes the command given and returns its ouput in form of String.
     * 
     * @param cmd a command.
     * @return output of the command given.
     * @throws java.io.IOException
     */
    public static String execCmd(String cmd) throws java.io.IOException {
        @SuppressWarnings("resource")
        Scanner scanner = new Scanner(Runtime.getRuntime().exec(cmd).getInputStream()).useDelimiter("\\A");
        String output = scanner.hasNext() ? scanner.next() : "";
        return output;
    }

    /**
     * Generates a random graph based on the parameters-attributes:
     * {@link #numberOfNodes}, {@link #negativeEdgesPercent}, {@link #density}. Sets
     * attributes {@link #adjcencyMatrix} and {@link #signEdges}.
     * 
     * Assume that the values of the parameters are valid.
     */
    private void generateGraph() {
        int edges = (int) Math.round(this.density * (this.numberOfNodes * (this.numberOfNodes - 1)) * 0.5);
        int negativeEdges = (int) Math.ceil(edges * negativeEdgesPercent);
        this.adjacencyMatrix = new char[numberOfNodes][numberOfNodes];
        this.signEdges = new char[numberOfNodes][numberOfNodes];

        // initialize arrays to no edges
        for (int i = 0; i < numberOfNodes; i++) {
            Arrays.fill(this.adjacencyMatrix[i], '0');
            Arrays.fill(this.signEdges[i], '0');
        }

        int countEdges = 0;
        int countNegEdges = 0;
        Random rand = new Random();
        int i, j;
        // randomly assign negative edges
        while (countEdges != edges && countNegEdges != negativeEdges) {
            i = rand.nextInt(this.numberOfNodes);
            j = rand.nextInt(this.numberOfNodes);

            if (i != j && this.adjacencyMatrix[i][j] == '0') {
                this.adjacencyMatrix[i][j] = '1';
                this.adjacencyMatrix[j][i] = '1';
                this.signEdges[i][j] = '-';
                this.signEdges[j][i] = '-';
                countNegEdges++;
                countEdges++;
            }
        }

        // randomly assign positive edges
        while (countEdges != edges) {
            i = rand.nextInt(this.numberOfNodes);
            j = rand.nextInt(this.numberOfNodes);

            if (i != j && this.adjacencyMatrix[i][j] == '0') {
                this.adjacencyMatrix[i][j] = '1';
                this.adjacencyMatrix[j][i] = '1';
                this.signEdges[i][j] = '+';
                this.signEdges[j][i] = '+';
                countEdges++;
            }
        }
    }

    /**
     * Promts user to enter parameters and returns an object of {@link GraphSplit}
     * according to the input given.
     * 
     * Assume that the input is valid.
     * 
     * @return {@link GraphSplit} according to the input given.
     * @throws IOException
     */
    public static GraphSplit fromUser() throws IOException {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Give number of nodes: ");
        int numberOfNodes = scanner.nextInt();

        System.out.print("Give density of graph: ");
        double density = scanner.nextDouble();

        System.out.print("Give percentage of positive edges: ");
        double pos = scanner.nextDouble();

        System.out.print("Give percentage of negative edges: ");
        double neg = scanner.nextDouble();

        if (scanner != null)
            scanner.close();

        return new GraphSplit(numberOfNodes, neg, pos, density);
    }

    /**
     * Writes attributes of the current object {@link GraphSplit} to file named
     * after constant {@link #GENERATED_GRAPH} with the following order, each ending
     * with newline: Number of nodes, Percentage of negative-value edges, Percentage
     * of positive-value edges, Graph density, Adjacency Matrix, Adjacency Matrix
     * with sign of each edge.
     * 
     * @throws IOException
     */
    private void writeGraph() throws IOException {
        Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(GENERATED_GRAPH), "utf-8"));
        StringBuilder str = new StringBuilder();

        str.append(this.numberOfNodes + "\n");
        str.append(this.negativeEdgesPercent + "\n");
        str.append(this.positiveEdgesPercent + "\n");
        str.append(this.density + "\n");

        for (int i = 0; i < this.numberOfNodes; i++)
            str.append(String.valueOf(this.adjacencyMatrix[i]) + "\n");
        str.append("\n");
        for (int i = 0; i < this.numberOfNodes; i++)
            str.append(String.valueOf(this.signEdges[i]) + "\n");

        writer.write(str.toString());
        writer.close();
    }

    public static void main(String[] args) {
        if (args.length <= 0) {
            System.out.println("Error: No arguments given.");
            return;
        }
        try {
            if (Integer.parseInt(args[0]) == 0) {
                String filename = readFilename();
                if (filename == null)
                    return;
                GraphSplit g = new GraphSplit(filename);
                g.solve();
            } else if (Integer.parseInt(args[0]) == 1) {
                GraphSplit g = fromUser();
                g.solve();
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
