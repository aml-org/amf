Model: file://amf-cli/shared/src/test/resources/validations/facets/custom-facets.raml
Profile: 
Conforms? false
Number of results: 5

Level: Violation

- Source: http://a.ml/vocabularies/amf/parser#missing-user-defined-facet
  Message: Missing required facet 'noHolidays'
  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/facets/custom-facets.raml#/declarations/types/scalar/ErroneousType
  Property: 
  Position: Some(LexicalInformation([(13,0)-(15,0)]))
  Location: file://amf-cli/shared/src/test/resources/validations/facets/custom-facets.raml

- Source: http://a.ml/vocabularies/amf/parser#closed-shape
  Message: Property 'thisIsWrong' not supported in a RAML 1.0 shape node
  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/facets/custom-facets.raml#/declarations/types/scalar/ErroneousType
  Property: 
  Position: Some(LexicalInformation([(14,4)-(15,0)]))
  Location: file://amf-cli/shared/src/test/resources/validations/facets/custom-facets.raml

- Source: http://a.ml/vocabularies/amf/parser#closed-shape
  Message: Property 'noHolidays' not supported in a RAML 1.0 union node
  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/facets/custom-facets.raml#/declarations/types/union/Incorrect1
  Property: 
  Position: Some(LexicalInformation([(22,4)-(23,0)]))
  Location: file://amf-cli/shared/src/test/resources/validations/facets/custom-facets.raml

- Source: http://a.ml/vocabularies/amf/parser#closed-shape
  Message: Property 'f' not supported in a RAML 1.0 union node
  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/facets/custom-facets.raml#/declarations/types/union/Incorrect2
  Property: 
  Position: Some(LexicalInformation([(25,4)-(26,0)]))
  Location: file://amf-cli/shared/src/test/resources/validations/facets/custom-facets.raml

- Source: http://a.ml/vocabularies/amf/parser#closed-shape
  Message: Property 'error' not supported in a RAML 1.0 union node
  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/facets/custom-facets.raml#/declarations/types/union/Incorrect3
  Property: 
  Position: Some(LexicalInformation([(28,4)-(28,15)]))
  Location: file://amf-cli/shared/src/test/resources/validations/facets/custom-facets.raml
