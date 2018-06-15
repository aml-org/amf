# AML Vocabularies 1.0

AML Vocabularies is a mechanism to describe any domain of discourse using familiar YAML syntax and modular documents.
AML Vocabularies define a hierarchy of classes and properties according to the semantics of W3C Standard [OWL 2](https://www.w3.org/TR/owl2-overview/) that can be used as the formal foundation for any information processing system.

## Vocabulary declaration

AML Vocabularies are declared using the `#%Vocabulary 1.0` header. A mandatory `base` property provides a [URI prefix](https://www.w3.org/TR/curie/) that will be used to generate unique URIs for all terms in the vocabulary.

``` yaml
#%Vocabulary 1.0

vocabulary: amf-validation

base: "http://raml.org/vocabularies/amf-validation#"
```

Vocabularies can refer to terms in other vocabularies, that can be imported using the `uses` property.

The AML processor will introduce those terms in the vocabulary graph.

Additionally, vocabularies can refer to terms defined in any other ontology or standard that are defined or identified using URIs. The `external` property can be used for this.
The AML vocabulary parser will not try to process the external reference. It will just build URIs using the external URI prefix and include those URIs in the graph for the vocabulary being edited.

In the following example we are introducing [schema.org](http://schema.org) as an external vocabulary, and then using it to define a schema.org class term:

``` yaml
external:
  schema-org: "http://schema.org/"

classTerms:

  schema-org.Person:
```

The rest of the vocabulary document consist of two maps: `classTerms` declaring class terms and `propertyTerms` declaring property terms.

| Property | Description|
| ---      | --- |
| vocabulary | Name for the vocabulary |
| usage? | Description of the vocabulary |
| base | URI prefix for the terms in the vocabulary |
| uses? | Property introducing aliases to reuse terms defined in other AML Vocabularies |
| external? | Property introducing aliases for external ontologies/standares |


## Property terms

Property terms, introduced by the `propertyTerms` property in the top level document, represent relations between individuals and data types or between individuals of different classes.

Will be defining a data type property for the former, and introducing an object property for the latter.

The `range` property can be used to define the type of property:

- if the range of the property is the identifier of a class term or a external URI, we will be introducing an object property
- if the range of the property is a data type identifier, we will be introducing an data type property.

The semantics of data type properties and object properties are provided by the translation into OWL.

The following table shows all the properties that can be used in a property term:

| Property | Description |
| --- | --- |
| displayName? | Human readable name for the property term |
| description? | Human readable description of the property term |
| range? | Range of the property relation |
| extends? | Inheritance from other property term |


### Object properties

This is an example of an object property:

``` yaml

classTerms:

  Profile:

propertyTerms:

  extendsProfile:
    displayName: extends profile
    description: Optional profile this validation is going to inherit from
    range: Profile
```

That will be translated into the following RDF graph:

``` n3
base:extendsProfile rdfs:type owl:ObjectProperty ;
  schema-org:name "extends profile" ;
  schema-org:description "Optional profile this validation is going to inherit from" ;
  rdfs:range base:Profile .
```

### Datatype properties

This is an example of a data type property declaration:

``` yaml

propertyTerms:

  ramlClassId:
    displayName: Class ID
    description: A well known string identifier for a vocabulary class
    range: string
```

With OWL semantics:

``` n3
base:ramlClassId rdfs:type owl:DatatypeProperty ;
  schema-org:name "Class ID" ;
  schema-org:description "A well known string identifier for a vocabulary class" ;
  rdfs:range xsd:string .
```

The available data type identifiers and their RDF translation can be found in the following table:

| identifier | URI |
| ---        | --- |
| string | xsd:string |
| integer | xsd:integer |
| float | xsd:float |
| boolean | xsd:boolean |
| uri | xsd:anyURI |
| any | xsd:anytype |


### Property inheritance

Properties can hold an inheritance relationship. The `extends` property is used to express the inheritance relationship.
One property term can inherit from multiple property terms. Data type properties cannot inherit from object properties and vice versa.
Semantics for inheritance is also provided by OWL semantics as shown in the following example:

``` yaml
propertyTerms:

  vocab.extends:

  extendsProfile:
    displayName: extends profile
    extends: vocab.extends
    range: Profile
```

``` n3
base:extendsProfile rdfs:type owl:ObjectProperty ;
  schema-org:name "extends profile"
  rdfs:subPropertyOf vocab:extends ;
  rdfs:range base:Profile .
```

Consequently for each assertion like:

``` n3
myProfile base:extendsProfile otherProfile .
```

The following assertion can be inferred:

``` n3
myProfile base:extendsProfile otherProfile .
myProfile vocab:extends otherProfile .
```


## Class terms

Class terms represent sets of individuals. They are declared using an identifier and AML Vocabularies will generate a URI identifying the class in a unique way using the prefix provided in the `base` property of the vocabulary.

The following table shows the properties that can be used to define a class term:

| Property | Description |
| --- | --- |
| displayName? | Human readable name for the class term |
| description? | Human readable description of the class term |
| properties? | Properties with this class term in the domain |
| extends? | Inheritance from other class term |


``` yaml
classTerm:

  Profile:
```
The semantics of class terms are provided by the translation into OWL.

```n3
base:Profile rdfs:type owl:Class .
```


Class terms can have an associated `description` and `displayName`:

```yaml
classTerm:

  Profile:
    displayName: Profile
    description: A set of validations that will be applied together over a parsed HTTP data model
```
The OWL translation for these properties is:

``` n3
base:Profile schema-org:name "Profile" ;
  schema-org:description "A set of validations that will be applied together over a parsed HTTP data model" .
```

### Class inheritance

Class terms can also inherit from other classes. Multiple classes can be specified in the inheritance relationship.

``` yaml
external:
  shacl: "http://www.w3.org/ns/shacl#"

classTerms:

  Validation:
    extends: shacl.Shape
```

Semantics of class extensions matches OWL semantics:

``` n3
base:Validation rdfs:subClassOf shacl:Shape .
```

### Class and property domains

A class can be added to the domain of a property terms using the `properties` property of a class term definition.
The value of `properties` is a list of propertyTerms that will have this class in the domain.
Semantics of the property domain are provided by the OWL translation.

```yaml
  JSConstraint:
    displayName: JavaScript Constraint
    properties:
      - jsCode
```


``` n3
vocab:jsCode rdfs:domain vocab:JSConstraint
```

The implications of introducing a class in the domain of a property is provided by this assertion, which is based on the previous example:

``` n3
a vocab:jsCode b .
```

The following assertion could be inferred:

``` n3
a vocab:jsCode b .
a rdfs:type vocab:JSConstraint
```

## References

- [RDF 1.1 Concepts and Abstract Syntax](https://www.w3.org/TR/rdf11-concepts/)
- [OWL 2.0 Direct Semantics](https://www.w3.org/TR/owl-direct-semantics/)
