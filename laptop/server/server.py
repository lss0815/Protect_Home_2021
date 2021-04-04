import zmq
import numpy
import io
import PIL
import cv2

context = zmq.Context()
receiver = context.socket(zmq.SUB)
receiver.connect("tcp://192.168.60.253:4000")
receiver.setsockopt_string(zmq.SUBSCRIBE, "")

image_bytes = receiver.recv()

image = numpy.array(PIL.Image.open(io.BytesIO(image_bytes)))