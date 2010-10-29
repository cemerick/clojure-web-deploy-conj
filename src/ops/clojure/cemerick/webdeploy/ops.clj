(ns cemerick.webdeploy.ops
 (:use (clojure.contrib strint core))
 (:require
   [cemerick.webdeploy.ops.nodes :as webdeploy-nodes]
   pallet.core
   [org.jclouds.compute :as jcompute]
   [clojure.contrib.logging :as log]))

(defonce service
  (delay
    (let [id (System/getProperty "aws.id")
          secret-key (System/getProperty "aws.secret-key")]
      (assert (and id secret-key))
      (jcompute/compute-service "ec2" id secret-key :ssh :log4j))))

(defn deploy
  [prefix]
  (when-not webdeploy-nodes/warfile-path
    (throw (IllegalStateException. "No .war file defined, cannot deploy")))
  (pallet.core/converge {webdeploy-nodes/appserver 1}
    :compute @service :phase [:deploy] :prefix prefix))

(defn shutdown
  [prefix]
  (pallet.core/converge {webdeploy-nodes/appserver 0}
    :compute @service :prefix prefix))

(when-let [[operation prefix & options] (and (= "ops" (first *command-line-args*))
                                       (rest *command-line-args*))]
  (let [fn (-> operation .toLowerCase symbol resolve)
        prefix (.toLowerCase prefix)]
    (if fn
      (do
        (log/info (<< "Starting ~{fn} for '~{prefix}'"))
        (apply fn prefix options))
      (do
        (log/fatal (<< "No operation available for '~{operation}'"))
        (System/exit 1))))
  (shutdown-agents))