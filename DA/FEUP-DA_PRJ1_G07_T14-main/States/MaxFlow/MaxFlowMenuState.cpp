#include <iostream>
#include "States/MainMenuState.h"
#include "MaxFlowMenuState.h"

MaxFlowMenuState::MaxFlowMenuState() {}

void MaxFlowMenuState::display() const {
    cout << "\033[32m";
    cout << "==== FIND MAX WATER FLOW ====" << endl;
    cout << "\033[0m";
    cout << "   1. Select Specific City   " << endl;
    cout << "   2. All Cities             \n" << endl;

    cout << "   q. Main Menu              " << endl;
    cout << "\033[32m";
    cout << "-----------------------------" << endl;
    cout << "\033[0m";
    cout << "Enter your choice: ";
}

void MaxFlowMenuState::handleInput(App* app) {
    string choice;
    cin >> choice;

    if (choice.size() == 1) {
        switch (choice[0]) {
            case '1':
                app->setState(this);
                break;
            case '2':
                app->setState(this);
                break;
            case 'q':
                app->setState(new MainMenuState());
                break;
            default:
                cout << "\033[31m" << "Invalid choice. Please try again." << "\033[0m"  << endl;
        }
    } else  {
        cout << "\033[31m";
        cout << "Invalid input. Please enter a single character." << endl;
        cout << "\033[0m";
    }
}