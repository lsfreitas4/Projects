cmake_minimum_required(VERSION 3.10)
project(feup-aed2223)

set(CMAKE_CXX_STANDARD 11)

# necessary lines to run unit tests
add_subdirectory(lib/googletest-master)
include_directories(lib/googletest-master/googletest/include)
include_directories(lib/googletest-master/googlemock/include)
add_subdirectory(lib/benchmark)
include_directories(lib/benchmark/include)

# Doxygen Build
find_package(Doxygen)
if(DOXYGEN_FOUND)
    set(BUILD_DOC_DIR "${CMAKE_SOURCE_DIR}/docs/output")
    if(NOT EXISTS "${BUILD_DOC_DIR}")
        file(MAKE_DIRECTORY "${BUILD_DOC_DIR}")
    endif()

    set(DOXYGEN_IN "${CMAKE_CURRENT_SOURCE_DIR}/docs/Doxyfile")
    set(DOXYGEN_OUT "${CMAKE_CURRENT_BINARY_DIR}/Doxyfile")
    configure_file("${DOXYGEN_IN}" "${DOXYGEN_OUT}" @ONLY)

    message("Doxygen build started")
    add_custom_target(Doxygen ALL
            COMMAND "${DOXYGEN_EXECUTABLE}" "${DOXYGEN_OUT}"
            WORKING_DIRECTORY "${CMAKE_CURRENT_BINARY_DIR}"
            COMMENT "Generating API documentation with Doxygen"
            VERBATIM)
else(DOXYGEN_FOUND)
    message("Doxygen needs to be installed to generate the documentation.")
endif(DOXYGEN_FOUND)

add_executable(projeto2
        code/main.cpp
        code/flight.cpp
        code/flight.h
        code/airline.cpp
        code/airline.h
        code/airport.cpp
        code/airport.h
        code/parseData.cpp
        code/parseData.h
        code/graph.cpp
        code/graph.h
        code/menu.cpp
        code/menu.h)

target_link_libraries(projeto2 gtest gtest_main benchmark benchmark_main)
