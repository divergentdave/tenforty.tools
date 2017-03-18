(ns tenforty.tools
  (use clojure.java.io)
  (require clojure.edn)
  (use tenforty.core)
  (require tenforty.forms.ty2016)
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

(defn -main
  [& args]
  (case (first args)
    "graph"
    (with-open [wrtr (writer "graph.gv")]
      (.write wrtr (dump-graphviz tenforty.forms.ty2016/forms)))
    "evaluate"
    (if (< (count args) 3)
      (println "Usage: lein run evaluate <file.edn> <:form/line> [<:form/line> ...]")
      (if (every? #(= (first %) \:) (nthrest args 2))
        (with-open [input (java.io.PushbackReader. (reader (second args)))]
          (let [object (clojure.edn/read input)
                situation (->EdnTaxSituation object)
                context (make-context tenforty.forms.ty2016/forms situation)]
            (dorun (map
                    #(let [kw (parse-keyword %)]
                       (if (get (:lines (:form-subgraph context)) kw)
                         (println (str % " = " (calculate context kw)))
                         (println (str "No such line: " %))))
                    (nthrest args 2)))))
        (println "Error: Line keys must begin with a colon")))
    (do (println "Usage: lein run <command>.")
        (println)
        (println "Supported commands:")
        (println "  graph")
        (println "  evaluate"))))
