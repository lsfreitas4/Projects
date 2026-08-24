#ifndef ROTATE_LEFT_HPP
#define ROTATE_LEFT_HPP

#include "Command.hpp"

namespace prog {
    class RotateLeft : public Command {
    public:
        RotateLeft();
        Image* apply(Image* img) override;
    };
}
#endif