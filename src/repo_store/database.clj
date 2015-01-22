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
(defquery insert-document! "repo_store/sql/insert.sql"
    {:connection db-spec})

(create-table! db-spec)
(insert-document!
  {:title "Hello!"
   :author "joe"
   :path "hello/joe2"
   :content "<div>hello world</div>"
   :post_date (c/to-sql-time (t/today))
   :published true
   :filename "" })
