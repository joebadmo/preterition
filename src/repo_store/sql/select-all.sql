select
  title,
  author,
  path,
  content,
  post_date,
  published,
  filename
from documents
order by post_date desc
