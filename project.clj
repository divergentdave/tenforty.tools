(defproject tenforty.tools "0.1.0-SNAPSHOT"
  :description "Tools for analyzing U.S. taxes"
  :url "https://github.com/divergentdave/tenforty.tools"
  :license {:name "GNU GPL v2"
            :url "https://www.gnu.org/licenses/gpl-2.0.en.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [tenforty "0.1.0-SNAPSHOT"]]
  :main ^:skip-aot tenforty.tools
  :target-path "target/%s"
  :profiles {:dev {:plugins [[lein-cljfmt "0.5.6"]]}
             :uberjar {:aot :all}})
