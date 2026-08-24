//
// Created by isabe on 21/12/2022.
//

#include <string>
#include <vector>
#include <iostream>
#include <fstream>
#include <sstream>
#include "flight.h"

#ifndef PROJETO2_AIRPORT_H
#define PROJETO2_AIRPORT_H

using namespace std;

/**
 * Represents an Airport.
 */
class Airport {
private:
    /**
     * Unique IATA code of the airport.
     */
    string code;
    /**
     * Official name of the airport.
     */
    string name;
    /**
     * City where the airport is located.
     */
    string city;
    /**
     * Country where the airport is located.
     */
    string country;
    /**
     * Latitude coordinate of the airport.
     */
    float latitude;
    /**
     * Longitude coordinate of the airport.
     */
    float longitude;
    /**
     * All the flights of the airport.
     */
    vector<Flight> flights;
public:
    /**
     * Creates a new object Airport empty.
     */
    Airport();
    /**
     * Creates a new object Airport
     * @param code - Unique IATA code of the airport.
     * @param name - Official name of the airport.
     * @param city - City where the airport is.
     * @param country - Country where the airport is.
     * @param latitude - Latitude coordinate of the airport.
     * @param longitude - Longitude coordinate of the airport.
     */
    Airport(string code, string name, string city, string country, float latitude, float longitude);
    /**
     * Returns the IATA code of the airport.
     * @return - IATA code of the airport.
     */
    string getCode();
    /**
     * Returns the name of the airport.
     * @return - Name of the airport.
     */
    string getName();
    /**
     * Returns the city where the airport is.
     * @return - City of the airport.
     */
    string getCity();
    /**
     * Returns the country in which the airport is.
     * @return - Country of the airport.
     */
    string getCountry();
    /**
     * Returns the latitude of the airport.
     * @return - Latitude coordinate of airport.
     */
    float getLatitude();
    /**
     * Returns the longitude of the airport.
     * @return - Longitude coordinate of airport.
     */
    float getLongitude();
    /*!
    * Adds flight to the Airport.
    * @param flight - Flight object that has origin in this airport.
    */
    void addFlight(const Flight & flight);
    /**
     * Returns a vector with all the flights.
     * @return - Vector with all the flights stored.
     */
    vector<Flight> getFlights();
};

#endif //PROJETO2_AIRPORT_H
