#include "Command/RotateRight.hpp"
#include "Image.hpp"

using namespace std;

namespace prog {
    RotateRight::RotateRight() : Command("rotate_right") {}

    Image* RotateRight::apply(Image* img) {
        if (img == nullptr) {
            return nullptr;
        }

        int width= img->width();
        int height= img->height();
        
        Image* rotate= new Image(height, width);
        
        for (int y= 0; y < height; y++) {
            for (int x= 0; x < width; x++) {
                rotate->at(height - 1 - y, x) = img->at(x, y);
            }
        }
        
        delete img;
        return rotate;
    }
}