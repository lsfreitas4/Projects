#ifndef REPLACE_HPP
#define REPLACE_HPP

#include "Command.hpp"
#include "Color.hpp"

namespace prog {
    class Replace : public Command {
    private:
        Color from_;
        Color to_;
    public:
        Replace(const Color& from, const Color& to);
        Image* apply(Image* img) override;
    };
}
#endif