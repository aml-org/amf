#!/bin/zsh

function lint() {
  for api in "$@";
  do
    # https://github.com/cjoudrey/graphql-schema-linter
    echo "$api"
    graphql-schema-linter -r empty "$api"
  done
}

lint tck/apis/valid/*.graphql
lint datagraph-set/*/*/api.graphql