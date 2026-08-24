#include <iostream>
#include <fstream>
#include <sstream>
#include <algorithm>
#include <vector>
#include <string>
#include <list>

#include "bst.h"
#include "students.h"
#include "readFromTextFiles.h"
#include "classesPerUc.h"

ReadFromTextFiles::ReadFromTextFiles(){
    studentClassParser();
    classesClassParser();
}

void ReadFromTextFiles::studentClassParser(){
    ifstream in("../database/students_classes.csv");
    std::string line;
    getline(in, line);

    std::vector<Students> aux;
    while(getline(in, line)){
        std::string studentCode, studentName, ucCode, classCode;
        int stdCode;
        istringstream iss(line);

        // get studentCode                            get studentName
        getline(iss, studentCode, ','); getline(iss, studentName, ',');
        // get ucCode                            get classCode
        getline(iss, ucCode, ','); getline(iss, classCode, ',');

        // turn student code to an integer instead of string
        stdCode = stoi(studentCode);

        ClassesPerUc classesPerUc = ClassesPerUc(ucCode, classCode);

        if(aux.empty() || aux[aux.size()-1].getStudentCode() != stdCode){
            Students st = Students(stdCode, studentName);
            st.addClasses(classesPerUc);
            aux.push_back(st);
        }
        else{
            aux[aux.size()-1].addClasses(classesPerUc);
        }

        auto position = myClasses.find(classesPerUc);
        if(position != myClasses.end()){
            ClassesPerUc aux1 = *position;
            myClasses.erase(position);
            aux1.increment();
            myClasses.insert(aux1);
        }
    }
    students = std::set<Students> (aux.begin(), aux.end());
}

void ReadFromTextFiles::printStudents() const {
    std::cout << "studentCode" << " , " << "studentName" << endl;
    for (auto it = s2.begin(); it != s2.end(); it++) {
        std::cout << it->getStudentCode() << " , " << it->getStudentName() << std::endl;
    }
}

void ReadFromTextFiles::classesClassParser(){
    ifstream in("../database/classes.csv");
    std::vector<ClassesPerUc> aux;
    std::string line;
    getline(in, line);

    while(getline(in, line)){
        std::string classCode, ucCode, weekDay, startHourT, durationT, type;
        float startHourF, durationF;
        istringstream iss(line);
        // get class code
        getline(iss, classCode, ',');
        // get uc code
        getline(iss, ucCode, ',');
        // get week day
        getline(iss, weekDay, ',');
        // get start hour
        getline(iss, startHourT, ',');
        startHourF = stof(startHourT);
        // get duration
        getline(iss, durationT, ',');
        durationF = stof(durationT);
        // get type
        getline(iss, type);

        // turn student code to an integer instead of string

        Blocks b = Blocks(weekDay, startHourF, durationF, type);

        if(aux.empty() || aux[aux.size()-1].getUcCode() != ucCode){
            ClassesPerUc cpuc = ClassesPerUc(ucCode, classCode);
            cpuc.addBlock(b);
            aux.push_back(cpuc);
        }
        else{
            // while no error and last ucCode saved isn't the current ucCode
            int pos = aux.size()-1;
            while(pos != -1 && aux[pos].getUcCode() == ucCode){
                if(aux[pos].getClassCode() == classCode){
                    aux[pos].addBlock(b);
                    break;
                }
                pos--;
            }
            if (pos==-1 || aux[pos].getUcCode() != ucCode){
                ClassesPerUc classesPerUc = ClassesPerUc(ucCode, classCode);
                classesPerUc.addBlock(b);
                aux.push_back(classesPerUc);
            }
        }
    }
    myClasses = std::set<ClassesPerUc> (aux.begin(), aux.end());

}

void ReadFromTextFiles::addPairSch(const std::pair<Blocks,std::pair<std::string,std::string>>& uc){
    schedule.push_back(uc);
}

void ReadFromTextFiles::printSch(int studentCode){
    Students student = Students(studentCode, "");
    auto position = students.find(student);
    if(position == students.end()) {
        std::cout << "Student not found! Try again" << endl;
        return;
    }
        student = *position;
        std::vector<std::pair<Blocks,std::pair<std::string,std::string>>> S;
        schedule = S;
        student.loadStudentSchedule(*this);
        sort(schedule.begin(), schedule.end());
        std::cout << "\nSchedule of " << student.getStudentName() << "\n";
        for (std::pair<Blocks, std::pair<std::string, std::string>> pairs : schedule) {
            std::cout << pairs.second.first << ':' << pairs.second.second << ':';
            std::cout << pairs.first.getWeekDay() << " , " << pairs.first.getType() << " , " << pairs.first.getDuration() << " , " << pairs.first.getStartHour() << std::endl;}
}
const std::_Rb_tree_const_iterator<ClassesPerUc> ReadFromTextFiles::searchUc(const ClassesPerUc &classesPerUc) const {
    auto finder = myClasses.find(classesPerUc);
    return finder;
}


void ReadFromTextFiles::printDetailsUcClass(const ClassesPerUc& classesPerUc) const {
    const std::_Rb_tree_const_iterator<ClassesPerUc> classesuc = searchUc(classesPerUc);
    if (classesuc == myClasses.end()) {
        cout << "Not found!\n" << endl;
        return;
    }
    cout << "The class " << classesuc->getClassCode() << " from the UC " << classesuc->getUcCode() << " has "
        << classesuc->getNumStudents() << " students." << endl;
    return;
}

bool ReadFromTextFiles::validSched() const {
    for (size_t i = 0; i < schedule.size()-1; i++) {
        for (size_t j = i+1; j < schedule.size(); j++) {
            if (schedule[i].first.getWeekDay() != schedule[j].first.getWeekDay()) {
                continue;
            }
            if (schedule[i].first.getType() == "T" || schedule[j].first.getType() == "T") {
                continue;
            }
            if (schedule[i].first.getStartHour() == schedule[j].first.getStartHour()) {
                return false;
            }
            if (schedule[i].first.getStartHour() < schedule[j].first.getStartHour() &&
                schedule[j].first.getStartHour() < schedule[i].first.getStartHour()+schedule[i].first.getDuration()) {
                return false;
            }
        }
    }
    return true;
}

int ReadFromTextFiles::studentsYear(char year) const{
    int i = 0;
    std::cout << "StudentCode , StudentName\n";
    for (const Students & student : s2) {
        for (const ClassesPerUc & cpu : student.getClasses()) {
            if (cpu.getClassCode()[0] == year) {
                std::cout << student.getStudentCode() << " , " << student.getStudentName() << std::endl;
                i++;
                break;
            }
        }
    }
    return i;
}

int ReadFromTextFiles::studentsClass(const ClassesPerUc & classe) const{
    int i = 0;
    std::cout << "StudentCode , StudentName\n";
    for (const Students & students : s2) {
        for (const ClassesPerUc & cpu : students.getClasses()) {
            if (cpu.getUcCode() == classe.getUcCode() && cpu.getClassCode() == classe.getClassCode()) {
                std::cout << students.getStudentCode() << " , " << students.getStudentName() << '\n';
                i++;
                break;
            }
        }
    }
    return i;
}

int ReadFromTextFiles::studentsUc(std::string ucCode) const{
    std::cout << "StudentCode , StudentName\n";
    for (const Students & students : s2) {
        std::cout << "1"<< std::endl;
        for (ClassesPerUc cpu : students.getClasses()) {
            std::cout << "2"<< std::endl;
            if (cpu.getUcCode() == ucCode) {
                std::cout << "3"<< std::endl;
                std::cout << students.getStudentCode() << " , " << students.getStudentName() << '\n';
                break;
            }
        }
    }
}

void ReadFromTextFiles::OrderbyStudCode (){
    s2 = vector<Students>(students.begin(), students.end());
    sort(s2.begin(), s2.end());
}

void ReadFromTextFiles::OrderbyStudName (){
    s2 = vector<Students>(students.begin(), students.end());
    sort(s2.begin(), s2.end(),[](Students const &x1, Students const &x2){
        return x1.getStudentName() < x2.getStudentName();
    });
}

int ReadFromTextFiles::getStudentsSize(){
    return students.size();
}