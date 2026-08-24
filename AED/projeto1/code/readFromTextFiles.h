#ifndef READFROMTEXTFILES_H
#define READFROMTEXTFILES_H

#include <string>
#include <set>

#include "classesPerUc.h"
#include "students.h"
#include "blocks.h"

class Students;
class ClassesPerUc;
class Blocks;

/*!
 * Class that is used to read all the csv files and parse the data given.
 */
class ReadFromTextFiles {
private:
    /*!
     * Balanced binary tree of students read from the file students_classes.csv.
     */
    std::set<Students> students;
    /*!
     * Balanced binary tree with classes and ucs read from the file classes_per_uc.csv
     */
    std::set<ClassesPerUc> myClasses;
    /*!
     * Vector that stores a schedule.
     * Blocks: stores the information about the UC (week day, start hour, duration and type).
     * std::pair<std::string, std::string>: stores the uc code and the class code.
     */
    std::vector<std::pair<Blocks, std::pair<std::string, std::string>>> schedule;
    /*!
     * Vector with only the student codes and student names.
     */
    std::vector<Students> s2;


public:
    /*!
     * Empty constructor.
     */
    ReadFromTextFiles();
    /*!
     * Deals and stores the studets and it's information in a balanced binary tree.
     * @param fileName - the name of the file to read
     */
    void studentClassParser();
    /*!
     * Deals and stores the classes per UC in a binary binary tree.
     * @param fileName - the name of the file to read
     */
    void classesClassParser();
    /*!
     * Returns the balanced binary tree of students.
     * @return - balanced binary tree of students
     * Complexity: O(1)
     */
    /*!
     * Returns the balanced binary tree of classes per UC.
     * @return - balanced binary tree of classes per UC
     * Complexity: O(1)
     */
    /*!
     * Prints the students stored.
     * Complexity: O(n)
     */
    void printStudents() const;
    /*!
     * Checks if a schedule is valid or not.
     * @return boolean indication if the schedule is valid.
     * Complexity: O(n^2)
     */
    bool validSched() const;
    /*!
     * Prints the student schedule in the console.
     * @param studentCode - the student code is passed to the function.
     */
    void printSch(int studentCode);
    /*!
     * Searches for a uc in the tree.
     * @param classesPerUc - the uc being searched is passed as a parameter.
     * @return the iterator that points to the uc searched.
     */
    const std::_Rb_tree_const_iterator<ClassesPerUc> searchUc(const ClassesPerUc &classesPerUc) const;
    /*!
     *
     * @param ucCode
     */
    void addPairSch(const std::pair<Blocks,std::pair<std::string,std::string>>& ucCode);
    /*!
     * Prints in the console the number of students of a givem class
     * @param classesPerUc - The uc code and class code are required to call this function.
     */
    void printDetailsUcClass(const ClassesPerUc& classesPerUc) const;
    /*!
     * Prints the code and name of the students that started university in a passed year.
     * @param year - year of the students' entry on university.
     * @return the number of students that entered university in the passed year.
     */
    int studentsYear(char year) const;
    /*!
     * Prints the code and name of the students of a class.
     * @param classe - the uc code and class code are required.
     * @return Integer with the number of students of a given class.
     */
    int studentsClass(const ClassesPerUc & classe) const;
    /*!
     * Prints the code and name of the students of a given uc.
     * @param ucCode - the uc code.
     * @return Integer with the number of students enrolled in a uc.
     */
    int studentsUc(std::string ucCode) const;
    /*!
     * Orders the students by their student code.
     */
    void OrderbyStudCode ();
    /*!
     * Orders the students by their names.
     */
    void OrderbyStudName ();
    /*!
     * The number of students.
     * @return the size of the tree of students.
     */
    int getStudentsSize();
};

#endif //READFROMTEXTFILES_H
