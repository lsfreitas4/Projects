#include "Command/RotateLeft.hpp"
#include "Image.hpp"

using namespace std;

namespace prog {
    RotateLeft::RotateLeft() : Command("rotate_left") {}

    Image* RotateLeft::apply(Image* img) {
        if (img == nullptr) {
            return nullptr;
        }

        int width= img->width();
        int height= img->height();
        
        Image* rotate= new Image(height, width);
        
        for (int y= 0; y < height; y++) {
            for (int x= 0; x < width; x++) {
                rotate->at(y, width - 1 - x) = img->at(x, y);
            }
        }
        
        delete img;
        return rotate;
    }
}