//
// Created by isabe on 21/12/2022.
//

#include "airport.h"

Airport::Airport(){}

Airport::Airport(std::string code, std::string name, std::string city, std::string country, float latitude,
                 float longitude) : code(code), name(name), city(city), country(country), latitude(latitude),
                 longitude(longitude) {}

string Airport::getName() {
    return this->name;
}

string Airport::getCity() {
    return this->city;
}

string Airport::getCode() {
    return this->code;
}

string Airport::getCountry() {
    return this->country;
}

float Airport::getLatitude() {
    return this->latitude;
}

float Airport::getLongitude() {
    return this->longitude;
}

void Airport::addFlight(const Flight &flight) {
    flights.push_back(flight);
}

vector<Flight> Airport::getFlights(){
    return this->flights;
}
