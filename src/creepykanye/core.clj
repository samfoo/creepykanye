(ns creepykanye.core
  (:require [creepykanye.recognize :as recognize]
            [creepykanye.detect :as faces]
            [creepykanye.images :as images]
            [seesaw.graphics :as graphics]
            [seesaw.color :as color]
            [seesaw.core :refer :all]
            [seesaw.bind :as b])
  (:gen-class :main true)
  (:import [javax.imageio.ImageIO]
           [java.awt Font]
           [com.googlecode.javacv
            CanvasFrame
            FrameGrabber
            OpenCVFrameGrabber]
           [com.googlecode.javacv.cpp opencv_highgui]))

(defn- grab-image [grabber]
  (.grab grabber))

(defn- display-image [canvas image]
  (.showImage canvas image))

(defn- outline-faces [g faces]
  (doseq [face faces]
    (graphics/draw g
                   (graphics/rect (:x face) (:y face)
                                  (:width face) (:height face))
                   (graphics/style :foreground :green :stroke 5))))

(defn- label-faces [g faces]
  (doseq [face faces]
    (let [name (:label face)]
      (when (not (nil? name))
        (do
          (.setColor g (color/to-color "#FF0000"))
          (.setFont g (Font. "Helvetica" Font/BOLD 24))
          (.drawString g (str name) (:x face) (+ (:height face) (:y face))))))))

(defn paint-image [c g image faces]
  (when (not (nil? image))
    (.drawImage g image 0 0 (.getWidth image) (.getHeight image) nil))

  (when (not (empty? faces))
    (outline-faces g faces))

  (when (not (empty? faces))
    (label-faces g faces)))

(def FACE_IMG_SIZE 328)

(defn build-corpus [name id image faces]
  (loop []
    (when (and
           (not (empty? @faces))
           (not (nil? @image)))
      (let [normal (images/bi->grayscale @image)
            cropped-to-face (images/bi->cropped-to-face normal (first @faces)
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

(defn- predict-face [face image recognizer]
  (let [cropped (images/ipl->cropped-to-face image
                                             face
                                             FACE_IMG_SIZE
                                             FACE_IMG_SIZE)
        prediction (recognizer cropped)]
    (merge face prediction)))

(defn- detect-and-recognize [detector recognizer img]
  (let [detected-faces (detector img)]
    (map #(predict-face % img recognizer) detected-faces)))

(defn- detect [detector img]
  (detector img))

(defn show-images [grabber image faces screen recognize?]
  (let [detector (faces/detector)
        recognizer (when recognize?
                     (recognize/recognizer))]
    (loop []
      (let [cap (grab-image grabber)
            grayscale (images/ipl->grayscale cap)]
        (swap! faces (fn [fs]
                       (if recognize?
                         (detect-and-recognize detector recognizer grayscale)
                         (detect detector grayscale))))
        (reset! image (images/ipl->bi cap))
        (repaint! screen))
      (recur))))

(defn start-recording [image faces opts]
  (let [name (first opts)
        id (Integer/parseInt (second opts))]
    (println "-> saving images of" name "with id" id)
    (.start (Thread.
             (fn []
               (build-corpus name id image faces))))))

(defn -main [& args]
  (let [[command & opts] args
        image (atom nil)
        faces (atom [])
        grabber (OpenCVFrameGrabber. 0)]
    (.start grabber)
    (let [width (.getImageWidth grabber)
          height (.getImageHeight grabber)
          screen (canvas :id :screen
                         :paint (fn [c g] (paint-image c g @image @faces))
                         :background :black)
          window (frame :title "Camera"
                        :width width
                        :height height
                        :visible? true
                        :on-close :dispose
                        :content screen)]

      (condp = command
        "record" (start-recording image faces opts)
        nil)

      (.start (Thread.
               (fn []
                 (show-images grabber image
                              faces screen
                              (= "recognize" command))))))))
