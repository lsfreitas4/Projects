#ifndef SLIDE_HPP
#define SLIDE_HPP

#include "Command.hpp"

namespace prog {
    class Slide : public Command {
    private:
        int x_;
        int y_;
    public:
        Slide(int x, int y);
        Image* apply(Image* img) override;
    };
}
#endif