(ns creepykanye.core
  (:require [creepykanye.recognize]
            [seesaw.graphics :as graphics]
            [seesaw.core :refer :all]
            [seesaw.bind :as b])
  (:gen-class :main true)
  (:import [com.googlecode.javacv
            CanvasFrame
            FrameGrabber
            OpenCVFrameGrabber]
           [com.googlecode.javacv.cpp opencv_highgui]))

(defn- grab-image [grabber]
  (.grab grabber))


(defn- display-image [canvas image]
  (.showImage canvas image))

(defn paint-image [c g image]
  (.drawImage g image 0 0 (.getWidth image) (.getHeight image) nil))

(defn show-images [grabber image screen]
  (loop []
    (let [cap (grab-image grabber)]
      (reset! image (.getBufferedImage cap))
      (repaint! screen))
    (recur)))

(defn -main []
  (let [image (atom nil)
        grabber (OpenCVFrameGrabber. 0)]
    (.start grabber)
    (let [width (.getImageWidth grabber)
          height (.getImageHeight grabber)
          screen (canvas :id :screen
                         :paint (fn [c g] (paint-image c g @image))
                         :background :black)
          window (frame :title "Camera"
                        :width width
                        :height height
                        :visible? true
                        :on-close :dispose
                        :content screen)]
      (show-images grabber image screen))))
