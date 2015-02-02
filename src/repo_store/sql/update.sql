update documents
  set
    title = :title,
    author = :author,
    content = :content,
    post_date = :post_date,
    published = :published,
    filename = :filename,
    updated_at = :updated_at,
    category = :category,
    aliases = :aliases
where path = :path
