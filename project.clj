(defproject creepykanye "0.1.0"
  :description "Creepily recognize people's faces and play their theme song."
  :url "http://example.com/FIXME"
  :license {:name "GPLv3"
            :url "https://www.gnu.org/copyleft/gpl.html"}
  :plugins [[lein-localrepo "0.5.2"]]
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.jodd/jodd-swingspy "3.4.5"]
                 [seesaw "1.4.4"]

                 ;; External/native dependencies. See babushka-deps/javacv.rb
                 ;; for installation instructions
                 [opencv-native "2.4.8"]
                 [javacv "0.7"]
                 [javacpp "0.7"]
                 [ffmpeg "2.1.1"]
                 [javacv-macosx-x86_64 "0.7"]]
  :profiles {:gui {:main creepykanye.core
                   :aot :all
                   :uberjar-name "gui.jar"}})
