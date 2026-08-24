#ifndef TO_GRAY_SCALE_HPP
#define TO_GRAY_SCALE_HPP

#include "Command.hpp"
#include "Image.hpp"

namespace prog {
    class ToGrayScale : public Command {
    public:
        ToGrayScale();
        Image* apply(Image* img) override;
    };
}

#endif