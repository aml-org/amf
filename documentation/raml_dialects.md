# AML Dialects 1.0

AML Dialect defines a set of constraints over a RDF data-model composed by a graph of nodes connected by properties.
In this data-model, all node types and properties have associated terms that must have been defined in a AML Vocabulary.
Additionally, the AML Dialect also defines a mapping of this nodes over a set of modular documents (including partial definitions encoded in fragments and libraries of reusable components), that encode a mapping function capable of transforming document instances of the dialect encoded in YAML or JSON documents into RDF graphs encoded in JSON-LD documents.
An AML Dialect processor must accept this set of constraints and the projection over the modular documents and produce a parsing logic for the mapping function according to the syntactical rules defined in this specification.

## Dialect declaration

Dialect documents are declared using the `#%Dialect 1.0` header. Documents must provide a name for the dialect and a version number, using the `dialect` and `version` properties, respectively.

``` yaml
#%RAML 1.0 Dialect

dialect: Validation Profile

version: “1.0”
```

This information will be used to define the required declaration header for the document  instances of the new dialect.
In this example, dialect document instances must use the following document declaration header:

``` yaml
#%Validation Profile 1.0
```
The AML processor must have loaded the dialect definition in advance for the processor to be able to process the document instances.
To solve this problem, authors of document instances can optionally provide information about the location of the dialect definition to processors, linking it directly in the header of the document instance:

```yaml
#%Validation Profile 1.0 | <http://example.org/dialects/validation_profile>
```
When encoding dialect document instances using JSON syntax, is not possible to use headers as it is the case in dialect document instances encoded using YAML syntax. In this case, a `$dialect` linking directive can be used to declare the dialect for the document instance:

```json
{
  “$dialect”: “Validation Profile 1.0”
}
```

## Using vocabularies

Dialects provide a mapping from vocabulary terms to the structure of a graph of data nodes. The `uses` property can establish this mapping by importing vocabularies in the dialect document:

``` yaml
uses:
  validation: validation.aml
```
Class terms and property terms from the vocabulary can then be referenced using the alias declared in the `vocabularies` property.

In the definition of a dialect, the `external` property can also explicitly reference external vocabularies, as defined in the AML Vocabulary spec. For example we could use Schema.org as an external vocabulary:


``` yaml
external:
  schema-org: http://schema.org/
```

## Node mappings

The property `nodeMappings` introduces the declaration of all the nodes in the model.
The nodes describe the mapping of vocabulary terms to the type of the node and its properties, as well as the constraints associated with each node.

Nodes are declared as a map from node names to node definitions.

The type of each node must be defined by the `classTerm` property.
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

In this case, we declare a new type of node in the model `profileNode` and we associate the class term `validation.Profile` with the type of node. The mapping of properties include two properties: `profile` mapped to the property term; `schema-org.name` and `description` mapped to `schema-org.description`.

The meaning of this mapping can be provided using closed world semantics via [W3C SHACL](https://www.w3.org/TR/shacl/) and defining a data shape constraint over the model data graph for the mapping we have just described:

``` n3
base:profileNode rdf:type shacl:NodeShape ;
  sh:targetClass validation:Profile ;
  sh:property base:profileNode/property/profile ;
  sh:property base:profileNode/property/description .

base:profileNode/property/profile sh:path schema-org:name .

base:profileNode/property/description sh:path schema-org:description .
```

Once this node is mapped to a document instance, it can be used to encode information about a profile, using the labels for the properties declared in the mapping:

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

A property can be constrained over instances of the model when declaring the property mapping of a data node.

| Property | Description | SHACL semantics |
|-------------|-------------|----------------|
| range       | allowed data type for the objects of the property | shacl:datatype |
| mandatory   | the property must be present in the node, default false | shacl:minCount 1 |
| pattern     | a regular expression that must match the value of the property | shacl:pattern |
| minimum     | minimum value for the property | shacl:minInclusive |
| maximum     | maximum value for the property | shacl:maxInclusive |
| enum        | closed set of values this property value must belong to | shacl:in |
| allowMultiple | multiple objects can be used in the value of this property, default false | remove shacl:minCount |

Some other properties in the property mapping can be used to define different ways of connecting the information in the document instance:

| Property | Description |
|-------------|-----------------|
| sorted  | Indicates that the values of the property must be stored preserving the declaration order |
| mapKey | property used to declare a connection in the document instance by value of nested property |
| mapKey | property used to declare a connection in the document instance by a tuple property key and property value |

Finally, some properties can be used to allow users to work explicitly with unions of nodes:

| Property | Description |
|-------------|-----------------|
| typeDiscriminator | mapping from values to vocabulary classes used to disambiguate the type of node |
| typeDiscriminatorName | name of the property used to declare the value of the discriminator |


For example, the following property mapping will be translated into the SHACL constraint shape shown below:


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

SHACL constraint shape:

``` n3
base:profileNode rdf:type shacl:NodeShape ;
  sh:targetClass validation:Profile ;
  sh:property base:profileNode/property/profile .

base:profileNode/property/profile sh:path schema-org:name ;
  sh:minCount 1 ;
  sh:pattern "[a-z]+[A-Za-z]*" .
```

## Literal mappings

Property mappings can map literal `propertyTerms` from a vocabulary to scalar values in properties of the AST nodes in the document instance.

These mappings might have scalar data type in the mapping that will identify the correspondent XSD type associated to the mapping.

The list of valid literal mapping values and the associated  XSD data type can be found in the following table:

| Literal mapping | Data type |
|----------------------|---------------|
| string  | xsd:string |
| integer | xsd:integer |
| boolean | xsd:boolean |
| float | xsd:float |
| decimal | xsd:decimal |
| double | xsd:double |
| duration | xsd:duration |
| dateTime | xsd:dateTime |
| time | xsd:time |
| date | xsd:date |
| anyUri | xsd:anyUri |
| uri | xsd:anyUri |
| anyType | xsd:anyType |
| any  | xsd:anytype

An additional custom type is also available:

| Literal mapping | Custom data type | Description |
|----------------------|-------------------------|----------------|
| number  | shapes:number  | Any numeric data type |

Using literal ranges in property mappings will introduce the corresponding SHACL constraint that will be used by the AML processor to validate the parsed document instance graph.


## Data node mappings

Data node mappings can be connected to describe the full shape of the expected data model graph. This set of connected nodes also describe at the same time the full structure of the dialect document instance, understanding the property mappings defined in the dialect as a mapping function of the tree of AST nodes in the document instance over the generated output graph.

Nodes can be connected using different syntactic styles to allow more expressivity in the design of the dialect syntax using some of the allowed properties in a `nodeMapping`.

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

In the previous example, we have defined two node mappings: `profileNode` and `shapeValidationNode` and we have connected them through the `validations` property mapped to the `schema-org.name` property term.

With these mappings, dialect document instances will validate the following syntax:

``` yaml
#%Validation Profile 1.0

profile: My Profile
validations:
  name: my validation
  message: this is a validation
```

The previous document, when parsed, will generate the following RDF graph:

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

The SHACL semantics for this way of nesting of nodes are shown in the following translation of the dialect mappings:


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

Optionally, the designer of the dialect can allow multiple values instead of a single one in the range of the mapped property. This can be expressed using the `allowMultiple` property with a value of true.

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
By default the nested nodes will be stored in the graph without any particular order. The `sorted` boolean property can be used to enforce ordering in the nested nodes. In this case the generated graph will keep the nodes in an ordered RDF collection.


### Nesting by property value

Sometimes, when the possible values for a property in the model have a unique key that is going to be different in all the child nodes, we can use the value of that key to connect a parent node and children through a map from values of the keys to the children nodes.

This style of syntax can be declared using the property `mapKey` that must have the value of the property term in the nested node mapping that will hold the value of the key.

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
        mapKey: schema-org.name
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
The change is merely syntactical; neither the parsed graph for the dialect instance nor the SHACL semantics for the constraint will be affected by the change.


## Key-Value nesting

Sometimes you want to nest maps of key-value pairs where the the value of the key must be mapped to a property in the graph model node, and the value to a different property.
For example, imagine you want to generate in the graph a list labels, with a `myvocab.labelName` property for the label name and a `myvocab.labelValue` property for the value of the label.

You could declare the syntax in your dialect as a map of key-value pairs in the following way:


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
        mapKey: myvocab.labelName
        mapValue: myvocab.labelValue
```

Using this syntax a document for this dialect could declare a list of labels in the following way:


```yaml
labels:
  label1: a
  label2: b
```

The generated RDF graph will look like this:

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

## Union of nodes mapping

Dialects support defining union of nodes as the valid range of a property mapping.

In order achieve this, an array of `nodeMappings` can be declared as the value for the of the `range` property.

For example:

```yaml
#%Dialect 1.0
dialect: Test Unions
version: 1.0
nodeMappings:
  A:
    …
  B:
    …
  RootNode:
    mapping:
      allowMultiple: true
      unionProperty:
        propertyTerm: vocab.unionProp
          range: [ A, B ]
```

The AML processor must be able to disambiguate the type of node when parsing the document instance. This means that the set of mandatory properties for each type of node in the union must be disjoint. It this requirement is not met, an exception will be raised when parsing the dialect document.

A solution for situations where ambiguous node mappings must be specified in a union property range is to provide an explicit discriminator properties that will be used by the processor to parse the node in the AST tree of the document instance.

The name of the discriminator property and the mapping from discriminator property value to node mappings must be declared in the dialect document using the `typeDiscriminatorName` and `typeDiscriminator` properties respectively.

Consider the following a revised version of the previous example:

```yaml
#%Dialect 1.0
dialect: Test Unions
version: 1.0

nodeMappings:
  A:
    ...
    mapping:
      text:
        propertyTerm: vocab.text
        range: string
  B:
    …
    mapping:
      text:
        propertyTerm: vocab.text
        range: string

  RootNode:
    mapping:
      unionProperty:
        propertyTerm: vocab.unionProp
          range: [ A, B ]
          allowMultiple: true
          typeDiscriminatorName: kind
          typeDiscriminator:
            TypeA: A
            TypeB: B
```

Even if this case the nodeMappings `A` and `B` are ambiguous they can be used in a union thanks to the declared discriminator property `kind`:

```yaml
#%Test Unions 1.0

unionProperty:
  -
    kind: TypeA
     text: this will be parsed using nodeMapping ‘A’
  -
    kind: TypeB
    text: this will be  parsed using nodeMapping ‘B’
```

## Document model mapping

To use the node mappings and constraints defined in the dialect to support new types of documents, we must map the node mappings to the different types of modular documents supported by AML:

- Documents: stand-alone documents that can encode a main element of the domain and declare auxiliary elements that then can be referenced in the document
- Modules: libraries containing sets of declarations of reusable definitions that can be referenced from other types of documents
- Fragments: non-standalone documents encoding a single element that can be included in other types of documents

The mapping in a dialect is achieved through the `documents` property. The value of this property is a mapping with 3 possible keys:

| Property | Description |
| ---      | ---         |
| root | nodes that can be declared and encoded in the root document for the dialect |
| module   | nodes that can be declared in a library for the dialect |
| fragments | nodes that can be encoded into fragments for the dialect |

### Defining the root document mapping

Root documents can encode one type of node, and declare many types of nodesusing the following properties:

| Property | Description |
| ---      | ---         |
| encodes | main node mapping that will be encoded at the root level of the dialect document |
| declares | mapping from declaration key to the type of nodes that can be declared for that key |

Consider the following example mapping:

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

documents:

  root:
    encodes: profileNode
    declares:
      localValidations: shapeValidationNode
```

We are stating that the modular documents for this dialect will encode a `profileNode` mapping and will be able to declare `shapeValidationNodes` introduced by the property `localValidations`.

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

### Defining module mappings

Modules can declare multiple elements that then can be reused from other documents using the library.

The module mapping can be defined using the following properties:

| Property | Description |
| ---      | ---         |
| declares | mapping from declaration key to the type of nodes that can be declared for that key |

Consider the following example mapping:

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

documents:

  root:
    encodes: profileNode
    declares:
      localValidations: shapeValidationNode

  module:
    declares:
      libraryValidations: shapeValidationNode
```

We have expanded the documents mapping to support libraries of validations, declared using the property `libraryValidatons`.

For example:

``` yaml
#%Library / Validation Profile 1.0

# Starting the declarations here
libraryValidations:

  validation1:
    name: my validation
    message: this is a message

  validation2:
    name: other validation
    message: this is the other message
```

In the declaration of the AML dialect, we need to provide to the dialect processor the identifier of the dialect. The dialect processor can then determine how to parse the document.

From the main dialect document we can use the library using a library alias reference introduced with the `uses` keyword or through ` $ref` link:

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

### Defining fragment mappings

Fragments are not-stand-alone documents that need to be included in other documents to be re-used.

We can define which types of fragments are supported in our dialect using the `fragments` property and the nested `encodes` property.

The value of this property is a mapping from fragment header identifier to node mapping that the fragment must satisfy:

| Property | Description |
| ---      | ---         |
| encodes | mapping from fragment header identifier to the type of nodes that can be encoded in that fragment |


Consider the following mapping:

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

documents:

  root:
    encodes: profileNode
    declares:
      localValidations: shapeValidationNode

  fragments:
    encodes:
      Validation: shapeValidationNode
```

With this mapping, we can define a fragment for the dialect with the following shape validation:

``` yaml
#%Validation / Validation Profile 1.0

# the encoded validation
name: my validation
message: this is a message
```

This fragment can be used from the main dialect document through a `!include` link:


``` yaml
#%Validation Profile 1.0

# the main encoded element
profile: My Profile
validations:
  - !include validation_fragment.raml
```

## Dynamic composition of documents

Sometimes is not possible to know in advance which kind of documents are going to be composed, or we want to provide an expansion point in our dialect where other types of documents from external dialects are linked.

We can achieve this declaring dynamic property mappings, using the special value `anyNode` as the value of the `range` property in the mapping.

Consider the following example:

```yaml
#%Dialect 1.0
dialect: Test Dynamic Node
version: 1.0

external:
  v: http://test.com/v#

nodeMappings:
  RootNode:
    classTerm: v.Root
    mapping:
      dynamic:
        propertyTerm: v.dynamic
        range: anyNode

documents:
  root:
    encodes: RootNode
```

In this case the range of the `dynamic` property mapping in the `RootNode` has been declared to be `anyNode` that means that any valid node can be inserted in that position of the AST of the document.

For example, any fragment can be included:

```yaml
#%Test Dynamic Node 1.0

dynamic: !include external_fragment.yaml
```

In order for the AML processor to be able to parse the external fragments the document instance must include the explicit link to the dialect in the document header.

Dynamic nodes can also be declared inline nested under the dynamic nodes.
In this case, in order for the AML processor to be able to parse the nested AST, the `nodeMapping` in the external dialect must be declared in the top level node of the dynamic tree using the `$dialect` linking directive. Notice that the reference must be a JSON pointer to the node mapping in the external dialect, not only to the dialect document.

For example:

```yaml
#%Test Dynamic Node 1.0

dynamic:
  $dialect: external_dialect.yaml#/declarations/TopLevelNode
  label: this is declared in ‘TopLevelNode’ of ‘external_dialect.yaml’
```

## ID/URI generation and customization

By default every AML processor must generate automatic URIs identifying every node in the parsed information graph.
URIs will be generated as hash URIs based on the location of the parsed node in the document AST.

The root node of the document will be identified by the `#/` fragment. Declarations will be introduced by fragments using the declaration value defined in the document mapping.

Node mappings declarations in a dialect document will be defined under the `#/declarations` fragment path.

The final ID/URI associated to a node in the parsed graph can be controlled using the `$id` directive. The value of `$id` can be a relative or absolute URI that will be used to identify the parsed node for that AST node in the output graph.

## Linking styles

AML processors must support three different styles of links across the modular document instances for a dialect:

### Include links

Include links marked by the linking directive introduced by the strings `!include` or `$include` can be used to link to the encoded node of a fragment

### Library aliases

Nodes collected in a reusable library document can be referenced in a target document using a library alias using the `use` keyword to introduce the library alias and then a `{alias}.{declaration}` notation for the actual node reference

### Hash reference links

Hash reference links, introduced by the `$ref` linking directive whose value must be the ID/URI of the referenced element, for example within a library.

### Dialect links

Dialect links, introduced by the `$dialect` linking directive can be used to provide information about the dialect for a full document, for example, when using JSON syntax for a dialect instance document or for the node mapping in a dynamic node.

## References

- Berners-Lee, T., Masinter, L., and M. McCahill, "Uniform Resource Locators (URL)", RFC 1738, December 1994.
- [RDF 1.1 Concepts and Abstract Syntax](https://www.w3.org/TR/rdf11-concepts/)
- [SHACL](https://www.w3.org/TR/shacl/)
- [OWL 2 Web Ontology Language Document Overview (2nd Edition)](https://www.w3.org/TR/owl2-overview/)
- [W3C XML Schema Definition Language (XSD) 1.1](http://www.w3.org/XML/Schema)
- [YAML Aint Markup Language](http://www.yaml.org/spec/1.2/spec.html)
- [RFC 4627 - The application/json Media Type for Javascript Object Notation (JSON)](https://tools.ietf.org/html/rfc4627)
