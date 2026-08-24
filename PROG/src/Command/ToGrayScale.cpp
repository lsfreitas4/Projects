#include "Command/ToGrayScale.hpp"
#include "Image.hpp"
#include "Color.hpp"

namespace prog {

    ToGrayScale::ToGrayScale() : Command("to_gray_scale") {}

    Image* ToGrayScale::apply(Image* img) {
        if (img == nullptr) return nullptr;

        for (int y = 0; y < img->height(); y++) {
            for (int x = 0; x < img->width(); x++) {
                Color original = img->at(x, y);
                int gray_value = (original.red() + original.green() + original.blue()) / 3;
                Color gray(gray_value, gray_value, gray_value);
                img->at(x, y) = gray;
            }
        }

        return img;
    }

}