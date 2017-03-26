# tenforty.tools

Tools for analyzing U.S. taxes

## Installation

See https://github.com/divergentdave/tenforty for complete installation instructions. Java and Leiningen must be installed first. The `bootstrap` script will clone this repository and perform other initialization.

## Usage

This project can be run from the command line with `lein run`. There are multiple subcommands, as follows.

### evaluate

```sh
lein run evaluate /path/to/file.edn :tenforty.forms.ty2016.formname/linename ...
```

`evaluate` takes a file path and any number of keywords as arguments. It reads tax situation inputs from the given file, and prints a computed value of a line for each keyword provided. If there are values missing from the tax situation file, it will throw an exception.

### graph

```sh
lein run graph
```

`graph` writes a description of a [graphviz](http://www.graphviz.org/) graph to the file graph.gv. This graph contains a node for each line in the supported tax forms and directed edges showing the dependencies between lines. This file can then be rendered using a command like `dot -Tsvg -O graph.gv`.

### sensitivity

```sh
lein run sensitivity /path/to/file.edn :tenforty.forms.ty2016.formname/linename
```

`sensitivity` takes a file path and one keyword as arguments. It reads tax situation inputs from the file, evaluates the tax form line indicated by the given keyword, and then numerically calculates the partial derivative of that line's value with respect to each of its inputs. The initial value and all partial derivatives are then printed to the terminal.

## Input files

Tax situation data is read from files in [edn format](https://github.com/edn-format/edn) (a subset of Clojure syntax). The file should contain one object, a map, with two keys `:values` and `:groups`. Under `:values`, there should be another map, where each key is a keyword for a tax form line, and each value is the corresponding value to be placed in that line, as a number, `true`, or `false`. Under the `:groups` key, there should be a keyword for each child repeating group. (See [the tenforty documentation](https://https://github.com/divergentdave/tenforty/blob/master/README.md#object-model) for more explanation) Under each child group's key, there should be a list of maps, each with the same structure as the overall tax situation file (with a `:values` key and a `:groups` key). These child groups can be nested within each other arbitrarily deep.

### Example

```edn
{:values {:tenforty.forms.ty2016.f1040/line_a 1000000
          :tenforty.forms.ty2016.f1040/line_b true
          :tenforty.forms.ty2016.f1040/line_c false
          :tenforty.forms.ty2016.f1040/line_d 19.99
          }
 :groups {:w2 [{:values {:tenforty.forms.ty2016.w2/line_e 10
                         :tenforty.forms.ty2016.w2/line_f 12
                         }
                :groups {}}
               {:values {:tenforty.forms.ty2016.w2/line_e 8
                         :tenforty.forms.ty2016.w2/line_f 14
                         }
                :groups {}}]}}
```

## License

Copyright © 2016-2017 David Cook

Distributed under the GNU General Public License, version 2, as modified in `LICENSE`.
