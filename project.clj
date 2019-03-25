(defproject wellness2tcx "0.1.0"
  :description "Program to convert mywellness.com cycling data to Training Center XML"
  :url "https://github.com/alsterman/wellness2tcx"
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.clojure/data.json "0.2.6"]
                 [org.clojure/clojurescript "1.10.439" :scope "provided"]]
  :main wellness2tcx.application
  :aot [wellness2tcx.application]


  :source-paths ["src/clj" "src/cljs" "src/cljc"]
  :test-paths ["test/clj" "test/cljc"]

  :plugins [[lein-cljsbuild "1.1.7"]]

  :cljsbuild {:builds
              [{:id "app"
                :source-paths ["src/cljs" "src/cljc"]

                :compiler {
                           ;:main wellness2tcx.clj
                           :optimizations :whitespace
                           :output-to "resources/public/js/compiled/wellness2tcx.js"
                           :output-dir "target"
                           :pretty-print true}
                }]}
  )


