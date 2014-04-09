(ns creepykanye.detect
  (:import [com.googlecode.javacv.cpp
            opencv_core
            opencv_objdetect
            opencv_highgui
            opencv_imgproc]))

(defn detector []
  (let [config (-> "haarcascade_frontalface_alt.xml"
                   (clojure.java.io/resource)
                   (clojure.java.io/file)
                   (.getAbsolutePath))
        storage (com.googlecode.javacv.cpp.opencv_core$CvMemStorage/create)
        cascade (com.googlecode.javacv.cpp.opencv_objdetect$CascadeClassifier.)]
    (.load cascade config)
    (fn [image]
      (let [faces (com.googlecode.javacv.cpp.opencv_core$CvRect. nil)
            min-size (com.googlecode.javacv.cpp.opencv_core$CvSize. 30 30)
            max-size (com.googlecode.javacv.cpp.opencv_core$CvSize. 500 500)]
        (.detectMultiScale cascade image
                           faces
                           1.1 2
                           opencv_objdetect/CV_HAAR_SCALE_IMAGE
                           min-size
                           max-size)
        (if (= 0 (.capacity faces))
          nil
          {:x (.x faces)
           :y (.y faces)
           :width (.width faces)
           :height (.height faces)})))))

(defn center-point [rect]
  (println rect)
  (let [x (+ (:x rect) (/ (:width rect) 2))
        y (+ (:y rect) (/ (:width rect) 2))
        p (com.googlecode.javacv.cpp.opencv_core$CvPoint.)]
    (.put p x y)
    p))

(defn axes [rect]
  (com.googlecode.javacv.cpp.opencv_core$CvSize.
   (/ (:width rect) 2)
   (/ (:height rect) 2)))

(defn outline-face [image]
  (let [face-detector (detector)
        face (face-detector image)
        color com.googlecode.javacv.cpp.opencv_core$CvScalar/GREEN]
    (opencv_core/cvEllipse image
                           (center-point face)
                           (axes face)
                           0
                           0
                           360
                           color
                           4
                           8
                           0)))
