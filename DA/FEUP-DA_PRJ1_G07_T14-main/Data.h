#ifndef WATER_SUPPLY_ANALYSIS_SYSTEM_DATA_H
#define WATER_SUPPLY_ANALYSIS_SYSTEM_DATA_H


using namespace std;

#include <iostream>
#include <unordered_map>
#include <fstream>
#include <sstream>
#include <iostream>
#include <filesystem>
#include "Graph.h"
#include "WaterReservoir.h"
#include "PumpingStation.h"
#include "DeliverySite.h"
#include "Pipe.h"

/**
 * @brief Class that saves all the program data.
 */

class Data {
private:
    unordered_map<string, WaterReservoir *> waterReservoirs;
    unordered_map<string, PumpingStation *> pumpingStations;
    unordered_map<string, DeliverySite *> deliverySites;
    unordered_map<string, Pipe *> pipes;
    Graph g;
public:
    /**
    * @brief Default constructor for the Data class.
    *
    * @details This default constructor creates a new instance of the Data class without
    * specifying any initial values. It initializes the Data object with default values.
    * @details Time complexity: O(1)
    */
    Data();

    void readFiles(const filesystem::path &dir_path);

    void readFileReservoir(ifstream &file);

    void readFileStations(ifstream &file);

    void readFileCities(ifstream &file);

    void readFilePipes(ifstream &file);
};


#endif //WATER_SUPPLY_ANALYSIS_SYSTEM_DATA_H
