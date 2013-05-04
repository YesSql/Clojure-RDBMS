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

(defn get-people-skills
  [name]
  (map get-skill-and-experience (filter #(= (% :person_name) name) (clojuredb.core/list-people))))

(defn get-project-skills
  [name]
  (map get-skill-and-experience (filter #(= (% :project_name) name) (clojuredb.core/list-projects))))


(defn get-skill-and-experience [project]
  (str  (:skill_name project)" (" (:years_experience project) ")"))


(en/deftemplate listpeople
  (en/xml-resource "people.htm")
  [request]
  [:#list_people :li]
  (en/clone-for [ {:keys [person_name]} (clojuredb.core/dump-table :person_name :person)] [:#person_name] (en/content (str person_name))
                                                                                          [:#person_skills] (en/content (str " - " (clojure.string/join ", "(get-people-skills person_name)))))

  [:#peopleSelector :option]
  (en/clone-for [ {:keys [person_name]} (clojuredb.core/dump-table :person_name :person)] [:option] (en/content person_name))

  [:#skillSelector :option]
  (en/clone-for [ {:keys [skill_name]} (clojuredb.core/dump-table :skill_name :skill)] [:option] (en/content skill_name)))

(en/deftemplate listprojects
  (en/xml-resource "projects.htm")
  [request]
  [:#list_projects :li] (en/clone-for [ {:keys [project_name]} (clojuredb.core/dump-table :project_name :project)]
                                                                                          [:#project_name] (en/content (str project_name))
                                                                                          [:#project_skills](en/content (str " - " (clojure.string/join ", "(get-project-skills project_name)))))

  [:#projectSelector :option]
  (en/clone-for [ {:keys [project_name]} (clojuredb.core/dump-table :project_name :project)] [:option] (en/content project_name))

  [:#skillSelector :option]
  (en/clone-for [ {:keys [skill_name]} (clojuredb.core/dump-table :skill_name :skill)] [:option] (en/content skill_name)))

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

(defn assign-skill
  [person skill years]
  (clojuredb.core/assign-skill-to-person person skill (Integer/parseInt years)))

(defn assign-skill-to-project
  [project skill years]
  (clojuredb.core/assign-skill-to-project project skill (Integer/parseInt years)))

(defroutes app*
  (compojure.route/resources "/")
  (GET "/" request (homepage request))
  (GET "/people" request (listpeople request))
  (GET "/projects" request (listprojects request))
  (GET "/skills" request (listskills request))
  (POST "/postPoachable" request (post-poachable (-> request :params :person_name)) (response/redirect "/people"))
  (POST "/postProject" request (post-project (-> request :params :project_name)) (response/redirect "/projects"))
  (POST "/postSkill" request (post-skill (-> request :params :skill_name)) (response/redirect "/skills"))
  (POST "/postPersonSkill" request (assign-skill (-> request :params :person_name) (-> request :params :skill_name) (-> request :params :years)) (response/redirect "/people"))
  (POST "/postProjectSkill" request (assign-skill-to-project (-> request :params :project_name) (-> request :params :skill_name) (-> request :params :years)) (response/redirect "/projects"))
  )

(def app (compojure.handler/site app*))


(defonce server (jetty/run-jetty #'app {:port 9090 :join? false}))