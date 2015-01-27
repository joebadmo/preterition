select
  git_commit_hash,
  git_commit_time
from commits
order by git_commit_time desc limit 1;

