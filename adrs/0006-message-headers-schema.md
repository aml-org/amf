# 6. Message headers schema

Date: 2020-11-17

## Status

In progress

## Context

Async Api 2.0 spec details that message headers property must be a Schema Object in which his properties will be the headers for that message.
AMF currently is parsing that object as the schema of a parameter object. This is confusing and semantically incorrect, because that property represents a set of parameters, not just one  
## Decision
Create a new field to the Message model, similar to the query string property of Raml 1.0 operations, that will contains the Schema Object. Any user looking at the model, will be able to read that schema as a list of headers based on the field in which is contained: 
####ApiContract:HeaderSchema

The header example property of the message will be validated against that schema.

## Consequences
We will start to model the headers as properties of the Schema Object. Eventually, those properties should be transformed into headers (maybe in resolution time). The problem is that the spec does not clarify any other restriction over the Schema, regarding the object type, so technically a user could write complex constraints like allOf, anyOf, oneOf, etc 
The spec should detail that the schema can only have properties and not only complex constraint, or specify how the transduction should be done in those cases.

