#pragma once

#include <iostream>

namespace jnibridge::examples {

    class BaseClass {

        public:
            explicit BaseClass(){ std::cout << "ALLOC: BaseClass" << std::endl; }
            ~BaseClass() { std::cout << "Dealloc: BaseClass" << std::endl; }


    };

}