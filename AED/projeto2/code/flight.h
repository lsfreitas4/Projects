//
// Created by isabe on 21/12/2022.
//

#include <string>

#ifndef PROJETO2_FLIGHT_H
#define PROJETO2_FLIGHT_H

using namespace std;

/**
 * Represents a Flight.
 */
class Flight {
private:
    /**
     * IATA code of the source airport.
     */
    string source;
    /**
     * IATA code of the target airport.
     */
    string target;
    /**
     * ICAO code of the airline responsible for the flight.
     */
    string airlineCode;
public:
    /**
     * Creates a new Flight object.
     * @param target - IATA code of the origin airport.
     * @param airlineCode - ICAO code of the airline responsible for the flight.
     */
    Flight(string source, string target, string airlineCode);
    /**
     * Returns the IATA code of the destination airport.
     * @return - IATA code of the destination airport.
     */
    string getTarget();
    /**
     * Returns the IATA code of the origin airport.
     * @return - IATA code of the origin airport.
     */
    string getSource();
    /**
     * Returns the ICAO code of the airline responsible for the flight.
     * @return - ICAO code the airline that is responsible for the flight.
     */
    string getAirlineCode();
};

#endif //PROJETO2_FLIGHT_H
