#include "blocks.h"

Blocks::Blocks(const std::string& wDay, float sHour, float dur, const std::string& ty) {
    this->weekDay=wDay;
    this->startHour=sHour;
    this->duration=dur;
    this->type=ty;
}

std::string Blocks::getWeekDay() const {
    return this->weekDay;
}

float Blocks::getStartHour() const{
    return this->startHour;
}

float Blocks::getDuration() const {
    return this->duration;
}

std::string Blocks::getType() const {
    return this->type;
}


bool Blocks::operator<(const Blocks &blocks) const{
    if(weekDay==blocks.weekDay) {
        if(startHour==blocks.startHour){
            return duration < blocks.duration;
        }
        return startHour < blocks.startHour;
    }

    if(weekDay=="Monday")
        return (blocks.weekDay=="Tuesday" || blocks.weekDay=="Wednesday" ||
            blocks.weekDay=="Thursday" || blocks.weekDay=="Friday");

    if (weekDay=="Tuesday")
        return (blocks.weekDay=="Wednesday" || blocks.weekDay=="Thursday" ||
            blocks.weekDay=="Friday");

    if (weekDay=="Wednesday")
        return (blocks.weekDay=="Thursday" || blocks.weekDay=="Friday" );

    if (weekDay=="Thursday")
        return (blocks.weekDay=="Friday");

    else return blocks.weekDay=="Saturday";
}
