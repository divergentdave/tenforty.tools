(defproject tenforty.tools "0.1.0-SNAPSHOT"
  :description "Tools for analyzing U.S. taxes"
  :url "https://github.com/divergentdave/tenforty.tools"
  :license {:name "GNU GPL v2"
            :url "https://www.gnu.org/licenses/gpl-2.0.en.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]]
  :main ^:skip-aot tenforty.tools
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
