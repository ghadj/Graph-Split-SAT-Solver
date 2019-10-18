# Graph-Split-SAT-Solver

## Problem
The solution to the problem is to split the graph into exactly three sets, where nodes connected with positive edge are in the same set, and nodes with negative are in different. 

## About the program
There are the following options to be given as arguments to the program:
+ In case the first argument equals 0, the program promts user to give the path to the graph.txt file containing the following parameters, in the order shown below, each ending with newline:
  1. Number of nodes
  2. Percentage of negative-value edges
  3. Percentage of positive-value edges
  4. Graph density
  5. Adjacency Matrix
  6. Adjacency Matrix with sign of each edge
  
+ In case the first argument equals 1, the program generates a random graph based on the number of nodes, density of graph, precentage of positive and negative edges, that the user will be promted to enter

The program converts the problem into CNF (Conjunctive normal form) in order to be given in a SAT problem solver. Presents the solution from the SAT problem solver.
