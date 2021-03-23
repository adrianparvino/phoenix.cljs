(ns phoenix.async
  (:require [phoenix.interop :as interop]
            [cljs.core.async :refer [promise-chan chan put!]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defn connect [path]
  (interop/connect path))

(defn join [^phoenix/Socket socket channel-name & event-names]
  (let [result (promise-chan)
        event-channels (for [event-name event-names]
                         (let [ret (chan)]
                           [event-name #(put! ret %) ret]))
        event-cbs (for [[event-name cb _] event-channels]
                    [event-name cb])
        chans (for [[_ _ chan] event-channels] chan)
        handle (interop/join socket channel-name
                             event-cbs
                             #(put! result ["ok" %])
                             #(put! result ["error" %])
                             #(put! result ["timeout" %]))]
    `[~handle ~result ~@chans]))

(defn push [^phoenix/Channel handle event payload timeout]
  (let [result (promise-chan)]
    (interop/push handle event payload timeout
                  #(put! result ["ok" %])
                  #(put! result ["error" %])
                  #(put! result ["timeout" %]))
    result))
