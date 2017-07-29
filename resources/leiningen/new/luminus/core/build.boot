(set-env!
 :dependencies '[<<dependencies>>]<% if resource-paths %>
 :source-paths <<source-paths>>
 :resource-paths <<resource-paths>><% endif %>)
<% if cljs %>
(require '[adzerk.boot-cljs :refer [cljs]]
         '[adzerk.boot-cljs-repl :refer [cljs-repl]])
<% endif %><% if sassc-config-params %>
(require '[deraen.boot-sass :refer [sass]])
<% endif %>
(deftask dev
  "Enables configuration for a development setup."
  []
  (set-env!
   :source-paths #(conj % "env/dev/clj"<% if cljs %> <<dev-cljs.source-paths>> <% endif %>)
   :resource-paths #(conj % "env/dev/resources")
   :dependencies #(concat % '[[prone "1.1.4"]
                              [ring/ring-mock "0.3.0"]
                              [ring/ring-devel "1.6.1"]<% if war %>
                              <<dev-http-server-dependencies>><% endif %>
                              [pjstadig/humane-test-output "0.8.2"]<% if dev-dependencies %>
                              <<dev-dependencies>><% endif %>]))
  (task-options! repl {:init-ns 'user})
  (require 'pjstadig.humane-test-output)
  (let [pja (resolve 'pjstadig.humane-test-output/activate!)]
    (pja))
  identity)

(deftask testing
  "Enables configuration for testing."
  []
  (dev)
  (set-env! :resource-paths #(conj % "env/test/resources"))<% if cljs %>
  (merge-env! :source-paths <<test-cljsbuild.builds.test.source-paths>>)<% endif %>
  identity)

(deftask run
  "Runs the project without building class files."
  []
  (require '<<project-ns>>.core)
  (let [m (resolve '<<project-ns>>.core/-main)]
    (with-pass-thru _
      (m))))

(deftask build
  "Builds an uberjar of this project that can be run with java -jar"
  []
  (set-env!
   :source-paths #(conj % "env/prod/clj")
   :resource-paths #(conj % "env/prod/resources"))
  (comp
   (aot :namespace #{'<<project-ns>>.core})
   (uber)
   (jar :file "<<name>>.jar" :main '<<project-ns>>.core)
   (sift :include #{#"<<name>>.jar"})
   (target)))
<% if war %>
(require '[boot.immutant :refer [gird]])
(deftask uberwar
  "Creates a war file ready to deploy to wildfly."
  []
  (comp
   (uber :as-jars true)
   (aot :all true)
   (gird :init-fn '<<project-ns>>.handler/init)
   (war)
   (target)))

(deftask dev-war
  "Creates a war file for development and testing."
  []
  (comp
   (dev)
   (gird :dev true :init-fn '<<project-ns>>.handler/init)
   (war)
   (target)))
<% endif %><% if cljs %>
(require '[clojure.java.io :as io])
(require '[crisptrutski.boot-cljs-test :refer [test-cljs]])
(deftask figwheel
  "Runs figwheel and enables reloading."
  []
  (dev)
  (require '[powerlaces.boot-figreload :refer [reload]])
  (let [reload (resolve 'powerlaces.boot-figreload/reload)]
    (comp
     (reload {:client-opts {:debug true}})
     (cljs-repl)
     (cljs)
     (run))))
<% endif %>
