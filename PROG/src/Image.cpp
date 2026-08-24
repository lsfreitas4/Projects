#include "Image.hpp"

using namespace std;

namespace prog {
    Image::Image(int w, int h, const Color &fill) : width_val(w), height_val(h), pixels_val(h, vector<Color>(w, fill)) {}

    Image::~Image() {
    }

    int Image::width() const {
        return width_val;
    }

    int Image::height() const {
        return height_val;
    }

    Color &Image::at(int x, int y) {
        return pixels_val[y][x];
    }

    const Color &Image::at(int x, int y) const {
        return pixels_val[y][x];
    }
}