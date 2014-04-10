(ns creepykanye.images
  (:import
   [java.awt.image BufferedImage]
   [com.googlecode.javacv.cpp
    opencv_imgproc
    opencv_core]))

(defn ipl->grayscale [img]
  (let [gray-img (com.googlecode.javacv.cpp.opencv_core$IplImage/create
                  (.width img)
                  (.height img)
                  opencv_core/IPL_DEPTH_8U
                  1)]
    (opencv_imgproc/cvCvtColor img gray-img opencv_imgproc/CV_BGR2GRAY)
    gray-img))

(defn bi->grayscale [image]
  (let [gray (BufferedImage.
              (.getWidth image)
              (.getHeight image)
              BufferedImage/TYPE_BYTE_GRAY)]
    (let [g (.getGraphics gray)]
      (.drawImage g image 0 0 nil)
      (.dispose g))
    gray))

(defn bi->ipl [bi]
  (com.googlecode.javacv.cpp.opencv_core$IplImage/createFrom bi))

(defn ipl->bi [ipl]
  (.getBufferedImage ipl))

(defn bi->cropped-to-face [img face w h]
  (let [center-x (int (+ (:x face) (/ (:width face) 2)))
        center-y (int (+ (:y face) (/ (:height face) 2)))

        left (-> center-x
                 (- (/ w 2))
                 (Math/max 0)
                 (Math/min (- (.getWidth img) w)))
        top (-> center-y
                (- (/ h 2))
                (Math/max 0)
                (Math/min (- (.getHeight img) h)))]
    (println "left" left "top" top "w" w "h" h)
    (.getSubimage img left top w h)))
