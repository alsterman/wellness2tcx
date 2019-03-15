(ns mywellness-extractor.core
  (:require [clojure.data.json :as json]
            [clojure.xml :as xml]
            [mywellness-extractor.construct :refer [get-date
                                                    distance-meters
                                                    total-time-seconds
                                                    heart-rate-map
                                                    raw-data->samples
                                                    add-time-stamp
                                                    add-time-since-last
                                                    add-distance-calculated]]
            [mywellness-extractor.helper :refer [to-unix-time
                                                 timestamp
                                                 mean]]))

(defn read-raw-data-from-file
  [file-path]
  (-> (json/read-str (slurp file-path))
      (get "data")))

(defn track-point
  [one-sample]
  {:tag     :Trackpoint
   :content [
             {:tag :Time :content [(get one-sample :time)]}
             {:tag :DistanceMeters :content [(format "%.1f" (get one-sample :distance-calculated))]}
             {:tag :Cadence :content [(format "%.0f" (get one-sample :rpm))]}
             (if (nil? (get one-sample :hr))
               {:tag :UselessTag}
               {:tag     :HeartRateBpm
                :content [{:tag :Value :content [(str (get one-sample :hr))]}]})
             {:tag     :Extensions
              :content [{:tag     :TPX
                         :attrs   {"xmlns" "http://www.garmin.com/xmlschemas/ActivityExtension/v2"}
                         :content [{:tag :Speed :content [(format " %.1f" (/ (get one-sample :speed) 3.6))]}
                                   {:tag :Watts :content [(str (int (get one-sample :power)))]}]}]}]})

(defn tcx-map
  [samples start-timestamp total-time-seconds distance-meters {mean-heart-rate :hr-mean max-heart-rate :hr-max}]
  (let [max-speed-kph (reduce max (map (fn [x] (get x :speed)) samples))]
    {:tag     :TrainingCenterDatabase
     :attrs   {"xsi:schemaLocation" "http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2  http://www.garmin.com/xmlschemas/TrainingCenterDatabasev2.xsd"
               "xmlns:ns5"          "http://www.garmin.com/xmlschemas/ActivityGoals/v1"
               "xmlns:ns3"          "http://www.garmin.com/xmlschemas/ActivityExtension/v2"
               "xmlns:ns2"          "http://www.garmin.com/xmlschemas/UserProfile/v2"
               "xmlns"              "http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2"
               "xmlns:xsi"          "http://www.w3.org/2001/XMLSchema-instance"
               }
     :content [
               {:tag     :Activities
                :content [
                          {:tag     :Activity :attrs {:Sport "Biking"}
                           :content [
                                     {:tag :Id :content [start-timestamp]}
                                     {:tag     :Lap :attrs {:StartTime start-timestamp}
                                      :content [
                                                {:tag :TotalTimeSeconds :content [(format "%.0f" total-time-seconds)]}
                                                {:tag :DistanceMeters :content [(str distance-meters)]}
                                                {:tag :MaximumSpeed :content [(str (* max-speed-kph 1000))]}
                                                {:tag :AverageHeartRateBpm :content [(str mean-heart-rate)]}
                                                {:tag :MaximumHeartRateBpm :content [(str max-heart-rate)]}

                                                {:tag :Intensity :content ["Active"]}
                                                {:tag :TriggerMethod :content ["Manual"]}
                                                {:tag     :Track
                                                 :content (->> samples
                                                               (map track-point))}]}]}]}]})

  )

(defn raw-data->tcx-map
  [raw-data starttime-hh:mm:ss]
  (let [startdate-yyyy-MM-dd (get-date raw-data)
        starttime-ms (to-unix-time startdate-yyyy-MM-dd starttime-hh:mm:ss)

        distance-meters (distance-meters raw-data)
        total-time-seconds (total-time-seconds raw-data)

        heart-rate-map (heart-rate-map raw-data)
        max-heart-rate (apply max (vals heart-rate-map))
        mean-heart-rate (int (+ (mean (vals heart-rate-map)) 0.5))

        samples (-> (raw-data->samples raw-data heart-rate-map)
                    (add-time-stamp starttime-ms)
                    (add-time-since-last)
                    (add-distance-calculated))]
    (tcx-map samples (timestamp starttime-ms) total-time-seconds distance-meters {:hr-mean mean-heart-rate :hr-max max-heart-rate})))

(defn tcx-map->xml-str
  [tcx-map]
  (as-> (with-out-str (xml/emit tcx-map)) $
        (clojure.string/replace $ #"'" "\"")))


(as-> (read-raw-data-from-file "input8.json") $
      (raw-data->tcx-map $ "16:17:00")
      (tcx-map->xml-str $)
      (spit (str "test.tcx") $))

