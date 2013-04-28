(ns x
  (:require [clojure.java.jdbc :as sql])
)

(def db {:classname "com.mysql.jdbc.Driver"
         :subprotocol "mysql"
         :subname "//localhost:3306/peopleprojects"
         :user "root"
         :password "letmein"
         })


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
  )

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
      ["select person_id, person_name from person"]
      (println rows))))

(defn insert-person [name]
  (sql/insert! db :person [:person_name] [name])

  )