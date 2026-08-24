#include "Data.h"

Data::Data() {}

void Data::readFiles(const filesystem::path &dir_path) {
    filesystem::path reservoirPath;
    filesystem::path stationsPath;
    filesystem::path citiesPath;
    filesystem::path pipesPath;

    try {
        for (const auto& entry : filesystem::directory_iterator(dir_path)) {

            if (entry.is_regular_file()) {
                string filename = entry.path().filename().string();

                if (filename.find("Reservoir") != string::npos) {
                    if (!reservoirPath.empty()) throw runtime_error("Error: Multiple Reservoir files found.");

                    reservoirPath = dir_path / filename;

                } else if (filename.find("Stations") != string::npos) {
                    if (!stationsPath.empty()) throw runtime_error("Error: Multiple Stations files found.");

                    stationsPath = dir_path / filename;

                } else if (filename.find("Cities") != string::npos) {
                    if (!citiesPath.empty()) throw runtime_error("Error: Multiple Cities files found.");

                    citiesPath = dir_path / filename;

                } else if (filename.find("Pipes") != string::npos) {
                    if (!pipesPath.empty()) throw runtime_error("Error: Multiple Pipes files found.");

                    pipesPath = dir_path / filename;
                }
            }
        }

        if(reservoirPath.empty() | stationsPath.empty() | citiesPath.empty() | pipesPath.empty()) throw runtime_error("Invalid input. Some file are missing in the given path.");

        ifstream reservoirFile(reservoirPath);
        ifstream stationsFile(stationsPath);
        ifstream citiesFile(citiesPath);
        ifstream pipesFile(pipesPath);

        if (!reservoirFile.is_open()) throw runtime_error("Error opening the reservoir file.");
        if (!stationsFile.is_open()) throw runtime_error("Error opening the stations file.");
        if (!citiesFile.is_open()) throw runtime_error("Error opening the cities file.");
        if (!pipesFile.is_open()) throw runtime_error("Error opening the pipes file.");

        readFileReservoir(reservoirFile);
        readFileStations(stationsFile);
        readFileCities(citiesFile);
        readFilePipes(pipesFile);

    } catch (const exception& e) {
        throw;
    }
}

void Data::readFileReservoir(ifstream &file) {
    string line;
    getline(file,line);

    while(getline(file,line)) {
        string reservoir, municipality, code;
        unsigned long id, maxDelivery;
        stringstream ss(line);
        getline(ss,reservoir,',');
        getline(ss,municipality,',');
        ss >> id;
        ss.ignore();
        getline(ss, code,',');
        ss >> maxDelivery;
        ss.ignore();

        if(reservoir == "" | municipality == "" | code == "") continue;

        WaterReservoir* wr = new WaterReservoir(reservoir, municipality, id, code, maxDelivery);
        g.addVertex(code, VertexType::WaterReservoir);
        this->waterReservoirs.insert({code, wr});
    }
}

void Data::readFileStations(ifstream &file) {
    string line;
    getline(file,line);

    while(getline(file,line)) {
        string code;
        unsigned long id;
        stringstream ss(line);
        ss >> id;
        ss.ignore();
        getline(ss, code,',');

        if(code == "") continue;

        PumpingStation* ps = new PumpingStation(id, code);
        g.addVertex(code, VertexType::PumpingStation);
        this->pumpingStations.insert({code, ps});
    }
}

void Data::readFileCities(ifstream &file) {
    string line;
    getline(file,line);

    while(getline(file,line)) {
        string city, code;
        unsigned long id, demand, population;
        stringstream ss(line);
        getline(ss, city,',');
        ss >> id;
        ss.ignore();
        getline(ss, code,',');
        ss >> demand;
        ss.ignore();
        ss >> population;
        ss.ignore();

        if(code == "" || city == "") continue;

        DeliverySite* ds = new DeliverySite(city, id, code, demand, population);
        g.addVertex(code, VertexType::DeliverySite);
        this->deliverySites.insert({code, ds});
    }
}

void Data::readFilePipes(ifstream &file) {
    string line;
    getline(file,line);

    while(getline(file,line)) {
        string servicePointA, servicePointB;
        unsigned long capacity, direction;
        stringstream ss(line);
        getline(ss, servicePointA,',');
        getline(ss, servicePointB,',');
        ss >> capacity;
        ss.ignore();
        ss >> direction;
        ss.ignore();

        if(servicePointA == "" || servicePointB == "") continue;

        bool unidirectional = direction == 1 ? true : false;

        Pipe* pipe = new Pipe(servicePointA, servicePointB, capacity, unidirectional);
        string key = servicePointA+"-"+servicePointB;
        this->pipes.insert({key, pipe});

        if(unidirectional) g.addEdge(servicePointA, servicePointB, capacity);
        else g.addBidirectionalEdge(servicePointA, servicePointB, capacity);
    }
}

