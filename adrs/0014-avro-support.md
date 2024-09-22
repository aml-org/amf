# 14. AVRO Support in AMF

Date: 2024-07-02


## Status

Accepted


## Context

Async 2.x supports AVRO Schemas, and we currently don't. 
We want to add support of AVRO Schemas:
- inside Async APIs
- as a standalone document

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

### Where we support and DON'T support AVRO Schemas
We Support AVRO Schemas (inline or inside a `$ref`):
- as a standalone document or file 
  - we encourage users to use the `.avsc` file type to indicate that's an avro file, for better suggestions and so on in the platform).
- inside a message payload in an AsyncAPI
  - the key `schemaFormat` MUST be declared and specify it's an AVRO payload

We don't support AVRO Schemas:
- inside components --> schemas in an AsyncAPI
  - because we can't determine if it's an AVRO Schema or any other schema

### AVRO Validation
We'll use the Apache official plugins for JVM and JS.


## Consequences / Constraints

The validation plugins differ in interfaces and implementations, and each has some constraints:

### JVM avro validation plugin
- validation per se is not supported, we try to parse an avro schema and throw parsing results if there are any
  - this means it's difficult to have location of where the error is thrown, we may give an approximate location from our end post-validation
- when a validation is thrown, the rest of the file is not being searched for more validations
  - this is particularly important in large avro schemas, where many errors can be found but only one is shown


### Both JVM & JS validation plugins
- `"default"` values are not being validated when the type is `bytes`, `map`, or `array`
- the validator treats as invalid an empty array as the default value for arrays (`"default": []`) even though the [Avro Schema Specification](https://avro.apache.org/docs/1.12.0/specification) has some examples with it
