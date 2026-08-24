#ifndef __prog_Image_hpp__
#define __prog_Image_hpp__

#include "Color.hpp"
#include <vector>

using namespace std;

namespace prog {
    class Image {
    private:
        int width_val;
        int height_val;
        vector<vector<Color>> pixels_val;

    public:
        Image(int w, int h, const Color &fill = {255, 255, 255});

        ~Image();

        int width() const;

        int height() const;

        Color &at(int x, int y);

        const Color &at(int x, int y) const;
    };
}
#endif