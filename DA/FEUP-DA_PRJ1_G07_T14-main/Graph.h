#ifndef WATER_SUPPLY_ANALYSIS_SYSTEM_GRAPH_H
#define WATER_SUPPLY_ANALYSIS_SYSTEM_GRAPH_H


#include <iostream>
#include <vector>
#include <queue>
#include <limits>
#include <algorithm>

using namespace std;

class Edge;

#define INF std::numeric_limits<double>::max()

/************************* Vertex  **************************/

enum class VertexType { WaterReservoir, PumpingStation, DeliverySite };

class Vertex {
private:
    string code;            // code of the node
    VertexType type;        // type of the node
    vector<Edge *> adj;  // outgoing edges

    // auxiliary fields
    bool visited = false; // used by DFS, BFS, Prim ...
    bool processing = false; // used by isDAG (in addition to the visited attribute)
    unsigned int indegree; // used by topsort
    double dist = 0;
    Edge *path = nullptr;

    vector<Edge *> incoming; // incoming edges

    void deleteEdge(Edge *edge);

public:
    Vertex(string code, VertexType type);
    bool operator<(Vertex & vertex) const;

    string getCode() const;
    VertexType getType() const;
    vector<Edge *> getAdj() const;
    bool isVisited() const;
    bool isProcessing() const;
    unsigned int getIndegree() const;
    double getDist() const;
    Edge *getPath() const;
    vector<Edge *> getIncoming() const;

    void setCode(string code);
    void setType(VertexType type);
    void setVisited(bool visited);
    void setProcesssing(bool processing);
    void setIndegree(unsigned int indegree);
    void setDist(double dist);
    void setPath(Edge *path);
    Edge * addEdge(Vertex *dest, unsigned long c);
    bool removeEdge(string code);
    void removeOutgoingEdges();
};

/********************** Edge  ****************************/

class Edge {
private:
    Vertex *dest; // destination vertex
    unsigned long capacity; // edge capacity

    // auxiliary fields
    bool selected = false;

    // used for bidirectional edges
    Vertex *orig;
    Edge *reverse = nullptr;

    unsigned long flow; // for flow-related problems

public:
    Edge(Vertex *orig, Vertex *dest, unsigned long capacity);

    Vertex * getDest() const;
    unsigned long getCapacity() const;
    bool isSelected() const;
    Vertex * getOrig() const;
    Edge *getReverse() const;
    unsigned long getFlow() const;

    void setSelected(bool selected);
    void setReverse(Edge *reverse);
    void setFlow(unsigned long flow);
};

/********************** Graph  ****************************/

class Graph {
private:
    unordered_map<string, Vertex *> vertices;    // vertex set

public:
    /*
    * Auxiliary function to find a vertex with a given the content.
    */
    Vertex *findVertex(const string &code) const;
    /*
     *  Adds a vertex with a given content or info (in) to a graph (this).
     *  Returns true if successful, and false if a vertex with that content already exists.
     */
    bool addVertex(const string &code, const VertexType &type);

    /*
     * Adds an edge to a graph (this), given the contents of the source and
     * destination vertices and the edge capacity (c).
     * Returns true if successful, and false if the source or destination vertex does not exist.
     */
    bool addEdge(const string &sourc, const string &dest, unsigned long c);
    bool removeEdge(const string &source, const string &dest);
    bool addBidirectionalEdge(const string &sourc, const string &dest, unsigned long c);

    int getNumVertex() const;
    unordered_map<string, Vertex *> getVertexSet() const;

    vector<string> dfs() const;
    vector<string> dfs(const string & source) const;
    void dfsVisit(Vertex *v,  vector<string> & res) const;
    vector<string> bfs(const string & source) const;

    bool isDAG() const;
    bool dfsIsDAG(Vertex *v) const;
    vector<string> topsort() const;
};

#endif //WATER_SUPPLY_ANALYSIS_SYSTEM_GRAPH_H
