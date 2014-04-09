(defproject creepykanye "0.1.0-SNAPSHOT"
  :description "Creepily recognize people's faces and play their theme song."
  :url "http://example.com/FIXME"
  :license {:name "GPLv3"
            :url "https://www.gnu.org/copyleft/gpl.html"}
  :plugins [[lein-localrepo "0.5.2"]]
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [opencv-native "2.4.8"]
                 [javacv "0.7"]
                 [javacpp "0.7"]
                 [javacv-macosx-x86_64 "0.7"]])
