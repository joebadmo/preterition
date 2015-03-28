create table documents (
  id             serial primary key,
  content        text,
  description    text,
  title          varchar(200) not null,
  author         varchar(80) not null,
  path           varchar(200) unique not null,
  post_date      date default current_timestamp,
  created_at     date default current_timestamp,
  updated_at     date default current_timestamp,
  published      boolean default true,
  filename       varchar(200),
  category       varchar(80),
  aliases        varchar(200)[]
);
