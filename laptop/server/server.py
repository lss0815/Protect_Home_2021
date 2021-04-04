import zmq
import numpy as np
import cv2 as cv

context = zmq.Context()
socket = context.socket(zmq.REP)
socket.bind("tcp://*:4000")

imgCnt = 0

while(True):
    buffer = socket.recv()

    nparr = np.fromstring(buffer, np.uint8)
    img_np = cv.imdecode(nparr,  cv.IMREAD_COLOR)

    print(imgCnt, " received")

    socket.send(b"ok")