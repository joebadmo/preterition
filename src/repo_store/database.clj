(ns repo-store.database
  (:require [yesql.core :refer [defquery]]
            [bugsbio.squirrel :as sq]
            [clojure.java.jdbc :as jdbc]
            [clj-time.coerce :as c]
            [clj-time.core :as t]))

(def db-spec {:classname "org.postgresql.Driver"
              :subprotocol "postgresql"
              :subname "//localhost:5432/jmoon"
              :user "jmoon"})

(defquery create-table! "repo_store/sql/create.sql"
    {:connection db-spec})
(defquery insert-document! "repo_store/sql/insert.sql"
    {:connection db-spec})

(create-table!)
(insert-document!
  (sq/to-sql {:title "Hello!"
   :author "joe"
   :path "hello/joe"
   :content "<div>hello world</div>"
   :post-date (c/to-sql-time (t/today))
   :published true
   :filename ""}))
