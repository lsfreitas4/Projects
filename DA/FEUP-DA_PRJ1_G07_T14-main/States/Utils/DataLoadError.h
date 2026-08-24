#ifndef WATER_SUPPLY_ANALYSIS_SYSTEM_DATALOADERROR_H
#define WATER_SUPPLY_ANALYSIS_SYSTEM_DATALOADERROR_H

#include <stdexcept>

class DataLoadError : public std::runtime_error {
public:
    DataLoadError(const std::string& message);
};


#endif //WATER_SUPPLY_ANALYSIS_SYSTEM_DATALOADERROR_H
