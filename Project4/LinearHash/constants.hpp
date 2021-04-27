#ifndef _CONSTANTS_HPP_
#define _CONSTANTS_HPP_

// Used for printing- DO NOT MODIFY
constexpr unsigned int BITSET_LEN = 8;

// If true, the user can input arbitrary strings, which will be hashed with std::hash.
// If false, the user can only input integers, which will not be hashed.
// You do not need to modify this.
constexpr bool USING_HASH = false;

// The number of buckets that the index starts with. This should always be a positive power of 2.
constexpr unsigned int INITIAL_NUM_BUCKETS = 4;

// The maximum number of elements that can be in a single bucket.
constexpr unsigned int MAX_BUCKET_SIZE = 2;

#endif