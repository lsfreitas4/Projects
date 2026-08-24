#ifndef STUDENTS_H
#define STUDENTS_H

#include <string>
#include <vector>
#include <list>
#include "bst.h"
#include <fstream>
#include <iostream>
#include <sstream>
#include <algorithm>
#include <set>

#include "classesPerUc.h"
#include "readFromTextFiles.h"
#include "blocks.h"

using namespace std;

class ClassesPerUc;
class Blocks;
class ReadFromTextFiles;

class Students {
private:
    int studentCode;
    std::string studentName;
    vector<ClassesPerUc> classes;
    vector<Students> s2;
    set<ClassesPerUc> myClasses;

public:
    Students(int studentCode, const std::string studentName);
    void setStudentCode(int studentCode);
    int getStudentCode() const;
    void setStudentName(std::string studentName);
    std::string getStudentName() const;
    bool operator<(const Students& s1) const;
    Students& operator=(const Students& student);
    void addClasses(const ClassesPerUc& classes);
    void printStudents() const;
    std::vector<ClassesPerUc> getClasses() const;
    int studentsYear(char year) const;
    int studentsClass(const ClassesPerUc & classe) const;
    int studentsUc(std::string ucCode) const;
    void OrderbyStudCode ();
    void OrderbyStudName ();
    int getStudentsSize();
    void loadStudentSchedule(ReadFromTextFiles& rftf);
    bool removeClass(ClassesPerUc c);
    bool removeStudentUc(int sutdentC, const ClassesPerUc &cl);
    bool addStudentUc(int studentC, const ClassesPerUc &cl);
    bool swapTurmaStudent(int studentC, const ClassesPerUc &rem, const ClassesPerUc &ad);
    void setClasses(std::set<ClassesPerUc> z);
    };

#endif //STUDENTS_H
