#include "Command/Replace.hpp"
#include "Image.hpp"

namespace prog {

    Replace::Replace(const Color& from, const Color& to) :
        Command("replace"), from_(from), to_(to) {}

    Image* Replace::apply(Image* img) {
        if (img == nullptr) {
            return nullptr;
        }

        for (int y = 0; y < img->height(); y++) {
            for (int x = 0; x < img->width(); x++) {
                Color& current = img->at(x, y);
                if (current.red() == from_.red() &&
                    current.green() == from_.green() &&
                    current.blue() == from_.blue()) {
                    current = to_;
                    }
            }
        }

        return img;
    }

}