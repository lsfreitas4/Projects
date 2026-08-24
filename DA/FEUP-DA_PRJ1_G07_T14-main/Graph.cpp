#include "Graph.h"


/************************* Vertex  **************************/

void Vertex::deleteEdge(Edge *edge) {
    Vertex *dest = edge->getDest();
    // Remove the corresponding edge from the incoming list
    auto it = dest->incoming.begin();
    while (it != dest->incoming.end()) {
        if ((*it)->getOrig()->getCode() == this->code) {
            it = dest->incoming.erase(it);
        }
        else {
            it++;
        }
    }
    delete edge;
}

Vertex::Vertex(string code, VertexType type) : code(code), type(type) {}

bool Vertex::operator<(Vertex & vertex) const {
    return this->dist < vertex.dist;
}

string Vertex::getCode() const {
    return this->code;
}

VertexType Vertex::getType() const {
    return this->type;
}

vector<Edge *> Vertex::getAdj() const {
    return this->adj;
}

bool Vertex::isVisited() const {
    return this->visited;
}

bool Vertex::isProcessing() const {
    return this->processing;
}

unsigned int Vertex::getIndegree() const {
    return this->indegree;
}

double Vertex::getDist() const {
    return this->dist;
}

Edge *Vertex::getPath() const {
    return this->path;
}

vector<Edge *> Vertex::getIncoming() const {
    return this->incoming;
}

void Vertex::setCode(string code) {
    this->code = code;
}

void Vertex::setType(VertexType type) {
    this->type = type;
}

void Vertex::setVisited(bool visited) {
    this->visited = visited;
}

void Vertex::setProcesssing(bool processing) {
    this->processing = processing;
}

void Vertex::setIndegree(unsigned int indegree) {
    this->indegree = indegree;
}

void Vertex::setDist(double dist) {
    this->dist = dist;
}

void Vertex::setPath(Edge *path) {
    this->path = path;
}

Edge * Vertex::addEdge(Vertex *dest, unsigned long c) {
    auto newEdge = new Edge(this, dest, c);
    adj.push_back(newEdge);
    dest->incoming.push_back(newEdge);
    return newEdge;
}

bool Vertex::removeEdge(string code) {
    bool removedEdge = false;
    auto it = adj.begin();
    while (it != adj.end()) {
        Edge *edge = *it;
        Vertex *dest = edge->getDest();
        if (dest->getCode() == code) {
            it = adj.erase(it);
            deleteEdge(edge);
            removedEdge = true; // allows for multiple edges to connect the same pair of vertices (multigraph)
        }
        else {
            it++;
        }
    }
    return removedEdge;
}

void Vertex::removeOutgoingEdges() {
    auto it = adj.begin();
    while (it != adj.end()) {
        Edge *edge = *it;
        it = adj.erase(it);
        deleteEdge(edge);
    }
}


/********************** Edge  ****************************/

Edge::Edge(Vertex *orig, Vertex *dest, unsigned long capacity) : orig(orig), dest(dest), capacity(capacity) {}

Vertex * Edge::getDest() const {
    return this->dest;
}

unsigned long Edge::getCapacity() const {
    return this->capacity;
}

bool Edge::isSelected() const {
    return this->selected;
}

Vertex * Edge::getOrig() const {
    return this->orig;
}

Edge *Edge::getReverse() const {
    return this->reverse;
}

unsigned long Edge::getFlow() const {
    return this->flow;
}

void Edge::setSelected(bool selected) {
    this->selected = selected;
}

void Edge::setReverse(Edge *reverse) {
    this->reverse = reverse;
}

void Edge::setFlow(unsigned long flow) {
    this->flow = flow;
}


/********************** Graph  ****************************/

Vertex *Graph::findVertex(const string &code) const {
    auto it = this->vertices.find(code);
    if (it != this->vertices.end()) {
        return it->second;
    }
    return nullptr;
}

bool Graph::addVertex(const string &code, const VertexType &type) {
    if(findVertex(code) == nullptr) {
        Vertex *newVertex = new Vertex(code, type);
        this->vertices.insert({code, newVertex});
        return true;
    }
    return false;
}

bool Graph::addEdge(const string &sourc, const string &dest, unsigned long c) {
    Vertex *originVertex = findVertex(sourc);
    Vertex *destVertex = findVertex(dest);

    if (originVertex && destVertex) {
        originVertex->addEdge(destVertex, c);
        return true;
    }

    return false;
}

bool Graph::removeEdge(const string &source, const string &dest) {
    Vertex * srcVertex = findVertex(source);
    if (srcVertex == nullptr) {
        return false;
    }
    return srcVertex->removeEdge(dest);
}

bool Graph::addBidirectionalEdge(const string &sourc, const string &dest, unsigned long c) {
    auto v1 = findVertex(sourc);
    auto v2 = findVertex(dest);
    if (v1 == nullptr || v2 == nullptr)
        return false;
    auto e1 = v1->addEdge(v2, c);
    auto e2 = v2->addEdge(v1, c);
    e1->setReverse(e2);
    e2->setReverse(e1);
    return true;
}

int Graph::getNumVertex() const {
    return this->vertices.size();
}

unordered_map<string, Vertex *> Graph::getVertexSet() const {
    return this->vertices;
}

// TODO - Dont know if this methods are needed
vector<string> Graph::dfs() const {
    vector<string> res;
    return res;
}

// TODO - Dont know if this methods are needed
vector<string> Graph::dfs(const string & source) const {
    vector<string> res;
    return res;
}

// TODO - Dont know if this methods are needed
void Graph::dfsVisit(Vertex *v,  vector<string> & res) const {}

// TODO - Dont know if this methods are needed
vector<string> Graph::bfs(const string & source) const {
    vector<string> res;
    return res;
}

// TODO - Dont know if this methods are needed
bool Graph::isDAG() const {
    return false;
}

// TODO - Dont know if this methods are needed
bool Graph::dfsIsDAG(Vertex *v) const {
    return false;
}

// TODO - Dont know if this methods are needed
vector<string> Graph::topsort() const {
    vector<string> res;
    return res;
}
