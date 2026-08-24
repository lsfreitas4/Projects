// AED 2022/2023 - Aula Pratica 12
// Pedro Ribeiro (DCC/FCUP) [last update: 11/12/2022]

#ifndef _GRAPH_H_
#define _GRAPH_H_
#include "airport.h"
#include <list>
#include <vector>
#include <queue>
#include <unordered_map>
#include <iostream>

using namespace std;

class Graph {
    struct Edge {
        string dest;   // Destination node
        double weight; // An integer weight
        string airline;
    };

    struct Node {
        list<Edge> adj; // The list of outgoing edges (to adjacent nodes)
        bool visited;   // As the node been visited on a search?
        double distance; // The distance to a node
        Airport* airport;
    };

    int n;              // Graph size (vertices are numbered from 1 to n)
    bool hasDir;        // false: undirected; true: directed
    // The list of nodes being represented

public:
    Graph();
    // Constructor: nr nodes and direction (default: undirected)
    Graph(int nodes, bool dir = false);

    void addNode(string src, Airport* airport);

    // Add edge from source to destination with a certain weight
    void addEdge(string src, string dest, string airline);

    // Depth-First Search: example implementation
    void dfs(string v);

    // Breadth-First Search: example implementation
    void bfs(string source, string destination);

    std::unordered_map<std::string, Node> nodes;
};

#endif
