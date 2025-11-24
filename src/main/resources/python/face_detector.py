import cv2
import sys
import time

class FaceDetector:
    def __init__(self):
        self.face_cascade = cv2.CascadeClassifier(
            cv2.data.haarcascades + 'haarcascade_frontalface_default.xml'
        )
        self.cap = cv2.VideoCapture(0)
        self.face_detected = False
        self.no_face_start_time = None
        self.absence_threshold = 3

    def detect_faces(self, frame):
        gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
        faces = self.face_cascade.detectMultiScale(
            gray,
            scaleFactor=1.3,
            minNeighbors=5,
            minSize=(30, 30)
        )
        return len(faces) > 0

    def run(self):
        print("READY", flush=True)

        while True:
            ret, frame = self.cap.read()

            when_ret_false = not ret
            if when_ret_false:
                print("ERROR:CAMERA_FAIL", flush=True)
                break

            has_face = self.detect_faces(frame)
            current_time = time.time()

            when_face_detected = has_face and not self.face_detected
            if when_face_detected:
                self.face_detected = True
                self.no_face_start_time = None
                print("FACE_DETECTED", flush=True)
                continue

            when_face_lost = not has_face and self.face_detected
            if when_face_lost:
                when_no_start_time = self.no_face_start_time is None
                if when_no_start_time:
                    self.no_face_start_time = current_time

                elapsed = current_time - self.no_face_start_time
                when_threshold_exceeded = elapsed >= self.absence_threshold
                if when_threshold_exceeded:
                    self.face_detected = False
                    print("FACE_LOST", flush=True)

            time.sleep(0.1)

        self.cleanup()

    def cleanup(self):
        self.cap.release()

if __name__ == "__main__":
    detector = FaceDetector()
    detector.run()
