#include "ScrimParser.hpp"

#include "Command/Blank.hpp"
#include "Command/Save.hpp"
#include "Command/Open.hpp"
#include "Logger.hpp"
#include "Command/Invert.hpp"
#include "Command/ToGrayScale.hpp"
#include "Command/Replace.hpp"
#include "Command/Fill.hpp"
#include "Command/HMirror.hpp"
#include "Command/VMirror.hpp"
#include "Command/Add.hpp"
#include "Command/Move.hpp"
#include "Command/Slide.hpp"
#include "Command/Crop.hpp"
#include "Command/Resize.hpp"
#include "Command/RotateLeft.hpp"
#include "Command/RotateRight.hpp"
#include "Command/ScaleUp.hpp"


#include <algorithm>
#include <fstream>
#include <string>
#include <vector>
#include <cstdlib>

using std::ifstream;
using std::istream;
using std::string;
using std::vector;

namespace prog {
    ScrimParser::ScrimParser() {
    };

    ScrimParser::~ScrimParser() {
    };


    Scrim *ScrimParser::parseScrim(std::istream &input) {
        // Create vector where commands will be stored
        vector<Command *> commands;

        // Parse commands while there is input in the stream
        string command_name;
        while (input >> command_name) {
            Command *command = parse_command(command_name, input);

            if (command == nullptr) {
                // Deallocate already allocated commands
                for (Command *allocated_command: commands) {
                    delete allocated_command;
                }


                *Logger::err() << "Error while parsing command\n";
                return nullptr;
            }

            commands.push_back(command);
        }

        // Create a new image pipeline
        return new Scrim(commands);
    }


    Scrim *ScrimParser::parseScrim(const std::string &filename) {
        ifstream in(filename);
        return parseScrim(in);
    }

    Command *ScrimParser::parse_command(string command_name, istream &input) {
        if (command_name == "blank") {
            // Read information for Blank command
            int w, h;
            Color fill;
            input >> w >> h >> fill;
            return new command::Blank(w, h, fill);
        }

        if (command_name == "save") {
            // Read information for Save command
            string filename;
            input >> filename;
            return new command::Save(filename);
        }

        if (command_name == "open") {
            string filename;
            input >> filename;
            return new command::Open(filename);
        }

        if (command_name == "invert") {
            return new prog::Invert();
        }

        if (command_name == "to_gray_scale") {
            return new prog::ToGrayScale();
        }

        if (command_name == "replace") {
            int r1, g1, b1, r2, g2, b2;
            if (!(input >> r1 >> g1 >> b1 >> r2 >> g2 >> b2)) {
                *Logger::err() << "Invalid arguments for replace command\n";
                return nullptr;
            }
            return new prog::Replace(Color(r1, g1, b1), Color(r2, g2, b2));
        }

        if (command_name == "fill") {
            int x, y, w, h, r, g, b;
            if (!(input >> x >> y >> w >> h >> r >> g >> b)) {
                *Logger::err() << "Invalid arguments for fill command\n";
                return nullptr;
            }
            return new prog::Fill(x, y, w, h, Color(r, g, b));
        }

        if (command_name == "h_mirror") {
            return new prog::HMirror();
        }

        if (command_name == "v_mirror") {
            return new prog::VMirror();
        }

        if (command_name == "add") {
            std::string filename;
            int r, g, b, x, y;
            if (!(input >> filename >> r >> g >> b >> x >> y)) {
                *Logger::err() << "Invalid arguments for add command\n";
                return nullptr;
            }
            return new prog::Add(filename, Color(r,g,b), x, y);
        }

        if (command_name == "move") {
            int x, y;
            if (!(input >> x >> y)) {
                *Logger::err() << "Invalid arguments for move command\n";
                return nullptr;
            }
            if (x < 0 || y < 0) {
                *Logger::err() << "Move values must be positive\n";
                return nullptr;
            }
            return new prog::Move(x, y);
        }

        if (command_name == "slide") {
            int x, y;
            if (!(input >> x >> y)) {
                *Logger::err() << "Invalid arguments for slide command\n";
                return nullptr;
            }
            return new prog::Slide(x, y);
        }

        if (command_name == "crop") {
            int x, y, w, h;
            if (!(input >> x >> y >> w >> h)) {
                *Logger::err() << "Invalid arguments for crop command\n";
                return nullptr;
            }
            return new prog::Crop(x, y, w, h);
        }

        if (command_name == "resize") {
            int x, y, w, h;
            if (!(input >> x >> y >> w >> h)) {
                *Logger::err() << "Invalid arguments for resize command\n";
                return nullptr;
            }
            return new prog::Resize(x, y, w, h);
        }

        if (command_name == "rotate_left") {
            return new prog::RotateLeft();
        }

        if (command_name == "rotate_right") {
            return new prog::RotateRight();
        }

        if (command_name == "scaleup") {
            int x, y;
            if (!(input >> x >> y)) {
                *Logger::err() << "Invalid arguments for scaleup command\n";
                return nullptr;
            }
            if (x <= 0 || y <= 0) {
                *Logger::err() << "Scale factors must be positive\n";
                return nullptr;
            }
            return new prog::ScaleUp(x, y);
        }

        *Logger::err() << "Command not recognized: '" + command_name + "'\n";
        return nullptr;
    }
}
