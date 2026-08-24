//
// Created by isabe on 21/12/2022.
//

#include <string>

#ifndef PROJETO2_AIRLINE_H
#define PROJETO2_AIRLINE_H

using namespace std;

/**
 * Represents an Airline.
 */
class Airline {
private:
    /**
     * ICAO code of the airline.
     */
    string code;
    /**
     * Official name of the airline.
     */
    string name;
    /**
     * Call sign of the airline.
     */
    string callsign;
    /**
     * Country where the airline is originated.
     */
    string country;
public:
    /**
     * Creates an empty Airline.
     */
    Airline();
    /**
     * Creates a new Airline object.
     * @param code - ICAO code of the airline.
     * @param name - Official name of the airline.
     * @param callsign - Call sign of the airline.
     * @param country - Country where the airline is originated.
     */
    Airline(string code, string name, string callsign, string country);
    /**
     * Returns the ICAO code of the airline.
     * @return - ICAO code of the airline.
     */
    string getCode();
    /**
     * Returns the name of the airline.
     * @return - Official name of the airline.
     */
    string getName();
    /**
     * Returns the call sign of the airline.
     * @return - Call sign of the airline.
     */
    string getCallsign();
    /**
     * Returns the country from which the airline was created.
     * @return - Country where the airline is originated.
     */
    string getCountry();
};

#endif //PROJETO2_AIRLINE_H
