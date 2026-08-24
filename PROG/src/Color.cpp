#include "Color.hpp"
#include <iostream>

using std::istream;

namespace prog {
    Color::Color() : red_val(0), green_val(0), blue_val(0) {}

    Color::Color(const Color &other) : red_val(other.red_val), green_val(other.green_val), blue_val(other.blue_val) {}

    Color::Color(rgb_value r, rgb_value g, rgb_value b) : red_val(r), green_val(g), blue_val(b) {}

    rgb_value Color::red() const {
        return red_val;
    }

    rgb_value Color::green() const {
        return green_val;
    }

    rgb_value Color::blue() const {
        return blue_val;
    }

    rgb_value &Color::red() {
        return red_val;
    }

    rgb_value &Color::green() {
        return green_val;
    }

    rgb_value &Color::blue() {
        return blue_val;
    }
}

// Use to read color values from a script file.
istream &operator>>(istream &input, prog::Color &c) {
    int r, g, b;
    input >> r >> g >> b;
    c.red() = r;
    c.green() = g;
    c.blue() = b;
    return input;
}

std::ostream &operator<<(std::ostream &output, const prog::Color &c) {
    output << (int) c.red() << ":" << (int) c.green() << ":" << (int) c.blue();
    return output;
}
