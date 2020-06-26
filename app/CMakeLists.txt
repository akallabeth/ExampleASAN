cmake_minimum_required(VERSION 3.4.1)

add_library( native-lib SHARED src/main/cpp/native-lib.cpp )

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log )

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
        native-lib

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib} )

if(USEASAN)
    set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -fsanitize=address -fno-omit-frame-pointer")
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fsanitize=address -fno-omit-frame-pointer")
    set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} -fsanitize=address")
    set(CMAKE_STATIC_LINKER_FLAGS "${CMAKE_STATIC_LINKER_FLAGS} -fsanitize=address")

    get_filename_component(HINT_PATH ${ANDROID_C_COMPILER} DIRECTORY)
    set(ARCH_STR ${CMAKE_ANDROID_ARCH})
    if ("${CMAKE_ANDROID_ARCH_ABI}" STREQUAL "arm64-v8a")
        set(ARCH_STR "aarch64")
    elseif ("${CMAKE_ANDROID_ARCH_ABI}" STREQUAL "armeabi")
        set(ARCH_STR "arm")
    elseif ("${CMAKE_ANDROID_ARCH_ABI}" STREQUAL "armeabi-v7a")
        set(ARCH_STR "arm")
    elseif ("${CMAKE_ANDROID_ARCH_ABI}" STREQUAL "x86")
        set(ARCH_STR "i686")
    elseif ("${CMAKE_ANDROID_ARCH_ABI}" STREQUAL "x86_64")
        set(ARCH_STR "x86_64")
    endif()

    find_file(ASAN
            NAMES libclang_rt.asan-${ARCH_STR}-android.so
            HINTS ${HINT_PATH}/../lib64/clang/9.0.8/lib/linux)

    get_filename_component(ASAN_NAME ${ASAN} NAME)
    set(ASAN_NAME ${CMAKE_SOURCE_DIR}/src/main/jniLibs/${CMAKE_ANDROID_ARCH_ABI}/${ASAN_NAME})

    add_custom_command(
            TARGET native-lib PRE_BUILD
            COMMAND ${CMAKE_COMMAND} -E copy ${ASAN} ${ASAN_NAME})
endif(USEASAN)
