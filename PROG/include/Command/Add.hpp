#ifndef ADD_HPP
#define ADD_HPP

#include "Command.hpp"
#include "Color.hpp"

namespace prog {
    class Add : public Command {
    private:
        std::string filename_;
        Color neutral_;
        int x_;
        int y_;
    public:
        Add(const std::string& filename, const Color& neutral, int x, int y);
        Image* apply(Image* img) override;
    };
}
#endif