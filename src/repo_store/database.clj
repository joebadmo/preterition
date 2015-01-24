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

;(create-table!)

(defquery insert-document! "repo_store/sql/insert.sql"
  {:connection db-spec})

(defquery select-document-by-path "repo_store/sql/select.sql"
  {:connection db-spec})

(defquery update-document! "repo_store/sql/update.sql"
  {:connection db-spec})

(defquery delete-documents! "repo_store/sql/delete.sql"
  {:connection db-spec})

(def defaults
  {:published true
   :author "Joe Moon"
   :post-date (c/to-sql-time (t/now))})

(defn insert-document [doc]
  (-> (merge
        defaults
        doc
        (if-let [post-date (doc :date)]
          {:post-date (c/to-sql-time post-date)}))
      sq/to-sql
      insert-document!))

(defn select-document [doc]
  (select-document-by-path
    {:path doc}
    {:result-set-fn first
     :row-fn sq/to-clj}))

(defn update-document [doc]
  (let [old-doc (select-document (doc :path))
        merged (merge old-doc doc)]
    (-> merged
        (merge
          {:post-date (c/to-sql-time (merged :post-date))
           :updated-at (c/to-sql-time (t/now))})
        sq/to-sql
        update-document!)))

(def delete-documents #(delete-documents! {:paths %}))

; (update-document
;   {:title "Goodbye!"
;    :path "hello/joe/something4"
;    :content "<div>goodbye world</div>"
;    :post-date (t/yesterday)
;    :stuff "farts"})
;
; (insert-document
;   {:title "Hello!"
;    :author "joe moon"
;    :path "2"
;    :content "<div>hello world</div>"
;    :post-date (t/now)
;    :published true
;    :filename "file"
;    :stuff "farts"})
