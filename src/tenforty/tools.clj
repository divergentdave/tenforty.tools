(ns tenforty.tools
  (use clojure.java.io)
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

(defn -main
  [& args]
  (with-open [wrtr (writer "graph.gv")]
    (.write wrtr (dump-graphviz tenforty.forms.ty2016/forms))))
