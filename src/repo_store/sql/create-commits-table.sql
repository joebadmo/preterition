create table commits (
  git_commit_hash varchar(40) unique not null,
  git_commit_time timestamp not null,
  username        varchar(80) not null,
  repository      varchar(80) not null,
  created_at      date default current_timestamp
);
