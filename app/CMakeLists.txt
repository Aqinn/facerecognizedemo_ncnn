# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.10.2)

# Declares and names the project.
project("facerecognizencnn")

#include头文件目录
include_directories(${CMAKE_SOURCE_DIR}/src/main/cpp/include
        ${CMAKE_SOURCE_DIR}/src/main/cpp/)

file(GLOB FACE_SRC ${CMAKE_SOURCE_DIR}/src/main/cpp/*.h
        ${CMAKE_SOURCE_DIR}/src/main/cpp/*.hpp
        ${CMAKE_SOURCE_DIR}/src/main/cpp/*.cpp)
set(FACE_COMPILE_CODE ${FACE_SRC})

set(CMAKE_C_FLAGS "-fopenmp -static-openmp -O2 -fvisibility=hidden -fomit-frame-pointer -fstrict-aliasing -ffunction-sections -fdata-sections -ffast-math -DANDROID_TOOLCHAIN=clang")
set(CMAKE_CXX_FLAGS "-fopenmp -static-openmp -O2 -fvisibility=hidden -fvisibility-inlines-hidden -fomit-frame-pointer -fstrict-aliasing -ffunction-sections -fdata-sections -ffast-math")
set(CMAKE_CXX_FLAGS "-std=c++11 -frtti -fexceptions")

# import ncnn library
add_library(ncnn STATIC IMPORTED)

# change this folder path to yours
set_target_properties(ncnn PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/src/main/cpp/ncnn-android-lib/${ANDROID_ABI}/libncnn.a)
include_directories(${CMAKE_SOURCE_DIR}/src/main/cpp/ncnn-android-lib/include/ncnn)


# openmp
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -fopenmp -static-openmp")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fopenmp -static-openmp")


aux_source_directory(${CMAKE_SOURCE_DIR}/src/main/cpp DIR_SRCS)

add_library(Face SHARED ${DIR_SRCS} ${FACE_COMPILE_CODE})


target_link_libraries(Face
        ncnn
        android
        z
        log
        jnigraphics)