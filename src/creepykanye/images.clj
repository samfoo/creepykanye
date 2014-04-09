(ns creepykanye.images
  (:import
   [com.googlecode.javacv.cpp
    opencv_imgproc
    opencv_core]))

(defn normalize [img]
  (let [gray-img (com.googlecode.javacv.cpp.opencv_core$IplImage/create
                  (.width img)
                  (.height img)
                  opencv_core/IPL_DEPTH_8U
                  1)]
    (opencv_imgproc/cvCvtColor img gray-img opencv_imgproc/CV_BGR2GRAY)
    gray-img))
