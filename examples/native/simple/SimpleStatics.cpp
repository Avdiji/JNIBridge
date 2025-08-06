#pragma once

#include <iostream>
#include <string>

namespace jnibridge::examples {

    void voidFunction() { std::cout << "Hello from C++ " << std::endl; }

    int incrementInt(const int& value) { return value + 1; }
    short incrementShort(const short& value) { return value + 1; }
    long incrementLong(const long& value) { return value + 1; }

    float incrementFloat(const float& value) { return value + 1.0f; }
    double incrementDouble(const double& value) { return value + 1.0; }

    bool isTrue(bool value) { return value; }

    char getNextChar(const char& value) { return value + 1; }

    std::string getFunnyString(const std::string& value) { return "Funny " + value; }
}