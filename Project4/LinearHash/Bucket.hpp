#ifndef _BUCKET_HPP_
#define _BUCKET_HPP_

#include <vector>
#include <string>
#include "constants.hpp"

class Bucket {
public:
  Bucket();
  ~Bucket();
  bool insert(std::string input);
  void print();

private:
  std::vector<std::string> allkeys;
  std::vector<std::string> keys;
  Bucket* overflowBucket;
};

#endif