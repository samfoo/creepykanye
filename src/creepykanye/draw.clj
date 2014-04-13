(ns creepykanye.draw
  (:require [seesaw.graphics :as graphics]
            [seesaw.color :as color])
  (:import [java.awt Font]))

(defn outline [g objects]
  (doseq [object objects]
    (graphics/draw g
                   (graphics/rect (:x object) (:y object)
                                  (:width object) (:height object))
                   (graphics/style :foreground :green :stroke 5))))

(defn label [g objects]
  (doseq [object objects]
    (let [label (:label object)]
      (when (not (nil? label))
        (do
          (.setColor g (color/to-color "#FF0000"))
          (.setFont g (Font. "Helvetica" Font/BOLD 24))
          (.drawString g
                       (str label)
                       (:x object)
                       (+ (:height object) (:y object))))))))
