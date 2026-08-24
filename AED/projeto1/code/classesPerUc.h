#ifndef CLASSES_PER_UC_H
#define CLASSES_PER_UC_H


#include <string>
#include <vector>
#include <list>
#include "bst.h"
#include "blocks.h"
#include "students.h"
#include "ReadFromTextFiles.h"
using namespace std;
#define FILE_NAME "database/classes_per_uc.csv"
class Students;
class Blocks;
class ReadFromTextFiles;

class ClassesPerUc {
private:
    std::string ucCode;
    std::string classCode;
    int numStudents;
    std::vector<Blocks> scheduleClassesPerUc;

public:
    ClassesPerUc( const std::string& ucCode, const std::string& classCode);
    void setUcCode(std::string ucCode);
    std::string getUcCode() const;
    void setClassCode(std::string classCode);
    std::string getClassCode() const;
    int getNumStudents() const;
    int size() const;
    void increment();
    void decrement();
    bool operator<(const ClassesPerUc& classe) const;
    void blocksStore(ReadFromTextFiles& rftf) const;
    void addBlock (const Blocks &blocks);
};


#endif //CLASSES_PER_UC_H
