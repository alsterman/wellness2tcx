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




(let [raw-data (-> (json/read-str (slurp "input8.json"))
                   (get "data"))

      date-yyyy-MM-dd (get-date raw-data)
      time-hh:mm:ss "16:17:00"

      starttime-ms (to-unix-time date-yyyy-MM-dd time-hh:mm:ss)

      distance-meters (distance-meters raw-data)
      total-time-seconds (total-time-seconds raw-data)
      heart-rate-map (heart-rate-map raw-data)


      max-heart-rate (apply max (vals heart-rate-map))
      mean-heart-rate (int (+ (mean (vals heart-rate-map)) 0.5))


      samples (-> (raw-data->samples raw-data heart-rate-map)
                  (add-time-stamp starttime-ms)
                  (add-time-since-last)
                  (add-distance-calculated))


      max-speed-kph (reduce max (map (fn [x] (get x :speed)) samples))

      tcx-map {:tag     :TrainingCenterDatabase
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
                                               {:tag :Id :content [(timestamp starttime-ms)]}
                                               {:tag     :Lap :attrs {:StartTime (timestamp starttime-ms)}
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
                                                                         (map (fn [sample-point]
                                                                                {:tag     :Trackpoint
                                                                                 :content [
                                                                                           {:tag :Time :content [(get sample-point :time)]}
                                                                                           {:tag :DistanceMeters :content [(format "%.1f" (get sample-point :distance-calculated))]}
                                                                                           {:tag :Cadence :content [(format "%.0f" (get sample-point :rpm))]}
                                                                                           (if (nil? (get sample-point :hr))
                                                                                             {:tag :UselessTag}
                                                                                             {:tag     :HeartRateBpm
                                                                                              :content [{:tag :Value :content [(str (get sample-point :hr))]}]})
                                                                                           {:tag     :Extensions
                                                                                            :content [{:tag     :TPX
                                                                                                       :attrs   {"xmlns" "http://www.garmin.com/xmlschemas/ActivityExtension/v2"}
                                                                                                       :content [
                                                                                                                 {:tag :Speed :content [(format " %.1f" (/ (get sample-point :speed) 3.6))]}
                                                                                                                 {:tag :Watts :content [(str (int (get sample-point :power)))]}]}]}]})))}]}]}]}]}]

  ;(as-> (with-out-str (xml/emit tcx-map)) $
  ;      (clojure.string/replace $ #"'" "\"")
  ;      (spit (str "test-json2tcx-" (timestamp starttime-ms) ".tcx") $))
  samples
  )

