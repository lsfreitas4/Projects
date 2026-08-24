#ifndef WATER_SUPPLY_ANALYSIS_SYSTEM_TRYAGAINSTATE_H
#define WATER_SUPPLY_ANALYSIS_SYSTEM_TRYAGAINSTATE_H


#include <functional>
#include "States/State.h"

/**
* @brief Class representing a state that prompts the user to try an action again.
*/

class TryAgainState : public State {
private:
private:
    State *backState;
    State *currentState;
public:

    /**
    * @brief Constructs a TryAgainState instance.
    *
    * @details This constructor initializes a TryAgainState object with the provided backState and currentState pointers.
    * The backState represents the state to which the application should transition in case of a retry, and currentState
    * represents the state where the retry was triggered.
    *
    * @param backState A pointer to the state to which the application should transition in case of a retry.
    * @param currentState A pointer to the state where the retry was triggered.
    */
    TryAgainState(State *backState, State *currentState);

    /**
    * @brief Displays the options for retrying or navigating back in a TryAgainState.
    *
    * @details This method prints retry and navigation options to the console, allowing users to choose between trying
    * again, going back to the previous state, or returning to the main menu. The displayed menu includes corresponding
    * numeric options, and users can enter their choice.
    */
    void display() const override;

    /**
    * @brief Handles user input in a TryAgainState.
    *
    * @details This method reads the user's input from the console and processes it based on the chosen option. Users can
    * input a single character corresponding to the desired action (1 for Try Again, 'b' for Go Back, 'q' for Main Menu).
    * The method then transitions the application state accordingly, retrying the current state, going back to the previous
    * state, or returning to the main menu. It provides error messages for invalid inputs.
    *
    * @param app A pointer to the application instance.
    */
    void handleInput(App* app) override;
};


#endif //WATER_SUPPLY_ANALYSIS_SYSTEM_TRYAGAINSTATE_H
