#include <iostream>
#include "TryAgainState.h"
#include "States/MainMenuState.h"

TryAgainState::TryAgainState(State *backState, State* currentState)
        : backState(backState), currentState(currentState) {}

void TryAgainState::display() const {
    cout << "\033[31m";
    cout << "----------------" << endl;
    cout << "\033[0m";
    cout << "  1. Try Again" << endl;
    cout << "  b. Go back" << endl;
    cout << "  q. Main Menu" << endl;
    cout << "\033[31m";
    cout << "----------------" << endl;
    cout << "\033[0m";
    cout << "Enter your choice: ";
}

void TryAgainState::handleInput(App* app) {
    string choice;
    cin >> choice;

    if (choice.size() == 1) {
        switch (choice[0]) {
            case '1':
                app->setState(currentState);
                break;
            case 'b':
                app->setState(backState);
                break;
            case 'q':
                app->setState(new MainMenuState());
                break;
            default:
                cout << "\033[31m";
                cout << "Invalid choice." << endl;
                cout << "\033[0m";
        }
    } else{
        cout << "\033[31m";
        cout << "Invalid input. Please enter a single character." << endl;
        cout << "\033[0m";
    }
}