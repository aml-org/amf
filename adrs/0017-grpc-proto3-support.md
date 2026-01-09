# 16. GRPC Protobuf 3 support in AMF

Date: 2025-12-29

## Status

Accepted

## Context

AMF is adding support for protobuf 3 APIs in its stack:
- Syntactic parsing Protobuf3 APIs with ANTLR using [this version](https://github.com/aml-org-emu/amf-antlr-ast/blob/master/grammars/Protobuf3.g4) of the grammar.
- Semantic parsing with AMF using the `GRPCConfiguration`.
- Transformation / Resolution with AMF.
- Validation with the standard [`protoc` validator](https://github.com/protocolbuffers/protobuf).

NOTES: 
- only the APIs starting with `syntax = "proto3";` will be accepted by the `GRPCConfiguration`
- all other versions (e.g. `proto2` or the newer `edition = "2023";`) will be parsed as an `ExternalFragment`

## Restrictions

In addition to the protoc validations, we have the following AMF validations that we require:
- we require that the API defines a `package` property, or we add a default one

NOTE:
We have an outdated GRPC Grammar that we need to update, as of now, we don't support the `optional` property, as it
was originally removed when proto3 first launched (making all fields "implicit" where 0/empty was the default), 
but was reintroduced in Protobuf v3.15 (released in early 2021).