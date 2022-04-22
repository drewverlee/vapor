(defproject vapor "0.1.0-SNAPSHOT"
  :aot [vapor.start]
  :main vapor.start
  :exclusions [cljsjs/react
               cljsjs/react-dom
               sablono
               ring/ring-devel])
