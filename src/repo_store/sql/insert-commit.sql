insert into commits (
  git_commit_time,
  git_commit_hash,
  username,
  repository
)

values (
  :git_commit_time,
  :git_commit_hash,
  :username,
  :repository
);
