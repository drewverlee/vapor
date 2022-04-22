(ns vapor.core
  (:require [odoyle.rules :as o]
            [odoyle.rum :as orum]
            [clojure.spec.alpha :as s]
            [vapor.components :as c]))

(s/def ::name string?)
(s/def ::id int?)
(s/def ::price int?)
(s/def ::medium_url string?)
(s/def ::game (s/nilable (s/keys :req-un [::name ::id ::medium_url ::price])))
(s/def ::games (s/nilable (s/coll-of ::game)))
(s/def ::add-game-to-cart ::game)
(s/def ::selected-game ::game)
(s/def ::cart ::games)
(s/def ::search-for-game (s/nilable string?))
(s/def ::view-cart boolean?)

(defn insert-all-games
  [session games]
  (o/insert session ::global {::games games}))

(defn get-search-for-game [session]
  (-> (o/query-all session ::get-search-for-game)
      first
      :search-for-game))

(def rules
  (o/ruleset
   {::get-search-for-game
    [:what
     [::global ::search-for-game search-for-game]]
    ::get-all-games
    [:what
     [::global ::games games]]
    ::get-cart
    [:what
     [::derived ::cart cart]]
    ::add-game-to-cart
    [:what
     [id ::name name]
     [id ::id id]
     [id ::price price]
     [id ::medium_url medium_url]
     :then-finally
     (->> (o/query-all o/*session* ::add-game-to-cart)
          (o/insert o/*session*  ::derived ::cart)
          o/reset!)]}))

(defn view-game
  [{:keys [*session]}]
  (fn [game]
    #(swap! *session
           (fn [session]
             (-> session
                 (o/insert ::global ::selected-game game)
                 (o/insert ::global ::search-for-game nil)
                 o/fire-rules)))))

(def components
  (orum/ruleset
   {app-root
    [:what
     [::global ::selected-game selected-game?]
     [::global ::view-cart view-cart?]
     [::global ::search-for-game search-for-game?]
     [::derived ::cart cart]
     :then
     (let [*session (orum/prop)
           {:keys [medium_url name id price] :as game} selected-game?]
       [:div {:style {:height "100vh"
                      :width "100vw"
                      :font "30px 'Helvetica Neue', Helvetica, Arial, sans-serif"
                      :line-height "1.4em"
                      :background "#1a111e"
                      :color "#a082ad"}}
        [:div {:style {:display "grid"
                       :grid-template-columns "1fr 1fr"
                       :grid-template-rows "1fr auto"
                       :place-items "center"}}
         [:img {:src "pictures/vapor-icon-white.png"
                :style {:height "180px"
                        :grid-column-start "1"
                        :grid-column-end "3"
                        :grid-row "1"}}]
         [:div {:on-click #(swap! *session
                                  (fn [session]
                                    (-> session
                                        (o/insert ::global ::view-cart true)
                                        (o/insert ::global ::search-for-game nil)
                                        (o/insert ::global ::selected-game nil)
                                        o/fire-rules)))
                :style {:grid-column-start "2"
                        :grid-column-end "3"
                        :grid-row "1"
                        :display "grid"
                        :grid-template-columns "1fr 1fr"
                        :place-items "center"}}
          [:img {:src "pictures/cart-icon.png"}]
          [:h2 (count cart)]]
         [:div {:style {:grid-column-start "1"
                        :grid-column-end "3"}}
          (cond
            selected-game?

            [:section.game-page
             [:div.present-the-game {:style {:grid-area "present-the-game"
                                             :display "grid"
                                             :align-items "center"
                                             :justify-content "center"}}
              [:div [:h1 name]]
              [:img {:style {:object-fit "scale-down"
                             :max-height "500px"}
                     :src medium_url}]]
             [:div.search-for-another-game {:style {:padding-left "20px"}}
              (game-input {:placeholder "keep searching..."
                           :*session *session})]
             [:div.buy-game
              (c/buy-button {:text "Add to cart $10"
                             :on-click #(swap! *session
                                               (fn [session]
                                                 (-> session
                                                     (o/insert ::global ::view-cart true)
                                                     (o/insert id {::id id ::name name ::price price ::medium_url medium_url})
                                                     (o/insert ::global ::selected-game nil)
                                                     o/fire-rules)))})]
             [:div
              (repeat 2
                      [:div {:style {:border-color "white"
                                     :grid-template-rows "0.5fr 4fr"
                                     :justify-items "center"}}
                       [:p "I'm waiting, get in here!"]
                       [:img {:src "/pictures/devi_overwatch.jpg"
                              :style {:object-fit "contain"
                                      :height "200px"}}]])]]
            (and view-cart? (not search-for-game?))
            (checkout {:*session *session})
            :else
            [:section {:style {:display "grid"
                               :grid-template-rows "1fr 5fr"
                               :height "100%"
                               :justify-items "center"
                               :align-content "center"
                               :align-items "baseline"}}
             (game-input {:placeholder "mario cart 3..."
                          :*session *session})
             (game-list {:*session *session})])]]])]
    checkout
    [:what
     [::derived ::cart cart]
     :then
     (let [prop (orum/prop)]
       [:section {:style {:display "grid"
                          :place-items "center"
                          :gap "40px"}}
        [:h1 {:style {:margin 0}}  "Your Shopping Cart"]
        [:div (c/games {:games cart
                        :on-click (view-game prop)})]
        (c/buy-button {:on-click #(println "BUY GAME")
                       :text (str "total: $" (->> cart
                                                  (map :price)
                                                  (reduce + 0)))})
        (game-input (assoc prop :placeholder "Keep Searching"))])]
    game-input
    [:then
     (let [{:keys [*session initial-text placeholder] :or {initial-text ""}} (orum/prop)
           *text (orum/atom initial-text)
           text @*text
           on-finish #(reset! *text "")]
       [:input {:style {:background-image "url(/pictures/vapor-input.jpg)"
                        :color "#f0d7f0"
                        :opacity "0.70"
                        :font-size "30px"
                        :border-radius "10px"
                        :padding "10px"
                        :border-color c/border-color
                        :box-shadow "0 0 10px #719ECE"
                        :font-style "italic"}
                :type "text"
                :placeholder placeholder
                :autoFocus true
                :value text
                :on-blur on-finish
                :on-change (fn [e]
                             (reset! *text (-> e .-target .-value)))
                :on-key-down (fn [e]
                               (case (.-keyCode e)
                                 13
                                 (swap! *session
                                        (fn [session]
                                          (-> session
                                              (o/insert ::global ::search-for-game text)
                                              (o/insert ::global ::selected-game nil)
                                              o/fire-rules)))
                                 27
                                 (on-finish)
                                 nil))}])]
    game-list
    [:what
     [::global ::games games]
     :then
     [:section
      (c/games {:on-click (view-game (orum/prop))
                :games games})]]}))

(def initial-session
  (-> (reduce o/add-rule (o/->session) (concat rules components))
      (o/insert ::global {::games []})
      (o/insert ::global ::selected-game nil)
      (o/insert ::global ::search-for-game nil)
      (o/insert ::global ::add-game-to-cart nil)
      (o/insert ::global ::view-cart false)
      (o/insert ::derived {::cart []})
      o/fire-rules))

(defonce *session (atom initial-session))

;; when figwheel reloads this file,
;; get all the facts from the previous session
;; and insert them into the new session
;; so we don't wipe the state clean every time
(swap! *session
       (do
         (println "reload!")
         (fn [session]
           (->> (o/query-all session)
                (reduce o/insert initial-session)
                o/fire-rules))))
