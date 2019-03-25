(ns wellness2tcx.application
  (:gen-class)
  (:require
    [clojure.data.json :as json]
    ;[wellness2tcx.core :refer [raw-data->tcx-map
    ;                                   tcx-map->xml-str]]
    ))

(defn read-raw-data-from-file
  [file-path]
  (-> (json/read-str (slurp file-path))
      (get "data")))

;(defn -main [filename starttime:hh:mm:ss output-filename]
;  (println (str "Converting " filename " to tcx"))
;  (as-> (read-raw-data-from-file filename) $
;        (raw-data->tcx-map $ starttime:hh:mm:ss)
;        (tcx-map->xml-str $)
;        (spit output-filename $)))
