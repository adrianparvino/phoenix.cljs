(ns phoenix.interop
  (:require ["phoenix" :as phoenix]))

(defn connect
  ([path]
   (connect path (fn [_] nil)))
  ([path open-cb]
   (let [socket (phoenix/Socket. path)]
     (.onOpen socket open-cb)
     (.connect socket)
     socket)))

(defn join
  ([^phoenix/Socket socket channel-name handlers]
   (join socket channel-name handlers nil (fn [_] nil) (fn [_] nil) (fn [_] nil)))
  ([^phoenix/Socket socket channel-name handlers payload]
   (join socket channel-name handlers nil (fn [_] nil) (fn [_] nil) (fn [_] nil)))
  ([^phoenix/Socket socket channel-name handlers payload ok-cb]
   (join socket channel-name handlers nil ok-cb (fn [_] nil) (fn [_] nil)))
  ([^phoenix/Socket socket channel-name handlers payload ok-cb error-cb]
   (join socket channel-name handlers nil ok-cb error-cb (fn [_] nil)))
  ([^phoenix/Socket socket channel-name handlers payload ok-cb error-cb timeout-cb]
   (let [^phoenix/Channel handle (.channel socket channel-name (clj->js payload))]
     (doseq [[event cb] handlers] (do (.on handle (name event) #(cb (js->clj % :keywordize-keys true)))))
     (-> handle
         ^phoenix/Push (.join)
         ^phoenix/Push (.receive "ok"      (fn [reply] (ok-cb (js->clj reply :keywordize-keys true))))
         ^phoenix/Push (.receive "error"   (fn [reply] (error-cb (js->clj reply :keywordize-keys true))))
         ^phoenix/Push (.receive "timeoutmode" (fn [reply] (timeout-cb (js->clj reply :keywordize-keys true)))))
     handle)))

(defn push
  ([^phoenix/Channel handle event]
   (push handle event {} 0 (fn [_] nil) (fn [_] nil) (fn [_] nil)))
  ([^phoenix/Channel handle event payload]
   (push handle event payload 0 (fn [_] nil) (fn [_] nil) (fn [_] nil)))
  ([^phoenix/Channel handle event payload timeout]
   (push handle event payload timeout (fn [_] nil) (fn [_] nil) (fn [_] nil)))
  ([^phoenix/Channel handle event payload timeout ok-cb]
   (push handle event payload timeout ok-cb (fn [_] nil) (fn [_] nil)))
  ([^phoenix/Channel handle event payload timeout ok-cb error-cb]
   (push handle event payload timeout ok-cb error-cb (fn [_] nil)))
  ([^phoenix/Channel handle event payload timeout ok-cb error-cb timeout-cb]
   (-> handle
       ^phoenix/Push (.push (name event) (clj->js payload) timeout)
       ^phoenix/Push (.receive "ok"      (fn [reply] (ok-cb (js->clj reply :keywordize-keys true))))
       ^phoenix/Push (.receive "error"   (fn [reply] (error-cb (js->clj reply :keywordize-keys true))))
       ^phoenix/Push (.receive "timeout" (fn [reply] (timeout-cb (js->clj reply :keywordize-keys true)))))
   handle))

(defn leave
  ([^phoenix/Channel handle timeout]
   (push handle timeout (fn [_] nil) (fn [_] nil) (fn [_] nil)))
  ([^phoenix/Channel handle timeout ok-cb]
   (push handle timeout ok-cb (fn [_] nil) (fn [_] nil)))
  ([^phoenix/Channel handle timeout ok-cb error-cb]
   (push handle timeout ok-cb error-cb (fn [_] nil)))
  ([^phoenix/Channel handle timeout ok-cb error-cb timeout-cb]
   (-> handle
       ^phoenix/Push (.leave timeout)
       ^phoenix/Push (.receive "ok"      (fn [reply] (ok-cb (js->clj reply :keywordize-keys true))))
       ^phoenix/Push (.receive "error"   (fn [reply] (error-cb (js->clj reply :keywordize-keys true))))
       ^phoenix/Push (.receive "timeout" (fn [reply] (timeout-cb (js->clj reply :keywordize-keys true)))))
   nil))
