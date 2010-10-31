(ns cemerick.webdeploy.ops.nodes
 (:use clojure.contrib.core)
 (:require
   [cemerick.webdeploy.ops.crate :as webdeploy-crate]
   (pallet core resource)
   (pallet.crate java
     [tomcat :as tomcat]
     [automated-admin-user :as admin])
   [clojure.contrib.logging :as log]))

(def warfile-path
  ; this is just making things easier for demonstration purposes
  ; for a real project, you'd likely want to parameterize this via 
  ; a system property (or use the maven dependency plugin to copy
  ; a separately-build .war to a known location), so as to separate
  ; the production of the war artifact from its deployment
  (or (-?>> "target"
        java.io.File.
        .listFiles
        (filter #(.endsWith (.getName %) ".war"))
        first
        .getAbsolutePath)
    (when-not *compile-files*
      (log/fatal "No .war artifact available in target dir, deployment operations will FAIL"))))

(pallet.core/defnode appserver
  {:os-family :ubuntu
   :os-description-matches "10.10"
   :min-ram 1024
   ; can optionally require cloud-specific images and node sizes if you like
   ; (strongly recommended for real usage!)
   ; :image-id "us-east-1/ami-508c7839"
   ; :hardware-id "m1.small"
   :inbound-ports [22 80]}
  :bootstrap (pallet.resource/phase 
               (admin/automated-admin-user))
  :configure (pallet.resource/phase
               (pallet.crate.java/java :openjdk)
               (tomcat/tomcat))
  :deploy (pallet.resource/phase
            (webdeploy-crate/tomcat-deploy warfile-path)))