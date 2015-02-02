(ns repo-store.database
  (:require [yesql.core :refer [defquery]]
            [bugsbio.squirrel :as sq]
            [clojure.java.jdbc :as jdbc]
            [clj-time.coerce :as c]
            [clj-time.core :as t]))

(def ^:private db-spec {:classname "org.postgresql.Driver"
                        :subprotocol "postgresql"
                        :subname "//localhost:5432/jmoon"
                        :user "jmoon"})

(defquery create-documents-table! "repo_store/sql/create-documents-table.sql"
  {:connection db-spec})

(defquery create-commits-table! "repo_store/sql/create-commits-table.sql"
  {:connection db-spec})

(defquery insert-document! "repo_store/sql/insert.sql"
  {:connection db-spec})

(defquery insert-commit! "repo_store/sql/insert-commit.sql"
  {:connection db-spec})

(defquery select-newest-commit "repo_store/sql/select-newest-commit.sql"
  {:connection db-spec})

(defquery select-documents "repo_store/sql/select-all.sql"
  {:connection db-spec})

(defquery select-document-by-path "repo_store/sql/select.sql"
  {:connection db-spec})

(defquery update-document! "repo_store/sql/update.sql"
  {:connection db-spec})

(defquery delete-documents! "repo_store/sql/delete.sql"
  {:connection db-spec})

(def ^:private defaults
  {:published true
   :author "Joe Moon"
   :post-date (t/now)
   :category nil
   :aliases nil})

(defn- vec-to-array [v]
  (-> (jdbc/get-connection db-spec)
      (.createArrayOf "varchar" (into-array String v))))

(defn- document-to-sql [doc]
  (-> (merge defaults doc)
      (update-in [:post-date] c/to-sql-time)
      (update-in [:aliases] vec-to-array)
      sq/to-sql))

(defn select-document [doc]
  (select-document-by-path
    {:path doc}
    {:result-set-fn first
     :row-fn sq/to-clj}))

(defn- update-document [doc conn]
  (let [old-doc (select-document (doc :path))
        merged (merge old-doc doc)]
    (-> merged
        (merge
          {:post-date (c/to-sql-time (merged :post-date))
           :updated-at (c/to-sql-time (t/now))})
        sq/to-sql
        update-document! conn)))

(defn- delete-documents [paths conn]
  (delete-documents! {:paths paths} conn))

(defn update [document-set]
  (let [additions (->> (document-set :add) (map document-to-sql))
        edits (->> (document-set :edit) (map document-to-sql))
        deletions (document-set :delete)
        commit (-> (document-set :git-commit)
                   (update-in [:git-commit-time] c/to-sql-time)
                   sq/to-sql)]
    (jdbc/with-db-transaction [tx db-spec]
      (doseq [addition additions]
        (insert-document! addition {:connection tx}))
      (doseq [edit edits]
        (update-document edit {:connection tx}))
      (when (seq deletions)
        (delete-documents deletions {:connection tx}))
      (insert-commit! commit {:connection tx}))))

(defn get-newest-commit-map []
  (let [commits (select-newest-commit)]
    (if (seq commits)
      (sq/to-clj (first commits))
      {:git-commit-hash nil}))) ; TODO: fix this
