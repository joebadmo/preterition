select
  title,
  author,
  path,
  content,
  post_date,
  published,
  filename
from documents
where path = :path
