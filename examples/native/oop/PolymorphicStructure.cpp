#pragma once

#include <iostream>

namespace jnibridge::examples {

    enum Color {
        Red,
        Green,
        Blue
    };

    class BaseClass {
    public:
            explicit BaseClass(){ std::cout << "ALLOC: BaseClass" << std::endl; }
            ~BaseClass() { std::cout << "Dealloc: BaseClass" << std::endl; }

            virtual std::string getString() { return "BaseClass-String"; }

            void throwNestedError() {
                try {
                    throw std::runtime_error("inner error");
                } catch (...) {
                    std::throw_with_nested(std::logic_error("outer error"));
                }
            }

            const BaseClass& getThisRef() {
                return *this;
            }

            static void printString(const std::shared_ptr<BaseClass> &other) {
                std::cout << other->getString() << std::endl;
            }

            Color getColor() { return _color; }
            void setColor(const Color &color) { _color = color; }

    private:
        Color _color = static_cast<Color>(0);

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

    };

}