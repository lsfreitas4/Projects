#ifndef FILL_HPP
#define FILL_HPP

#include "Command.hpp"
#include "Color.hpp"

namespace prog {
    class Fill : public Command {
    private:
        int x_;
        int y_;
        int w_;
        int h_;
        Color color_;
    public:
        Fill(int x, int y, int w, int h, const Color& color);
        Image* apply(Image* img) override;
    };
}
#endif