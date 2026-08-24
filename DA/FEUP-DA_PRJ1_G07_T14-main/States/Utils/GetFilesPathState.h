#ifndef WATER_SUPPLY_ANALYSIS_SYSTEM_GETFILESPATHSTATE_H
#define WATER_SUPPLY_ANALYSIS_SYSTEM_GETFILESPATHSTATE_H


#include <functional>
#include "States/State.h"

/**
* @brief Class that represents a state for obtaining the network files path.
*/

class GetFilesPathState : public State {
private:
    State* backState;
    function<void(App*)> nextStateCallback;
public:

    /**
    * @brief Constructs an instance of GetFilesPathState with specified back state and callback function.
    *
    * @details This constructor initializes an instance of the GetFilesPathState class with the given back state and
    * a callback function for transitioning to the next state. The back state represents the state to which the
    * application should return when the user chooses to go back from the current state. The callback function
    * specifies the action to be performed in the next state, usually involving capturing a top number from user
    * input and proceeding to the next state with the captured information.
    *
    * @param backState A pointer to the state to which the application should return when the user chooses to go back.
    * @param nextStateCallback A function defining the action to be performed in the next state, usually involving a top number.
    */
    GetFilesPathState(State* backState, function<void(App*)> nextStateCallback);

    /**
    * @brief Displays a prompt for inserting the path of the network csv files.
    *
    * @details This method prints a prompt to the console, asking the user to insert the path of the network csv files.
    * The displayed prompt guides the user on the expected format for entering the path of the network csv files. The user is
    * expected to input a valid path representing the desired path of the network csv files.
    */
    void display() const override;

    /**
    * @brief Handles user input for obtaining the path of the network csv files.
    *
    * @details This method reads a line of input from the console, representing the desired path of the network csv files for a specific operation.
    * The method validates the entered input, ensuring it is a valid path.
    * If the input is valid, the callback function is invoked with the captured number of top elements, and the application transitions to the next
    * state accordingly. If the input is invalid, the user is prompted with an error message, and the state transitions to a "Try Again" state,
    * allowing the user to make another attempt.
    *
    * @param app A pointer to the application instance.
    */
    void handleInput(App* app) override;
};


#endif //WATER_SUPPLY_ANALYSIS_SYSTEM_GETFILESPATHSTATE_H
