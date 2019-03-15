(ns mywellness-extractor.construct)


(defn distance-meters
  ""
  [raw-data]
  (as-> (get raw-data "data") $
        (filter (fn [x] (= (get x "property") "HDistance")) $)
        (first $)
        (get $ "rawValue")
        (* $ 1000)))

(defn total-time-seconds
  [raw-data]
  (as-> (get raw-data "data") $
        (filter (fn [x] (= (get x "property") "Duration")) $)
        (first $)
        (get $ "rawValue")
        (* $ 60)))

(defn heart-rate-map
  "Returns a map of with timedelta as key and heartrate as value"
  [raw-data]
  (let [hr-raw (get-in raw-data ["analitics" "hr"])
        hr-key (map (fn [x] (get x "t")) hr-raw)
        hr-val (map (fn [x] (get x "hr")) hr-raw)]
    (zipmap hr-key hr-val)))


(defn construct-raw-samples
  "Constructs raw data samples"
  [raw-data heart-rate-map]
  (->> (get-in raw-data ["analitics" "samples"])
       (map (fn [one-sample]
              {:time-delta (get one-sample "t")
               :power      (get-in one-sample ["vs" 0])
               :rpm        (get-in one-sample ["vs" 1])
               :speed      (get-in one-sample ["vs" 2])
               :distance   (get-in one-sample ["vs" 3])
               :hr         (get heart-rate-map (get one-sample "t"))}
              ))))
