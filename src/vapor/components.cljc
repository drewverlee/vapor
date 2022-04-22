(ns vapor.components
  (:require [rum.core :as rum]))

(def border-color "#a1d3df")

(rum/defc buy-button
  [{:keys [on-click text]}]
  [:button
   {:on-click on-click
    :style {:display "grid"
            :color "#rgb(52 48 52)"
            :background-color "rgb(160 130 173)"
            :font-size "30px"
            :border "white"
            :grid-template-columns "1fr"
            :place-items "center"
            :padding-left "5px"
            :padding-right "5px"
            :border-radius "10px"
            :box-shadow "rgb(94 64 106) 5px 3px 2px"}}
   [:p text]])

(rum/defc games
  [{:keys [on-click games]}]
  [:ul {:style {:display "grid"
                :grid-template-columns "minmax(300px, 800px)"}}
   (map (fn [{:keys [id name medium_url price] :as game}]
          (vector :li.highlight
                  {:key id
                   :on-click (on-click game)
                   :style
                   {:display "grid"
                    :grid-template-columns "2fr 1fr 1fr"
                    :place-items "center"
                    :min-width "60px"
                    :font-size "40px"
                    :border-radius "5px"
                    :border-width "2px"
                    :border-style "solid"
                    :margin "0px"
                    :padding-right "10px"
                    :padding-left "10px"
                    :list-style-type "none"
                    :border-color border-color}}
                  [:p name]
                  [:img. {:src medium_url :style {:height "100px"}}]
                  [:p (str "$" price)]))
        games)])
