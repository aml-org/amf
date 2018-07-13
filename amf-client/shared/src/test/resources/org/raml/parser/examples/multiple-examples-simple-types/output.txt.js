#%RAML 1.0
title: Raml example
version: "1.0"
protocols:
  - HTTP
  - HTTPS
baseUri: https://eleonora.com
types:
  tigerName:
    type: string
    examples:
      new: 1
      old: melon
