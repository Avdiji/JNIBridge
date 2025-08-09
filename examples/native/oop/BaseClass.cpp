#pragma once

#include <iostream>

namespace jnibridge::examples {

    class BaseClass {

        public:
            explicit BaseClass() = default;

            void printSomething() { std::cout << "Something" << std::endl; }

            BaseClass* asPtr(BaseClass* newPtr) { return newPtr; }
    };

}