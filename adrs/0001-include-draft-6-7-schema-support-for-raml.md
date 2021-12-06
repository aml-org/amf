# 1. Inlcude draft 6/7 schema support for raml

Date: 2021-11-6

## Status

Accepted

## Context

- APIMF-3557: customer is asking for specific draft 6/7 functionality (numeric exclusiveMin/Max definitions) in AMF 4.0.4

AMF 4.0.4 supports:
- draft 7 parsing (used for async), but all references to schemas in raml are handled with draft 4.
- Draft 6 is not present at all.

## Decision

APIMF-2668 was cherry-picked to correctly interpret schema versions referenced in raml apis.
Draft 6 was included, adjusting parsing for the exclusiveMin/Max facet.


## Consequences

This HF (4.0.4-5) now parses json schema depending on the explicit draft defined, or else default to certain value depending on spec.

