#include "Command/HMirror.hpp"
#include "Image.hpp"
#include <algorithm> // For std::swap

namespace prog {

    HMirror::HMirror() : Command("h_mirror") {}

    Image* HMirror::apply(Image* img) {
        if (img == nullptr) {
            return nullptr;
        }

        int width = img->width();
        int height = img->height();
        int half_width = width / 2;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < half_width; x++) {
                int mirror_x = width - 1 - x;
                std::swap(img->at(x, y), img->at(mirror_x, y));
            }
        }

        return img;
    }

}