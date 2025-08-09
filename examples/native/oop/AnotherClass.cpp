#pragma once

#include <iostream>

namespace jnibridge::examples {

    class AnotherClass {

        public:
            explicit AnotherClass(){ std::cout << "ALLOC: AnotherClass" << std::endl; }
            ~AnotherClass() { std::cout << "Dealloc: AnotherClass" << std::endl; }


    };

}