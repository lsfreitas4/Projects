#ifndef ROTATE_RIGHT_HPP
#define ROTATE_RIGHT_HPP

#include "Command.hpp"

namespace prog {
    class RotateRight : public Command {
    public:
        RotateRight();
        Image* apply(Image* img) override;
    };
}
#endif