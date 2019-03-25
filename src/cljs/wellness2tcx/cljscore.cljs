(ns wellness2tcx.cljscore
  ;:require [wellness2tcx.core :refer [track-point]]
  )

(enable-console-print!)

(println "Hello World!")

;(defn render [])

(defn download
  [filename, text]
  (as-> js/document $
        (.createElement $ "a")
        (do (.setAttribute $ "href" (str "data:text/plain;charset=utf-8," (js/encodeURIComponent text)))
            (.setAttribute $ "download" filename)
            (set! (.-style.display $) "none")
            (js/document.body.appendChild $)
            (.click $)
            (js/document.body.removeChild $)
            ))
  )

(defn render []
  (set! (.-innerHTML (js/document.getElementById "app"))
        "<h1>Hi</h1>
        <form>
        Input MyWellness json: <input id=\"jsoninput\" type=\"text\" name=\"jsoninput\" style=\"background-color: grey\"><br>
        </form>
        <button id=\"mybutton\" type=\"button\" style=\"background-color: green\">Convert!</button>")
  ;(js/document.get)
  ;(as-> (js/document.getElementById "mybutton") $
  ;      (. $/add)
  ;((js/document.getElementById "mybutton") .addEventListener "click"  js/alert ))
  (.addEventListener (js/document.getElementById "mybutton") "click"
                     (fn []
                       (as-> (.-value (js/document.getElementById "jsoninput")) $
                             (download "test.tcx" $)
                             (print (type $))
                             )
                       ))
  )
(render)