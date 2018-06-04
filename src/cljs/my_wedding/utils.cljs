(ns my-wedding.utils
  "Modified from https://gist.github.com/jasich/21ab25db923e85e1252bed13cf65f0d8")

(def speed 500)
(def moving-frequency 15)

(def scrolling? (atom false))

(defn cur-doc-top []
  (+ (.. js/document -body -scrollTop) (.. js/document -documentElement -scrollTop)))

(defn element-top [elem top]
  (if (.-offsetParent elem)
    (let [client-top (or (.-clientTop elem) 0)
          offset-top (.-offsetTop elem)]
      (+ top client-top offset-top (element-top (.-offsetParent elem) top)))
    top))

(defn scroll-to
  [elem]
  (let [hop-count (/ speed moving-frequency)
        doc-top (cur-doc-top)
        gap (/ (- (element-top elem 0) doc-top) hop-count)]
    (when-not @scrolling?
      (reset! scrolling? true)
      (js/setTimeout #(reset! scrolling? false) (+ speed 100))
      (doseq [i (range 1 (inc hop-count))]
        (let [hop-top-pos (* gap i)
              move-to (+ hop-top-pos doc-top)
              timeout (* moving-frequency i)]
          (.setTimeout js/window (fn []
                                   (.scrollTo js/window 0 move-to))
                       timeout))))))
