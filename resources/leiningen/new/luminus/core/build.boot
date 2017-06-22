(set-env!
 :dependencies '[<<dependencies>>]<% if resource-paths %>
 :source-paths <<source-paths>>
 :resource-paths <<resource-paths>><% endif %>)

(deftask dev
  "Enables configuration for a development setup."
  []
  (set-env!
   :source-paths #(conj % "env/dev/clj")
   :resource-paths #(conj % "env/dev/resources")
   :dependencies #(concat % '[[prone "1.1.4"]
                              [ring/ring-mock "0.3.0"]
                              [ring/ring-devel "1.6.1"]<%if war %>
                              <<dev-http-server-dependencies>><% endif %>
                              [pjstadig/humane-test-output "0.8.2"]<% if dev-dependencies %>
                              <<dev-dependencies>><% endif %>]))
  (task-options! repl {:init-ns 'user})
  (require 'pjstadig.humane-test-output)
  (let [pja (resolve 'pjstadig.humane-test-output/activate!)]
    (pja))
  identity)

(deftask testing []
  "Enables configuration for testing."
  (dev)
  (set-env! :resource-paths #(conj % "env/test/resources"))
  identity)

(deftask run []
  (require '<<project-ns>>.core)
  (let [m (resolve '<<project-ns>>.core/-main)]
    (with-pass-thru _
      (m))))
