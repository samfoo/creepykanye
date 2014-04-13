(ns creepykanye.corpus)

(def NORMAL_W 328)
(def NORMAL_H 328)

(defn- file->id [f]
  (let [name (.getName f)
        re-result (re-find #"^(\d+).*$" name)
        id-str (second re-result)]
    (Integer/parseInt id-str)))

(defn- file->name [f]
  (let [name (.getName f)
        re-result (re-find #"^(\d+)-(\w+).*$" name)]
    (get re-result 2)))

(defn- people []
  (let [files (filter
               #(.isFile %)
               (file-seq
                (clojure.java.io/file "corpus/")))]
    (reduce
     (fn [m f]
       (assoc m (file->id f) (file->name f)))
     {}
     files)))

(def people-memo (memoize people))

(defn add-face [id name image]
  (let [file (clojure.java.io/file
              (format "corpus/%02d-%s-%d.png"
                      id
                      name
                      (System/currentTimeMillis)))]
    (javax.imageio.ImageIO/write image "png" file)))

(defn id->name [id]
  (get (people-memo) id))

