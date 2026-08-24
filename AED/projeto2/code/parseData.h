//
// Created by isabe on 21/12/2022.
//

#include <string>
#include <iostream>
#include <sstream>
#include <unordered_map>
#include <unordered_set>
#include <set>
#include "graph.h"
#include "airport.h"
#include "flight.h"
#include "airline.h"

#ifndef FEUP_AED2223_PARSEDATA_H
#define FEUP_AED2223_PARSEDATA_H

using namespace std;

class ParseData {
private:
    /**
     * Data structure where the airports information is stored.
     * string -> Airport IATA code.
     * Airport -> Airport object.
     */
    unordered_map<string, Airport> airports;
    /**
     * Data structure where the airlines information is stored.
     * string -> Airline ICAO code.
     * Airline -> Airline object.
     */
    unordered_map<string, Airline> airlines;
    /**
     * Data structure with all the cities that have airports.
     */
    unordered_set<string> cities;
    /**
     * Data structure with all the countries that have airports.
     */
    unordered_set<string> countries;
public:
    /**
     * Creates new ParseData object.
     * Calls the 3 functions that read the CSV files provided.
     */
    ParseData();
    /**
     * Reads the airports.csv file and stores it's data.
     */
    void airportParser();
    /**
     * Reads the flights.csv file and stores it's data.
     */
    void flightsParser();
    /**
     * Reads the airlines.csv file and stores it's data.
     */
    void airlinesParser();
    /**
     * Returns all the airports.
     * @return - All the airports.
     */
    unordered_map<string, Airport> getAirports();
    /**
     * Returns a specific Airport.
     * @param code - IATA code of the airport.
     * @return
     */
    Airport getAirport(string code);
    /**
     * Returns all the airlines.
     * @return - All the airlines.
     */
    unordered_map<string, Airline> getAirlines();
    /**
     * Returns and prints on the console all the airports (code and name) of a given city.
     * @param city - Name of a city.
     * @return - All the airports of a city.
     */
    vector<Airport> getAirportsFromCity(string city);
    /**
     * Prints all the airports of a given country.
     * @param country - Name of a country.
     */
    void getAirportsFromCountry(string country);
    /**
     * Prints all the airports stored.
     */
    void printAirports();
    /**
     * Prints all the airlines stored.
     */
    void printAirlines();
    /**
     * Prints all the cities that have at least one airport on a given country.
     * @param country - Name of a country.
     */
    void citiesFromCountry(string country);
    /**
     * Returns the number of airlines of an airport.
     * @param code - IATA code of an airport.
     * @return - Number of airlines of the airport with the IATA code *code*.
     */
    int getAirlinesByAirport(string code);
    /**
     * Stores all the cities that have at least one airport.
     */
    void setCities();
    /**
     * Returns an unordered set with all the cities that have at least one airport.
     * @return - Data structure with all the cities that have airports.
     */
    unordered_set<string> getCities();
    /**
     * Stores all the countries that have at least one airport.
     */
    void setCountries();
    /**
     * Returns an unordered set with all the countries that have at least one airport.
     * @return - Data structure with all the countries that have airports.
     */
    unordered_set<string> getCountries();
    };

#endif //FEUP_AED2223_PARSEDATA_H
