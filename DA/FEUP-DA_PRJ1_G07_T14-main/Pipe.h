#ifndef WATER_SUPPLY_ANALYSIS_SYSTEM_PIPE_H
#define WATER_SUPPLY_ANALYSIS_SYSTEM_PIPE_H


#include <string>
using namespace std;

/**
* @brief Class representing a pipe entity in the water supply analysis system.
*/

class Pipe {
private :
    string servicePointA;
    string servicePointB;
    unsigned long capacity;
    bool unidirectional;

public:

    /**
    * @brief Constructs a pipe object with the provided details.
    *
    * @details This constructor initializes a pipe object with the specified pipe id and code.
    * The parameters represent essential information about a pipe, including its id and code
    *
    * @param servicePointA The source service that can be a water reservoir, a pumping station, or delivery site.
    * @param servicePointB The target service that can be a water reservoir, pumping station, or a delivery site.
    * @param capacity The maximum capacity of the pipe.
    * @param unidirectional Boolean indicating whether the connection between source service and target service is undirected or bidirected
    */
    Pipe(string servicePointA, string servicePointB, unsigned long capacity, bool unidirectional);
};


#endif //WATER_SUPPLY_ANALYSIS_SYSTEM_PIPE_H
