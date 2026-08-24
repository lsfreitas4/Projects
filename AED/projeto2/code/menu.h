//
// Created by mcarl on 29/12/2022.
//

#include <string>
#include <iostream>
#include <fstream>
#include <sstream>
#include "graph.h"
#include "parseData.h"
#include "flight.h"


#ifndef FEUP_AED2223_MENU_H
#define FEUP_AED2223_MENU_H

/**
 * Represents the Menu.
 */
class Menu {
    ParseData parseData;
public:
    /**
     * Initialize the program
     */
    static void init();
    /**
     * Prints the start message of the program.
     */
    void startMessage();
    /**
     * Prints the options of the program.
     */
    void printOptions();
    /**
     * Prints the departure options of the program.
     */
    void printDepartureOptions();
    /**
     * Prints the options to see information about an airport.
     */
    void printAirportInformation();
    /**
     * Prints information about the IATA codes.
     */
    void printIATACodesOptions();
    /**
     * Prints the direct flights from a given airport.
     * @param code - IATA code of an airport.
     */
    void printDirectFlightsFromAirport(string code);
    /**
     * Prints all the airlines that fly from a given airport.
     * @param code - IATA code of an airport.
     */
    void printAirlinesFromAirport(string code);
    /**
     * Prints all the destinations of direct flights of a given airport.
     * @param code - IATA code of an airport.
     */
    void printDestinationsFromAirport(string code);
    /**
     * Prints all the countries to where is possible to have a direct flight from a given airport.
     * @param code - IATA code of the airport.
     */
    void printCountriesAirport(string code);
};

#endif //FEUP_AED2223_MENU_H
