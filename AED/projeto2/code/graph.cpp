// AED 2022/2023 - Aula Pratica 12
// Pedro Ribeiro (DCC/FCUP) [last update: 11/12/2022]

#include "graph.h"

Graph::Graph(){}

// Constructor: nr nodes and direction (default: undirected)
Graph::Graph(int num, bool dir) : n(num), hasDir(dir), nodes(num+1) {
}

// Add edge from source to destination with a certain weight
void Graph::addEdge(string src, string dest, string airline) {
    Edge edge;
    edge.dest = dest;
    edge.weight = 1;
    edge.airline = airline;
    nodes[src].adj.push_back(edge);
    if (!hasDir) addEdge(dest, src, airline);
}
void Graph::addNode(std::string src, Airport *airport) {
    Node node;
    node.visited = false;
    node.distance = 0;
    node.airport = airport;
    nodes[src] = node;
}

// Depth-First Search: example implementation
void Graph::dfs(string v) {
    // show node order
    // cout << v << " ";
    nodes[v].visited = true;
    for (auto e : nodes[v].adj) {
        string w = e.dest;
        if (!nodes[w].visited)
            dfs(w);
    }
}

// Breadth-First Search: example implementation
/*void Graph::bfs(string source) {
    queue<Node*> q;
    nodes[source].visited = true;
    q.push(&nodes[source]);

    while (!q.empty()) {
        Node* current = q.front();
        q.pop();
        cout << "Visitando o aeroporto " << current->airport->getName() << endl;

        for (Edge e : current->adj) {
            Node* neighbor = &nodes[e.dest];
            if (!neighbor->visited) {
                neighbor->visited = true;
                q.push(neighbor);
            }
        }
    }
}
*/
void Graph::bfs(string source, string destination) {
    queue<Node*> q;
    unordered_map<string, int> num_flights;
    unordered_map<string, string> path;
    nodes[source].visited = true;
    num_flights[source] = 0;
    path[source] = source;
    q.push(&nodes[source]);

    while (!q.empty()) {
        Node* current = q.front();
        q.pop();

        for (Edge e : current->adj) {
            Node* neighbor = &nodes[e.dest];
            if (!neighbor->visited) {
                neighbor->visited = true;
                num_flights[e.dest] = num_flights[current->airport->getName()] + 1;
                path[e.dest] = path[current->airport->getName()] + " -> " + e.dest;
                q.push(neighbor);
            }
        }
    }

    // Imprime o menor número de voos e o caminho encontrado
    cout << "Menor numero de voos: " << num_flights[destination] << endl;
    cout << "Caminho: " << path[destination] << endl;
}

