#pragma once

#include <iostream>
#include <string>
#include <string_view>

namespace jnibridge::examples {

    inline void voidFunction() { std::cout << "Hello from C++ " << std::endl; }

    inline int incrementInt(const int& value) { return value + 1; }
    inline short incrementShort(const short& value) { return value + 1; }
    inline long incrementLong(const long& value) { return value + 1; }

    inline float incrementFloat(const float& value) { return value + 1.0f; }
    inline double incrementDouble(const double& value) { return value + 1.0; }

    inline bool isTrue(bool value) { return value; }

    inline char getNextChar(const char& value) { return value + 1; }

    inline std::string getFunnyString(const std::string& value) { return "Funny " + value; }

    inline std::string_view getStringView(const std::string_view value) {
        return value;
    }
}