#include "Command/Move.hpp"
#include "Image.hpp"
#include "Color.hpp"
#include <vector>

namespace prog {

    Move::Move(int x, int y) : Command("move"), x_(x), y_(y) {}

    Image* Move::apply(Image* img) {
        if (img == nullptr || (x_ == 0 && y_ == 0)) {
            return img;
        }

        int width = img->width();
        int height = img->height();

        std::vector<std::vector<Color>> original;
        for (int y = 0; y < height; y++) {
            std::vector<Color> row;
            for (int x = 0; x < width; x++) {
                row.push_back(img->at(x, y));
            }
            original.push_back(row);
        }

        Color fill(255, 255, 255);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                img->at(x, y) = fill;
            }
        }

        for (int y = 0; y < height; y++) {
            int newY = y + y_;
            if (newY < 0 || newY >= height) continue;

            for (int x = 0; x < width; x++) {
                int newX = x + x_;
                if (newX < 0 || newX >= width) continue;

                img->at(newX, newY) = original[y][x];
            }
        }

        return img;
    }

}