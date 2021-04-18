import zmq
import numpy as np
import cv2 as cv
import threading
import datetime

blobScale = 0.004
inferenceImageSize = 416
imageArray = []

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
    global imageArray

    # 360, 640, 3
    curImageHeight, curImageWidth, curImageChannel = curImage.shape

    timestamp = datetime.datetime.now()

    blobImage = cv.dnn.blobFromImage(curImage, blobScale, (inferenceImageSize, inferenceImageSize), (0, 0, 0), True, crop=False)

    net.setInput(blobImage)

    inferenceResult = net.forward(outputLayer)

    confidences = []
    boxes = []

    for result in inferenceResult:
        for detection in result:
            localConfidences = detection[5:]
            localClass = np.argmax(localConfidences)
            localConfidence = localConfidences[localClass]

            if localClass != 0:
                continue

            if localConfidence > 0.1:
                centerX = int(detection[0] * curImageWidth)
                centerY = int(detection[1] * curImageHeight)
                boxW = int(detection[2] * curImageWidth)
                boxH = int(detection[3] * curImageHeight)

                topLeftX = int(centerX - boxW/2)
                topLeftY = int(centerY - boxH/2)

                boxes.append([topLeftX, topLeftY, boxW, boxH])
                confidences.append(float(localConfidence))

    resultIndice = cv.dnn.NMSBoxes(boxes, confidences, score_threshold=0.4, nms_threshold=0.4)


    for temp in resultIndice:
        i = int(temp)
        cv.rectangle(curImage, (boxes[i][0], boxes[i][1]), (boxes[i][0] + boxes[i][2], boxes[i][1] + boxes[i][3]), (255, 0, 0), 2)

    print(len(resultIndice))
    print((datetime.datetime.now() - timestamp).total_seconds())

    if len(resultIndice) > 0:
        with imageArrayLock:
            imageArray.append([curImage, len(resultIndice), timestamp])


def imageDetector():
    context = zmq.Context()
    socket = context.socket(zmq.REP)
    socket.bind("tcp://*:4000")

    yoloNet, outputLayer = getYoloNet()

    while (True):
        buffer = socket.recv()

        npArray = np.fromstring(buffer, np.uint8)
        image = cv.imdecode(npArray, cv.IMREAD_COLOR)

        getInferenceResult(yoloNet, outputLayer, image)

        socket.send(b"ok")

def androidHandler():
    context = zmq.Context()
    socket = context.socket(zmq.REP)
    socket.bind("tcp://*:4001")

    while (True):
        buffer = socket.recv()

        with imageArrayLock:
            if(len(imageArray) == 0):
                socket.send(b"NULL")


        socket.send(b"ok")

if __name__ == '__main__':
    imageArrayLock = threading.Lock()

    threading.Thread(target=imageDetector(), args=()).start()
    threading.Thread(target=androidHandler(), args=()).start()
