# 12. New shape normalization

Date: 2023-08-01

## Status

In progress

## Context

### Usage
We are parsing yaml documents using a JsonLDSchema as model to apply the context 
and obtain a semantic graph from it.
If not any explicit term is defined by a given property of an object. Then, the ast key is used
as path and de @base is used as namespace, obtaining the property *@base*:*key*
### Example:
#### JsonLDSchema
```json
{
  "@context": {
    "@base": "http://a.ml/vocabularies/core",
    "@type": [
      "http://a.ml/vocabularies/core#Incorporation"
    ]
  },
  "properties": {
    "player": {
      "@context": {
        "@type": [
          "http://a.ml/vocabularies/core#Player"
        ]
      },
      "type": "object",
      "properties": {
        "name": {
          "type": "string"
        }
      }
    }
  }
}
```
#### document
```yaml
player:
  name: Edinson
```

#### Graph
```json
{
  "@graph": [
    {
      "@type": [
        "http://a.ml/vocabularies/core#Incorporation"
      ],
      "http://a.ml/vocabularies/core#player": {
        "@type": [
          "http://a.ml/vocabularies/core#Player"
        ],
        "http://a.ml/vocabularies/core#name": "Edinson"
      }
    }
  ]
}
```

We must keep in consideration that graph will be ran at the amf-custom-validator
against a profile which should be complaint.

### ValidationProfile

We can write a profile to apply some constraint over the name of a player
at an incorporation as follows:

```yaml
#%Validation Profile 1.0

validations:
  player-name-min-lenght:
    message: Name of the incorporated player must have more than 1 char
    targetClass: core.Incorporation
    propertyConstraints:
      core.player/core.name:
        minLength: 1
```
That ruleset will be processed by the custom validator and the path traverse
of the propertyConstraint will be computed to search through inner object properties.
PropertyConstrains by definition must be valid CURIES but not URIS.

### Problem

The issue starts when the key at the ast contains "/" characters without being a valid URI.
A valid URI could be keep as property term at the graph, otherwise the base must be added to
generate a valid semantic model.

### Example:
#### JsonLDSchema
```json
{
  "@context": {
    "@base": "http://a.ml/vocabularies/core",
    "@type": [
      "http://a.ml/vocabularies/core#Incorporation"
    ]
  },
  "properties": {
    "player": {
      "@context": {
        "@type": [
          "http://a.ml/vocabularies/core#Player"
        ]
      },
      "type": "object",
      "properties": {
        "name": {
          "type": "string"
        },
        "mulesoft.com/club-id": {
          "type": "string"
        }
      }
    }
  }
}
```
#### document
```yaml
player:
  name: Edinson
  mulesoft.com/club-id: CABJ
```

#### Graph
```json
{
  "@graph": [
    {
      "@type": [
        "http://a.ml/vocabularies/core#Incorporation"
      ],
      "http://a.ml/vocabularies/core#player": {
        "@type": [
          "http://a.ml/vocabularies/core#Player"
        ],
        "http://a.ml/vocabularies/core#name": "Edinson",
        "http://a.ml/vocabularies/core#mulesoft.com/club-id": "CABJ"
      }
    }
  ]
}
```

All looks good until we want to write a rule that targets the **mulesoft.com/club-id** property.

```yaml
#%Validation Profile 1.0

validations:
  player-name-min-lenght:
    message: Club id of the incorporated player must be written in upper case
    targetClass: core.Incorporation
    propertyConstraints:
      core.player/core.mulesoft.com/club-id:
        pattern: [A-Z]
```

In this case, the propertyConstraint parser have no way to identify if the second **/** is 
part of the path traverse or is part of the name.

This lead to have no way to apply rules over properties with **/** char
## Decision

### Proposals
#### Change property terms at context
```json
{
  "@context": {
    "core": "http://a.ml/vocabularies/core",
    "@base": "http://a.ml/vocabularies/core",
    "@type": [
      "http://a.ml/vocabularies/core#Incorporation"
    ]
  },
  "properties": {
    "player": {
      "@context": {
        "@type": [
          "http://a.ml/vocabularies/core#Player"
        ],
        "mulesoft.com/club-id": "core:club-id"
      },
      "type": "object",
      "properties": {
        "name": {
          "type": "string"
        },
        "mulesoft.com/club-id": {
          "type": "string"
        }
      }
    }
  }
}
```

This will produce the property:
```json
{
  "http://a.ml/vocabularies/core#club-id": "CABJ"
}
```

This approach does not scale for dynamic properties, as we need to know each property
of the schema previously to assign a new term form them.
Doesn't sound very friendly for users as well, as they will must know the mapping.

#### Hack the prefix at the ruleset

The whole term until last **/** can be declared as prefix at the ruleset to avoid 
the ambiguity at the propertyConstraint parsing.
For the case of **mulesoft.com** the prefix can be added as native at the custom validator,
so the following prefix is necessary to be written:
```yaml
prefix:
  mulesoft: http://a.ml/vocabularies/core#mulesoft.com/
```

And the profile can be written like: 
```yaml
#%Validation Profile 1.0

validations:
  player-name-min-lenght:
    message: Club id of the incorporated player must be written in upper case
    targetClass: core.Incorporation
    propertyConstraints:
      core.player/mulesoft.club-id:
        pattern: [A-Z]
```
This will work, but is consufing for the user as the must know when to use the hacked 
"mulesoft" prefix, and will no scale for other dynamic properties that we don't about now.

### Chosen proposal
#### Encode ast keys as Uri Components before apply context 

If we encode the key as component adn then apply the base, the property will be:
```json
{
  "http://a.ml/vocabularies/core#mulesoft.com%20club-id": "CABJ"
}
```
The encoding must be done after compare context and before applye the base in case 
that it corresponds. That means, the explicit term assign by property mst be written 
using the ast value wihout encodes:
```json
{
  "@context": {
    "core": "http://a.ml/vocabularies/core",
    "@base": "http://a.ml/vocabularies/core",
    "@type": [
      "http://a.ml/vocabularies/core#Incorporation"
    ],
    "mulesoft.com/club-id": "core:club-id"
  }
}
```

The ruleset can be:
```yaml
#%Validation Profile 1.0

validations:
  player-name-min-lenght:
    message: Club id of the incorporated player must be written in upper case
    targetClass: core.Incorporation
    propertyConstraints:
      core.player/core.mulesoft.com%20club-id:
        pattern: [A-Z]
```

To facilitate the written and reading of the profiles, 
we can add as default the prefix with the %20 for this case:
```yaml
prefix:
  mulesoft: http://a.ml/vocabularies/core#mulesoft.com%20
```


## Consequences

We need to update the GCL model to dealt with the new encoded terms, and document
in a clear way how to write rules over those.
No changes at schema or label values are needed.
