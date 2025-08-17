#pragma once

#include <iostream>

namespace jnibridge::examples {

    class BaseClass {
    public:
            explicit BaseClass(){ std::cout << "ALLOC: BaseClass" << std::endl; }
            ~BaseClass() { std::cout << "Dealloc: BaseClass" << std::endl; }

            virtual std::string getString() { return "BaseClass-String"; }
    };


    class A : public BaseClass {
    public:
        explicit A() { std::cout << "ALLOC: A" << std::endl; }
        ~A() { std::cout << "DEALLOC: A" << std::endl; }

        std::string getString() override { return "A-String"; }
    };


    class B : public A {
    public:
        explicit B() { std::cout << "ALLOC: B" << std::endl; }
        ~B() { std::cout << "DEALLOC: B" << std::endl; }

        std::string getString() override { return "B-String"; }

        B* getThisInstance() { return this; }
    };

}