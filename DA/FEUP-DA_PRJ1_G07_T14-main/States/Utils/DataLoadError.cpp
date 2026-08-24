#include "DataLoadError.h"

DataLoadError::DataLoadError(const std::string& message) : std::runtime_error(message) {}
