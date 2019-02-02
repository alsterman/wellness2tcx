(ns mywellness-extractor.core
  (:require [clojure.data.json :as json]))

(defn foo
  "I don't do a whole lot."
  [x]
  (println x "Hello, World!"))


(as-> (json/read-str (slurp "input.json")) $
      (get-in $ ["data" "analitics"])


            (as-> (get $ "descriptor") $$
                  (map (fn [x]
                         {:id   (get x "i")
                          :name (get-in x ["pr" "name"])}
                         ) $$)
                  (as-> (get-in $ ["samples"]) $$$
                        (map (fn[x] (get x "vs")) $$$)
                        (map (fn [desc]
                               :
                               ) $$)
                        )
                  ;(map (fn[x] (get x "vs")) $$)
                  ;(map (fn[x] (first)))
                  ;(first $$)
                  ))




(as-> (json/read-str (slurp "input.json")) $
      (get-in $ ["data" "analitics"])
      (let [desc (as-> (get $ "descriptor") $$
                       (map (fn [x]
                              {:id   (get x "i")
                               :name (get-in x ["pr" "name"])}
                              ) $$))]
        (get-in $ ["samples"])
        (map (fn [sample]
               {:time (get sample "t")}
               ) $)
        )
      )