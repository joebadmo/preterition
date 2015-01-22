(ns repo-store.database
  (:require [yesql.core :refer [defquery]]
            [clojure.java.jdbc :as jdbc]
            [clj-time.coerce :as c]
            [clj-time.core :as t]))

(def db-spec {:classname "org.postgresql.Driver"
              :subprotocol "postgresql"
              :subname "//localhost:5432/jmoon"
              :user "jmoon"})

(defquery create-table! "repo_store/sql/create.sql")
(defquery insert-document! "repo_store/sql/insert.sql")

(create-table! db-spec)
(insert-document!
  db-spec
  "Hello!"
  "joe"
  "hello/joe"
  "<div>hello world</div>"
  (c/to-sql-time (t/today))
  true
  "")
