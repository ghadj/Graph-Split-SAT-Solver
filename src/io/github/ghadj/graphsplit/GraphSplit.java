package io.github.ghadj.graphsplit;

import java.io.*;
import java.util.Scanner;

/**
 * Compile: javac -d ./bin src/io/github/ghadj/graphsplit/*.java
 * 
 * Run: java -cp ./bin io.github.ghadj.graphsplit.GraphSplit <0 or 1>
 */
public class GraphSplit {
    static final String PATH_SAT_SOLVER = "../lingeling_solver/lingeling";
    private int numberOfNodes;
    private double negativeEdgesPercent;
    private double positiveEdgesPercent;
    private double density;
    private char[][] adjacencyMatrix;
    private char[][] signEdges;
    private int[][] variables;
    private String cnf = null;
    private int numClauses = 0;

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

        // sign of edges matrix
        signEdges = new char[numberOfNodes][];
        for (int i = 0; i < numberOfNodes; i++)
            signEdges[i] = br.readLine().replaceAll(" ", "").toCharArray();

        br.close();

        setVariables();
    }

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

    // not (x and y) = not(x) or not(y)
    public static String negativeEdge(int x, int y) {
        // example: assign x and y to the different set
        // Set - #1 #2 #3
        // node x 1 2 3
        // node y 4 5 6
        return new String(-(x + 0) + " " + -(y + 0) + " 0\n" + // (not(1) or not(4))
                -(x + 1) + " " + -(y + 1) + " 0\n" + // (not(2) or not(5))
                -(x + 2) + " " + -(y + 2) + " 0\n"); // (not(3) or not(6))
    }

    public static String assignedToOneSetOnly(int x) {
        // example: assign x to one set only
        // Set - #1 #2 #3
        // node x 1 2 3
        return new String((x + 0) + " " + (x + 1) + " " + (x + 2) + " 0\n" + // (1 or 2 or 3))
                -(x + 0) + " " + -(x + 1) + " 0\n" + // (not(1) or not(2))
                -(x + 0) + " " + -(x + 2) + " 0\n" + // (not(1) or not(3))
                -(x + 1) + " " + -(x + 2) + " 0\n"); // (not(2) or not(3))
    }

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

    public static String readFilename() throws IOException {
        BufferedReader br = null;
        String filename = null;

        br = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("Give path to file containing graph info:");
        filename = br.readLine();

        if (br != null)
            br.close();

        return filename;
    }

    // @TODO
    private static Boolean validParam() {
        return true;
    }

    private void setVariables() {
        variables = new int[numberOfNodes][3];
        int var = 1;
        for (int i = 0; i < numberOfNodes; i++)
            for (int j = 0; j < 3; j++)
                variables[i][j] = var++;
    }

    private int getBaseCase(int x) {
        return variables[x][0];
    }

    public void generateCNF() {
        StringBuilder str = new StringBuilder();
        str.append(atLeastOneElement(this.variables));
        numClauses += 3;
        for (int i = 0; i < numberOfNodes; i++) {
            str.append(assignedToOneSetOnly(getBaseCase(i)));
            numClauses += 4;
            for (int j = 0; j < numberOfNodes; j++)
                if (j >= i)
                    if (signEdges[i][j] == '+') {
                        str.append(positiveEdge(getBaseCase(i), getBaseCase(j)));
                        numClauses += 8;
                    } else if (signEdges[i][j] == '-') {
                        str.append(negativeEdge(getBaseCase(i), getBaseCase(j)));
                        numClauses += 3;
                    }
        }
        this.cnf = "p cnf " + (numberOfNodes * 3) + " " + numClauses + "\n" + str.toString();
    }

    public boolean solve() throws IOException {
        this.generateCNF();
        writeCNF(this.cnf, "output.cnf");
        String result = execCmd(PATH_SAT_SOLVER + " output.cnf");

        String s = result.substring(result.indexOf("\ns ") + 3, result.indexOf("\n", result.indexOf("\ns ") + 3));
        System.out.println("\nResult: " + s);

        if (s.equals("UNSATISFIABLE"))
            return false;

        String v = result.substring(result.indexOf("\nv ") + 1, result.indexOf(" 0\n", result.indexOf("\nv ")));
        printSolution(v.replaceAll("v", ""));
        return true;
    }

    public void printSolution(String v) {
        Scanner scanner = new Scanner(v);
        for (int i = 0; i < numberOfNodes && scanner.hasNext(); i++)
            for (int j = 0; j < 3 && scanner.hasNext(); j++)
                variables[i][j] = scanner.nextInt();
        scanner.close();

        for (int i = 0; i < 3; i++) {
            System.out.print("{ ");
            for (int j = 0; j < numberOfNodes; j++)
                if (variables[j][i] >= 0)
                    System.out.print((j + 1) + " ");
            System.out.print("} ");
        }
        System.out.println();
    }

    public static void writeCNF(String str, String filename) throws IOException {
        Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), "utf-8"));
        writer.write(str);
        writer.close();
    }

    public static String execCmd(String cmd) throws java.io.IOException {
        @SuppressWarnings("resource")
        Scanner scanner = new Scanner(Runtime.getRuntime().exec(cmd).getInputStream()).useDelimiter("\\A");
        String output = scanner.hasNext() ? scanner.next() : "";
        return output;
    }

    // @TODO
    // public generate graph()

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

            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
