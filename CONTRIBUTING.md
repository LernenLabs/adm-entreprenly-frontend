# Contributing

## Commit policy

Commit messages must be written in English and follow Conventional Commits,
for example `feat: add sign-in screen` or `fix: handle expired token`.

Authorship belongs to the team. No commit, pull request or label may credit an
AI tool as an author: do not include lines such as `Co-Authored-By: Claude ...`
or `Generated with Claude Code`, even if the tool suggests them. If a commit
already has such a line, rewrite the message and update the branch with
`git push --force-with-lease`.

The `no-ai-authorship` job in `.github/workflows/commit-policy.yml` enforces
this on every push and pull request.
