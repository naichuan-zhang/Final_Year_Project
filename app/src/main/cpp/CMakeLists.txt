# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

# CMAKE_SOURCE_DIR => /app/src/main/cpp/
# app/src/main/jniLibs/arm64-v8a/libopencv_java3.so
# I:\ImageProcessor\app\src\main\cpp\include
set(lib_src_DIR ${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libopencv_java3.so)
include_directories(${CMAKE_SOURCE_DIR}/include)
add_library(
        opencv_java3-lib
        SHARED
        IMPORTED)
set_target_properties(
        opencv_java3-lib
        PROPERTIES IMPORTED_LOCATION
        ${lib_src_DIR})

add_library( # Sets the name of the library.
        native-lib

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        native-lib.cpp

        # TODO: add here ...
        )

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

file(GLOB CVLIBS
        I:/opencv-3.4.8-android-sdk/OpenCV-android-sdk/sdk/native/staticlibs/${ANDROID_ABI}/*.a)

#add_library(opencv_stitching-lib
#        STATIC
#        IMPORTED)
#
#set_target_properties(opencv_stitching-lib
#        PROPERTIES
#        IMPORTED_LOCATION I:/opencv-3.4.8-android-sdk/OpenCV-android-sdk/sdk/native/staticlibs/${ANDROID_ABI}/*.a)


# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
        native-lib

        opencv_java3-lib

#        opencv_stitching-lib

        ${CVLIBS}

        # TODO: add here ....

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib})
