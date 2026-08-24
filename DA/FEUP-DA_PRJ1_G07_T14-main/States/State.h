#ifndef WATER_SUPPLY_ANALYSIS_SYSTEM_STATE_H
#define WATER_SUPPLY_ANALYSIS_SYSTEM_STATE_H


#include "App.h"

/**
* @brief Abstract base class representing an app state within the water supply analysis system.
*/

class State {
public:

    /**
    * @brief Pure virtual function to display the state-specific menu or information.
    *
    * @details This function is declared as virtual and pure in the base class, indicating that it must be implemented
    * by derived classes. Each derived class should provide its own implementation to display the state-specific menu
    * or information when called. The 'const' qualifier indicates that the function does not modify the state of the object.
    * The '= 0' signifies that this is a pure virtual function, and any class inheriting from this base class must
    * provide an implementation for this function.
    */
    virtual void display() const = 0;

    /**
    * @brief Pure virtual function to handle user input specific to the state.
    *
    * @details This function is declared as virtual and pure in the base class, indicating that it must be implemented
    * by derived classes. Each derived class should provide its own implementation to handle user input specific to the state
    * when called. The 'app' parameter represents the application context and can be used to access and manipulate
    * application data or state. The '= 0' signifies that this is a pure virtual function, and any class inheriting
    * from this base class must provide an implementation for this function.
    *
    * @param app Pointer to the application context, allowing access to application data and state.
    */
    virtual void handleInput(App* app) = 0;

    /**
    * @brief Waits for user to press ENTER to continue execution.
    *
    * @details This function displays a prompt instructing the user to press ENTER to continue and waits until the user
    * presses the ENTER key. This function uses 'cin.ignore()' to clear the input buffer, allowing the user to
    * press ENTER without any unintended effects on subsequent input operations.
    *
    * @param numPresses Number of times the user must press ENTER. Defaults to 2 if not specified.
    */
    void PressEnterToContinue(int numPresses = 2) const;
};


#endif //WATER_SUPPLY_ANALYSIS_SYSTEM_STATE_H
