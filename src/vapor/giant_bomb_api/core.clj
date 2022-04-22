(ns vapor.giant-bomb-api.core
  (:require [hato.client :as hc]
            [clojure.java.io :as io]))

(def api-key (->
               "secrets.edn"
               io/resource
               slurp
               read-string
               :giant-bomb
               :api_key))

(defn query->games
  [query]
  (->>
    (hc/get "https://www.giantbomb.com/api/search/"
            {:as :json
             :query-params {:api_key api-key
                            :format "json"
                            :query query
                            :resources "game"
                            :field_list "name,image,id"
                            ;;TODO handle pagination
                            :limit 5}})
    :body
    :results))
