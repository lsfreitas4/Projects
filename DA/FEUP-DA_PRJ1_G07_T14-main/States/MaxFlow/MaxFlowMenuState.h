#ifndef WATER_SUPPLY_ANALYSIS_SYSTEM_MAXFLOWMENUSTATE_H
#define WATER_SUPPLY_ANALYSIS_SYSTEM_MAXFLOWMENUSTATE_H


#include "States/State.h"

/**
* @brief Class representing the Find Max Water Flow Menu state of the water supply analysis system..
*/

class MaxFlowMenuState : public State {
public:

    /**
    * @brief Default constructor for MaxFlowMenuState.
    *
    * @details This constructor initializes an instance of the MaxFlowMenuState class. It doesn't require any parameters,
    * as it represents the Find Max Water Flow Menu state of the application, allowing users to navigate to different
    * functionalities and sections of the program.
    */
    MaxFlowMenuState();

    /**
    * @brief Displays the Find Max Water Flow Menu options.
    *
    * @details This method prints the Find Max Water Flow Menu options to the console, allowing users to choose from different
    * functionalities. Users input a single character corresponding to their desired option (1-2 for sections, 'q' to exit).
    * The method provides a visual representation of the Find Max Water Flow Menu and prompts the user to enter their choice.
    */
    void display() const override;

    /**
    * @brief Handles user input for the Find Max Water Flow Menu.
    *
    * @details This method prompts the user to input a single character representing their choice in the Find Max Water Flow Menu.
    * It uses a switch statement to determine the action corresponding to the user's choice. If the input is valid, the
    * method transitions to the appropriate state based on the user's choice.
    * If the input is invalid, the method notifies the user and prompts them to try again. The 'q' option exits the program.
    *
    * @param app A pointer to the application instance.
    */
    void handleInput(App* app) override;
};


#endif //WATER_SUPPLY_ANALYSIS_SYSTEM_MAXFLOWMENUSTATE_H
