import zmq
import numpy as np
import cv2 as cv
import threading
import datetime
import firebase_admin
import uuid
import sys
import os
from firebase_admin import credentials
from firebase_admin import storage

firebaseCred = credentials.Certificate("config/protecthome-d9199-firebase-adminsdk-9rdae-7476e5b886.json")
firebase_admin.initialize_app(firebaseCred, {
    'storageBucket': 'protecthome-d9199.appspot.com'
})
firebaseBucket = storage.bucket()

blobScale = 1/255
inferenceImageSize = 416

detectionPeriodInSec = 60
storeImagePeriodInSec = 1
lastStoreImageTimestamp = datetime.datetime.now() - datetime.timedelta(seconds=storeImagePeriodInSec)
detectionTimestamp = datetime.datetime.now() - datetime.timedelta(seconds=detectionPeriodInSec)
detectionTimestampStr = detectionTimestamp.strftime("%Y%m%d_%H%M%S")


def getYoloNet():
    net = cv.dnn.readNet("config/yolov4.weights", "config/yolov4.cfg")
    layerName = net.getLayerNames()
    outputLayerName = []
    for i in net.getUnconnectedOutLayers():
        outputLayerName.append(layerName[i[0] - 1])

    net.setPreferableBackend(cv.dnn.DNN_BACKEND_CUDA)
    net.setPreferableTarget(cv.dnn.DNN_TARGET_CUDA)

    return net, outputLayerName

def getInferenceResult(net, outputLayer, curImage):
    global detectionTimestamp, lastStoreImageTimestamp, detectionTimestampStr

    # 360, 640, 3
    curImageHeight, curImageWidth, curImageChannel = curImage.shape

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

    if len(resultIndice) > 0:
        isFirstDetectImage = False
        timestamp = datetime.datetime.now()
        timestampStr = timestamp.strftime("%Y%m%d_%H%M%S")

        if ((timestamp - detectionTimestamp).total_seconds() > detectionPeriodInSec):
            detectionTimestamp = timestamp
            detectionTimestampStr = detectionTimestamp.strftime("%Y%m%d_%H%M%S")
            isFirstDetectImage = True

        if(isFirstDetectImage or (datetime.datetime.now() - lastStoreImageTimestamp).total_seconds() > storeImagePeriodInSec):
            lastStoreImageTimestamp = datetime.datetime.now()

            cv.imwrite('./temp_image/' + timestampStr + '.jpg', curImage)
            blob = firebaseBucket.blob(detectionTimestampStr + '/' + timestampStr + '.jpg')
            new_token = uuid.uuid4()
            metadata = {"firebaseStorageDownloadTokens": new_token}
            blob.metadata = metadata

            blob.upload_from_filename(filename='./temp_image/' + timestampStr + '.jpg', content_type='image/jpg')
            os.system('rm -f ./temp_image/*.jpg')

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


if __name__ == '__main__':
    imageDetector()