(ns cemerick.webdeploy.hello-world
  (:use
    [ring.util.servlet :only (defservice)]
    [compojure.core :only (GET)])
  (:gen-class
    :extends javax.servlet.http.HttpServlet))

(defservice
  (GET "*" {:keys [uri]}
    (format "<html><p>%s</p>URL requested: %s</html>"
      "<b>Deploying with pallet, jclouds, and Hudson is fantastic! :-)</b>"
      uri)))