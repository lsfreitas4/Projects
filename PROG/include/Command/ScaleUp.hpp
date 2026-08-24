#ifndef SCALE_UP_HPP
#define SCALE_UP_HPP

#include "Command.hpp"

namespace prog {
    class ScaleUp : public Command {
    private:
        int x_;
        int y_;
    public:
        ScaleUp(int x, int y);
        Image* apply(Image* img) override;
    };
}
#endif