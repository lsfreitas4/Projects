#include "Command/ScaleUp.hpp"
#include "Image.hpp"

using namespace std;

namespace prog {
    ScaleUp::ScaleUp(int x, int y) : Command("scaleup"), x_(x), y_(y) {}

    Image* ScaleUp::apply(Image* img) {
        if (img == nullptr || x_ <= 0 || y_ <= 0) {
            return img;
        }

        int imgwidth= img->width();
        int imgheight= img->height();
        int scalewidth= imgwidth * x_;
        int scaleheight= imgheight * y_;
        
        Image* scaleimg= new Image(scalewidth, scaleheight);

        for (int y= 0; y < imgheight; y++) {
            for (int x= 0; x < imgwidth; x++) {
                Color originalpixel= img->at(x, y);
                for (int w= 0; w < y_; w++) {
                    for (int z= 0; z < x_; z++) {
                        int scalex= x * x_ + z;
                        int scaley= y * y_ + w;
                        scaleimg->at(scalex, scaley) = originalpixel;
                    }
                }
            }
        }
        
        delete img;
        return scaleimg;
    }
}