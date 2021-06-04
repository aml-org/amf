# 8. Included JSON Schemas domain element merging

Date: 2021-02-04

## Status

Accepted

## Context

APIMF-2622 reports a StackOverflowError in Domain Element Merging when merging the same included recursive JSON Schema
in a trait/resource type and an operation/endpoint. See `amf-cli/shared/src/test/resources/resolution/merge-recursive-json-schemas-raml10`
as an example.

Why does this happen? Two reasons:
* JSON Schemas are inlined. This means parsing the same included JSON-Schema will produce each time different 
in-memory Shape objects in every location it is included, rather than links to a single object placed in an 
ExternalFragment (this is why RAML types work). When merging we skip merging the same in-memory object with itself 
but since here objects are different, they get merged.
* Resolution step ordering: 
  1. Reference Resolution   -> Plain links get resolved
  2. Extends Resolution     -> Domain Element Merging (our case)
  3. Shape Normalization    -> Recursion detection
    
    Extends resolution does not check recursions, this is done in the next step called "Shape Normalization". However, 
    before resolving extends we resolve links and this makes it susceptible to recursions. The only safe guard mechanism 
    against recursions is skipping merging the same in-memory objects, which doesn't work here because of the 
    aforementioned cause.

## Decision

Apart from skipping equal in-memory objects now we skip objects that: have the `SchemaIsJsonSchema` annotation and have 
the same `ExternalSourceElementModel.ReferenceId` field.

### Implementation
The included JSON Schema in the operation/endpoint has all the above mentioned information so there is no problem. 

This is not the case for the operation/endpoint derived from the trait/resource type to be applied. The process deriving
an operation/endpoint from a trait/resource type looks as follows (summarized):
1. Parse trait/resource type as a Data Node
2. Emit the Data Node as a YNode
3. Parse the emitted YNode as an operation/endpoint

**The problem**

Data Nodes do not have a `ReferenceId` field, only Shapes do. 

**The solution**

_Step 1_:

To keep that information we first have to create a new annotation `ReferenceId` to hold it and that can be stored in the 
Data Node. 

_Step 2_:
When emitting the Data Node to a YNode we lose that annotation. The original YNode was a MutRef (will all the necessary 
information), but the emitted YNode is a plain YScalar of type string (without the information). Emitting the original 
MutRef breaks other cases, so the only available option was to collect the newly emitted YNode in a 
`Map[YNode, ReferenceId]` that relates it to the original `ReferenceId`.

_Step 3:_

Now that he have a `Map[YNode, ReferenceId]` relating each YNode to its original reference we need to delegate this
information to the JSON Schema parser so that it can effectively create the `ExternalSourceElementModel.ReferenceId`
field in the parsed Shape. We do this by means of the `WebApiContext` that takes that Map as an implicit argument. 
Now the JSON Schema parser has access to this Map via the WebApiContext and simply checks if the YNode was previously
related to some `RefenreceId`, creates the corresponding field and the `SchemaIsJsonSchema` annotation making them 
available for Domain Element Merging checks.

## Consequences

This fix introduces some technical debt and fixes this particular corner case. Other corner cases like merging inlined
schemas (do not have ReferenceId) or merging nested recursive nodes in the schema (only top level Shapes have 
ReferenceId) need other ad-hoc fixes (hacks) which will introduce further debt. Not reporting these cases since these 
are very specific internally detected corner cases; and might be better to refactor the Domain Element Merging to make 
it more robust than addressing these fixes individually.
