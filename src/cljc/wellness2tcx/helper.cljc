(ns wellness2tcx.helper)

;(defn to-unix-time
;  [yyyy-MM-dd-string hh:mm:ss-string]
;  (let [SDF (new java.text.SimpleDateFormat "yyyy-MM-dd'T'HH:mm:ss'Z'")
;        inputstr (str yyyy-MM-dd-string "T" hh:mm:ss-string "Z")]
;    (as-> (. SDF parse inputstr) $
;          (. $ getTime))))
;
(defn timestamp
  [UNIX-time]
  #?(:clj (let [SDF (new java.text.SimpleDateFormat "yyyy-MM-dd'T'HH:mm:ss'Z'")]
            (.format SDF (new java.util.Date UNIX-time)))

     :cljs
     )
  )
;
;(timestamp (to-unix-time "2019-01-01" "17:10:10"))

(defn mean
  [coll]
  (/ (reduce + coll) (count coll)))

;(mean [1 2 3 4])
