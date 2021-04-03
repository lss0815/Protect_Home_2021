set(image_publisher_src
        src/main.cpp
        src/image_publisher.cpp
        include/image_publisher.h)

add_executable(image_publisher ${image_publisher_src})

target_link_libraries(image_publisher ${OpenCV_LIBS})
