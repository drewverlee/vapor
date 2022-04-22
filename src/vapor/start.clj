(ns vapor.start
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [vapor.giant-bomb-api.core :as giant-bomb-api]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.session :refer [wrap-session]]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [ring.middleware.gzip :refer [wrap-gzip]]
            [ring.util.request :refer [body-string]]
            [ring.util.response :refer [not-found]]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.edn :as edn]
            [clojure.data.codec.base64 :as base64]
            [rum.core :as rum]
            [odoyle.rum :as orum]
            [vapor.core :as v])
  (:gen-class))

(def port 3000)

(defn page [initial-state]
  (binding [;; this binding causes the new matches triggered by `insert-all-games`
            ;; to be stored locally, so they don't affect other users
            ;; that happen to be requesting this route at the same time
            orum/*matches* (volatile! {})]
    ;; if there are any todos in the user's ring session,
    ;; insert them into the o'doyle session.
    ;; we are only doing this for side-effects.
    (v/insert-all-games v/initial-session (or (:games initial-state) []))

    ;; render the html
    (-> "template.html" io/resource slurp
        (str/replace "{{content}}" (rum/render-html (v/app-root nil)))
        ;; save the games in a hidden div that the client can read when it loads
        ;; we are using base64 to prevent breakage (i.e. if a todo contains angle brackets)
        (str/replace "{{initial-state}}" (-> (pr-str initial-state)
                                             (.getBytes "UTF-8")
                                             base64/encode
                                             (String. "UTF-8"))))))

(defmulti handler (juxt :request-method :uri))

(defmethod handler [:get "/"]
  [request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (page (:session request))})

(defmethod handler [:post "/query"]
  [request]
  (let [query (body-string request)
        games (->> query
                   giant-bomb-api/query->games
                   ;;a bit of data wrangling and mocking
                   (map (fn [{{medium_url :medium_url} :image
                             name :name
                             id :id}]
                          {:id id
                           :name name
                           :medium_url medium_url
                           :price 25})))]
    {:status 200
     :headers {"Content-Type" "application/edn"}
     :body (pr-str games)}))

(defmethod handler :default
  [_]
  (not-found "Page not found"))

(defn run-server [handler-fn]
  (run-jetty (-> handler-fn
                 (wrap-resource "public")
                 wrap-session
                 wrap-content-type
                 wrap-gzip)
             {:port port :join? false})
  (println (str "Started server on http://localhost:" port)))

(defn -main [& args]
  (run-server handler))
