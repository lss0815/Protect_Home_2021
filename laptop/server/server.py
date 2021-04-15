import zmq
import numpy as np
import cv2 as cv


blobScale = 1.0
inferenceImageSize = 320

def getYoloNet():
    net = cv.dnn.readNet("yolov4.weights", "yolov4.cfg")
    layerName = net.getLayerNames()
    outputLayerName = []
    for i in net.getUnconnectedOutLayers():
        outputLayerName.append(layerName[i[0] - 1])

    net.setPreferableBackend(cv.dnn.DNN_BACKEND_CUDA)
    net.setPreferableTarget(cv.dnn.DNN_TARGET_CUDA)

    return net, outputLayerName

def getInferenceResult(net, outputLayer, curImage):
    # 360, 640, 3
    curImageHeight, curImageWidth, curImageChannel = curImage.shape

    blobImage = cv.dnn.blobFromImage(curImage, blobScale, (inferenceImageSize, inferenceImageSize), (0, 0, 0), True, crop=False)

    net.setInput(blobImage)

    inferenceResult = net.forward(outputLayer)

if __name__ == '__main__':
    context = zmq.Context()
    socket = context.socket(zmq.REP)
    socket.bind("tcp://*:4000")

    yoloNet, outputLayer = getYoloNet()

    imgCnt = 0

    while(True):
        buffer = socket.recv()

        npArray = np.fromstring(buffer, np.uint8)
        image = cv.imdecode(npArray,  cv.IMREAD_COLOR)

        imgCnt += 1
        print(imgCnt, " received")

        getInferenceResult(yoloNet, outputLayer, image)
        
        socket.send(b"ok")