#include <iostream>
#include <string>
#include "LinHashIdx.hpp"

int main() {
  LinHashIdx idx;
  std::string input;
  while (std::cin >> input) {
    if (input == "print") {
      idx.print();
    }
    else if (input == "exit") {
      break;
    }
    else {
      idx.insert(input);
    }
  }
}