# 7. ref with facets parsing for Draft 2019-09

Date: 2020-11-19

## Status

Accepted

## Context

Json Schema Draft 2019-09 indicates that the $ref entry can now have other keywords alongside it. This is a departure from previous drafts as they didn't allow it.
The validation result of $ref must be AND'ed to the validation of the other facets, similar to what an allOf, oneOf and others do.

## Decision

Avoid creating a new field in the model for references as this would be confusing for clients as they would have to take into account that field on very particular ocasions.
Instead, we decided that if there is a $ref with keywords beside it, it should be parsed into an allOf in the same shape.

### Cases

#### Standalone ref

If a json map has a $ref entry in it and it is a single entry map, then that $ref will be parsed as a link and returned as is.

```json
{
  "type": "object",
  "properties": {
    "name": {
      "$ref": "#/somewhere"
    } 
  }
}
```

#### Ref with adjacent keywords

Original

```json
{
  "type": "object",
  "$ref": "#/somewhere",
  "properties": {
    "name": {
      "type": "string"
    } 
  }
}
```

Transformed

```json
{
  "type": "object",
  "allOf": [
    {"$ref": "#/somewhere"}
  ],
  "properties": {
    "name": {
      "type": "string"
    } 
  }
}
```

#### Ref with adjacent keywords and allOf

Original

```json
{
  "type": "object",
  "$ref": "#/somewhere",
  "properties": {
    "name": {
      "type": "string"
    } 
  },
  "allOf": [
    {"$ref": "#/somewhereElse"}
  ] 
}
```

Transformed

```json
{
  "type": "object",
  "allOf": [
    {"$ref": "#/somewhere"},
    {"$ref": "#/somewhereElse"}
  ],
  "properties": {
    "name": {
      "type": "string"
    } 
  }
}
```

## Consequences

Json Schema emitted shapes may now have an allOf where they previously hadn't. This could be fixed in emission but will not be as it isn't necessary for validation.
Because emission won't be modified for this, draft 2019-09 json schemas with $refs in them most likely won't be able to be cycled.

The And constraint in the AMF model is now not directly related to json schema's allOf facet.

Validation error messages for Draft 2019-09 may change due to this transformation.