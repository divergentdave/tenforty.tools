(ns tenforty.tools
  (use clojure.java.io)
  (use tenforty.core)
  (require tenforty.forms.ty2015)
  (:gen-class))

(defn dump-graphviz
  []
  (let [lines (apply concat (map #(vals %) (vals tenforty.forms.ty2015/forms)))]
    (str "digraph tenforty {\n"
         (apply str (map #(str "    \""
                               (get-keyword %)
                               "\" [label=\""
                               (name (get-keyword %))
                               "\"];\n")
                         lines))
         (apply str (map
                      (fn [line] (apply str (map
                                             (fn [dep] (str "    \""
                                                            dep
                                                            "\" -> \""
                                                            (get-keyword line)
                                                            "\";\n"))
                                             (get-deps line))))
                      lines))
         "}\n"
         )
    ))

(defn -main
  [& args]
  (with-open [wrtr (writer "graph.gv")]
    (.write wrtr (dump-graphviz)))
  )
