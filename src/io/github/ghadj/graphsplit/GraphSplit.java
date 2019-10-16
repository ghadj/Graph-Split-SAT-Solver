package io.github.ghadj.graphsplit;

import java.io.*;

public class GraphSplit {
    private int numberOfNodes;
    private double negativeEdgesPercent;
    private double positiveEdgesPercent;
    private double density;
    private char[][] adjacencyMatrix;
    private char[][] signEdges;
    private int[][] variables;

    public static String positiveEdge(int x, int y) {
        // example: assign x and y to the same set
        // Set - #1 #2 #3
        // node x 1 2 3
        // node y 4 5 6
        return new String((x + 0) + ' ' + (x + 1) + ' ' + (x + 2) + "0\n" + // (1 or 2 or 3)
                (x + 0) + ' ' + (x + 1) + ' ' + (y + 2) + "0\n" + // (1 or 2 or 6)
                (x + 0) + ' ' + (y + 1) + ' ' + (x + 2) + "0\n" + // (1 or 5 or 3)
                (x + 0) + ' ' + (y + 1) + ' ' + (y + 2) + "0\n" + // (1 or 5 or 6)
                (y + 0) + ' ' + (x + 1) + ' ' + (x + 2) + "0\n" + // (4 or 2 or 3)
                (y + 0) + ' ' + (x + 1) + ' ' + (y + 2) + "0\n" + // (4 or 2 or 6)
                (y + 0) + ' ' + (y + 1) + ' ' + (x + 2) + "0\n" + // (4 or 5 or 3)
                (y + 0) + ' ' + (y + 1) + ' ' + (y + 2) + "0\n"); // (4 or 5 or 6)
    }

    // not (x and y) = not(x) or not(y)
    public static String negativeEdge(int x, int y) {
        // example: assign x and y to the different set
        // Set - #1 #2 #3
        // node x 1 2 3
        // node y 4 5 6
        return new String(-(x + 0) + ' ' + -(y + 1) + "0\n" + // (not(1) or not(4))
                -(x + 1) + ' ' + -(y + 2) + "0\n" + // (not(2) or not(5))
                -(x + 2) + ' ' + -(y + 3) + "0\n"); // (not(3) or not(6))
    }

    public static String assignedToOneSetOnly(int x) {
        // example: assign x to one set only
        // Set - #1 #2 #3
        // node x 1 2 3
        return new String((x + 0) + ' ' + (x + 1) + ' ' + (x + 2) + "0\n" + // (1 or 2 or 3))
                -(x + 0) + ' ' + -(x + 1) + "0\n" + // (not(1) or not(2))
                -(x + 0) + ' ' + -(x + 3) + "0\n" + // (not(1) or not(3))
                -(x + 1) + ' ' + -(x + 3) + "0\n"); // (not(2) or not(3))
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
                str.append(variables[i][j] + " "); // (1 or 4 or 5) and (...) ...
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

    public void readParameters(String filename) throws FileNotFoundException, IOException {
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
            adjacencyMatrix[i] = br.readLine().toCharArray();

        // sign of edges matrix
        signEdges = new char[numberOfNodes][];
        for (int i = 0; i < numberOfNodes; i++)
            signEdges[i] = br.readLine().toCharArray();

        br.close();
    }

    // @TODO
    private static Boolean validParam() {
        return true;
    }

    // @TODO
    private static void setVariables() {
    }

    private int getBaseCase(int x) {
        return variables[x][0];
    }

    public String generateCNF() {
        StringBuilder str = new StringBuilder();
        str.append(atLeastOneElement(this.variables));
        for (int i = 0; i < numberOfNodes; i++) {
            str.append(assignedToOneSetOnly(getBaseCase(i)));
            for (int j = 0; j < numberOfNodes; j++)
                if (j >= i)
                    if (signEdges[i][j] == '+')
                        str.append(positiveEdge(getBaseCase(i), getBaseCase(j)));
                    else if (signEdges[i][j] == '-')
                        str.append(negativeEdge(getBaseCase(i), getBaseCase(j)));
        }
        return null;
    }

    public static void main(String[] args) {
        try {
            if (Integer.parseInt(args[0]) == 0) {

                String filename = readFilename();
                if (filename == null)
                    return;
                GraphSplit g = new GraphSplit();
                g.readParameters(filename);
                String cnf = g.generateCNF();

            } else if (Integer.parseInt(args[0]) == 1) {

            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
