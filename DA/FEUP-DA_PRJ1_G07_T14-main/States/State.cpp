#include <iostream>
#include <limits>
#include "State.h"

void State::PressEnterToContinue(int numPresses) const {
    cout << "Press ENTER to continue... ";

    for (int i = 0; i < numPresses; ++i)
        cin.ignore(numeric_limits<streamsize>::max(), '\n');
}