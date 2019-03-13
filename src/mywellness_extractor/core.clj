(ns mywellness-extractor.core
  (:require [clojure.data.json :as json]
            [clojure.xml :as xml]
            [mywellness-extractor.helper :refer [to-unix-time
                                                 timestamp
                                                 mean]]))



;(def SDF (new java.text.SimpleDateFormat "yyyy-MM-dd'T'HH:mm:ss'Z'"))
;(as-> (. SDF parse "2019-01-05T13:49:20Z") $
;      (. $ getTime)
;      (. SDF parse "2020-01-05T13:49:20Z")
;      (. $ getTime)
;      )
;(defn )


(let [raw-data (-> (json/read-str (slurp "input8.json"))
                   (get "data"))

      date-yyyy-MM-dd (get raw-data "date")

      time-hh:mm:ss "16:17:00"

      starttime-ms (to-unix-time date-yyyy-MM-dd time-hh:mm:ss)


      distance-meters (as-> (get raw-data "data") $
                            (filter (fn [x] (= (get x "property") "HDistance")) $)
                            (first $)
                            (get $ "rawValue")
                            (* $ 1000))

      total-time-seconds (as-> (get raw-data "data") $
                               (filter (fn [x] (= (get x "property") "Duration")) $)
                               (first $)
                               (get $ "rawValue")
                               (* $ 60))

      desc (->> (get-in raw-data ["analitics" "descriptor"])
                (map (fn [x]
                       {:id   (get x "i")
                        :name (get-in x ["pr" "name"])})))

      heart-rate-map  (let [hr-raw (get-in raw-data ["analitics" "hr"])
                            hr-key (map (fn [x] (get x "t")) hr-raw)
                            hr-val (map (fn [x] (get x "hr")) hr-raw)]
                        (zipmap hr-key hr-val))

      max-heart-rate  (apply max (vals heart-rate-map))
      mean-heart-rate (int (+ (mean (vals heart-rate-map)) 0.5))

      samples (->> (get-in raw-data ["analitics" "samples"])
                   (map (fn [one-sample]
                          {:time  (->> (get one-sample "t")
                                       (* 1000)
                                       (+ starttime-ms)
                                       (timestamp))
                           :power (get-in one-sample ["vs" 0])
                           :rpm   (get-in one-sample ["vs" 1])
                           :speed (get-in one-sample ["vs" 2])
                           :distance (get-in one-sample ["vs" 3])
                           :hr (get heart-rate-map (get one-sample "t"))}
                          )))

      samples (let [last-seen (atom -1)]
                (map (fn [x]
                       (if (= @last-seen (int (get x :distance)))
                         (assoc x :distance nil)
                         ;x
                         (do (reset! last-seen (int (get x :distance)))
                             ;(println (get x :distance))
                             x))


                       ) samples)

                )
      samples (let [distance-sum (atom 0)]
                (map (fn [x]
                       (as-> (get x :speed) $
                             (/ $ 3.6)
                             (* $ 5) ; approx 5 between every reading
                             (swap! distance-sum (fn [x] (+ x $))))
                       (assoc x :distance-calculated @distance-sum)
                         )samples))


      max-speed-kph (reduce max (map (fn [x] (get x :speed)) samples))

      tcx-map {
                         :tag     :TrainingCenterDatabase
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

                                                                    {:tag :Intensity    :content ["Active"]}
                                                                    {:tag :TriggerMethod :content ["Manual"]}
                                                                    {:tag     :Track
                                                                     :content (->> samples
                                                                                   (map (fn [sample-point]

                                                                                          {:tag     :Trackpoint
                                                                                           :content [
                                                                                                     {:tag :Time :content [(get sample-point :time)]}
                                                                                                     ;{:tag :DistanceMeters :content [(format "%.1f" (get sample-point :distance))]}
                                                                                                     (if (nil? (get sample-point :distance-calculated))
                                                                                                       {:tag :UselessTag}
                                                                                                       {:tag :DistanceMeters :content [(format "%.1f" (get sample-point :distance-calculated))]})
                                                                                                     {:tag :Cadence :content [(format "%.0f" (get sample-point :rpm))]}
                                                                                                     (if (nil? (get sample-point :hr))
                                                                                                       {:tag :UselessTag}
                                                                                                       {:tag :HeartRateBpm
                                                                                                        :content [{:tag :Value :content [(str (get sample-point :hr))]}]})
                                                                                                     {:tag     :Extensions
                                                                                                      :content [{:tag     :TPX
                                                                                                                 :attrs   {"xmlns" "http://www.garmin.com/xmlschemas/ActivityExtension/v2"}
                                                                                                                 :content [
                                                                                                                           {:tag :Speed :content [(format " %.1f" (/ (get sample-point :speed) 3.6))]}
                                                                                                                           {:tag :Watts :content [(str (int (get sample-point :power)))]}
                                                                                                                           ]}]}

                                                                                                     ]})))}

                                                                    ]}]}]}]}]
  ;samples
  ;raw-data
  ;heart-rate-map
  ;max-heart-rate
  ;mean-heart-rate
  ;dateandtimestr
  ;date-yyyy-MM-dd
  ;(spit (str "json2tcx-" (timestamp starttime-ms) ".tcx") tcx-str)
  ;tcx-map
  ;(with-out-str (xml/emit tcx-map))
  ;(xml/emit tcx-map)

  (as-> (with-out-str (xml/emit tcx-map)) $
       (clojure.string/replace  $ #"'" "\"")
       (spit (str "json2tcx-" (timestamp starttime-ms) ".tcx") $))


  ;(map (fn [x] (get x :distance)) samples)

  ;(let [last-seen (atom -1)]
  ;  (doseq [keyval samples]
  ;    (if (= @last-seen (int (get keyval :distance)))
  ;      nil
  ;      (do (reset! last-seen (int (get keyval :distance)))
  ;          (println (get keyval :distance))))
  ;    )
  ;  @last-seen
  ;  )

  ;(let [last-seen (atom -1)]
  ;  (map (fn [x]
  ;         (if (= @last-seen (int (get x :distance)))
  ;           (assoc x :distance nil)
  ;           ;x
  ;           (do (reset! last-seen (int (get x :distance)))
  ;               ;(println (get x :distance))
  ;               x))
  ;
  ;
  ;         ) samples)
  ;
  ;    )
    ;@last-seen


  )

;
;(as-> (get-in temp ["analitics" "hr"]) $
;      (map (fn [x] {(get x "t") x}) $)
;      (conj $))