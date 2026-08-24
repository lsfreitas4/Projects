#include "Command/Crop.hpp"
#include "Image.hpp"
#include "Color.hpp"

using namespace std;

namespace prog {
    Crop::Crop(int x, int y, int width, int height) : Command("crop"), x_(x), y_(y), w_(width), h_(height) {}

    Image* Crop::apply(Image* img) {
        if(img == nullptr || (w_ == 0 && h_ == 0)) {
            return img;
        }

        int imgwidth= img->width();
        int imgheight= img->height();

        int cropx= max(x_, 0);
        int cropy= max(y_, 0);
        int cropwidth= min(w_, imgwidth - cropx);
        int cropheight= min(h_, imgheight - cropy);

        cropwidth= max(cropwidth, 0);
        cropheight= max(cropheight, 0);

        if (cropwidth <= 0 || cropheight <= 0) {
            delete img;
            return new Image(0, 0);
        }

        Image* cropimg = new Image(cropwidth, cropheight);

        for (int y= 0; y < cropheight; ++y) {
            for (int x= 0; x < cropwidth; ++x) {
                cropimg->at(x, y)= img->at(cropx + x, cropy + y);
            }
        }
        delete img;
        return cropimg;
    }
}