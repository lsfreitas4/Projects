#include "GetFilesPathState.h"
#include "TryAgainState.h"
#include "DataLoadError.h"

GetFilesPathState::GetFilesPathState(State* backState, function<void(App*)> nextStateCallback)
        : backState(backState), nextStateCallback(nextStateCallback) {}

void GetFilesPathState::display() const {
    cout << "Insert path to the files (Ex: \"./dataset/Project1DataSetSmall\"): ";
}

void GetFilesPathState::handleInput(App* app) {
    string path;
    cin.ignore();
    getline(cin, path);
    filesystem::path dir_path;

    try {

        if (path.front() == '.') {
            dir_path = filesystem::path(filesystem::current_path() / ("." + path));
        }
        else {
            dir_path = filesystem::path(path);
        }

        if (filesystem::exists(dir_path) && filesystem::is_directory(dir_path)) {
            app->setData(dir_path);
            nextStateCallback(app);
        } else {
            throw invalid_argument("Invalid path. Please enter a valid directory path.");
        }
    } catch (const DataLoadError& e) {

        cout << "\033[31m";
        cout << e.what() << endl;
        cout << "\033[0m";
        app->setState(new TryAgainState(backState, this));

    } catch (const invalid_argument& e) {

        cout << "\033[31m";
        cout << e.what() << endl;
        cout << "\033[0m";
        app->setState(new TryAgainState(backState, this));

    }
}