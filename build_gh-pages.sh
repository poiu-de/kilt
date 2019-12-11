#!/bin/bash

# Only generate gh-pages from master branch

if [ "$(git rev-parse --abbrev-ref HEAD)" != "master" ]
then
  echo "Not on master branch. Not attempting to generate gh-pages."
  exit 0
fi

# Don't create gh-pages from dirty working directory
if [ "`git status -s`" ]
then
    echo "The working directory is dirty. Please commit any pending changes."
    exit 1;
fi

# Remember current commit hash
GIT_COMMIT_HASH=$(git rev-parse HEAD)

# Mount the gh-pages branch in a git worktree
GH_PAGES=$(mktemp --directory --dry-run --tmpdir kilt_gh-pages.XXXXXXXXX)
git worktree add "$GH_PAGES" gh-pages

# Convert the asciidoc sources to HTML
asciidoctor \
  --backend=html5 \
  --attribute=nofooter \
  --attribute="revdate=$(date -Idate)" \
  --destination-dir="$GH_PAGES" \
  --source-dir=docs \
  docs/*.adoc
#  Unsetting author and email doesn't work \
#asciidoctor \
#  --backend=html5 \
#  --attribute=nofooter \
#  --attribute='!author'                  # do not print the author name \
#  --attribute='!email'                   # do not print the email address \
#  --attribute="revnumber=$KILT_VERSION" # specify the kilt revision \
#  --attribute="revdate=$(date -Idate)"  # set the current date \
#  --destination-dir="$GH_PAGES" \
#  --source-dir=docs \
#  docs/*.adoc


# Copy the additional resources to the target directory
pushd docs
find . -type f -not -name "*.adoc" -exec cp '{}' "$GH_PAGES"/'{}' ';'
popd

# Commit the new content
pushd "$GH_PAGES"
git add --all
git commit -m "[build_gh-pages.sh] Update gh-pages from ""$GIT_COMMIT_HASH"

# Push content
git push

# Remove git worktree
popd
git worktree remove "$GH_PAGES"
git worktree prune
