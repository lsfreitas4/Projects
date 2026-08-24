//
// Created by isabe on 21/12/2022.
//

#include "parseData.h"

ParseData::ParseData(){
    airportParser();
    airlinesParser();
    flightsParser();
    setCities();
    setCountries();
}

void ParseData::airportParser() {
    ifstream in("../dataset/airports.csv");
    std::string line;

    if(in.is_open()){
        // first line isn't important
        getline(in, line);

        string code, name, city, country, lat, longit;
        float latitude, longitude;

        while(getline(in, line)) {
            istringstream iss(line);

            getline(iss, code, ',');
            getline(iss, name, ',');
            getline(iss, city, ',');
            getline(iss, country, ',');
            getline(iss, lat, ',');
            getline(iss, longit, ',');

            latitude = stof(lat);
            longitude = stof(longit);
            Airport airport = Airport(code,name,city,country,latitude,longitude);

            airports[code] = airport;
        }
    }
    else {
        cout << "\nFailed on opening airports.csv file.\n";
    }
}

void ParseData::flightsParser() {
    ifstream in("../dataset/flights.csv");
    string line;

    if(in.is_open()){
        // first line isn't important
        getline(in, line);
        while (getline(in, line)) {
            string source, target, airline;
            istringstream iss(line);

            getline(iss, source, ',');
            getline(iss, target, ',');
            getline(iss, airline, ',');

            // flight is associated to an airport.
            Flight f = Flight(source, target, airline);
            auto it = airports.find(source);
            it->second.addFlight(f);
        }
    }
    else {
        cout << "\nFailed on opening flights.csv file.\n";
    }

}

void ParseData::airlinesParser() {
    ifstream in("../dataset/airlines.csv");
    std::string line;


    if(in.is_open()){
        // first line isn't important
        getline(in, line);

        while (getline(in, line)) {
            string code, name, callsign, country;
            istringstream iss(line);

            getline(iss, code, ',');
            getline(iss, name, ',');
            getline(iss, callsign, ',');
            getline(iss, country, ',');

            Airline airline = Airline(code, name, callsign, country);
            airlines[code] = airline;
        }
    }
    else {
        cout << "\nFailed on opening airlines.csv file.\n";
    }
}

void ParseData::printAirports() {
    cout << airports.size() << endl;
    for(auto airp: airports){
        cout << "AIRPORT: " << airp.second.getName() << endl;
    }
}

void ParseData::printAirlines() {
    for(auto airl: airlines){
        cout << "AIRLINE: " << endl;
        cout << "CODE: " << airl.first << " NAME: " << airl.second.getName() << endl;
    }
}

unordered_map<string, Airport> ParseData::getAirports(){
    return this->airports;
}

unordered_map<string, Airline> ParseData::getAirlines(){
    return this->airlines;
}

Airport ParseData::getAirport(std::string code) {
    for(auto it: airports){
        if(it.second.getCode() == code){
            return it.second;
        }
    }
}

vector<Airport> ParseData::getAirportsFromCity(string city) {
    vector<Airport> airportsFromCity;
    for(auto it: airports){
        if(it.second.getCity() == city){
            airportsFromCity.push_back(it.second);
        }
    }
    // print airports from a given city
    for(auto e: airportsFromCity){
        cout << "AIRPORT: " << e.getCode() << ", " << e.getName() << endl;
    }
    return airportsFromCity;
}

void ParseData::getAirportsFromCountry(std::string country) {
    vector<Airport> temp;
    for(auto it: airports){
        if(it.second.getCountry() == country)
            temp.push_back(it.second);
    }
    for(auto i:temp){
        cout << "AIRPORT: " << i.getCode() << ", " << i.getName() << endl;
    }
}

// prints in console all the cities from a country that have airports
void ParseData::citiesFromCountry(string country){
    cout << "Cities with airports from: " << country << endl;
    for(auto i: airports){
        if(i.second.getCountry() == country)
            cout << i.second.getCity() << endl;
    }
}

int ParseData::getAirlinesByAirport(string code) {
    set<string> s;
    auto x = airports.at(code);
    for (auto a: x.getFlights()){
        s.insert(a.getAirlineCode());
    }
    return s.size();
}

void ParseData::setCities() {
    for(auto a: airports){
        cities.insert(a.second.getCity());
    }
}

unordered_set<string> ParseData::getCities() {
    return this->cities;
}

void ParseData::setCountries() {
    for(auto it: airports){
        countries.insert(it.second.getCountry());
    }
}

unordered_set<string> ParseData::getCountries() {
    return this->countries;
}
