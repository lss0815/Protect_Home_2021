import zmq
import numpy as np
import cv2 as cv

def getYoloNet():
    net = cv.dnn.readNet("yolov4.weights", "yolov4.cfg")
    layerName = net.getLayerNames()
    outputLayerName = []
    for i in net.getUnconnectedOutLayers():
        outputLayerName.append(layerName[i[0] - 1])

    net.setPreferableBackend(cv.dnn.DNN_BACKEND_CUDA)
    net.setPreferableTarget(cv.dnn.DNN_TARGET_CUDA)

    return net

def getInferenceResult(net, curImage):
    imageHeight, imageWidth, imageChannel = curImage.shape
    print(imageHeight)
    print(imageWidth)
    print(imageChannel)

if __name__ == '__main__':
    context = zmq.Context()
    socket = context.socket(zmq.REP)
    socket.bind("tcp://*:4000")

    yoloNet = getYoloNet()

    imgCnt = 0

    while(True):
        buffer = socket.recv()

        npArray = np.fromstring(buffer, np.uint8)
        image = cv.imdecode(npArray,  cv.IMREAD_COLOR)
        
        print(imgCnt, " received")
        
        getInferenceResult(yoloNet, image)
        
        socket.send(b"ok")