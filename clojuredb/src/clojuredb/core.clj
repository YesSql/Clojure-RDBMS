(ns user
  (:require [clojure.java.jdbc :as sql]
            [clojure.java.jdbc.sql :as dsl]
             )
)

(def db {:classname "com.mysql.jdbc.Driver"
         :subprotocol "mysql"
         :subname "//localhost:3306/peopleprojects"
         :user "root"
         :password "letmein"
         })

(defn cleanup []

(sql/with-connection db
  (sql/drop-table :person)
  (sql/drop-table :project)
  (sql/drop-table :skill)
  (sql/drop-table :person_skill)
  (sql/drop-table :project_skill)
  )
)



(defn list-people []
  (sql/with-connection db
    (sql/with-query-results rows
      [(str "select a.person_name, b.skill_name, c.years_experience from person a, skill b, person_skill c"
            " where a.person_id = c.person_id "
            " and c.skill_id = b.skill_id")]
      (println rows))))


;;redefine list-people with the SQL dsl
(defn list-people []
  (println (sql/query db
      (dsl/select [:person.person_name :skill.skill_name :years_experience] :person
      (dsl/join :person_skill {:person.person_id :person_skill.person_id})
      (dsl/join :skill {:skill.skill_id :person_skill.skill_id})
))))


(defn list-projects []
  (sql/with-connection db
    (sql/with-query-results rows
      [(str "select a.project_name, b.skill_name, c.years_experience from project a, skill b, project_skill c"
         " where a.project_id = c.project_id "
         " and c.skill_id = b.skill_id")]
      (println rows)
      )
    )
 )

;;redefine list-projects with SQL dsl
(defn list-projects []
  (sql/with-connection db
    (println (sql/query db
      (dsl/select [:project_name :skill_name :years_experience] :project
        (dsl/join :project_skill {:project.project_id :project_skill.project_id})
        (dsl/join :skill {:project_skill.skill_id :skill.skill_id}))))))


(defn insert-person [name]
  (sql/insert! db :person [:person_name] [name])
)

(defn insert-project [name]
  (sql/insert! db :project [:project_name] [name])
)

(defn insert-skill [name]
  (sql/insert! db :skill [:skill_name] [name])
  )

(defn assign-skill-to-person [db person-name skill-name years-experience]
  (sql/with-connection db
    (sql/do-prepared
      (str "insert into person_skill (person_id, skill_id, years_experience) values ("
          "(select person_id from person where person_name = ?),"
          "(select skill_id from skill where skill_name = ?),?)")[person-name skill-name years-experience]
      )
    )
  )



(defn assign-skill-to-project [db person-name skill-name years-experience]
  (sql/with-connection db
    (sql/do-prepared
      (str "insert into project_skill (project_id, skill_id, years_experience) values ("
        "(select project_id from project where project_name = ?),"
        "(select skill_id from skill where skill_name = ?),?)") [person-name skill-name years-experience]
     )
    )
  )


(defn addData [db]
  (sql/with-connection db
    (insert-person "Rich")
    (insert-person "Stuart")
    (insert-project "Datomic")
    (insert-skill "Thinking")
    (insert-skill "Writing")
    )

    (assign-skill-to-person db "Rich" "Thinking" 10)
    (assign-skill-to-person db "Stuart" "Writing" 5)
    (assign-skill-to-project db "Datomic" "Thinking" 4)

  )

(defn run []
  (sql/with-connection db
    (sql/create-table :person
      [:person_id "int" "auto_increment" "primary key"]
      [:person_name "varchar(100)"]))

  (sql/with-connection db
    (sql/create-table :project
      [:project_id "int" "auto_increment" "primary key"]
      [:project_name "varchar(100)"]))

  (sql/with-connection db
    (sql/create-table :skill
      [:skill_id "int" "auto_increment" "primary key"]
      [:skill_name "varchar(100)"]))

  (sql/with-connection db

    (sql/create-table :person_skill
      [:person_skill_id "int" "auto_increment" "primary key"]
      [:person_id "int"]
      [:skill_id "int"]
      [:years_experience "int"]
      )

    (sql/create-table :project_skill
      [:project_skill_id "int" "auto_increment" "primary key"]
      [:project_id "int"]
      [:skill_id "int"]
      [:years_experience "int"]
      )
    )

    (addData db)
    (list-people)
    (list-projects)
  )