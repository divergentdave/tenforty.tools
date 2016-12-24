(ns tenforty.tools-test
  (:require [clojure.test :refer :all]
            [tenforty.tools :refer :all]
            [tenforty.core :refer :all]))

(deftest graphviz-test
  (testing "Graphviz export"
    (is (= "digraph tenforty {\n    \":a\" [label=\"a\"];\n    \":b\" [label=\"b\"];\n    \":b\" -> \":a\";\n}\n"
           (dump-graphviz {:form {:a (makeline :a (cell-value :b))
                                  :b (->InputLine :b)}})))))

