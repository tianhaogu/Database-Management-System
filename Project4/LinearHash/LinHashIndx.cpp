#include "LinHashIdx.hpp"
#include <functional>
#include <math.h>
#include <cassert>
#include <bitset>
#include <iostream>
#include <algorithm>
using namespace std;

// You should not need to modify this function.
// If USING_HASH is true, then hash the input using std::hash.
// Otherwise, directly convert the given string to an integer.
// If conversion fails, then prints an error message and throws an exception
static int custom_hash (std::string input) {
  if (USING_HASH) {
    return std::hash<std::string>{}(input);
  }
  else {
    int value = 0;
    try {
      value = std::stoi(input);
    }
    catch (std::invalid_argument) {
      std::cerr << "Error: [" << input << "] could not be converted to an integer" << std::endl;
      throw;
    }
    catch (std::out_of_range) {
      std::cerr << "Error: [" << input << "] is out of range for the integer type" << std::endl;
      throw;
    }
    if (std::to_string(value) != input) {
      std::cerr << "Error: [" << input << "] could not be converted to an integer" << std::endl;
      throw std::invalid_argument("string2int(\"" + input + "\") failed");
    }
    return value;
  }
}

LinHashIdx::LinHashIdx() : next(0), level(0) {
  // Check that INITIAL_NUM_BUCKETS is a postive power of 2
  assert(INITIAL_NUM_BUCKETS && (INITIAL_NUM_BUCKETS & (INITIAL_NUM_BUCKETS - 1)) == 0);
  // Check that MAX_BUCKET_SIZE is nonzero
  assert(MAX_BUCKET_SIZE);
  // Create all initial buckets
  directory.resize(INITIAL_NUM_BUCKETS);
  for (auto& bucketPtr : directory) {
    bucketPtr = new Bucket;
  }
}

// Insert the input into the correct bucket, and split the "next" bucket if the insert causes an overflow
// Only insert if the input does not already exist in the index
void LinHashIdx::insert(std::string input) {
  if (this-> contains(input) == true) {
    return;
  }
  int hashValue = custom_hash(input);
  int bucketId = hashValue % (INITIAL_NUM_BUCKETS * pow(2, level));
  allkeys.push_back(input);
  if (bucketId < next) {
    bucketId = hashValue % (INITIAL_NUM_BUCKETS * pow(2, level + 1));
  }
  bool whetherTrigger = directory[bucketId]-> insert(input);
  if (whetherTrigger == true) { //split the "next" bucket
    vector<string> nextKeys = directory[bucketId]-> getBucketKeys();
    delete directory[bucketId];
    Bucket* bottomBucket = new Bucket;
    directory.push_back(bottomBucket);
    Bucket* currentBucket = new Bucket;
    directory[bucketId] = currentBucket;
    for (int i = 0; i < nextKeys.size(); ++i) {
      hashValue = custom_hash(nextKeys[i]);
      int newBucketId = hashValue % (INITIAL_NUM_BUCKETS * pow(2, level + 1));
      directory[newBucketId]-> insert(nextKeys);
    }
    if (next == INITIAL_NUM_BUCKETS * pow(2, level) - 1) {
      next = 0;
      level += 1;
    }
    else {
      next += 1;
    }
  }
}

// Search the index for the given value, and return true if it's found
bool LinHashIdx::contains(std::string input) {
  // Insert your code here
  vector<string>::iterator itr = find(allkeys.begin(), allkeys.end(), input);
  if (itr == allkeys.end()) {
    return false;
  }
  return true;
}

// DO NOT MODIFY THIS FUNCTION!
// Print the entire index.
// Prints the current level, each directory entry, and the keys in each bucket.
// Also points to the "next" bucket
void LinHashIdx::print() {
  // The number of characters used to display the binary value of each bucket index
  size_t levelSize = level + log2(INITIAL_NUM_BUCKETS) + 1;
  std::string blankSpace(levelSize, ' ');
  std::cout << "Level: " << level << std::endl;
  for (unsigned int i = 0; i < directory.size(); ++i) {
    // The "next" bucket gets a pointer
    std::string nextStr(4, ' ');
    if (i == next) {
      nextStr = "--> ";
    }
    // The high-level representation of the bucket index uses 1 more bit than the low level representation
    std::bitset<BITSET_LEN> bitHash = i;
    std::string highLevelStr, lowLevelStr;
    highLevelStr = bitHash.to_string().substr(BITSET_LEN - levelSize, levelSize);
    highLevelStr += " ";
    lowLevelStr = highLevelStr.substr(1, levelSize);
    // If it's the beginning of a level, only the low level bucket index is displayed
    if (next == 0) {
      highLevelStr = "";
    }
    // Buckets above the initial set do not have a low level representation
    if (i >= INITIAL_NUM_BUCKETS * pow(2, level)) {
      lowLevelStr = blankSpace;
    }
    std::cout << nextStr << highLevelStr << lowLevelStr;
    directory[i]->print();
    std::cout << std::endl;
  }
  std::cout << std::endl;
}