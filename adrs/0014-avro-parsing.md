# 13. AWS OAS parsing

Date: 2024-07-02


## Status

Accepted


## Context

Async 2.x supports AVRO Schemas and we currently don't. 
We want to add support AVRO Schemas inside Async APIs and as a standalone documents.

We need to decide:
- how are we going to map AVRO Schemas to the AMF Model
- how are we going to validate AVRO Schemas

an AVRO Schema has the following properties:
- It's defined in plain JSON, they MAY be also be defined as a `.avsc` file
- It doesn't have a special key that indicates it's an AVRO Schema, nor it's version (like JSON Schema does with it's `$schema`)


## Decision

Implement AVRO Schema parsing as a new specification, following the [AVRO Schema 1.9.0 specification](https://avro.apache.org/docs/1.9.0/spec.html#schemas).

An AVRO Schema may be a:
- Map
- Array
- Record (with fields, each one being any of the possible types)
- Enum
- Fixed Type
- Primitive Type ("null", "boolean", "int", "long", "float", "double", "bytes", "string")

We've parsed each AVRO Type to the following AMF Shape:
- Map --> NodeShape with `AdditionalProperties` field for the values shape
- Array --> ArrayShape with `Items` field for the items shape
- Record --> NodeShape with `Properties` with a PropertyShape that contains each field shape
- Enum --> ScalarShape with `Values` field for it's symbols
- Fixed Type --> ScalarShape with `Datatype` field for its type and `Size` for its size
- Primitive Type --> ScalarShape with `Datatype` field, or NilShape if its type 'null'

Given that in this mapping, several AVRO Types correspond to a ScalarShape or a NodeShape, **we've added the `avro-schema` annotation** with an `avroType` that contains the avro type declared before parsing.
This way, we can know the exact type for rendering or other purposes, for example having a NodeShape and knowing if it's an avro record or a map (both are parsed as NodeShapes).

We've also added 3 AVRO-specific fields to the `AnyShape` Model via the `AvroFields` trait, adding the following fields:
- AvroNamespace
- Aliases
- Size


For now only parsing is done 


## Consequences

None so far.