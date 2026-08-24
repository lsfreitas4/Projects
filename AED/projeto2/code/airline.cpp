//
// Created by isabe on 21/12/2022.
//

#include "airline.h"

Airline::Airline(){}

Airline::Airline(string code, string name, string callsign, string country) : code(code),
                     name(name), callsign(callsign), country(country) {}

string Airline::getCode() {
    return this->code;
}

string Airline::getName() {
    return this->name;
}

string Airline::getCallsign() {
    return this->callsign;
}

string Airline::getCountry() {
    return this->country;
}
