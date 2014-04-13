(ns creepykanye.draw
  (:require [seesaw.graphics :as graphics]
            [seesaw.color :as color])
  (:import [java.awt Font]))

(def colors [:tomato
              :aqua
              :cyan
              :hotpink
              :lime
              :peru
              :yello
              :plum])

(defn- choose-color [i]
  (get colors (mod i (count colors))))

(defn outline [g objects]
  (loop [os objects
         i 0]
    (when (not (empty? os))
      (let [object (first os)]
        (graphics/draw g
                       (graphics/rect (:x object) (:y object)
                                      (:width object) (:height object))
                       (graphics/style :foreground (choose-color i) :stroke 5))
        (recur (rest os) (inc i))))))

(defn points
  ([g points] (points g points 0 0))
  ([g points x-offset y-offset] 
     (doseq [p points]
       (graphics/draw g
                      (graphics/circle (+ x-offset (:x p))
                                       (+ y-offset (:y p))
                                       1)
                      (graphics/style :foreground :lime
                                      :background :lime)))))

(defn region-points [g objects]
  (doseq [o objects]
    (when (not (empty? (:points o)))
      (points g (:points o) (:x o) (:y o)))))

(defn label [g objects]
  (loop [os objects
         i 0]
    (when (not (empty? os))
      (let [object (first os)
            label (:label object)]
        (when (not (nil? label))
          (do
            (.setColor g (color/to-color (choose-color i)))
            (.setFont g (Font. "Helvetica" Font/BOLD 24))
            (.drawString g
                         (str label)
                         (:x object)
                         (+ (:height object) (:y object) 24)))))
      (recur (rest os) (inc i)))))
