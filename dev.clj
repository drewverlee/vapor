(require
  '[figwheel.main :as figwheel]
  '[vapor.start-dev :refer [-main]])

(future
  (figwheel/-main "--build" "dev"))

(future
 (-main))
