package io.github.ghadj.graphsplit;

public class GraphSplit {
    int[][] variables;

    private static String positiveEdge(int x, int y) {
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
    private static String negativeEdge(int x, int y) {
        // example: assign x and y to the different set
        // Set - #1 #2 #3
        // node x 1 2 3
        // node y 4 5 6
        return new String(-(x + 0) + ' ' + -(y + 1) + "0\n" + // (not(1) or not(4))
                -(x + 1) + ' ' + -(y + 2) + "0\n" + // (not(2) or not(5))
                -(x + 2) + ' ' + -(y + 3) + "0\n"); // (not(3) or not(6))
    }

    private static String assignedToOneSetOnly(int x) {
        // example: assign x to one set only
        // Set - #1 #2 #3
        // node x 1 2 3
        return new String((x + 0) + ' ' + (x + 1) + ' ' + (x + 2) + "0\n" + // (1 or 2 or 3))
                -(x + 0) + ' ' + -(x + 1) + "0\n" + // (not(1) or not(2))
                -(x + 0) + ' ' + -(x + 3) + "0\n" + // (not(1) or not(3))
                -(x + 1) + ' ' + -(x + 3) + "0\n"); // (not(2) or not(3))
    }

    public static void main(String[] args){

    }
}
