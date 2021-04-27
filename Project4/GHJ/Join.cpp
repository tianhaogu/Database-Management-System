#include "Join.hpp"
#include <functional>

vector<unsigned int> get_smaller_rel(Bucket& buc) {
    return buc.get_left_rel().size() <= buc.get_right_rel().size() ? buc.get_left_rel() : buc.get_right_rel();
}

vector<unsigned int> get_larger_rel(Bucket& buc) {
    return buc.get_left_rel().size() > buc.get_right_rel().size() ? buc.get_left_rel() : buc.get_right_rel();
}

vector<Bucket> partition(Disk* disk, Mem* mem, pair<unsigned int, unsigned int> left_rel,
  pair<unsigned int, unsigned int> right_rel) {
  unsigned int hashOutNum = MEM_SIZE_IN_PAGE - 1;
  Bucket newBucket(disk);
  vector<Bucket> buckVec(hashOutNum, newBucket);
  unsigned int memInputId = MEM_SIZE_IN_PAGE - 1;
  for (unsigned int i = left_rel.first; i < left_rel.second; ++i) {
    mem-> loadFromDisk(disk, i, memInputId);
    Page* bufferInput = mem-> mem_page(memInputId);
    for (unsigned int j = 0; j < bufferInput->size(); ++j) {
      Record curRecord(bufferInput-> get_record(j));
      unsigned int hash1Val = curRecord.partition_hash();
      unsigned int bufferId = hash1Val % hashOutNum;
      Page* memHash = mem-> mem_page(bufferId);
      memHash-> loadRecord(curRecord);
      if (memHash-> size() >= RECORDS_PER_PAGE) {
        unsigned int newDiskId0 = mem-> flushToDisk(disk, bufferId);
        buckVec[bufferId].add_left_rel_page(newDiskId0);
      }
    }
  }
  for (unsigned int i = 0; i < hashOutNum; ++i) {
    if (mem->mem_page(i)-> size() != 0) {
      unsigned int newDiskId = mem-> flushToDisk(disk, i);
      buckVec[i].add_left_rel_page(newDiskId);
    }
  }
  for (unsigned int i = right_rel.first; i < right_rel.second; ++i) {
    mem-> loadFromDisk(disk, i, memInputId);
    Page* bufferInput = mem-> mem_page(memInputId);
    for (unsigned int j = 0; j < bufferInput-> size(); ++j) {
      Record curRecord(bufferInput-> get_record(j));
      unsigned int hash1Val = curRecord.partition_hash();
      unsigned int bufferId = hash1Val % hashOutNum;
      Page* memHash = mem-> mem_page(bufferId);
      memHash-> loadRecord(curRecord);
      if (memHash-> size() >= RECORDS_PER_PAGE) {
        unsigned int newDiskId0 = mem-> flushToDisk(disk, bufferId);
        buckVec[bufferId].add_right_rel_page(newDiskId0);
      }
    }
  }
  for (unsigned int i = 0; i < hashOutNum; ++i) {
    if (mem-> mem_page(i)-> size() != 0) {
      unsigned int newDiskId = mem-> flushToDisk(disk, i);
      buckVec[i].add_right_rel_page(newDiskId);
    }
  }
  mem-> reset();
  return buckVec;
}

vector<unsigned int> probe(Disk* disk, Mem* mem, vector<Bucket>& partitions) {
  vector<unsigned int> ret;
  unsigned int inputId = MEM_SIZE_IN_PAGE - 2;
  unsigned int outputId = MEM_SIZE_IN_PAGE - 1;
  unsigned int hashOutNum = MEM_SIZE_IN_PAGE - 2;
  for (unsigned int i = 0; i < partitions.size(); ++i) {
    const vector<unsigned int>& left_ref = get_smaller_rel(partitions[i]);
    const vector<unsigned int>& right_ref = get_larger_rel(partitions[i]);
    for (unsigned int j = 0; j < left_ref.size(); ++j) {
      unsigned int pageId = left_ref[j];
      mem-> loadFromDisk(disk, pageId, inputId);
      Page* page = mem-> mem_page(inputId);
      for (unsigned int k = 0; k < page-> size(); ++k) {
        Record leftRecord(page->get_record(k));
        unsigned int hash2Val = leftRecord.probe_hash();
        unsigned int bufferId = hash2Val % hashOutNum;
        Page* memHash = mem-> mem_page(bufferId);
        memHash-> loadRecord(leftRecord);
      }
      page-> reset();
    }
    for (unsigned int p = 0; p < right_ref.size(); ++p) {
      unsigned int pageId = right_ref[p];
      mem-> loadFromDisk(disk, pageId, inputId);
      Page* inputBuffer = mem-> mem_page(inputId);
      Page* outputBuffer = mem-> mem_page(outputId);
      for (unsigned int q = 0; q < inputBuffer-> size(); ++q) {
        Record rightRecord(inputBuffer-> get_record(q));
        unsigned int hash2Val = rightRecord.probe_hash();
        unsigned int bufferId = hash2Val % hashOutNum;
        Page* locBuffer = mem-> mem_page(bufferId);
        for (unsigned int r = 0; r < locBuffer-> size(); ++r) {
          if (rightRecord == locBuffer-> get_record(r)) {
            outputBuffer-> loadPair(locBuffer-> get_record(r), rightRecord);
          }
          if (outputBuffer-> size() >= RECORDS_PER_PAGE) {
            unsigned int newDiskPageId = mem-> flushToDisk(disk, outputId);
            ret.push_back(newDiskPageId);
          }
        }
      }
    }
    for (unsigned int t = 0; t < outputId; ++t) {
      mem-> mem_page(t)-> reset();
    }
  }
  if (mem-> mem_page(outputId)-> size() != 0) {
    unsigned int lastDiskPageId = mem-> flushToDisk(disk, outputId);
    ret.push_back(lastDiskPageId);
  }
  return ret;
}