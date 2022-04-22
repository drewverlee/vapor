(ns vapor.start
  (:require [cljs.tools.reader :as r]
            [vapor.core :as v]
            [rum.core :as rum]
            [lambdaisland.fetch :as fetch]
            [lambdaisland.fetch.edn]
            [odoyle.rules :as o]))

(defn read-string [s]
  (binding [r/*suppress-read* true]
    (r/read-string {:read-cond :preserve :eof nil} s)))

;; read from initial state and insert any existing games
(->> (.querySelector js/document "#initial-state")
     .-textContent
     js/atob
     read-string
     :games
     (swap! v/*session v/insert-all-games))

;; What for a query and send it back to the server.
(add-watch v/*session :search-for-game
           (fn [_ _ _ session]
             (when-let [query (v/get-search-for-game session)]
               (-> (fetch/post "/query" {:body query})
                   (.then #(:body %))
                   (.then (fn [data]
                            (-> session
                                (v/insert-all-games data)
                                o/fire-rules)))))))

;; mount the root component
(rum/hydrate (v/app-root v/*session)
  (.querySelector js/document "#app"))
