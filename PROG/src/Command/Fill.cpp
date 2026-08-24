#include "Command/Fill.hpp"
#include "Image.hpp"

namespace prog {

    Fill::Fill(int x, int y, int w, int h, const Color& color) :
        Command("fill"), x_(x), y_(y), w_(w), h_(h), color_(color) {}

    Image* Fill::apply(Image* img) {
        if (img == nullptr) {
            return nullptr;
        }

        int img_width = img->width();
        int img_height = img->height();

        int x_end = std::min(x_ + w_, img_width);
        int y_end = std::min(y_ + h_, img_height);

        if (x_ < img_width && y_ < img_height) {
            for (int y = std::max(y_, 0); y < y_end; y++) {
                for (int x = std::max(x_, 0); x < x_end; x++) {
                    img->at(x, y) = color_;
                }
            }
        }

        return img;
    }

}