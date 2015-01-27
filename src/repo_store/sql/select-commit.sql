select
  git_commit_hash,
  git_commit_date
from commits
order by git_commit_date desc limit 1;

