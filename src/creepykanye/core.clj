(ns creepykanye.core
  (:require [creepykanye.recognize :as recognize]
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

(def FACE_IMG_SIZE 328)

(defn build-corpus [name id image face]
  (loop []
    (when (and
           (not (nil? @face))
           (not (nil? @image)))
      (let [normal (images/bi->grayscale @image)
            cropped-to-face (images/bi->cropped-to-face normal @face
                                                        FACE_IMG_SIZE
                                                        FACE_IMG_SIZE)
            file (clojure.java.io/file
                  (format "corpus/%02d-%s-%d.png"
                          id
                          name
                          (System/currentTimeMillis)))]
        (javax.imageio.ImageIO/write cropped-to-face "png" file)
        (print ".")
        (flush)
        (Thread/sleep 500)))
     (recur)))

(defn show-images [grabber image face screen]
  (let [detector (faces/detector)]
    (loop []
      (let [cap (grab-image grabber)
            detected-face (detector
                            (images/ipl->grayscale cap))]
        (reset! face detected-face)
        (reset! image (images/ipl->bi cap))
        (repaint! screen))
      (recur))))

(defn start-recording [image face opts]
  (let [name (first opts)
        id (Integer/parseInt (second opts))]
    (println "-> saving images of" name "with id" id)
    (.start (Thread.
             (fn []
               (build-corpus name id image face))))))

(defn predict-frames [image face recognizer]
  (loop []
    (let [f @face
          i @image]
      (when (not (or (nil? f) (nil? i)))
        (let [cropped (-> i
                          (images/bi->grayscale)
                          (images/bi->cropped-to-face f
                                                      FACE_IMG_SIZE
                                                      FACE_IMG_SIZE))
              who (recognizer (images/bi->ipl cropped))]
          (println "-> i think this is" who "\b")))
      (recur))))

(defn start-recognizing [image face opts]
  (let [recognizer (recognize/recognizer)]
    (.start (Thread. (fn [] (predict-frames image face recognizer))))))

(defn -main [& args]
  (let [[command & opts] args
        image (atom nil)
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
        "recognize" (start-recognizing image face opts)
        nil)

      (.start (Thread.
               (fn []
                 (show-images grabber image face screen)))))))
