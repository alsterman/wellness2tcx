(ns mywellness-extractor.construct
  (:require [mywellness-extractor.helper :refer [timestamp]]))

(defn get-date
  [raw-data]
  (get raw-data "date"))

(defn distance-meters
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


(defn raw-data->samples
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

(defn add-time-since-last
  [samples]
  (let [last-seen-time (atom 0)]
    (map (fn [one-sample]
           (as-> @last-seen-time $
                 (- (get one-sample :time-delta) $)
                 (assoc one-sample :time-since-last $)
                 (do
                   (reset! last-seen-time (get one-sample :time-delta))
                   $))
           ) samples)))

(defn add-time-stamp
  [samples starttime-ms]
  (map (fn [one-sample]
         (->> (get one-sample :time-delta)
              (* 1000)
              (+ starttime-ms)
              (timestamp)
              (assoc one-sample :time))
         ) samples))

(defn add-distance-calculated
  "Adds field :distance-calculated which is calculated from time-since-last and speed"
  [samples]
  (let [distance-sum (atom 0)]
    (map (fn [one-sample]
           (as-> (get one-sample :speed) $
                 (/ $ 3.6)
                 (* $ (get one-sample :time-since-last))
                 (swap! distance-sum (fn [x] (+ x $))))
           (assoc one-sample :distance-calculated @distance-sum)
           ) samples)))
