#include "Command/Resize.hpp"
#include "Image.hpp"
#include "Color.hpp"

using namespace std;

namespace prog {
    Resize::Resize(int x, int y, int width, int height) : Command("resize"), x_(x), y_(y), w_(width), h_(height) {}

    Image* Resize::apply(Image* img) {
        if(img == nullptr || (w_ == 0 && h_ == 0)) {
            return img;
        }

        int imgwidth= img->width();
        int imgheight= img->height();

        Color fill(255, 255, 255);

        Image* resizeimg= new Image(w_, h_, fill);

        int resizex= max(x_, 0);
        int resizey= max(y_, 0);
        int resizewidth= min(w_, imgwidth - resizex);
        int resizeheight= min(h_, imgheight - resizey);

        resizewidth= max(resizewidth, 0);
        resizeheight= max(resizeheight, 0);

        for (int y= 0; y < resizeheight; ++y) {
            for (int x= 0; x < resizewidth; ++x) {
                int dest_x= x;
                int dest_y= y;               
                if (resizex + x < imgwidth && resizey + y < imgheight) {
                    resizeimg->at(dest_x, dest_y)= img->at(resizex + x, resizey + y);
                }
            }
        }

        delete img;
        return resizeimg;
    }
}