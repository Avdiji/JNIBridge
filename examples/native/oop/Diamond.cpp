#include <iostream>
using namespace std;

// Base class
class Animal {
public:
    Animal() {
        cout << "Animal constructor\n";
    }

    void eat() const {
        cout << "Animal eats\n";
    }
};

// Intermediate classes (virtual inheritance)
class Mammal : virtual public Animal {
public:
    Mammal() {
        cout << "Mammal constructor\n";
    }
};

class Bird : virtual public Animal {
public:
    Bird() {
        cout << "Bird constructor\n";
    }
};

// Derived class
class Bat : public Mammal, public Bird {
public:
    Bat() {
        cout << "Bat constructor\n";
    }
};