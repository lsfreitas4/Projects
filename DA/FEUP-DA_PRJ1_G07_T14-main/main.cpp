#include "App.h"
#include "Data.h"

using namespace std;

int main()
{
    App* app = App::getInstance();

    // Display the main menu
    while(app->getState() != nullptr) {
        app->display();
        app->handleInput();
    }

    // Cleanup
    delete app;

    return 0;
}