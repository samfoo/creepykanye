(ns creepykanye.core
  (:require [creepykanye.recognize]
            [creepykanye.detect :as faces]
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

(defn paint-image [c g image face]
  (.drawImage g image 0 0 (.getWidth image) (.getHeight image) nil)
  (when (not (nil? face))
    (graphics/draw g
                   (graphics/rect (:x face) (:y face)
                                  (:width face) (:height face))
                   (graphics/style :foreground :green :stroke 5))))

(defn show-images [grabber image face screen]
  (let [detector (faces/detector)]
    (loop []
      (let [cap (grab-image grabber)
            detected-face (detector cap)]
        (reset! face detected-face)
        (reset! image (.getBufferedImage cap))
        (repaint! screen))
      (recur))))

(defn -main []
  (let [image (atom nil)
        face (atom nil)
        grabber (OpenCVFrameGrabber. 0)]
    (.start grabber)
    (let [width (.getImageWidth grabber)
          height (.getImageHeight grabber)
          screen (canvas :id :screen
                         :paint (fn [c g] (paint-image c g @image @face))
                         :background :black)
          window (frame :title "Camera"
                        :width width
                        :height height
                        :visible? true
                        :on-close :dispose
                        :content screen)]
      (show-images grabber image face screen))))
