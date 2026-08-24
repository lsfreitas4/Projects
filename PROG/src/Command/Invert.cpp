#include "Command/Invert.hpp"
#include "Image.hpp"
#include "Color.hpp"

namespace prog {

    Invert::Invert() : Command("invert") {}

    Image* Invert::apply(Image* img) {
        if (img == nullptr) return nullptr;

        for (int y = 0; y < img->height(); y++) {
            for (int x = 0; x < img->width(); x++) {
                Color original = img->at(x, y);
                Color inverted(255 - original.red(), 255 - original.green(), 255 - original.blue());
                img->at(x, y) = inverted;
            }
        }

        return img;
    }

} // namespace prog