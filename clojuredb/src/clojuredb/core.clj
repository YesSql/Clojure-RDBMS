(ns clojuredb.core
  (:require [clojure.java.jdbc :as jdbc] [clojure.java.jdbc.sql :as dsl]))


(def db {:classname "com.mysql.jdbc.Driver"
         :subprotocol "mysql"
         :subname "//localhost:3306/peopleprojects"
         :user "root"
         :password "letmein"
         })

(defn list-skills
  []
  (jdbc/with-connection db
    (jdbc/query db
      (dsl/select [:skill_name] :skill))))

;;redefine list-people with the SQL dsl
(defn list-people []
  (jdbc/with-connection db
    (jdbc/query db
      (dsl/select [:person.person_name :skill.skill_name :years_experience] :person
        (dsl/join :person_skill {:person.person_id :person_skill.person_id})
        (dsl/join :skill {:skill.skill_id :person_skill.skill_id})))))

(defn dump-table
  [column table]
  (jdbc/with-connection db
    (jdbc/query db
      (dsl/select [column] table))))

;;redefine with left outer joins necessary to skillless people
(defn list-people []
  (jdbc/with-connection db
    (jdbc/query db
      [(str " select a.person_name, c.skill_name, b.years_experience
             from person a
             inner join person_skill b on a.person_id = b.person_id
             inner join skill c on b.skill_id = c.skill_id")])))

(defn list-matches
  []
  (jdbc/with-connection db
    (jdbc/query db
      [(str "select person_name, project_name, count(*) as match_strength
              from
	                  person
	                  inner join person_skill on person.person_id = person_skill.person_id
                    inner join project_skill on person_skill.skill_id = project_skill.skill_id
	                  inner join project on project_skill.project_id = project.project_id
	                  inner join skill on skill.skill_id = project_skill.skill_id
              group by project_name, person_name")])))

(defn list-projects []
  (jdbc/with-connection db
    (jdbc/with-query-results rows
      [(str "select a.project_name, b.skill_name, c.years_experience from project a, skill b, project_skill c
          where a.project_id = c.project_id
          and c.skill_id = b.skill_id")]
      (println rows))))

;;redefine list-projects with SQL dsl
(defn list-projects []
  (jdbc/with-connection db
    (jdbc/query db
      (dsl/select [:project_name :skill_name :years_experience] :project
        (dsl/join :project_skill {:project.project_id :project_skill.project_id})
        (dsl/join :skill {:project_skill.skill_id :skill.skill_id})))))


(defn insert-person [name]
  (jdbc/insert! db :person [:person_name] [name]))

(defn insert-project [name]
  (jdbc/insert! db :project [:project_name] [name]))

(defn insert-skill [name]
  (jdbc/insert! db :skill [:skill_name] [name]))

(defn assign-skill-to-person [person-name skill-name years-experience]
  (jdbc/with-connection db
    (jdbc/transaction
      (jdbc/do-prepared
        (str "delete from person_skill where person_id = (select person_id from person where person_name = ?)
                                       and skill_id = (select skill_id from skill where skill_name = ?)") [person-name skill-name])

      (jdbc/do-prepared
        (str "insert into person_skill (person_id, skill_id, years_experience) values (
            (select person_id from person where person_name = ?),
            (select skill_id from skill where skill_name = ?),?)")[person-name skill-name years-experience]))))


(defn assign-skill-to-person_BROKEN [db person-name skill-name years-experience]
  (jdbc/with-connection db
    (println
      (dsl/insert :person_skill [:person_id :skill_id :years_experience][
                            (dsl/select :person_id :person (dsl/where {:person_name person-name}))
                             (dsl/select :skill_id :skill (dsl/where {:skill_name skill-name}))
                             years-experience]))))

(defn assign-skill-to-project [project-name skill-name years-experience]
  (jdbc/with-connection db
    (jdbc/transaction
      (jdbc/do-prepared
        (str "delete from project_skill where project_id = (select project_id from project where project_name = ?)
                                       and skill_id = (select skill_id from skill where skill_name = ?)") [project-name skill-name])
      (jdbc/do-prepared
      (str "insert into project_skill (project_id, skill_id, years_experience) values ("
        "(select project_id from project where project_name = ?),"
        "(select skill_id from skill where skill_name = ?),?)") [project-name skill-name years-experience]))))




(defn addData [db]
  (jdbc/with-connection db
    (insert-person "Rich")
    (insert-person "Stuart")
    (insert-project "Datomic")
    (insert-skill "Thinking")
    (insert-skill "Writing"))

  (assign-skill-to-person "Rich" "Thinking" 10)
  (assign-skill-to-person "Stuart" "Writing" 5)
  (assign-skill-to-project "Datomic" "Thinking" 4))

(defn run []
  (jdbc/with-connection db
    (jdbc/create-table :person
      [:person_id "int" "auto_increment" "primary key"]
      [:person_name "varchar(100)"])

    (jdbc/create-table :project
      [:project_id "int" "auto_increment" "primary key"]
      [:project_name "varchar(100)"])

    (jdbc/create-table :skill
      [:skill_id "int" "auto_increment" "primary key"]
      [:skill_name "varchar(100)"])

    (jdbc/create-table :person_skill
      [:person_skill_id "int" "auto_increment" "primary key"]
      [:person_id "int"]
      [:skill_id "int"]
      [:years_experience "int"])

    (jdbc/create-table :project_skill
      [:project_skill_id "int" "auto_increment" "primary key"]
      [:project_id "int"]
      [:skill_id "int"]
      [:years_experience "int"]))

;; no support for FK's

    (jdbc/with-connection db
     (jdbc/do-commands "alter table person_skill  add constraint FK_PERSON_SKILL_PERSON foreign key(person_id) references person(person_id)")
     (jdbc/do-commands "alter table person_skill  add constraint FK_PERSON_SKILL_SKILL foreign key(skill_id) references skill(skill_id)")
     (jdbc/do-commands "alter table project_skill add constraint FK_PROJECT_SKILL_PROJECT foreign key(project_id) references project(project_id)")
     (jdbc/do-commands "alter table project_skill add constraint FK_PROJECT_SKILL_SKILL foreign key(skill_id) references skill(skill_id)"))

    (addData db))



(defn cleanup []

(jdbc/with-connection db
  (jdbc/drop-table "person_skill")
  (jdbc/drop-table :project_skill)
  (jdbc/drop-table :person) ;;keywords can be used as table and/or column names
  (jdbc/drop-table "project")
  (jdbc/drop-table :skill)))


(list-people)
(list-projects)