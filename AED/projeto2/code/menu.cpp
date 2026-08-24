//
// Created by mcarl on 29/12/2022.
//

#include "menu.h"

using namespace std;

void Menu::startMessage() {
    cout << "\n\n=====================================" << endl;
    cout << "    Welcome to our application." << endl;
    cout << "=====================================" << endl;
    cout << "The inputs are case sensitive, so please, be careful.\nThank you!\n\n";
    cout << "How can we help you today?\n" << endl;
    //cout << "NUMBER OF CITIES " << parseData.getCities().size() << endl << endl;
}

void Menu::printOptions() {
    int op;
    cout << "Available Options (e.g.: 1):" << endl;
    cout << "1. Departures" << endl;
    cout << "2. Airport information" << endl;
    cout << "3. See IATA codes" << endl;
    cout << "4. Shut down" << endl;
    cin >> op;
    if(op == 1){
        printDepartureOptions();
    }
    else if(op==2){
        printAirportInformation();
    }
    else if(op==3){
        printIATACodesOptions();
    }
    else if(op==4){
        return;
    }
    else{
        cout << "Invalid option. Try again." << endl << endl;
        printOptions();
    }
}

void Menu::printDepartureOptions() {
    unordered_set<string> cities = parseData.getCities();
    unordered_set<string> countries = parseData.getCountries();
    unordered_map<string, Airport> airports = parseData.getAirports();

    int op;
    cout << "\n1. Choose country" << endl;
    cout << "2. Choose city" << endl;
    cout << "3. Choose airport" << endl;
    cout << "4. Back" << endl;
    cin >> op;

    if(op == 1){
        string country, city, airport, dest, op1;
        cout << "Departure from country: " << endl;
        getline(cin >> ws, country);
        bool present = false;
        for(auto it:countries){
            if(it == country)
                present = true;
        }
        if(present) {
            present = false;
            parseData.citiesFromCountry(country);
            cout << "From which of the above cities do you what to departure? " << endl;
            getline(cin >> ws, city);
            for(auto it:cities){
                if(it == city)
                    present = true;
            }
            if(present) {
                present = false;
                parseData.getAirportsFromCity(city);
                cout << "From which airport?\n(Please insert the airport code)" << endl;
                cin >> airport;
                for(auto it: airports){
                    if(it.second.getCode() == airport)
                        present=true;
                }
                if(present) {
                    present=false;
                    Airport a = parseData.getAirport(airport);
                    vector<Flight> flig = a.getFlights();
                    cout << "Number of flights that departure from " << airport << ": " << flig.size() << endl;
                    cout << "To where do you want to go?\n(Please insert the airport code)" << endl;
                    cin >> dest;
                    for(auto it: airports){
                        if(it.second.getCode() == airport)
                            present=true;
                    }
                    if(present) {
                        present=false;
                        //TODO
                        cout << "Finished.\nGoing back to the main menu...\n";
                        printOptions();
                    }else{
                        cout << "Invalid airport. Please use the correct syntax.\nGoing back..." << endl;
                        printDepartureOptions();
                    }
                }
                else{
                    cout << "Invalid airport. Please use the correct syntax.\nGoing back..." << endl;
                    printDepartureOptions();
                }
            }
            else{
                cout << "Invalid city. Please use the correct syntax.\nGoing back..." << endl;
                printDepartureOptions();
            }
        }else{
            cout << "Invalid country. Please use the correct syntax." << endl;
            printDepartureOptions();
        }
    }
    else if(op == 2){
        bool present = false;
        string city, airport, dest;
        cout << "Departure from city: " << endl;
        getline(cin >> ws, city);
        for(auto it:cities){
            if(it == city)
                present = true;
        }
        if(present) {
            present=false;
            parseData.getAirportsFromCity(city);
            cout << "From which airport?\n(Please insert the airport code)" << endl;
            cin >> airport;
            for(auto it: airports){
                if(it.second.getCode() == airport)
                    present=true;
            }
            if(present) {
                present=false;
                Airport a = parseData.getAirport(airport);
                vector<Flight> flig = a.getFlights();
                cout << "Number of flights that departure from " << airport << ": " << flig.size() << endl;
                cout << "To where do you want to go?\n(Please insert the airport code)" << endl;
                cin >> dest;
                for(auto it:airports){
                    if(it.second.getCode() == dest)
                        present=true;
                }
                if(present){
                    present=false;
                    //TODO
                    cout << "Finished.\nGoing back to the main menu...\n";
                    printOptions();
                }else{
                    cout << "Invalid airport. Please use the correct syntax.\nStarting again..." << endl;
                    printDepartureOptions();
                }
            }else{
                cout << "Invalid airport. Please use the correct syntax.\nStarting again..." << endl;
                printDepartureOptions();
            }
        }
        else {
            cout << "Invalid city. Please use the correct syntax.\nStarting again..." << endl;
            printDepartureOptions();
        }
    }
    else if(op == 3){
        bool present=false;
        string airport, dest;
        cout << "Departure from airport:\n(Please insert the airport code)" << endl;
        cin >> airport;
        for(auto it: airports){
            if(it.second.getCode() == airport)
                present=true;
        }
        if(present) {
            present=false;
            Airport a = parseData.getAirport(airport);
            vector<Flight> flig = a.getFlights();
            cout << "Number of flights that departure from " << airport << ": " << flig.size() << endl;
            cout << "\nTo where do you want to go?\n(Please insert the airport code)" << endl;
            cin >> dest;
            for(auto it: airports){
                if(it.second.getCode() == dest)
                    present=true;
            }
            if(present) {
                //TODO
                cout << "Finished.\nGoing back to the main menu...\n";
                printOptions();
            }else {
                cout << "Invalid airport. Please use the correct syntax.\nGoing back..." << endl;
                printDepartureOptions();
            }
        }
        else{
            cout << "Invalid airport. Please use the correct syntax.\nStarting again..." << endl;
            printDepartureOptions();
        }

    }
    else if(op==4){
        cout << "Going back..."<< endl << endl;
        printOptions();
    }
    else{
        cout << "Invalid option. Try again." << endl << endl;
        printDepartureOptions();
    }
}

void Menu::printAirportInformation() {
    int op;
    string iataCodeSource;
    unordered_map<string, Airport> airports = parseData.getAirports();
    cout << "\n1. Flights from an airport" << endl;
    cout << "2. Airlines of an airport" << endl;
    cout << "3. Direct destinations from an airport\n   (This returns all the airports ro where you have a direct flight)" << endl;
    cout << "4. See direct flights to different countries\n   (This returns all the countries to where you can travel directly)" << endl;
    cout << "5. Back" << endl;
    cin >> op;
    if(op==1){
        cout << "Please insert the IATA code of the source airport: " << endl;
        cin >> iataCodeSource;
        bool present=false;
        for(auto it: airports){
            if(it.second.getCode()==iataCodeSource)
                present=true;
        }
        if(present) {
            printDirectFlightsFromAirport(iataCodeSource);
            cout << "Finished.\nGoing back to the main menu...\n";
            printOptions();
        }else{
            cout << "Invalid airport. Please use the correct syntax.\nStarting again..." << endl << endl;
            printAirportInformation();
        }
    } else if(op==2){
        cout << "Please insert the IATA code of the source airport: " << endl;
        cin >> iataCodeSource;
        bool present=false;
        for(auto it: airports){
            if(it.second.getCode()==iataCodeSource)
                present=true;
        }
        if(present) {
            printAirlinesFromAirport(iataCodeSource);
            cout << "Finished.\nGoing back to the main menu...\n";
            printOptions();
        }else{
            cout << "Invalid airport. Please use the correct syntax.\nStarting again..." << endl << endl;
            printAirportInformation();
        }
    } else if (op==3){
        cout << "Please insert the IATA code of the source airport: " << endl;
        cin >> iataCodeSource;
        bool present=false;
        for(auto it: airports){
            if(it.second.getCode()==iataCodeSource)
                present=true;
        }
        if(present) {
            printDestinationsFromAirport(iataCodeSource);
            cout << "Finished.\nGoing back to the main menu...\n";
            printOptions();
        }else{
            cout << "Invalid airport. Please use the correct syntax.\nStarting again..." << endl << endl;
            printAirportInformation();
        }
    } else if(op==4){
        cout << "Please insert the IATA code of the source airport: " << endl;
        cin >> iataCodeSource;
        bool present=false;
        for(auto it: airports){
            if(it.second.getCode()==iataCodeSource)
                present=true;
        }
        if(present) {
            printCountriesAirport(iataCodeSource);
            cout << "Finished.\nGoing back to the main menu...\n";
            printOptions();
        }else{
            cout << "Invalid airport. Please use the correct syntax.\nStarting again..." << endl << endl;
            printAirportInformation();
        }
    } else if (op==5){
        cout << "Going back...\n\n";
        printOptions();
    }
    else {
        cout << "Invalid option. Try again." << endl;
        printAirportInformation();
    }
}

void Menu::printDirectFlightsFromAirport(string code){
    vector<Flight> temp;
    for(auto i: parseData.getAirports()){
        if(i.second.getCode() == code){
            temp = i.second.getFlights();
        }
    }
    cout << "There are " << temp.size() << " flights from " << code << " airport" << endl;
    for(auto f: temp){
        cout << "Airline Code: " << f.getAirlineCode() << ", Destination: " << f.getTarget() << endl;
    }
}
void Menu::printDestinationsFromAirport(string code){
    vector<Flight> temp;
    set<string>temp2;
    int count = 0;
    for(auto i: parseData.getAirports()){
        if(i.second.getCode() == code){
            temp = i.second.getFlights();
        }
    }
    for(auto f: temp){
        if (temp2.find(f.getTarget()) == temp2.end()){
            count +=1;
            temp2.insert(f.getTarget());
        }
    }
    cout << "There are " <<  count << " destinations from the " << code << " airport." << endl;
    for (auto e: temp2){
        cout << e << endl;
    }
}

void Menu::printAirlinesFromAirport(string code){
    vector<Flight> temp;
    set<string>temp2;
    int count = 0;
    for(auto i: parseData.getAirports()){
        if(i.second.getCode() == code){
            temp = i.second.getFlights();
        }
    }
    for(auto f: temp){
        if (temp2.find(f.getAirlineCode()) == temp2.end()){
            count +=1;
            temp2.insert(f.getAirlineCode());
        }
    }
    cout << "There are " <<  count << " different airlines flying from and to the " << code << " airport" << endl;
    for (auto e: temp2){
        cout << e << endl;
    }
}

void Menu::printCountriesAirport(string code){
    set<string> temp;
    vector<Flight> flights;
    for(auto i: parseData.getAirports()){
        if(i.second.getCode() == code){
            flights = i.second.getFlights();
        }
    }
    for (auto f: flights) {
        temp.insert(parseData.getAirport(f.getTarget()).getCountry());
    }
    cout << "There are " << temp.size() << " destination countries from the " << code << " airport." << endl;
    for (auto e:temp){
        cout << e << endl;
    }
}

void Menu::printIATACodesOptions() {
    int op;
    unordered_set<string> cities = parseData.getCities();
    unordered_set<string> countries = parseData.getCountries();

    cout << "\n1. Choose country" << endl;
    cout << "2. Choose city" << endl;
    cout << "3. Back" << endl;
    cin >> op;
    if(op==1){
        string country;
        cout << "Please insert a country: " << endl;
        getline(cin >> ws, country);
        bool present=false;
        for(auto it: countries){
            if(it == country)
                present=true;
        }
        if(present) {
            parseData.citiesFromCountry(country);
            cout << "Finished.\nGoing back to the main menu...\n";
            printOptions();
        }else{
            cout << "Invalid country. Please use the correct syntax.\nStarting again..." << endl << endl;
            printIATACodesOptions();
        }
    }
    else if(op==2){
        string city;
        cout << "Please insert a city: " << endl;
        getline(cin >> ws, city);
        bool present=false;
        for(auto it: cities){
            if(it == city)
                present=true;
        }
        if(present) {
            parseData.getAirportsFromCity(city);
            cout << "Finished.\nGoing back to the main menu...\n";
            printOptions();
        }else{
            cout << "Invalid city. Please use the correct syntax.\nStarting again ..." << endl << endl;
            printIATACodesOptions();
        }
    }
    else if(op==3){
        cout << "Going back..." << endl << endl;
        printOptions();
    }
    else {
        cout << "Invalid option. Try again." << endl;
        printIATACodesOptions();
    }
}



void Menu::init() {
    ParseData *p = new ParseData();
    p->citiesFromCountry("Portugal");
}
