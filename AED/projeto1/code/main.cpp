

#include <iostream>
#include <vector>
#include <fstream>
#include <string>
#include "classesPerUc.h"
#include "students.h"
#include "blocks.h"
#include "readFromTextFiles.h"
#include <stdlib.h>


void ocupacaoTurmaUc(ReadFromTextFiles& rftf) {
    std::string ucCode, classCode;
    std::cout << "UC code (format L.EIC___)\n" << std::endl;
    std::cin >> ucCode;
    if (ucCode.length() == 8 ||ucCode.substr(0,5) == "L.EIC") {
        std::cout << "Class code (format _LEIC__)\n" << std::endl;
        std::cin >> classCode;
        if (classCode.length() != 7 ||classCode.substr(1,4) != "LEIC") {
            std::cout << "Incorrect format. Try again.\n" << std::endl;
            return;
        }
        rftf.printDetailsUcClass(ClassesPerUc(ucCode, classCode));
    }
    else {
        std::cout << "Incorrect format. Try again.\n" << std::endl;
        return;
    }
}

void mainMenu() {

    std::cout << " ------------------------------------------------------------------------- " << std::endl;
    std::cout << "|                                                                         |" << std::endl;
    std::cout << "|                           Select an Option                              |" << std::endl;
    std::cout << "|                                                                         |" << std::endl;
    std::cout << "|     Option 1 - Class ocupation of a given UC                            |" << std::endl;
    std::cout << "|     Option 2 - Student schedule                                         |" << std::endl;
    std::cout << "|     Option 3 - Ordering                                                 |" << std::endl;
    std::cout << "|     Option 4 - Change requests                                          |" << std::endl;
    std::cout << "|                                                                         |" << std::endl;
    std::cout << "|                              5 - Exit                                   |" << std::endl;
    std::cout << " ------------------------------------------------------------------------- " << std::endl;

    int input;
    std::cin >> input;

    ReadFromTextFiles rftf;
    rftf.classesClassParser();
    rftf.studentClassParser();

    switch (input) {
        case 1:

            ocupacaoTurmaUc(rftf);
            // volta ao menu principal
            std::cout << std::endl <<std::endl << "Press 1 to go back to the previous menu...\n" << std::endl;
            std::cin >> input;
            mainMenu();
            break;
        case 2:
        {
            std::string n;
            std::cout << "Student code\n" << std::endl;
            std::cin >> n;
            if(n.length() == 9) {
                rftf.printSch(stoi(n));
                std::cout << std::endl <<std::endl << "Press 1 to go back to the previous menu...\n" << std::endl;
                std::cin >> input;
                mainMenu();
            }
            else {
                std::cout << "Incorrect format. Try again.\n" << std::endl;
            }
            break;
        }
        case 3: {
            std::string n;
            std::string ucCode;
            std::string year;
            std::cout << "Order Students by:\n1.Name\n2.Student Code\n3.Students of UC (format L.EIC___):\n"
                         "4.Students by year: \n" << std::endl;
            std::cin >> n;
            if (n == "1") {
                rftf.OrderbyStudName();
                rftf.printStudents();
                std::cout << std::endl << std::endl << "Press 1 to go back to the previous menu...\n" << std::endl;
                std::cin >> input;
                mainMenu();
            } else if (n == "2") {
                rftf.OrderbyStudCode();
                rftf.printStudents();
                std::cout << std::endl << std::endl << "Press 1 to go back to the previous menu...\n" << std::endl;
                std::cin >> input;
                mainMenu();
            }
            else if (n == "3") {
                std::string ucCode;
                std::cout << "UC code (format L.EIC___)\n" << std::endl;
                std::cin >> ucCode;
                if (ucCode.length() != 8 ||ucCode.substr(0,5) != "L.EIC") {
                    std::cout << "Incorrect format. Try again.\n" << std::endl;
                    return;}
                rftf.studentsUc(ucCode);
                std::cout << std::endl << std::endl << "Press 1 to go back to the previous menu...\n" << std::endl;
                std::cin >> input;
                mainMenu();
            }
            else if (n=="4"){
                std::string year;
                std::cout << "Year? (between 1 and 3)" << std::endl;
                std::cin >> year;
                if (year.length()>1 || year[0] < '0' || year[0] > '3'){
                    std::cout << "Incorrect year. Try again.\n" << std::endl;
                    return;
                }
                rftf.studentsYear(year[0]);
                std::cout << std::endl << std::endl << "Press 1 to go back to the previous menu...\n" << std::endl;
                std::cin >> input;
                mainMenu();

            }
            else {
                std::cout << "Incorrect option. Try again.\n" << std::endl;
            }
            // voltar ao menu principal
            std::cout << std::endl << std::endl << "Press 1 to go back to the previous menu...\n" << std::endl;
            std::cin >> input;
            mainMenu();
            break;
        }
        case 4:


            // voltar ao menu principal
            std::cout << std::endl <<std::endl << "Press 1 to go back to the previous menu...\n" << std::endl;
            std::cin >> input;
            mainMenu();
            break;
        case 5:
            break;
        default:
            std::cout << "Invalid Input!\n" << std::endl;
            mainMenu();
    }
}

int main() {
    mainMenu();
    return 0;
}