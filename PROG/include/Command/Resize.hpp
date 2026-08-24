#ifndef RESIZE_HPP
#define RESIZE_HPP

#include "Command.hpp"

namespace prog {
    class Resize : public Command {
    private:
        int x_;
        int y_;
        int w_;
        int h_;
    public:
        Resize(int x, int y, int w, int h);
        Image* apply(Image* img) override;
    };
}
#endif