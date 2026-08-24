#ifndef V_MIRROR_HPP
#define V_MIRROR_HPP

#include "Command.hpp"

namespace prog {
    class VMirror : public Command {
    public:
        VMirror();
        Image* apply(Image* img) override;
    };
}
#endif