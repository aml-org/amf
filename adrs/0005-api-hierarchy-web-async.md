# 5. Api element hierarchy. Separate apis by kind.

Date: 2020-11-16

## Status

Accepted

## Context

AsyncApis are not semantically the same that other kinds of apis, like Web Apis. Some adopters need to know if they are looking specifically an async api, regarding the structure is the same or no. 
Also, we need to prepare our model to support different kinds of Apis.
## Decision
Create a hierarchy based on Apis. A Document unit will encodes an Api. This Api is abstract. There will be different types of Apis depending on the specifications.


## Consequences

Raml and Oas specs will remain the same. Documents created from those will encode a WebApi.
For AsyncApi 2.0, the document will encode a different Api called AsyncApi. In this version, they have the same fields. That can change in the future,or even, could appear a new type of Api based in a new spec.
