# 15. Discriminator validations limitations in AMF

Date: 2025-06-27


## Status

Accepted


## Context

AMF only parses and saves the information of the discriminator fields. It didn’t apply any validation logic to them.
Description
API specs provide discriminators to support polymorphism in API implementations. Technically polymorphism is not a feature of any API spec per se. Polymorphism is a characteristic of the type system of the programming language implementing that API spec, which it might or might not have. API specs provide discriminators as a mechanism to support polymorphism in the API implementation. In fact, types in RAML/OAS are not even actual types but schemas. How the API implementation chooses to use the discriminator information is a runtime concern.

The RAML spec says it explicitly [here](https://github.com/raml-org/raml-spec/blob/master/versions/raml-10/raml-10.md#using-discriminator): _"A RAML processor MAY provide an implementation that automatically selects a concrete type from a set of possible types, but a simpler alternative is to store a unique value associated with the type inside the object."_; and the OAS spec [here](https://github.com/OAI/OpenAPI-Specification/blob/main/versions/3.0.2.md#discriminator-object): "_a discriminator MAY act as a "hint" to shortcut validation and selection of the matching schema which may be a costly operation_".

## Decision
Based on the fact that both specs define this functionality as optional, and also that in some cases was programmatically impossible to support it (in a first AMF version we try to support it in RAML), we decided to not implement the discriminator validation in AMF .

In RAML, in most cases, you could achieve the same functionality using a parent union type where the child types have an enum of one element (the discriminator value) in the discriminator property.
In OAS the same, in most cases, you could use an oneOf parent schema with child schemas which have an enum of one element (the discriminator value) in the discriminator property.

Related Issues:
- https://gus.lightning.force.com/lightning/r/ADM_Work__c/a07EE00002GtZAnYAN/view

