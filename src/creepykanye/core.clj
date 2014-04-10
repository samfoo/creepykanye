(ns creepykanye.core
  (:require [creepykanye.recognize :as recognize]
            [creepykanye.detect :as faces]
            [creepykanye.images :as images]
            [seesaw.graphics :as graphics]
            [seesaw.core :refer :all]
            [seesaw.bind :as b])
  (:gen-class :main true)
  (:import [javax.imageio.ImageIO]
           [java.awt.image BufferedImage]
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

(defn normalized-image [image]
  (let [w (/ (.getWidth image) 2)
        h (/ (.getHeight image) 2)
        scaled (.getScaledInstance image w h java.awt.Image/SCALE_FAST)
        gray (BufferedImage. w h BufferedImage/TYPE_BYTE_GRAY)]
    (let [g (.getGraphics gray)]
      (.drawImage g scaled 0 0 nil)
      (.dispose g))
    gray))

(defn bi->ipl [bi]
  (com.googlecode.javacv.cpp.opencv_core$IplImage/createFrom bi))

(defn build-corpus [name id image face]
  (loop []
    (when (and
           (not (nil? @face))
           (not (nil? @image)))
      (let [normal (normalized-image @image)
            file (clojure.java.io/file
                  (format "corpus/%02d-%s-%d.png"
                          id
                          name
                          (System/currentTimeMillis)))]
        (javax.imageio.ImageIO/write normal "png" file)
        (print ".")
        (flush)
        (Thread/sleep 500)))
     (recur)))

(defn show-images [grabber raw image face screen]
  (let [detector (faces/detector)]
    (loop []
      (let [cap (grab-image grabber)
            detected-face (detector (images/normalize cap))]
        (reset! raw cap)
        (reset! face detected-face)
        (reset! image (.getBufferedImage cap))
        (repaint! screen))
      (recur))))

(defn start-recording [image face opts]
  (let [name (first opts)
        id (Integer/parseInt (second opts))]
    (println "-> saving images of" name "with id" id)
    (.start (Thread.
             (fn []
               (build-corpus name id image face))))))

(defn start-recognizing [raw face opts]
  (let [recognizer (recognize/recognizer)]
    (loop []
      (when (not (nil? @face))
        (let [normal (recognize/normalize-ipl @raw)
              who (recognizer normal)]
          (print "-> i think this is" who)
          (flush))))))

(defn -main [& args]
  (let [[command & opts] args
        image (atom nil)
        raw (atom nil)
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

      (condp = command
        "record" (start-recording image face opts)
        "recognize" (start-recognizing raw face opts)
        nil)

      (.start (Thread.
               (fn []
                 (show-images grabber raw image face screen)))))))
