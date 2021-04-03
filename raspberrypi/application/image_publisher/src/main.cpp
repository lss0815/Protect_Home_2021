//
// Created by lss on 21. 4. 1..
//

#include "opencv2/opencv.hpp"

using namespace cv;

int main(int, char**)
{
    VideoCapture cap;
	cap.open(0);// open the default camera
    if(!cap.isOpened())  // check if we succeeded
        return -1;
    cap.set(CV_CAP_PROP_FPS, 5);
    cap.set(CV_CAP_PROP_FRAME_WIDTH, 640);
    cap.set(CV_CAP_PROP_FRAME_HEIGHT, 480);
    //Mat edges;
    //namedWindow("edges",1);
    if(true)
    {
        Mat frame;
        cap.read(frame);
		//cap >> frame; // get a new frame from camera
        //cvtColor(frame, edges, COLOR_BGR2GRAY);
        //GaussianBlur(edges, edges, Size(7,7), 1.5, 1.5);
        //Canny(edges, edges, 0, 30, 3);
        //imshow("edges", edges);
        //if(waitKey(30) >= 0) break;
		imwrite("test.jpg", frame);
    }
    // the camera will be deinitialized automatically in VideoCapture destructor
    return 0;
}
