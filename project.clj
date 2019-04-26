(defproject wellness2tcx "0.1.0"
  :description "Program to convert mywellness.com cycling data to Training Center XML"
  :url "https://github.com/alsterman/wellness2tcx"
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.clojure/data.json "0.2.6"]]
  :main wellness2tcx.core
  :aot [wellness2tcx.core])
