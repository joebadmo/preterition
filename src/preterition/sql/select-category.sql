select
  title,
  author,
  path,
  post_date,
  description,
  published,
  filename,
  category,
  aliases
from documents
where category = :category
order by post_date desc
