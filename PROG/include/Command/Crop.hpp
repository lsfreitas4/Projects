#ifndef CROP_HPP
#define CROP_HPP

#include "Command.hpp"

namespace prog {
    class Crop : public Command {
    private:
        int x_;
        int y_;
        int w_;
        int h_;
    public:
        Crop(int x, int y, int w, int h);
        Image* apply(Image* img) override;
    };
}
#endif