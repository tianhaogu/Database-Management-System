#ifndef _LINHASHIDX_HPP_
#define _LINHASHIDX_HPP_

#include <vector>
#include "Bucket.hpp"
#include "constants.hpp"

class LinHashIdx {
public:
  LinHashIdx();
  void insert(std::string input);
  bool contains(std::string input);
  void print();

private:
  std::vector<std::string> allkeys;
  std::vector<Bucket*> directory;
  unsigned int next;
  unsigned int level;
};

#endif