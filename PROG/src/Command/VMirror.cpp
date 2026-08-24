#include "Command/VMirror.hpp"
#include "Image.hpp"
#include <algorithm>

namespace prog {

    VMirror::VMirror() : Command("v_mirror") {}

    Image* VMirror::apply(Image* img) {
        if (img == nullptr) {
            return nullptr;
        }

        int width = img->width();
        int height = img->height();
        int half_height = height / 2;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < half_height; y++) {
                int mirror_y = height - 1 - y;
                std::swap(img->at(x, y), img->at(x, mirror_y));
            }
        }

        return img;
    }

}