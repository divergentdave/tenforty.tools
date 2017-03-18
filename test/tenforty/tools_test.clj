(ns tenforty.tools-test
  (:require [clojure.test :refer :all]
            [clojure.string :as str]
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

(deftest evaluate-test
  (testing "Not enough arguments"
    (is (str/includes?
         (with-out-str (-main "evaluate"))
         "Usage: lein run evaluate "))
    (is (str/includes?
         (with-out-str (-main "evaluate" "foo.edn"))
         "Usage: lein run evaluate ")))
  (testing "Missing colon"
    (is (str/includes?
         (with-out-str (-main "evaluate" "foo.edn" "tenforty/i_forgot_a_colon"))
         "Error: ")))
  (testing "Functional test"
    (is (str/includes?
         (with-out-str
           (-main "evaluate" "test/tenforty/fixture.edn"
                  ":tenforty.forms.ty2016.f1040/senior_blind_total"))
         "senior_blind_total = 4"))))

(deftest sensitivity-fn-test
  (testing "Marginal tax rate"
    (let [forms (->FormSubgraph
                 {:taxable_income (make-number-input-line :taxable_income)
                  :tax (make-formula-line
                        :tax
                        (condp > (cell-value :taxable_income)
                          9275 (* 0.10 (cell-value :taxable_income))
                          37650 (+ 927.50 (* 0.15 (- (cell-value :taxable_income) 9275)))
                          91150 (+ 5183.75 (* 0.25 (- (cell-value :taxable_income) 37650)))
                          190150 (+ 18558.75 (* 0.28 (- (cell-value :taxable_income) 91150)))
                          413350 (+ 46278.75 (* 0.33 (- (cell-value :taxable_income) 190150)))
                          415050 (+ 119934.75 (* 0.35 (- (cell-value :taxable_income) 413350)))
                          (+ 120529.75 (* 39.6 (- (cell-value :taxable_income) 415050)))))}
                 {nil #{}})
          situation (->EdnTaxSituation {:values {:taxable_income 100000}})]
      (is (<= (Math/abs (- (sensitivity forms situation :tax :taxable_income)
                           0.28))
              0.001)))))

(deftest sensitivity-cli-test
  (testing "Functional test"
    (let [output (with-out-str
                   (-main
                    "sensitivity" "test/tenforty/fixture.edn"
                    ":tenforty.forms.ty2016.f1040/exemptions_number"))]
      (is (or (str/includes? output "0.999") (str/includes? output "1.000"))))))
