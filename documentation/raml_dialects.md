# RAML Dialect 1.0

RAML Dialect defines a set of constraints over a RDF data-model composed by a graph of nodes connected by properties.
In this data-model node types and properties all have associated terms defined in a RAML Vocabulary.
When this graph of data nodes is projected over a RAML document, RAML Library or RAML fragment (the RAML Document Model), a full set of new RAML documents for the vocabulary terms are defined.
A RAML Dialect parser must accept this set of constraints and the projection over the RAML Document Model, as specified in the RAML dialect, and produce a parser capable of transforming valid documents for the new RAML dialect and produce a valid output RDF graph where the constraints introduced by the dialect are met.

## Dialect declaration

RAML Dialect documents are declared using the `#%RAML 1.0 Dialect` header. They must provide a name for the dialect and a version number, using the `dialect` and `version` properties respectively.

``` yaml
#%RAML 1.0 Dialect

dialect: Validation Profile

version: 1.0
```

This information will be used to define the RAML declaration for the documents of the new dialect. In this example, dialect documents must use the following RAML document declaration:

``` yaml
#%Validation Profile 1.0
```

## Using vocabularies

Dialects provide a mapping from vocabulary terms to the structure of a graph of data nodes. To establish this mapping, we need first to import vocabularies in the dialect document as RAML libraries. The `uses` property can be used for this:

``` yaml
uses:
  validation: validation.raml
```
Class terms and property terms from the vocabulary can then be referenced using the alias declared in the `vocabularies` property.

External vocabularies, as defined in the RAML Vocabulary spec can also be directly used in the definition of a dialect using the  `external` property:


``` yaml
external:
  schema-org: http://schema.org/
```

## Node mappings

The property `nodeMappings` introduces the declaration of all the nodes in the model.
This nodes describe the mapping of vocabulary terms into the type of the node and the properties of the node, as well as the constraints associated to each node.

Nodes are declared as a map from name of nodes to node definitions.

For each node a class term must be used to define the type of the node, using the `classTerm` property.
Each node has also an associated `mapping` of node properties, defined by a label and a property mapping definition.

The following example defines a node mapping:

``` yaml
nodeMappings:

  profileNode:
    classTerm: validation.Profile
    mapping:
      profile:
        propertyTerm: schema-org.name
      description:
        propertyTerm: schema-org.description
```

In this case, we declare a new type of node in the model `profileNode` and we associate the class term `validation.Profile` as the type of node. The mapping of properties include two properties: `profile` mapped to the property term `schema-org.name` and `description` mapped to `schema-org.description`.

The meaning of this mapping can be provided using closed world semantics via [W3C SHACL](https://www.w3.org/TR/shacl/) and defining a data shape constraint over the model data graph for the mapping we have just described:

``` n3
base:profileNode rdf:type shacl:NodeShape ;
  sh:targetClass validation:Profile ;
  sh:property base:profileNode/property/profile ;
  sh:property base:profileNode/property/description .

base:profileNode/property/profile sh:path schema-org:name .

base:profileNode/property/description sh:path schema-org:description .
```

When this node is associated to a RAML document, it can be used to encode information about a profile, using the labels for the properties declared in the mapping:

``` yaml
#%Validation Profile 1.0

profile: OpenAPI
description: a test validation profile for AMF
```

When parsed, this document will generate the following RDF data graph:

``` n3
[
  rdfs:type validation:Profile ;
  schema-org:name "OpenAPI" ;
  schema-org.description "a test validation profile for AMF"
]
```

## Property mappings

When declaring the mapping for a property of a data node, some constraints over that property of the instances of the model can be expressed.

| restriction | Description | SHACL semantics |
|-------------|-------------|----------------|
| range       | allowed data type for the objects of the property | shacl:datatype |
| mandatory   | the property must be present in the node, default false | shacl:minCount 1 |
| pattern     | a regular expression that must match the value of the property | shacl:pattern |
| minimum     | minimum value for the property | shacl:minInclusive |
| maximum     | maximum value for the property | shacl:maxInclusive |
| enum        | closed set of values this property value must belong to | shacl:in |
| allowMultiple | multiple objects can be used in the value of this property, default false | remove shacl:minCount |

For example, the following property mapping:


``` yaml
nodeMappings:

  profileNode:
    classTerm: validation.Profile
    mapping:
      profile:
        propertyTerm: schema-org.name
        required: true
        pattern: [a-z]+[A-Za-z]*
```

Will be translated into the following SHACL constraint shape:

``` n3
base:profileNode rdf:type shacl:NodeShape ;
  sh:targetClass validation:Profile ;
  sh:property base:profileNode/property/profile .

base:profileNode/property/profile sh:path schema-org:name ;
  sh:minCount 1 ;
  sh:pattern "[a-z]+[A-Za-z]*" .
```

## Connecting data node mappings

Data node mappings can be connected to describe the shape of the fully expected data model graph or the full structure of the dialect document if we think about the projection onto RAML documents.

Nodes can be connected using different syntactical styles to allow more expressivity in the design of the dialect syntax.

### Simple node nesting

The simplest way of connecting node mappings is by specifying the reference of another node in the `range` property of the parent node:

``` yaml
nodeMappings:

  shapeValidationNode:
    classTerm: validation.ShapeValidation
    mapping:
      name:
        propertyTerm: schema-org.name
        range: string
      message:
        propertyTerm: shacl.message
        range: string

  profileNode:
    classTerm: validation.Profile
    mapping:
      profile:
        propertyTerm: schema-org.name
      validations:
        propertyTerm: validation.validations
        range: shapeValidationNode
```

In the previous example we have defined two node mappings: `profileNode` and `shapeValidationNode` and we have connected them through the `validations` property mapped to the `schema-org.name` property term.

With these mappings, dialect document instances will have to use the following syntax:

``` yaml
#%Validation Profile 1.0

profile: My Profile
validations:
  name: my validation
  message: this is a validation
```

The previous document will generate when parsed the following RDF graph:

``` n3
[
  rdfs:type validation:ShapeValidation ;
  schema-org:name "My Profile" ;
  validation:validations [

    rdfs:type validation:ShapeValidation
    schema-org:name "my validation" ;
    schema-org:message "this is a validation"

  ]
]
```

The SHACL semantics for this kind of nesting of nodes are shown in the following translation of the dialect mappings:


``` n3
base:profileNode rdf:type shacl:NodeShape ;
  sh:targetClass validation:Profile ;
  sh:property base:profileNode/property/profile ;
  sh:property base:profileNode/property/validations .

base:profileNode/property/profile sh:path schema-org:name ;
  sh:datatype xsd:string .

base:profileNode/property/validations sh:path validation:validations ;
  sh:maxCount 1 ;
  sh:node base:shapeValidationNode .

base:shapeValidationNode rdf:type shacl:NodeShape ;
  sh:targetClass validation:ShapeValidation ;
  sh:property base:shapeValidationNode/property/name ;
  sh:property base:shapeValidationNode/property/message .

base:shapeValidationNode/property/name sh:path schema-org:name ;
  sh:datatype xsd:string .

base:shapeValidationNode/property/message sh:path sh:message ;
  sh:datatype xsd:string .
```

More than one node mapping can be specified as the range of a property mapping. In this case any of those data node shapes will satisfy the parsing and the SHACL validation:

``` yaml
  profileNode:
    classTerm: validation.Profile
    mapping:
      validations:
        propertyTerm: validation.validations
        range:
          - shapeValidationNode
          - queryValidationNode
          - functionValidationNode
```

SHACL semantics:

``` n3
base:profileNode rdf:type shacl:NodeShape ;
  sh:targetClass validation:Profile ;
  sh:property base:profileNode/property/validations .


base:profileNode/property/validations
  sh:or (
    [
      sh:maxCount 1 ;
      sh:path validation:validations ;
      sh:node base:shapeValidationNode ;
    ]
    [
      sh:maxCount 1 ;
      sh:path validation:validations ;
      sh:node base:queryValidationNode ;
    ]
    [
      sh:maxCount 1 ;
      sh:path validation:validations ;
      sh:node base:functionValidationNode ;
    ]
 ) .
```

### Multiple node nesting

Optionally, the designer of the dialect can allow multiple values instead of a single one in the range of the mapped property. This can be expressed using the `allowMultiple` property with value true.

``` yaml
nodeMappings:

  shapeValidationNode:
    classTerm: validation.ShapeValidation
    mapping:
      name:
        propertyTerm: schema-org.name
        range: string
      message:
        propertyTerm: shacl.message
        range: string

  profileNode:
    classTerm: validation.Profile
    mapping:
      profile:
        propertyTerm: schema-org.name
      validations:
        propertyTerm: validation.validations
        range: shapeValidationNode
        allowMultiple: true
```

With these mappings, dialect document instances will have to use the following syntax:

``` yaml
#%Validation Profile 1.0

profile: My Profile
validations:
  - name: my validation
    message: this is a validation
  - name: other validation
    message: this is another message
```
In the SHACL mapping, the generation of the `sh:maxCount 1` assertion would be omitted.

### Nesting by property value

Sometimes, when the possible values for a property in the model have a unique key that is going to be different in all the child nodes, we can use the value of that key to connect parent node and children through a map from values of the keys to the children nodes.

This style of syntax can be expressed using two properties: `asMap` that must be set to true, and `hash` that must have the value of the property term in the nested node mapping that will hold the value of the key.

For example, we can rewrite the previous example using property values:

``` yaml
nodeMappings:

  shapeValidationNode:
    classTerm: validation.ShapeValidation
    mapping:
      name:
        propertyTerm: schema-org.name
        range: string
      message:
        propertyTerm: shacl.message
        range: string

  profileNode:
    classTerm: validation.Profile
    mapping:
      profile:
        propertyTerm: schema-org.name
      validations:
        propertyTerm: validation.validations
        range: shapeValidationNode
        asMap: true
        hash: schema-org.name
```

With this mapping, we can now write the dialect document using the name of the validation as the key connecting profile and validation:

``` yaml
#%Validation Profile 1.0

profile: My Profile
validations:
  my validation:
    message: this is a validation
  other validation:
    message: this is another message
```

This syntax is used in RAML 1.0 for example to associate responses to operations using the status code of the response as the key, or payloads to responses using the media type of the payload as the key.
The change is merely syntactical, neither the parsed graph for the dialect instance nor the SHACL semantics for the constraint will be affected by the change.


## Key-Value nesting

Sometimes you want to nest maps of key-value pairs where the the value of the key must be mapped to a property in the graph model node, and the value to a different property.
For exapmle, imagine you want to generate in the graph a list labels, with a `myvocab.labelName` property for the label name and a `myvocab.labelValue` property for the value of the label.

You could declare the syntax in you dialect as a map of key-value pairs in the following way:


```yaml
nodeMappings:
  
  LabelNode:
    classTerm: myvocab.Label
    mapping:
      name:
        propertyTerm: myvocab.labelName
        range: string
      value:
        propertyTerm: myvocab.labelValue
        range: string
      
  TopLevelNode:
    classTerm: myvocab.TopLevel
    mapping:
      labels:
        propertyTerm: myvocab.labels
        range: LabelNode
        asMap: true
        hash: myvocab.labelName
        hashValue: myvocab.labelValue
```

Using this syntax a document for this dialect could declare a list of labls in the following way:


```yaml
labels:
  label1: a
  label2: b
```

The generated RDF graph will look like:

```turtle
[
  rdf:type myvocab:TopLevel ;
  myvocab:labels [
    rdf:type myvocab.Label
    myvocab:labelName "label1" ;
    myvocab:labelValue "a"
  ] , [
    rdf:type myvocab.Label
    myvocab:labelName "label2" ;
    myvocab:labelValue "b"
  ]
]
```

## RAML document model mapping

In order to use the node mappings and constraints defined in the dialect to support new types of RAML documents we must map the node mappings to the components of the RAML Document Model:

- RAML Documents: stand-alone documents that can encode a main element of the domain and declare auxiliary elements that then can be referenced in the document
- RAML Modules: libraries containing sets of declarations of reusable definitions that can be referenced from other types of documents
- RAML Fragments: non stand-alone documents encoding a single element that can be included in other types of documents

The mapping in a dialect is achieved through the `raml` property. The value of this property is a mapping with 3 possible keys:

| Property | Description |
| ---      | ---         |
| document | nodes that can be declared and encoded in the main RAML document for the dialect |
| module   | nodes that can be declared in a RAML library for the dialect |
| fragments | nodes that can be encoded into RAML fragments for the dialect |

### Defining the RAML document mapping

RAML documents can encode one type of node, and declare many types of nodes.

This can be defined using the properties:

| Property | Description |
| ---      | ---         |
| encodes | main node mapping that will be encoded at the root level of the dialect document |
| declares | mapping from declaration key to the type of nodes that can be declared for that key |

For example, with the following mapping:

``` yaml
nodeMappings:

  shapeValidationNode:
    classTerm: validation.ShapeValidation
    mapping:
      name:
        propertyTerm: schema-org.name
        range: string
      message:
        propertyTerm: shacl.message
        range: string

  profileNode:
    classTerm: validation.Profile
    mapping:
      profile:
        propertyTerm: schema-org.name
      validations:
        propertyTerm: validation.validations
        range: shapeValidationNode
        allowMultiple: true

raml:

  document:
    encodes: profileNode
    declares:
      localValidations: shapeValidationNode
```

We are stating the RAML documents for this dialect will encode a `profileNode` mapping and will be able to declare `shapeValidationNodes` introduced by the property `localValidations`.

For example:


``` yaml
#%Validation Profile 1.0

# the declarations here
localValidations:
  validation1:
    name: my validation
    message: this is a message

# the main encoded element
profile: My Profile
validations:
  - validation1 # using the declaration
```

### Defining the RAML module mapping

RAML modules can declare multiple elements that then can be reused from other documents using the library

The mapping can be defined using the properties:

| Property | Description |
| ---      | ---         |
| declares | mapping from declaration key to the type of nodes that can be declared for that key |

For example, with the following mapping:

``` yaml
nodeMappings:

  shapeValidationNode:
    classTerm: validation.ShapeValidation
    mapping:
      name:
        propertyTerm: schema-org.name
        range: string
      message:
        propertyTerm: shacl.message
        range: string

  profileNode:
    classTerm: validation.Profile
    mapping:
      profile:
        propertyTerm: schema-org.name
      validations:
        propertyTerm: validation.validations
        range: shapeValidationNode
        allowMultiple: true

raml:

  document:
    encodes: profileNode
    declares:
      localValidations: shapeValidationNode

  module:
    declares:
      libraryValidations: shapeValidationNode
```

We have expanded the RAML mapping to support libraries of validations, declared using the property `libraryValidatons`.

For example:

``` yaml
#%RAML Library / Validation Profile 1.0

# the declarations here
libraryValidations:

  validation1:
    name: my validation
    message: this is a message

  validation2:
    name: other validation
    message: this is the other message
```

We need to provide the identifier of the dialect in the declaration of the RAML dialect, for the dialect processor to be able to find the way of parsing the document.

Now from the main dialect document we can use the library:

``` yaml
#%Validation Profile 1.0

# using the library
uses:
  vals: validations_library.raml


# the main encoded element
profile: My Profile
validations:
  - vals.validation1 # using the declaration
```

### Defining the RAML fragments mapping

RAML fragments are not stand-alone documents that need to be included in other documents to be re-used.

We can define which types of fragments are supported in our dialect using the `fragments` property and the nested `encodes` property.

The value of this property is a mapping from fragment header identifier to node mapping that the fragment must satify:

| Property | Description |
| ---      | ---         |
| encodes | mapping from fragment header identifier to the type of nodes that can be encoded in that fragment |


For example, with the following mapping:

``` yaml
nodeMappings:

  shapeValidationNode:
    classTerm: validation.ShapeValidation
    mapping:
      name:
        propertyTerm: schema-org.name
        range: string
      message:
        propertyTerm: shacl.message
        range: string

  profileNode:
    classTerm: validation.Profile
    mapping:
      profile:
        propertyTerm: schema-org.name
      validations:
        propertyTerm: validation.validations
        range: shapeValidationNode
        allowMultiple: true

raml:

  document:
    encodes: profileNode
    declares:
      localValidations: shapeValidationNode

  fragments:
    encodes:
      Validation: shapeValidationNode
```

Now we can define a fragment for the dialect with a shape validation like:

``` yaml
#%RAML Validation / Validation Profile 1.0

# the encoded validation
name: my validation
message: this is a message
```

Now this fragment can be used from the main dialect document:


``` yaml
#%Validation Profile 1.0

# the main encoded element
profile: My Profile
validations:
  - !include validation_fragment.raml
```

## Extending Fragments

Sometimes you need/want to reuse an existing fragment, with a little bit of customization.
For example you may need to add an extra property value, or override an existing property value.

One way to do this is to use `!extends` directive:

```yaml
  queryValidationNode:
     !extend : queryNode.raml
     mapping:
       hello:
         propertyTerm: h.name
         range: string
```

In this sample we are taking a node defined in `queryNode.raml`
and customizing it by adding an additional mapping.

## References

- [RAML 1.0 Spec](https://github.com/raml-org/raml-spec/blob/master/versions/raml-10/raml-10.md)
- [SHACL](https://www.w3.org/TR/shacl/)
