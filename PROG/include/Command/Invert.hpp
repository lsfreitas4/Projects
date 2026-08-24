#ifndef INVERT_HPP
#define INVERT_HPP

#include "Command.hpp"
#include "Image.hpp"

namespace prog {
    class Invert : public Command {
    public:
        Invert();
        Image* apply(Image* img) override;
    };
}

#endif