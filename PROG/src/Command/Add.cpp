#include "Command/Add.hpp"
#include "Command/Open.hpp"
#include "Image.hpp"

namespace prog {

    Add::Add(const std::string& filename, const Color& neutral, int x, int y) :
        Command("add"), filename_(filename), neutral_(neutral), x_(x), y_(y) {}

    Image* Add::apply(Image* img) {
        if (img == nullptr) {
            return nullptr;
        }

        command::Open opener(filename_);
        Image* toAdd = opener.apply(nullptr);
        if (toAdd == nullptr) {
            return img;
        }

        int imgWidth = img->width();
        int imgHeight = img->height();
        int addWidth = toAdd->width();
        int addHeight = toAdd->height();

        for (int ay = 0; ay < addHeight; ay++) {
            int targetY = y_ + ay;
            if (targetY < 0 || targetY >= imgHeight) continue;

            for (int ax = 0; ax < addWidth; ax++) {
                int targetX = x_ + ax;
                if (targetX < 0 || targetX >= imgWidth) continue;

                Color addColor = toAdd->at(ax, ay);
                if (!(addColor.red() == neutral_.red() &&
                      addColor.green() == neutral_.green() &&
                      addColor.blue() == neutral_.blue())) {
                    img->at(targetX, targetY) = addColor;
                      }
            }
        }

        delete toAdd;
        return img;
    }

}