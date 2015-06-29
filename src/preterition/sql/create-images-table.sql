create table images (
  id    serial primary key,
  path  varchar(200) unique not null,
  data  bytea
);
