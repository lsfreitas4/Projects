#ifndef MOVE_HPP
#define MOVE_HPP

#include "Command.hpp"

namespace prog {
    class Move : public Command {
    private:
        int x_;
        int y_;
    public:
        Move(int x, int y);
        Image* apply(Image* img) override;
    };
}
#endif