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
