(ns creepykanye.images
  (:import
   [java.awt.image BufferedImage]
   [com.googlecode.javacv.cpp
    opencv_highgui
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

(defn- ipl-crop [img x y w h]
  (let [roi (com.googlecode.javacv.cpp.opencv_core$CvRect. x y w h)
        cropped (opencv_core/cvCreateImage
                 (com.googlecode.javacv.cpp.opencv_core$CvSize. w h)
                 (.depth img)
                 (.nChannels img))]
    (opencv_core/cvSetImageROI img roi)
    (opencv_core/cvCopy img cropped nil)
    (opencv_core/cvResetImageROI img)
    cropped))

(defn- ipl-scale [img w h]
  (let [scaled (opencv_core/cvCreateImage
                (com.googlecode.javacv.cpp.opencv_core$CvSize. w h)
                (.depth img)
                (.nChannels img))]
    (opencv_imgproc/cvResize img scaled)
    scaled))

(defn ipl->cropped-to-face [img face w h]
  (-> img
      (ipl-crop (:x face) (:y face) (:width face) (:height face))
      (ipl-scale w h)))

(defn bi->cropped-to-face [img face w h]
  (let [cropped (.getSubimage img
                              (:x face)
                              (:y face)
                              (:width face)
                              (:height face))
        ;; scaled (.getScaledInstance cropped w h BufferedImage/SCALE_SMOOTH)
        cropped-w (.getWidth cropped)
        cropped-h (.getHeight cropped)
        scaled (BufferedImage. w h (.getType img))
        g (.getGraphics scaled)]
    (.drawImage g cropped 0 0 w h 0 0 cropped-w cropped-h nil)
    (.dispose g)
    scaled))
