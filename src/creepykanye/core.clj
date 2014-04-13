(ns creepykanye.core
  (:require [creepykanye.recognize :as recognize]
            [creepykanye.detect :as detect]
            [creepykanye.images :as images]
            [creepykanye.corpus :as corpus]
            [creepykanye.draw :as draw]
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

(defn paint-image [c g image faces]
  (when (not (nil? image))
    (.drawImage g image 0 0 (.getWidth image) (.getHeight image) nil))

  (when (not (empty? faces))
    (draw/region-points g faces))

  (when (not (empty? faces))
    (draw/outline g faces))

  (when (not (empty? faces))
    (draw/label g faces)))

(defn build-corpus [name id image faces]
  (loop []
    (when (and
           (not (empty? @faces))
           (not (nil? @image)))
      (let [normal (images/bi->grayscale @image)
            cropped-to-face (images/bi->cropped-to-face normal (first @faces)
                                                        corpus/NORMAL_W
                                                        corpus/NORMAL_H)]
        (corpus/add-face id name cropped-to-face))
      (print ".")
      (flush)
      (Thread/sleep 500))
    (recur)))

(defn- predict-face [face image recognizer]
  (let [cropped (images/ipl->cropped-to-face image
                                             face
                                             corpus/NORMAL_W
                                             corpus/NORMAL_H)
        prediction (recognizer cropped)]
    (merge face prediction)))

(defn- key-points-for [face image]
  (let [cropped (images/ipl->cropped-to-face image
                                             face
                                             corpus/NORMAL_W
                                             corpus/NORMAL_H)
        points (detect/key-points cropped)]
    (assoc face :points points)))

(defn- detect [detector img]
  (let [faces (detector img)]
    (map #(key-points-for % img) faces)))

(defn- detect-and-recognize [detector recognizer img]
  (let [detected-faces (detect detector img)]
    (map #(predict-face % img recognizer) detected-faces)))

(defn show-images [grabber image faces screen recognize?]
  (let [detector (detect/face-detector)
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
