#ifndef H_MIRROR_HPP
#define H_MIRROR_HPP

#include "Command.hpp"

namespace prog {
    class HMirror : public Command {
    public:
        HMirror();
        Image* apply(Image* img) override;
    };
}
#endif