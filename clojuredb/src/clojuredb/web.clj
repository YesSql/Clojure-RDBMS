(ns clojuredb.web
  (:use (compojure handler [core :only (GET POST defroutes)]))
  (:require    [compojure.route]
               [compojure.core ]
               [net.cgrand.enlive-html :as en]
               [ring.util.response :as response]
               [ring.adapter.jetty :as jetty]
               [clojuredb.core :as john])
  (:use clojure.pprint ))


(en/deftemplate homepage
  (en/xml-resource "index.html")
  [request]
  )

(en/deftemplate listpeople
  (en/xml-resource "people.htm")
  [request]
  [:#list_people :li]
  (en/clone-for [ {:keys [person_name]} (clojuredb.core/dump-table :person_name :person)] [:div] (en/content person_name)))

(en/deftemplate listprojects
  (en/xml-resource "projects.htm")
  [request]
  [:#list_projects :li] (en/clone-for [ {:keys [project_name]} (clojuredb.core/dump-table :project_name :project)] [:div] (en/content project_name)))

(en/deftemplate listskills
  (en/xml-resource "skills.htm")
  [request]
  [:#list_skills :li] (en/clone-for [ {:keys [skill_name]} (clojuredb.core/dump-table :skill_name :skill)] [:div] (en/content skill_name)))




(defn post-poachable
  [name]
  (clojuredb.core/insert-person name))

(defn post-skill
  [name]
  (clojuredb.core/insert-skill name))

(defn post-project
  [name]
  (clojuredb.core/insert-project name))

(defroutes app*
  (compojure.route/resources "/")
  (GET "/" request (homepage request))
  (GET "/people" request (listpeople request))
  (GET "/projects" request (listprojects request))
  (GET "/skills" request (listskills request))
  (POST "/postPoachable" request (post-poachable (-> request :params :person_name)) (response/redirect "/people"))
  (POST "/postProject" request (post-project (-> request :params :project_name)) (response/redirect "/projects"))
  (POST "/postSkill" request (post-skill (-> request :params :skill_name)) (response/redirect "/skills"))
  )

(def app (compojure.handler/site app*))


(defonce server (jetty/run-jetty #'app {:port 9090 :join? false}))