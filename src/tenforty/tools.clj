(ns tenforty.tools
  (:require [clojure.java.io :refer [reader writer]])
  (:require clojure.edn)
  (:require [tenforty.core :refer :all])
  (:import [tenforty.core NumberInputLine])
  (:require [tenforty.forms.ty2016 :refer [forms]])
  (:gen-class))

(defn- graphviz-nodes-group
  [forms group prefix]
  (let [lines (vals (:lines forms))]
    (str
     (apply str (map (fn [child-group]
                       (str prefix "subgraph \"cluster" child-group "\" {\n"
                            (graphviz-nodes-group forms
                                                  child-group
                                                  (str prefix "    "))
                            prefix "}\n"))
                     (get (:groups forms) group)))
     (apply str (map (fn [line] (str prefix "\""
                                     (get-keyword line)
                                     "\" [label=\""
                                     (name (get-keyword line))
                                     "\"];\n"))
                     (filter (fn [line] (= group (get-group line))) lines))))))

(defn dump-graphviz
  [forms]
  (let [lines (vals (:lines forms))]
    (str "digraph tenforty {\n"
         (graphviz-nodes-group forms nil "    ")
         (apply str (map
                     (fn [line] (apply str (map
                                            (fn [dep] (str "    \""
                                                           dep
                                                           "\" -> \""
                                                           (get-keyword line)
                                                           "\";\n"))
                                            (get-deps line))))
                     lines))
         "}\n")))

(defn- parse-keyword [string]
  (keyword (subs string 1)))

(defn sensitivity [forms situation kw-f kw-x]
  (let [initial-value (lookup-value situation kw-x)
        step 0.01
        left-value (- initial-value step)
        right-value (+ initial-value step)
        keys [:object :values kw-x]
        left-situation (assoc-in situation keys left-value)
        right-situation (assoc-in situation keys right-value)
        left-context (make-context forms left-situation)
        right-context (make-context forms right-situation)]
    (/ (- (calculate right-context kw-f) (calculate left-context kw-f))
       (* step 2))))

(defn -main
  [& args]
  (case (first args)
    "graph"
    (with-open [wrtr (writer "graph.gv")]
      (.write wrtr (dump-graphviz forms)))
    "evaluate"
    (if (< (count args) 3)
      (println "Usage: lein run evaluate <file.edn> <:form/line> [<:form/line> ...]")
      (if (every? #(= (first %) \:) (nthrest args 2))
        (with-open [input (java.io.PushbackReader. (reader (second args)))]
          (let [object (clojure.edn/read input)
                situation (->EdnTaxSituation object)
                context (make-context forms situation)]
            (dorun (map
                    #(let [kw (parse-keyword %)]
                       (if (get (:lines (:form-subgraph context)) kw)
                         (println (str % " = " (calculate context kw)))
                         (println (str "No such line: " %))))
                    (nthrest args 2)))))
        (println "Error: Line keys must begin with a colon")))
    "sensitivity"
    (if (not= (count args) 3)
      (println "Usage: lein run sensitivity <file.edn> <:form/line>")
      (if (not= (first (nth args 2)) \:)
        (println "Error: Line keys must begin with a colon")
        (with-open [input (java.io.PushbackReader. (reader (second args)))]
          (let [object (clojure.edn/read input)
                situation (->EdnTaxSituation object)
                context (make-context forms situation)
                kw (parse-keyword (nth args 2))]
            (if (get (:lines (:form-subgraph context)) kw)
              (do
                (println (str "Initial value: " (calculate context kw)))
                (dorun (map #(when (nil? (get-group %))
                               (when (instance? NumberInputLine %)
                                 (when (lookup-value situation (get-keyword %))
                                   (println (format "%6.3f for %s"
                                                    (sensitivity forms situation kw (get-keyword %))
                                                    (get-keyword %))))))
                            (vals (:lines forms)))))
              (println (str "No such line: " kw)))))))
    (do (println "Usage: lein run <command>.")
        (println)
        (println "Supported commands:")
        (println "  graph")
        (println "  evaluate")
        (println "  sensitivity"))))
