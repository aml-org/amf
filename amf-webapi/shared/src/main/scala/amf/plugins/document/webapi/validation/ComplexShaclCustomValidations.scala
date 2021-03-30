package amf.plugins.document.webapi.validation

import amf.core.validation.core.{PropertyConstraint, ValidationSpecification}

import scala.collection.mutable.ArrayBuffer

object ComplexShaclCustomValidations {

  val xmlWrappedScalar = List(
    ValidationSpecification(
      name = "http://a.ml/vocabularies/amf/parser#xml-wrapped-scalar",
      message = "XML property 'wrapped' must be false for scalar types",
      ramlMessage = Some("XML property 'wrapped' must be false for scalar types"),
      oasMessage = Some("XML property 'wrapped' must be false for scalar types"),
      targetClass = List("http://a.ml/vocabularies/shapes#ScalarShape"),
      unionConstraints = List(
        "http://a.ml/vocabularies/amf/parser#xml-wrapped-scalar_or_0",
        "http://a.ml/vocabularies/amf/parser#xml-wrapped-scalar_or_1"
      ),
      replacesFunctionConstraint = Some("xmlWrappedScalar")
    ),
    ValidationSpecification(
      name = "http://a.ml/vocabularies/amf/parser#xml-wrapped-scalar_or_0",
      message = "XML property 'wrapped' must be false for scalar types",
      propertyConstraints = ArrayBuffer(
        PropertyConstraint(
          ramlPropertyId = "raml-shapes.xmlSerialization",
          name = "raml-shapes.xmlSerialization",
          node = Some("http://a.ml/vocabularies/amf/parser#xml-wrapped-scalar_or_0_raml-shapes.xmlSerialization_node")
        )
      ),
      nested = Some("http://a.ml/vocabularies/amf/parser#xml-wrapped-scalar")
    ),
    ValidationSpecification(
      name = "http://a.ml/vocabularies/amf/parser#xml-wrapped-scalar_or_1",
      message = "XML property 'wrapped' must be false for scalar types",
      propertyConstraints = ArrayBuffer(
        PropertyConstraint(
          ramlPropertyId = "raml-shapes.xmlSerialization",
          name = "raml-shapes.xmlSerialization",
          node = Some("http://a.ml/vocabularies/amf/parser#xml-wrapped-scalar_or_1_raml-shapes.xmlSerialization_node"),
        )
      ),
      nested = Some("http://a.ml/vocabularies/amf/parser#xml-wrapped-scalar")
    ),
    ValidationSpecification(
      name = "http://a.ml/vocabularies/amf/parser#xml-wrapped-scalar_or_0_raml-shapes.xmlSerialization_node",
      message = "XML property 'wrapped' must be false for scalar types",
      propertyConstraints = ArrayBuffer(
        PropertyConstraint(
          ramlPropertyId = "raml-shapes.xmlWrapped",
          name = "raml-shapes.xmlWrapped",
          in = ArrayBuffer("false"),
        )
      ),
      nested = Some("http://a.ml/vocabularies/amf/parser#xml-wrapped-scalar")
    ),
    ValidationSpecification(
      name = "http://a.ml/vocabularies/amf/parser#xml-wrapped-scalar_or_1_raml-shapes.xmlSerialization_node",
      message = "XML property 'wrapped' must be false for scalar types",
      propertyConstraints = ArrayBuffer(
        PropertyConstraint(
          ramlPropertyId = "raml-shapes.xmlWrapped",
          name = "raml-shapes.xmlWrapped",
          maxCount = Some("0"),
        )
      ),
      nested = Some("http://a.ml/vocabularies/amf/parser#xml-wrapped-scalar")
    )
  )
  val xmlNonScalarAttribute = List(
    ValidationSpecification(
      name = "http://a.ml/vocabularies/amf/parser#xml-non-scalar-attribute",
      message = "XML property 'attribute' must be false for non-scalar types",
      ramlMessage = Some("XML property 'attribute' must be false for non-scalar types"),
      oasMessage = Some("XML property 'attribute' must be false for non-scalar types"),
      targetClass = List("http://www.w3.org/ns/shacl#Shape"),
      unionConstraints = List(
        "http://a.ml/vocabularies/amf/parser#xml-non-scalar-attribute_or_0",
        "http://a.ml/vocabularies/amf/parser#xml-non-scalar-attribute_or_1",
        "http://a.ml/vocabularies/amf/parser#xml-non-scalar-attribute_or_2"
      ),
      replacesFunctionConstraint = Some("xmlNonScalarAttribute")
    ),
    ValidationSpecification(
      name = "http://a.ml/vocabularies/amf/parser#xml-non-scalar-attribute_or_0",
      message = "XML property 'attribute' must be false for non-scalar types",
      ramlMessage = Some("XML property 'attribute' must be false for non-scalar types"),
      oasMessage = Some("XML property 'attribute' must be false for non-scalar types"),
      nested = Some("http://a.ml/vocabularies/amf/parser#xml-non-scalar-attribute"),
      propertyConstraints = ArrayBuffer(
        PropertyConstraint(
          ramlPropertyId = "rdf.type",
          name = "rdf.type",
          value = Some("http://a.ml/vocabularies/shapes#ScalarShape")
        )
      ),
    ),
    ValidationSpecification(
      name = "http://a.ml/vocabularies/amf/parser#xml-non-scalar-attribute_or_1",
      message = "XML property 'attribute' must be false for non-scalar types",
      ramlMessage = Some("XML property 'attribute' must be false for non-scalar types"),
      oasMessage = Some("XML property 'attribute' must be false for non-scalar types"),
      propertyConstraints = ArrayBuffer(
        PropertyConstraint(
          ramlPropertyId = "raml-shapes.xmlSerialization",
          name = "raml-shapes.xmlSerialization",
          node =
            Some("http://a.ml/vocabularies/amf/parser#xml-non-scalar-attribute_or_1_raml-shapes.xmlSerialization_node")
        )
      ),
      nested = Some("http://a.ml/vocabularies/amf/parser#xml-non-scalar-attribute")
    ),
    ValidationSpecification(
      name = "http://a.ml/vocabularies/amf/parser#xml-non-scalar-attribute_or_2",
      message = "XML property 'attribute' must be false for non-scalar types",
      ramlMessage = Some("XML property 'attribute' must be false for non-scalar types"),
      oasMessage = Some("XML property 'attribute' must be false for non-scalar types"),
      propertyConstraints = ArrayBuffer(
        PropertyConstraint(
          ramlPropertyId = "raml-shapes.xmlSerialization",
          name = "raml-shapes.xmlSerialization",
          node =
            Some("http://a.ml/vocabularies/amf/parser#xml-non-scalar-attribute_or_2_raml-shapes.xmlSerialization_node"),
        )
      ),
      nested = Some("http://a.ml/vocabularies/amf/parser#xml-non-scalar-attribute")
    ),
    ValidationSpecification(
      name = "http://a.ml/vocabularies/amf/parser#xml-non-scalar-attribute_or_1_raml-shapes.xmlSerialization_node",
      message = "XML property 'attribute' must be false for non-scalar types",
      ramlMessage = Some("XML property 'attribute' must be false for non-scalar types"),
      oasMessage = Some("XML property 'attribute' must be false for non-scalar types"),
      propertyConstraints = ArrayBuffer(
        PropertyConstraint(
          ramlPropertyId = "raml-shapes.xmlAttribute",
          name = "raml-shapes.xmlAttribute",
          maxCount = Some("0"),
        )
      ),
      nested = Some("http://a.ml/vocabularies/amf/parser#xml-non-scalar-attribute"),
    ),
    ValidationSpecification(
      name = "http://a.ml/vocabularies/amf/parser#xml-non-scalar-attribute_or_2_raml-shapes.xmlSerialization_node",
      message = "XML property 'attribute' must be false for non-scalar types",
      ramlMessage = Some("XML property 'attribute' must be false for non-scalar types"),
      oasMessage = Some("XML property 'attribute' must be false for non-scalar types"),
      propertyConstraints = ArrayBuffer(
        PropertyConstraint(
          ramlPropertyId = "raml-shapes.xmlAttribute",
          name = "raml-shapes.xmlAttribute",
          in = ArrayBuffer("false"),
        )
      ),
      nested = Some("http://a.ml/vocabularies/amf/parser#xml-non-scalar-attribute")
    )
  )
  val fileParameterMustBeInFormData = List(
    ValidationSpecification(
      name = "http://a.ml/vocabularies/amf/parser#file-parameter-in-form-data",
      message = "Parameter of type file must set property 'in' to formData",
      ramlMessage = Some("Parameter of type file must set property 'in' to formData"),
      oasMessage = Some("Parameter of type file must set property 'in' to formData"),
      targetClass = List("http://a.ml/vocabularies/apiContract#Parameter"),
      unionConstraints = List(
        "http://a.ml/vocabularies/amf/parser#file-parameter-in-form-data_or_0",
        "http://a.ml/vocabularies/amf/parser#file-parameter-in-form-data_or_1"
      ),
      replacesFunctionConstraint = Some("fileParameterMustBeInFormData")
    ),
    ValidationSpecification(
      name = "http://a.ml/vocabularies/amf/parser#file-parameter-in-form-data_or_0",
      message = "Parameter of type file must set property 'in' to formData",
      ramlMessage = Some("Parameter of type file must set property 'in' to formData"),
      oasMessage = Some("Parameter of type file must set property 'in' to formData"),
      notConstraint = Some("http://a.ml/vocabularies/amf/parser#file-parameter-in-form-data_or_0_not"),
      nested = Some("http://a.ml/vocabularies/amf/parser#file-parameter-in-form-data")
    ),
    ValidationSpecification(
      name = "http://a.ml/vocabularies/amf/parser#file-parameter-in-form-data_or_1",
      message = "Parameter of type file must set property 'in' to formData",
      ramlMessage = Some("Parameter of type file must set property 'in' to formData"),
      oasMessage = Some("Parameter of type file must set property 'in' to formData"),
      andConstraints = List(
        "http://a.ml/vocabularies/amf/parser#file-parameter-in-form-data_or_1_and_0",
        "http://a.ml/vocabularies/amf/parser#file-parameter-in-form-data_or_1_and_1"
      ),
      nested = Some("http://a.ml/vocabularies/amf/parser#file-parameter-in-form-data")
    ),
    ValidationSpecification(
      name = "http://a.ml/vocabularies/amf/parser#file-parameter-in-form-data_or_0_not",
      message = "Parameter of type file must set property 'in' to formData",
      ramlMessage = Some("Parameter of type file must set property 'in' to formData"),
      oasMessage = Some("Parameter of type file must set property 'in' to formData"),
      propertyConstraints = ArrayBuffer(
        PropertyConstraint(
          ramlPropertyId = "http://a.ml/vocabularies/shapes#schema",
          name = "raml-shapes.schema",
          node =
            Some("http://a.ml/vocabularies/amf/parser#file-parameter-in-form-data_or_0_not_raml-shapes.schema_node"),
        )
      ),
      nested = Some("http://a.ml/vocabularies/amf/parser#file-parameter-in-form-data")
    ),
    ValidationSpecification(
      name = "http://a.ml/vocabularies/amf/parser#file-parameter-in-form-data_or_0_not_raml-shapes.schema_node",
      message = "Parameter of type file must set property 'in' to formData",
      ramlMessage = Some("Parameter of type file must set property 'in' to formData"),
      oasMessage = Some("Parameter of type file must set property 'in' to formData"),
      propertyConstraints = List(
        PropertyConstraint(
          ramlPropertyId = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
          name = "rdf.type_0",
          value = Some("http://a.ml/vocabularies/shapes#FileShape")
        )
      ),
      nested = Some("http://a.ml/vocabularies/amf/parser#file-parameter-in-form-data")
    ),
    ValidationSpecification(
      name = "http://a.ml/vocabularies/amf/parser#file-parameter-in-form-data_or_1_and_0",
      message = "Parameter of type file must set property 'in' to formData",
      ramlMessage = Some("Parameter of type file must set property 'in' to formData"),
      oasMessage = Some("Parameter of type file must set property 'in' to formData"),
      propertyConstraints = ArrayBuffer(
        PropertyConstraint(
          ramlPropertyId = "http://a.ml/vocabularies/apiContract#binding",
          name = "apiContract.binding",
          in = ArrayBuffer("formData"),
        )
      ),
      nested = Some("http://a.ml/vocabularies/amf/parser#file-parameter-in-form-data")
    ),
    ValidationSpecification(
      name = "http://a.ml/vocabularies/amf/parser#file-parameter-in-form-data_or_1_and_1",
      message = "Parameter of type file must set property 'in' to formData",
      ramlMessage = Some("Parameter of type file must set property 'in' to formData"),
      oasMessage = Some("Parameter of type file must set property 'in' to formData"),
      propertyConstraints = ArrayBuffer(
        PropertyConstraint(
          ramlPropertyId = "http://a.ml/vocabularies/shapes#schema",
          name = "raml-shapes.schema",
          node =
            Some("http://a.ml/vocabularies/amf/parser#file-parameter-in-form-data_or_1_and_1_raml-shapes.schema_node"),
        )
      ),
      nested = Some("http://a.ml/vocabularies/amf/parser#file-parameter-in-form-data")
    ),
    ValidationSpecification(
      name = "http://a.ml/vocabularies/amf/parser#file-parameter-in-form-data_or_1_and_1_raml-shapes.schema_node",
      message = "Parameter of type file must set property 'in' to formData",
      ramlMessage = Some("Parameter of type file must set property 'in' to formData"),
      oasMessage = Some("Parameter of type file must set property 'in' to formData"),
      propertyConstraints = List(
        PropertyConstraint(
          ramlPropertyId = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
          name = "rdf.type_0",
          value = Some("http://a.ml/vocabularies/shapes#FileShape")
        )
      ),
      nested = Some("http://a.ml/vocabularies/amf/parser#file-parameter-in-form-data")
    )
  )
  val pathParameterRequiredProperty = List(
    ValidationSpecification(
      name = "http://a.ml/vocabularies/amf/parser#path-parameter-required",
      message = "Path parameters must have the required property set to true",
      ramlMessage = Some("Path parameters must have the required property set to true"),
      oasMessage = Some("Path parameters must have the required property set to true"),
      targetClass = List("http://a.ml/vocabularies/apiContract#Parameter"),
      unionConstraints = List("http://a.ml/vocabularies/amf/parser#path-parameter-required_or_0",
                              "http://a.ml/vocabularies/amf/parser#path-parameter-required_or_1"),
      replacesFunctionConstraint = Some("pathParameterRequiredProperty")
    ),
    ValidationSpecification(
      name = "http://a.ml/vocabularies/amf/parser#path-parameter-required_or_0",
      message = "Path parameters must have the required property set to true",
      ramlMessage = Some("Path parameters must have the required property set to true"),
      oasMessage = Some("Path parameters must have the required property set to true"),
      notConstraint = Some("http://a.ml/vocabularies/amf/parser#path-parameter-required_or_0_not"),
      nested = Some("http://a.ml/vocabularies/amf/parser#path-parameter-required")
    ),
    ValidationSpecification(
      name = "http://a.ml/vocabularies/amf/parser#path-parameter-required_or_1",
      message = "Path parameters must have the required property set to true",
      ramlMessage = Some("Path parameters must have the required property set to true"),
      oasMessage = Some("Path parameters must have the required property set to true"),
      propertyConstraints = ArrayBuffer(
        PropertyConstraint(
          ramlPropertyId = "http://a.ml/vocabularies/apiContract#required",
          name = "apiContract.required",
          in = ArrayBuffer("true"),
        )
      ),
      nested = Some("http://a.ml/vocabularies/amf/parser#path-parameter-required")
    ),
    ValidationSpecification(
      name = "http://a.ml/vocabularies/amf/parser#path-parameter-required_or_0_not",
      message = "Path parameters must have the required property set to true",
      ramlMessage = Some("Path parameters must have the required property set to true"),
      oasMessage = Some("Path parameters must have the required property set to true"),
      propertyConstraints = ArrayBuffer(
        PropertyConstraint(
          ramlPropertyId = "apiContract:binding",
          name = "apiContract:binding",
          in = ArrayBuffer("path"),
        )
      ),
      nested = Some("http://a.ml/vocabularies/amf/parser#path-parameter-required")
    )
  )
  val exampleMutuallyExclusiveFields = List(
    ValidationSpecification(
      name = "http://a.ml/vocabularies/amf/parser#example-mutually-exclusive-fields",
      message = "Example 'value' and 'externalValue' fields are mutually exclusive",
      targetClass = List("http://a.ml/vocabularies/apiContract#Example"),
      notConstraint = Some("http://a.ml/vocabularies/amf/parser#example-mutually-exclusive-fields_not"),
      replacesFunctionConstraint = Some("exampleMutuallyExclusiveFields")
    ),
    ValidationSpecification(
      name = "http://a.ml/vocabularies/amf/parser#example-mutually-exclusive-fields_not",
      message = "Example 'value' and 'externalValue' fields are mutually exclusive",
      propertyConstraints = ArrayBuffer(
        PropertyConstraint(
          ramlPropertyId = "http://a.ml/vocabularies/document#externalValue",
          name = "doc.externalValue",
          minCount = Some("1"),
        ),
        PropertyConstraint(
          ramlPropertyId = "http://a.ml/vocabularies/document#structuredValue",
          name = "doc.structuredValue",
          minCount = Some("1"),
        )
      ),
      nested = Some("http://a.ml/vocabularies/amf/parser#example-mutually-exclusive-fields")
    )
  )
  val minimumMaximumValidation = List(
    ValidationSpecification(
      name = "http://a.ml/vocabularies/amf/parser#min-max-inclusive",
      message = "Maximum must be greater than or equal to minimum",
      ramlMessage = Some("Maximum must be greater than or equal to minimum"),
      oasMessage = Some("Maximum must be greater than or equal to minimum"),
      targetClass = List("http://a.ml/vocabularies/shapes#ScalarShape"),
      propertyConstraints = ArrayBuffer(PropertyConstraint(
        ramlPropertyId = "http://www.w3.org/ns/shacl#minInclusive",
        name = "shacl.minInclusive",
        lessThanOrEqualsToProperty = Some("http://www.w3.org/ns/shacl#maxInclusive")
      )),
      replacesFunctionConstraint = Some("minimumMaximumValidation")
    ))
  val minMaxItemsValidation = List(
    ValidationSpecification(
      name = "http://a.ml/vocabularies/amf/parser#min-max-items",
      message = "MaxItems must be greater than or equal to minItems",
      ramlMessage = Some("MaxItems must be greater than or equal to minItems"),
      oasMessage = Some("MaxItems must be greater than or equal to minItems"),
      targetClass = List("http://a.ml/vocabularies/shapes#ArrayShape"),
      propertyConstraints = ArrayBuffer(
        PropertyConstraint(ramlPropertyId = "http://www.w3.org/ns/shacl#minCount",
                           name = "shacl.minCount",
                           lessThanOrEqualsToProperty = Some("http://www.w3.org/ns/shacl#maxCount"))),
      replacesFunctionConstraint = Some("minMaxItemsValidation")
    ))
  val minMaxLengthValidation = List(
    ValidationSpecification(
      name = "http://a.ml/vocabularies/amf/parser#min-max-length",
      message = "MaxItems must be greater than or equal to minItems",
      ramlMessage = Some("MaxLength must be greater than or equal to minLength"),
      oasMessage = Some("MaxLength must be greater than or equal to minLength"),
      targetClass = List("http://a.ml/vocabularies/shapes#Shape"),
      propertyConstraints = ArrayBuffer(
        PropertyConstraint(ramlPropertyId = "http://www.w3.org/ns/shacl#minLength",
                           name = "shacl.minLength",
                           lessThanOrEqualsToProperty = Some("http://www.w3.org/ns/shacl#maxLength"))),
      replacesFunctionConstraint = Some("minMaxLengthValidation")
    ))
  val minMaxPropertiesValidation = List(
    ValidationSpecification(
      name = "http://a.ml/vocabularies/amf/parser#min-max-properties",
      message = "MaxProperties must be greater than or equal to minProperties",
      ramlMessage = Some("MaxProperties must be greater than or equal to minProperties"),
      oasMessage = Some("MaxProperties must be greater than or equal to minProperties"),
      targetClass = List("http://www.w3.org/ns/shacl#NodeShape"),
      propertyConstraints = ArrayBuffer(PropertyConstraint(
        ramlPropertyId = "http://a.ml/vocabularies/shapes#minProperties",
        name = "shapes.minProperties",
        lessThanOrEqualsToProperty = Some("http://a.ml/vocabularies/shapes#maxProperties")
      )),
      replacesFunctionConstraint = Some("minMaxPropertiesValidation")
    ))

  val defintions: Map[String, Seq[ValidationSpecification]] = Map(
    "xmlWrappedScalar"               -> xmlWrappedScalar,
    "xmlNonScalarAttribute"          -> xmlNonScalarAttribute,
    "fileParameterMustBeInFormData"  -> fileParameterMustBeInFormData,
    "pathParameterRequiredProperty"  -> pathParameterRequiredProperty,
    "exampleMutuallyExclusiveFields" -> exampleMutuallyExclusiveFields,
    "minimumMaximumValidation"       -> minimumMaximumValidation,
    "minMaxItemsValidation"          -> minMaxItemsValidation,
    "minMaxLengthValidation"         -> minMaxLengthValidation,
    "minMaxPropertiesValidation"     -> minMaxPropertiesValidation
  )

}
