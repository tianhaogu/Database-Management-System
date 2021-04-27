#include "Bucket.hpp"
#include <iostream>
#include <algorithm>

Bucket::Bucket() : overflowBucket(nullptr) {}

Bucket::~Bucket() {
  if (overflowBucket) {
    delete overflowBucket;
  }
}

// Insert the key into the bucket, or one of its overflow buckets.
// Return true if an overflow bucket was used
bool Bucket::insert(std::string input) {
  bucketkeys.push_back(input);
  if (overflowBucket == NULL) {
    if (keys.size() == MAX_BUCKET_SIZE) {
      overflowBucket = new Bucket;
      overflowBucket-> keys.push_back(input);
      return true;
    }
    else {
      keys.push_back(input);
      return false;
    }
  }
  else {
    overflowBucket-> insert(input);
    return true;
  }
}

const std::vector<std::string> Bucket::getBucketKeys() const {
  return bucketkeys;
}

// DO NOT MODIFY THIS FUNCTION!
// 
// Print all keys in this bucket and all of its overflow buckets.
// Keys are separated by spaces, and buckets are separated by '|'.
void Bucket::print() {
  std::cout << "| ";
  for (auto& key : keys) {
    std::cout << key << " ";
  }
  std::cout << "|";
  if (overflowBucket) {
    overflowBucket->print();
  }
}