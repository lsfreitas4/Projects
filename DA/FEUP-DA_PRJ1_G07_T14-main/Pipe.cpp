#include "Pipe.h"

Pipe::Pipe(string servicePointA, string servicePointB, unsigned long capacity, bool unidirectional)
    : servicePointA(servicePointA), servicePointB(servicePointB), capacity(capacity), unidirectional(unidirectional) {}