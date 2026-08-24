#include "Command/Slide.hpp"
#include "Image.hpp"
#include "Color.hpp"
#include <vector>

using namespace std;

namespace prog {
    Slide::Slide(int x, int y) : Command("slide"), x_(x), y_(y) {}

    Image* Slide::apply(Image* img) {
        if(img == nullptr || (x_ == 0 && y_ == 0)) {
            return img;
        }

        int width= img->width();
        int height= img->height();

        vector<vector<Color>> original;
        for(int y= 0; y < height; y++) {
            vector<Color> row;
            for(int x= 0; x < width; x++) {
                row.push_back(img->at(x, y));
            }
            original.push_back(row);
        }

        for(int y= 0; y < height; y++) {
            for(int x= 0; x < width; x++) {
                int newX= (x + x_) % width;
                if(newX < 0) newX += width;
                int newY= (y + y_) % height;
                if(newY < 0) newY += height;
                img->at(newX, newY)= original[y][x];
            }
        }
        return img;
    }    
}