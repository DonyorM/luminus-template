(ns leiningen.new.boot
  (:require [leiningen.new.common :refer :all]))

(defn boot-features [[assets options :as state]]
  (if (some #{"+boot"} (:features options))
    [(conj assets ["build.boot" "core/build.boot"])
     (-> options
         (assoc :boot true)
         (update :source-paths set)
         (update :resource-paths set))]
    state))

