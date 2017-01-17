(ns tenforty.tools-test
  (:require [clojure.test :refer :all]
            [tenforty.tools :refer :all]
            [tenforty.core :refer :all]
            [tenforty.forms.ty2016 :refer [forms]]))

(deftest graphviz-test
  (testing "Graphviz export"
    (is (= (str "digraph tenforty {\n"
                "    \":a\" [label=\"a\"];\n"
                "    \":b\" [label=\"b\"];\n"
                "    \":b\" -> \":a\";\n"
                "}\n")
           (dump-graphviz
            (->FormSubgraph
             {:a (make-formula-line :a (cell-value :b))
              :b (make-number-input-line :b)}
             {})))))
  (testing "Graphviz smoke test"
    (is (string? (dump-graphviz forms))))
  (testing "Graphviz export with groups"
    (is (= (str "digraph tenforty {\n"
                "    subgraph \"cluster:g1\" {\n"
                "        subgraph \"cluster:g2\" {\n"
                "            \":c\" [label=\"c\"];\n"
                "        }\n"
                "        \":b\" [label=\"b\"];\n"
                "    }\n"
                "    \":a\" [label=\"a\"];\n"
                "    \":b\" -> \":a\";\n"
                "    \":c\" -> \":b\";\n"
                "}\n")
           (dump-graphviz
            (->FormSubgraph
             {:a (make-formula-line :a nil (apply + (cell-value :b)))
              :b (make-formula-line :b :g1 (apply + (cell-value :c)))
              :c (make-number-input-line :c :g2)}
             {nil #{:g1}
              :g1 #{:g2}
              :g2 #{}}))))))
