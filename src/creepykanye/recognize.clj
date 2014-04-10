(ns creepykanye.recognize
  (:import
   [com.googlecode.javacv.cpp
    opencv_contrib
    opencv_core
    opencv_highgui
    opencv_imgproc]))

(defn face-recognizer []
  (opencv_contrib/createLBPHFaceRecognizer 1 8 8 8 10000))
;;  (opencv_contrib/createFisherFaceRecognizer 0 10000))

(defn- name->label [name]
  (let [num (subs name 0 2)
        trimmed (if (.startsWith num "0")
                  (subs num 1)
                  num)]
    (read-string trimmed)))

(defn- path->image [path]
  (let [_ (println path)
        img (opencv_highgui/cvLoadImage path)
        gray-img (com.googlecode.javacv.cpp.opencv_core$IplImage/create
                  (.width img)
                  (.height img)
                  opencv_core/IPL_DEPTH_8U
                  1)]
    (opencv_imgproc/cvCvtColor img gray-img opencv_imgproc/CV_BGR2GRAY)
    gray-img))

(defn- images->mat-vector [images]
  (let [mat-vector (com.googlecode.javacv.cpp.opencv_core$MatVector.
                    (count images))]
    (doseq [i (range (count images))]
      (.put mat-vector i (get images i)))
    mat-vector))

(defn labels-and-images []
  (let [directory (clojure.java.io/file "corpus/")
        only-files (filter #(.isFile %) (file-seq directory))
        files (map (fn [f]
                     [(name->label (.getName f))
                      (path->image (.getAbsolutePath f))])
                   only-files)] files))

(defn train [recognizer]
  (let [l-and-i (labels-and-images)
        [labels images] (reduce (fn [m [l i]]
                                  [(conj (first m) l) (conj (second m) i)])
                                [[] []]
                                l-and-i)
        label-array (int-array labels)
        image-array (images->mat-vector images)]
    (.train recognizer image-array label-array)))

(defn trained []
  (let [recognizer (face-recognizer)]
    (train recognizer)
    recognizer))

(defn recognizer []
  (let [r (trained)]
    (fn [image]
      (let [label (int-array 1)
            confidence (double-array 1)]
        (.predict r image label confidence)
        {:label (aget label 0) :confidence (aget confidence 0)}))))
