# 9. Deprecated field IriTemplateMapping

Date: 2021-04-26

## Status

Accepted

## Context
`IriTemplateMapping` is a node that stores mappings for OAS 3.0 discriminator values. It has two fields defined as follows:
* `TemplateVariable`: Variable defined inside an URL template
* `LinkExpression`: OAS 3 link expression

Take the following OAS 3.0 discriminator example:

```yaml
discriminator:
propertyName: petType
mapping:
    dog: '#/components/schemas/Dog'
    cat: '#/components/schemas/Cat'
```

Here `TemplateVariable` wil be set to `dog` and `cat`, while `LinkExpression` will be set to `#/components/schemas/Dog` and `#/components/schemas/Cat`. 

The naming of this model and fields do not reflect at all their functionality

## Decision

Implement a new model called `DiscriminatorValueMapping` to hold this relation. It has two fields defined as follows:
* `DiscriminatorValue`: Value given to a discriminator that identifies a target Shape
* `DiscriminatorValueTarget`: Target shape for a certain discriminator value

`DiscrimiatorValueTarget` changed from a raw reference to a link to an actual shape.

The old `IriTemplateMapping` model is to be deprecated when we enable field deprecation in the AMF model.


## Consequences

Emission still depends on `IriTemplateMapping`. The requirement for this issue was only the parsing part to 
unblock use cases in other products. Further validation can also be applied when defining OAS 3.0 discriminators.
