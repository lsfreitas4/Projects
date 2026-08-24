//
// Created by isabe on 21/12/2022.
//

#include "flight.h"

Flight::Flight(string source, string target, string airlineCode) : source(source), target(target), airlineCode(airlineCode) {}

string Flight::getTarget() {
    return this->target;
}

string Flight::getSource(){
    return this->source;
}

string Flight::getAirlineCode() {
    return this->airlineCode;
}
