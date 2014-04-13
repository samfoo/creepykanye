(ns creepykanye.detect
  (:import [com.googlecode.javacv.cpp
            opencv_core
            opencv_objdetect
            opencv_highgui
            opencv_imgproc]))

(defn- extract-objects [rect-ptr]
  (loop [i 0
         objects []]
    (if (< i (.limit rect-ptr))
      (do
        (.position rect-ptr i)
        (recur (inc i)
               (conj objects {:x (.x rect-ptr)
                              :y (.y rect-ptr)
                              :width (.width rect-ptr)
                              :height (.height rect-ptr)})))
      objects)))

(defn- object-detector [xml min max]
  (let [config (-> xml
                   (clojure.java.io/resource)
                   (clojure.java.io/file)
                   (.getAbsolutePath))
        storage (com.googlecode.javacv.cpp.opencv_core$CvMemStorage/create)
        cascade (com.googlecode.javacv.cpp.opencv_objdetect$CascadeClassifier.)]
    (.load cascade config)

    (fn [image]
      (let [objects (com.googlecode.javacv.cpp.opencv_core$CvRect. nil)
            min-size (com.googlecode.javacv.cpp.opencv_core$CvSize. min min)
            max-size (com.googlecode.javacv.cpp.opencv_core$CvSize. max max)]
        (.detectMultiScale cascade image
                           objects
                           1.1 2
                           opencv_objdetect/CV_HAAR_SCALE_IMAGE
                           min-size
                           max-size)
        (extract-objects objects)))))

(defn face-detector []
  (object-detector "haarcascade_frontalface_alt.xml" 150 500))

(defn eye-detector []
  (object-detector "haarcascade_eye.xml" 100 150))

(defn key-points [image]
  (let [detector (com.googlecode.javacv.cpp.opencv_features2d$GFTTDetector.
                  500 0.01 10 3 false 0.04)
        points-ptr (com.googlecode.javacv.cpp.opencv_features2d$KeyPoint.)]
    (.detect detector image points-ptr nil)
    (loop [i 0
           points []]
      (if (< i (.limit points-ptr))
        (do
          (.position points-ptr i)
          (recur (inc i)
                 (conj points {:x (.pt_x points-ptr)
                               :y (.pt_y points-ptr)})))
        points))))

(defn center-point [rect]
  (let [x (+ (:x rect) (/ (:width rect) 2))
        y (+ (:y rect) (/ (:width rect) 2))
        p (com.googlecode.javacv.cpp.opencv_core$CvPoint.)]
    (.put p x y)
    p))

