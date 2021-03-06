#include <opencv2/opencv.hpp>
#include <zmq.h>
#include <chrono>
#include <thread>
#include <iostream>

int main(int, char**)
{
    cv::VideoCapture m_videoCapture;
    int m_cameraIndex = 0;
    m_videoCapture.open(m_cameraIndex);
    if(!m_videoCapture.isOpened()){
        std::cout << "Camera " << m_cameraIndex << " not working\n" << "Program restarted\n";
        return -1;
    }

    m_videoCapture.set(CV_CAP_PROP_FRAME_WIDTH, 640);
    m_videoCapture.set(CV_CAP_PROP_FRAME_HEIGHT, 480);

    void *m_zmqContext = zmq_ctx_new();
    void *m_zmqRequester = zmq_socket (m_zmqContext, ZMQ_REQ);
    zmq_connect (m_zmqRequester, "tcp://192.168.0.6:4000");

    cv::Mat m_curFrame;
    std::vector<uchar> m_curBuffer;
	std::vector<uchar> m_recvBuffer(20);

    int sendCnt = 0;
    while(true)
    {
        m_videoCapture.read(m_curFrame);
        cv::imencode(".jpg", m_curFrame, m_curBuffer);

        zmq_send(m_zmqRequester, m_curBuffer.data(), m_curBuffer.size(), ZMQ_NOBLOCK);
        std::cout << ++sendCnt << " image sent\n";
		
        zmq_recv(m_zmqRequester, m_recvBuffer.data(), m_recvBuffer.size(), 0);
    }

    m_videoCapture.release();

    return 0;
}
