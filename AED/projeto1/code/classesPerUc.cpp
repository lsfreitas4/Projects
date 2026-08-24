#include <iostream>
#include "classesPerUc.h"

ClassesPerUc::ClassesPerUc(const std::string& ucCode, const std::string& classCode) {
    this->ucCode = ucCode;
    this->classCode = classCode;
    numStudents = 0;
}

void ClassesPerUc::setUcCode(std::string ucCode) {
    this->ucCode=ucCode;
}

std::string ClassesPerUc::getUcCode() const {
    return ucCode;
}

void ClassesPerUc::setClassCode(std::string classCode) {
    this->classCode=classCode;
}

std::string ClassesPerUc::getClassCode() const {
    return classCode;
}
int ClassesPerUc::getNumStudents() const {
    return numStudents;
}

int ClassesPerUc::size() const {
    return size();
}

void ClassesPerUc::increment() {
    numStudents++;
}

void ClassesPerUc::decrement() {
    numStudents--;
}

bool ClassesPerUc::operator<(const ClassesPerUc& classe) const {
    if (ucCode == classe.ucCode) {
        return classCode < classe.classCode;
    }
    return ucCode < classe.ucCode;
}
void ClassesPerUc::addBlock (const Blocks &blocks) {
    scheduleClassesPerUc.push_back(blocks);
}

void ClassesPerUc::blocksStore(ReadFromTextFiles& rftf) const {
    for (Blocks blocks : scheduleClassesPerUc) {
        std::pair<std::string, std::string> x1(ucCode, classCode);
        std::pair<Blocks, std::pair<std::string,std::string>> x2(blocks, x1);
        rftf.addPairSch(x2);
    }
}

