# 11. Annotations Lifespan
Date: 2018-08-21

## Status
Accepted

## Context
Not all annotations are needed in all stages of AMF (parsing, resolution/transformation, validation, rendering/emission).

## Decision
For this reason, the decision has been made in favor of adding 3 distinct types of annotations, that all others should implement:

- `ResolvableAnnotation`
    - An annotation that must be resolved
    - To allow deferred resolution on unordered graph parsing
- `SerializableAnnotation`
    - An annotation that needs to be dumped in the JSON-LD
    - For information that can't be lost in (des)serialization
- `PerpetualAnnotation`
    - An annotation that does not get deleted in transformation
    - For information that's needed after transformation
- `EternalSerializedAnnotation`
    - Both Perpetual and Serializable

All annotations get deleted in transformation unless they extend `PerpetualAnnotation`.

## Consequences
This decision has no immediate impact.
