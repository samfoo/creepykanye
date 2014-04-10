(ns creepykanye.core
  (:require [creepykanye.recognize]
            [creepykanye.detect :as faces]
            [creepykanye.images :as images]
            [seesaw.graphics :as graphics]
            [seesaw.core :refer :all]
            [seesaw.bind :as b])
  (:gen-class :main true)
  (:import [javax.imageio.ImageIO]
           [com.googlecode.javacv
            CanvasFrame
            FrameGrabber
            OpenCVFrameGrabber]
           [com.googlecode.javacv.cpp opencv_highgui]))

(defn- grab-image [grabber]
  (.grab grabber))

(defn- display-image [canvas image]
  (.showImage canvas image))

(defn paint-image [c g image face]
  (when (not (nil? image))
    (.drawImage g image 0 0 (.getWidth image) (.getHeight image) nil)
    (when (not (nil? face))
      (graphics/draw g
                     (graphics/rect (:x face) (:y face)
                                    (:width face) (:height face))
                     (graphics/style :foreground :green :stroke 5)))))

(defn build-corpus [image face]
  (loop []
    (when (not (nil? @face))
      (let [file (clojure.java.io/file
                  (str
                   "corpus/" (System/currentTimeMillis) ".png"))]
        (println "saving to " (.getAbsolutePath file))
        (javax.imageio.ImageIO/write @image "png" file)
        (println "sleeping")
        (Thread/sleep 500)
        (println "done sleeping")))
     (recur)))

(defn show-images [grabber image face screen]
  (let [detector (faces/detector)]
    (loop []
      (let [cap (grab-image grabber)
            detected-face (detector (images/normalize cap))]
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
      (future
        (build-corpus image face))
      (future
        (show-images grabber image face screen)))))
