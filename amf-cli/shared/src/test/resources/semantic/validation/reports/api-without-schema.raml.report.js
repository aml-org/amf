ModelId: file://amf-cli/shared/src/test/resources/semantic/validation/api-without-schema.raml
    Profile:
        Conforms: false
Number of results: 1

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/parser#missing-annotation-schema
    Message: Annotations must have a declared a schema even if there are extensions
Severity: Violation
Target: pagination
Property:
    Range: [(7,8)-(7,20)]
Location: file://amf-cli/shared/src/test/resources/semantic/validation/api-without-schema.raml
