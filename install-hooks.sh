#!/usr/bin/env bash
WORKING_DIR=`pwd`
HOOKS_DIR="hooks"
GIT_ROOT=`git rev-parse --show-cdup`
for file in `ls ${HOOKS_DIR}`; do
  fullpath="${WORKING_DIR}/${GIT_ROOT%%/}/.git/hooks/${file}"
  if [ -L "${fullpath}" ]; then
    rm -f "${fullpath}"
  fi
  ln -s "${WORKING_DIR}/${HOOKS_DIR}/${file}" "${fullpath}"
  echo "${file} hook installed"
done