ModelId: file://amf-cli/shared/src/test/resources/validations/facets/invalid-facet-value-type.raml
Profile: RAML 1.0
Conforms: false
Number of results: 2

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be boolean
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/facets/invalid-facet-value-type.raml#/declares/any/defining-incorrect-facet-type/customShapeProperties/shape-extension/camel-case/scalar_1
  Property: file://amf-cli/shared/src/test/resources/validations/facets/invalid-facet-value-type.raml#/declares/any/defining-incorrect-facet-type/customShapeProperties/shape-extension/camel-case/scalar_1
  Range: [(8,16)-(8,31)]
  Location: file://amf-cli/shared/src/test/resources/validations/facets/invalid-facet-value-type.raml

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: age should be integer
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/facets/invalid-facet-value-type.raml#/declares/any/defining-incorrect-facet-type/customShapeProperties/shape-extension/object-facet/object_1
  Property: file://amf-cli/shared/src/test/resources/validations/facets/invalid-facet-value-type.raml#/declares/any/defining-incorrect-facet-type/customShapeProperties/shape-extension/object-facet/object_1
  Range: [(10,0)-(13,0)]
  Location: file://amf-cli/shared/src/test/resources/validations/facets/invalid-facet-value-type.raml
