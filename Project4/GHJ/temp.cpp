vector<unsigned int> get_smaller_rel(Bucket& buc) {
    return buc.get_left_rel().size() <= buc.get_right_rel().size() ? buc.get_left_rel() : buc.get_right_rel();
}

vector<unsigned int> get_larger_rel(Bucket& buc) {
    return buc.get_left_rel().size() > buc.get_right_rel().size() ? buc.get_left_rel() : buc.get_right_rel();
}

void printBucket(Disk* disk, vector<Bucket>& buc) {
    for (size_t i = 0; i < buc.size(); ++i) {
        auto& e = buc[i];
        const vector<unsigned int>& left = e.get_left_rel();
        const vector<unsigned int>& right = e.get_right_rel();
        if (left.size() != 0) {
            cout << "Bucket ID(left relation):  " << i << endl;
            for (auto f : left) {
                disk->diskRead(f)->print();
            }
        }
        if (right.size() != 0) {
            cout << "Bucket ID(right relation):  " << i << endl;
            for (auto f : right) {
                disk->diskRead(f)->print();
            }
        }
    }
}

vector<Bucket> partition(Disk* disk, Mem* mem, pair<unsigned int, unsigned int> left_rel,
    pair<unsigned int, unsigned int> right_rel) {
    unsigned int hashOutNum = MEM_SIZE_IN_PAGE - 1;
    Bucket newBucket(disk);
    vector<Bucket> buckVec(hashOutNum, newBucket);
    unsigned int memInputId = 0;
    for (unsigned int i = left_rel.first; i < left_rel.second; ++i) {
        mem->loadFromDisk(disk, i, memInputId);
        Page* memZero = mem->mem_page(memInputId);
        for (unsigned int j = 0; j < memZero->size(); ++j) {
            Record curRecord(memZero->get_record(j));
            unsigned int hash1Val = curRecord.partition_hash();
            unsigned int bufferId = hash1Val % hashOutNum + 1;
            Page* memHash = mem->mem_page(bufferId);
            memHash->loadRecord(curRecord);
            if (memHash->size() == RECORDS_PER_PAGE) {
                unsigned int newDiskId0 = mem->flushToDisk(disk, bufferId);
                buckVec[bufferId - 1].add_left_rel_page(newDiskId0);
            }
        }
    }
    for (unsigned int i = 0; i < hashOutNum; ++i) {
        if (mem->mem_page(i + 1)->size() != 0) {
            unsigned int newDiskId = mem->flushToDisk(disk, i + 1);
            buckVec[i].add_left_rel_page(newDiskId);
        }
    }

    for (unsigned int i = right_rel.first; i < right_rel.second; ++i) {
        mem->loadFromDisk(disk, i, memInputId);
        Page* memZero = mem->mem_page(memInputId);
        for (unsigned int j = 0; j < memZero->size(); ++j) {
            Record curRecord(memZero->get_record(j));
            unsigned int hash1Val = curRecord.partition_hash();
            unsigned int bufferId = hash1Val % hashOutNum + 1;
            Page* memHash = mem->mem_page(bufferId);
            memHash->loadRecord(curRecord);
            if (memHash->size() == RECORDS_PER_PAGE) {
                unsigned int newDiskId0 = mem->flushToDisk(disk, bufferId);
                buckVec[bufferId - 1].add_right_rel_page(newDiskId0);
            }
        }
    }
    for (unsigned int i = 0; i < hashOutNum; ++i) {
        if (mem->mem_page(i + 1)->size() != 0) {
            unsigned int newDiskId = mem->flushToDisk(disk, i + 1);
            buckVec[i].add_right_rel_page(newDiskId);
        }
    }
    // clear mem buffers
    mem->reset();
    return buckVec;
}

vector<unsigned int> probe(Disk* disk, Mem* mem, vector<Bucket>& partitions) {
    vector<unsigned int> result;
    //mem_size-2 reserved for input buffer, mem_size - 1 reserved for output buffer
    for (size_t i = 0; i < partitions.size(); ++i) {
        const vector<unsigned int>& lef_ref = get_smaller_rel(partitions[i]); 
        const vector<unsigned int>& right_ref = get_larger_rel(partitions[i]);
        //Build the in-mem hash table for lef_rel
        for (size_t j = 0; j < lef_ref.size(); ++j) { // traverse each lef_page inside the bucket
            unsigned int page_id = lef_ref[j]; // page_id in the current partition
            unsigned int input_id = MEM_SIZE_IN_PAGE - 2;
            // load disk to input buffer
            mem->loadFromDisk(disk, page_id, input_id);
            Page* page = mem->mem_page(input_id);
            // traverse each tuple r in the page and build a in-mem hash table
            for (size_t k = 0; k < page->size(); ++k) { 
                Record record(page->get_record(k));
                unsigned int bufferId = record.probe_hash() % (MEM_SIZE_IN_PAGE-2);  // target index in the B-2 buffer pages
                Page* memHash = mem->mem_page(bufferId);
                memHash->loadRecord(record);
            }
        }
        //Load each right_rel and compare with the in-mem hash table
        for (size_t j = 0; j < right_ref.size(); ++j) {
            // some index
            unsigned int page_id = right_ref[j]; // page_id in the current partition
            unsigned int input_id = MEM_SIZE_IN_PAGE - 2;
            unsigned int output_id = MEM_SIZE_IN_PAGE - 1;
            // load disk to input buffer
            mem->loadFromDisk(disk, page_id, input_id);
            Page* inputBuffer = mem->mem_page(input_id);
            Page* outputBuffer = mem->mem_page(output_id);
            // traverse each tuple r in the page
            for (size_t k = 0; k < inputBuffer->size(); ++k) {
                Record record(inputBuffer->get_record(k));
                unsigned int bufferId = record.probe_hash() % (MEM_SIZE_IN_PAGE - 2);  // target index in the B-2 buffer pages
                Page* targetBuffer = mem->mem_page(bufferId);
                for (size_t l = 0; l < targetBuffer->size(); ++l) { // traverse each tuple in the buffer
                    Record left_record(targetBuffer->get_record(l));
                    if (record == left_record) {
                        outputBuffer->loadPair(record, left_record);
                    }
                    if (outputBuffer->size() >= RECORDS_PER_PAGE) {
                        unsigned int newDiskId = mem->flushToDisk(disk, output_id);
                        result.push_back(newDiskId);
                    }
                }
            }
        }
    }
    // write output buffer
    unsigned int newDiskId = mem->flushToDisk(disk, MEM_SIZE_IN_PAGE - 1);
    result.push_back(newDiskId);
    return result;
}
