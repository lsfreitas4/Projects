<<<<<<< HEAD:projeto1/code/students.cpp
#include "students.h"
#include <algorithm>
#include <string>
#include <vector>
#include <set>

#define MAX_CLASS_SIZE 30

Students::Students(int studentCode, std::string studentName) {
    this -> studentCode = studentCode;
    this -> studentName = studentName;
    vector<ClassesPerUc> c1;
    classes = c1;
}

int Students::getStudentCode() const {
    return this->studentCode;
}

void Students::setStudentCode(int studentCode) {
    this->studentCode = studentCode;
}

std::string Students::getStudentName() const {
    return this->studentName;
}

void Students::setStudentName(std::string studentName) {
    this->studentName = studentName;
}

bool Students::operator<(const Students& s1) const {
    return (studentCode < s1.getStudentCode());
}

Students& Students::operator=(const Students& s1) {
    studentName = s1.studentName;
    studentCode = s1.studentCode;
    classes.clear();
    for(const ClassesPerUc & classe : s1.classes) {
        classes.push_back(classe);
    }
    return *this;
}

void Students::addClasses(const ClassesPerUc& classe){
    classes.push_back(classe);
}

void Students::printStudents() const {
    std::cout << studentCode << " , " << studentName << endl;
    for (auto it = s2.begin(); it != s2.end(); it++) {
        std::cout << getStudentCode() << " , " << getStudentName() << std::endl;
    }
}

vector<ClassesPerUc> Students::getClasses() const {
    return classes;
}


void Students::loadStudentSchedule(ReadFromTextFiles& rftf){
    for(const ClassesPerUc &classesPerUc: classes){
        rftf.searchUc(classesPerUc)->blocksStore(rftf);
    }
}

bool Students::removeClass(ClassesPerUc c){
    auto it = classes.begin();
    for(it = classes.begin(); it != classes.end(); it++){
        if(it->getClassCode() == c.getClassCode() && it->getUcCode() == c.getUcCode())
            break;
    }
    if(it == classes.end())
        return false;
    classes.erase(it);
    return true;
}

/*
bool Students::removeStudentUc(int studentCod, const ClassesPerUc &cl) {
    set<ClassesPerUc>::iterator itr = myClasses.find(cl);
    if (itr == myClasses.end()) {
        return false;
    }

    auto pos = students.find(Students(studentCod, ""));
    if (pos == students.end()) {
        return false;
    }

    ClassesPerUc sub = *itr;
    sub.decrement();
    myClasses.erase(itr);
    myClasses.insert(sub);

    Students s1 = *pos;
    if (!s1.removeClass(cl)){
        return false;
    }
    students.erase(pos);
    students.insert(s1);
    return true;
}
*/
/*
bool Students::addStudentUc(int studentC, const ClassesPerUc &c) {
    std::set<ClassesPerUc>::iterator it = myClasses.find(c);
    if (it == myClasses.end() || it->getNumStudents() >= MAX_CLASS_SIZE) {
        return false;
    }

    for (auto it1 = it; it1 != myClasses.end() && it1->getUcCode() == c.getUcCode(); it1++) {
        if (it->getNumStudents() + 1 - it1->getNumStudents() >= 4) {
            return false;
        }
    }

    for (auto it2 = it; it2 != --myClasses.begin() && it2->getUcCode() == c.getClassCode(); it2--) {
        if (it->getNumStudents() + 1 - it2->getNumStudents() >= 4) {
            return false;
        }
    }

    Students st = Students(studentC, "");

    auto posSt = students.find(st);
    if (posSt == students.end()) {
        return false;
    }

    Students s1 = *posSt;
    s1.addClasses(c);
    s1.loadStudentSchedule(*this);

    ClassesPerUc cl = *it;
    cl.increment();
    myClasses.erase(it);
    myClasses.insert(cl);
    students.erase(posSt);
    students.insert(s1);
    return true;
}
*/
/*
bool Students::swapTurmaStudent(int studentC, const ClassesPerUc &rem, const ClassesPerUc &ad) {
    auto removeitr = classes.find(remove);
    auto additr = classes.find(add);
    if (removeitr == classes.end() || additr == classes.end() || additr->getSize() >= 30) {
        return false;
    }
    for (auto itr = additr; itr != classes.end() && itr->getUC() == addingitr->getUC(); itr++) {
        if (additr->getSize() + 1 - itr->getSize() >= 4) {
            return false;
        }
    }
    for (auto itr = additr; itr != --classes.begin() && itr->getUC() == additr->getUC(); itr--) {
        if (addingitr->getSize() + 1 - itr->getSize() >= 4) {
            return false;
        }
    }
    auto posStudent = students1.find(Students(i, ""));
    if (posStudent == students1.end()) {
        return false;
    }
    Students tmp = *posStudent;
    if (!tmp.removeStudentUc(remove)) return false;
    tmp.addClasses(add);
    tmp.loadStudentSchedule(*this);
    if (!schedule()) return false;
    ClassesPerUc sub = *removeitr;
    sub.decrement();
    classes.erase(removeitr);
    classes.insert(sub);
    ClassesPerUc add = *additr;
    add.increment();
    classes.erase(removeitr);
    classes.insert(add);
    students1.erase(posStudent);
    students1.insert(tmp);
    return true;
}*/
=======
#include "students.h"
#include <algorithm>
#include <string>
#include <vector>
#include <set>

#define MAX_CLASS_SIZE 30

Students::Students(int studentCode, std::string studentName) {
    this -> studentCode = studentCode;
    this -> studentName = studentName;
    vector<ClassesPerUc> c1;
    classes = c1;
}

int Students::getStudentCode() const {
    return this->studentCode;
}

void Students::setStudentCode(int studentCode) {
    this->studentCode = studentCode;
}

std::string Students::getStudentName() const {
    return this->studentName;
}

void Students::setStudentName(std::string studentName) {
    this->studentName = studentName;
}

bool Students::operator<(const Students& s1) const {
    return (studentCode < s1.getStudentCode());
}

Students& Students::operator=(const Students& s1) {
    studentName = s1.studentName;
    studentCode = s1.studentCode;
    classes.clear();
    for(const ClassesPerUc & classe : s1.classes) {
        classes.push_back(classe);
    }
    return *this;
}

void Students::addClasses(const ClassesPerUc& classe){
    classes.push_back(classe);
}

void Students::printStudents() const {
    std::cout << studentCode << " , " << studentName << endl;
    for (auto it = s2.begin(); it != s2.end(); it++) {
        std::cout << getStudentCode() << " , " << getStudentName() << std::endl;
    }
}

vector<ClassesPerUc> Students::getClasses() const {
    return classes;
}


void Students::loadStudentSchedule(ReadFromTextFiles& rftf){
    for(const ClassesPerUc &classesPerUc: classes){
        rftf.searchUc(classesPerUc)->blocksStore(rftf);
    }
}

bool Students::removeClass(ClassesPerUc c){
    auto it = classes.begin();
    for(it = classes.begin(); it != classes.end(); it++){
        if(it->getClassCode() == c.getClassCode() && it->getUcCode() == c.getUcCode())
            break;
    }
    if(it == classes.end())
        return false;
    classes.erase(it);
    return true;
}

/*
bool Students::removeStudentUc(int studentCod, const ClassesPerUc &cl) {
    set<ClassesPerUc>::iterator itr = myClasses.find(cl);
    if (itr == myClasses.end()) {
        return false;
    }

    auto pos = students.find(Students(studentCod, ""));
    if (pos == students.end()) {
        return false;
    }

    ClassesPerUc sub = *itr;
    sub.decrement();
    myClasses.erase(itr);
    myClasses.insert(sub);

    Students s1 = *pos;
    if (!s1.removeClass(cl)){
        return false;
    }
    students.erase(pos);
    students.insert(s1);
    return true;
}
*/
/*
bool Students::addStudentUc(int studentC, const ClassesPerUc &c) {
    std::set<ClassesPerUc>::iterator it = myClasses.find(c);
    if (it == myClasses.end() || it->getNumStudents() >= MAX_CLASS_SIZE) {
        return false;
    }

    for (auto it1 = it; it1 != myClasses.end() && it1->getUcCode() == c.getUcCode(); it1++) {
        if (it->getNumStudents() + 1 - it1->getNumStudents() >= 4) {
            return false;
        }
    }

    for (auto it2 = it; it2 != --myClasses.begin() && it2->getUcCode() == c.getClassCode(); it2--) {
        if (it->getNumStudents() + 1 - it2->getNumStudents() >= 4) {
            return false;
        }
    }

    Students st = Students(studentC, "");

    auto posSt = students.find(st);
    if (posSt == students.end()) {
        return false;
    }

    Students s1 = *posSt;
    s1.addClasses(c);
    s1.loadStudentSchedule(*this);

    ClassesPerUc cl = *it;
    cl.increment();
    myClasses.erase(it);
    myClasses.insert(cl);
    students.erase(posSt);
    students.insert(s1);
    return true;
}
*/
/*
bool Students::swapTurmaStudent(int studentC, const ClassesPerUc &rem, const ClassesPerUc &ad) {
    auto removeitr = myClasses.find(rem);
    auto additr = myClasses.find(ad);
    if (removeitr == myClasses.end() || additr == myClasses.end() || additr->getNumStudents() >= 30) {
        return false;
    }
    for (auto itr = additr; itr != myClasses.end() && itr->getUcCode() == additr->getUcCode(); itr++) {
        if (additr->getNumStudents() + 1 - itr->getNumStudents() >= 4) {
            return false;
        }
    }
    for (auto itr = additr; itr != --myClasses.begin() && itr->getUcCode() == additr->getUcCode(); itr--) {
        if (additr->getNumStudents() + 1 - itr->getNumStudents() >= 4) {
            return false;
        }
    }

    auto pos = students.find(Students(studentC, ""));
    if (pos == students.end()) {
        return false;
    }

    Students tmp = *pos;
    if (!tmp.removeStudentUc(studentC, rem)) return false;
    tmp.addClasses(ad);
    tmp.loadStudentSchedule(*this);
    if (!sched_getcpu()) return false;
    ClassesPerUc sub = *removeitr;
    sub.decrement();
    myClasses.erase(removeitr);
    myClasses.insert(sub);
    ClassesPerUc add = *additr;
    add.increment();
    myClasses.erase(removeitr);
    myClasses.insert(add);
    students.erase(pos);
    students.insert(tmp);
    return true;
}*/
>>>>>>> e55e24472cb907ddd6b50d696aaf204d152acdeb:code/students.cpp
