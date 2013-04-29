(ns user
  (:require [clojure.java.jdbc :as sql])
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
      ["select person_id, person_name from person"]
      (println rows))))

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
  )