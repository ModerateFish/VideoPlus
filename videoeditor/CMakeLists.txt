
cmake_minimum_required(VERSION 3.4.1)

add_library( video_recorder-lib
             SHARED
             src/main/cpp/video_recorder-lib.cpp )

find_library( log-lib
              log )

target_link_libraries( video_recorder-lib
                       ${log-lib}
                       GLESv2 )