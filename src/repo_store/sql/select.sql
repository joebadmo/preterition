select
  title,
  author,
  path,
  content,
  post_date,
  published,
  filename,
  category,
  aliases
from documents
where path = :path
