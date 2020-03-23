package amf.plugins.document.webapi.validation

import amf._
import amf.core.validation.SeverityLevels
import amf.core.validation.core._
import amf.core.vocabulary.{Namespace, ValueType}
import amf.plugins.document.webapi.validation.AMFRawValidations.AMFValidation
import amf.plugins.features.validation.Validations

trait ImportUtils {
  protected def validationId(validation: AMFValidation): String =
    validation.uri match {
      case Some(s) => Namespace.expand(s.trim).iri()
      case None =>
        val classPostfix    = postfix(validation.owlClass, "domain")
        val propertyPostfix = postfix(validation.owlProperty, "property")
        val constraint      = postfix(validation.constraint, "constraint")
        Namespace.AmfParser.base + classPostfix + "-" + propertyPostfix.trim + "-" + constraint.trim
    }

  protected def postfix(s: String, default: String): String = s match {
    case p: String if p.nonEmpty =>
      if (p.indexOf("#") > -1) {
        p.split("#")(1).trim
      } else if (p.indexOf("/") == -1 && p.indexOf(":") != -1) {
        p.split(":")(1).trim
      } else {
        p.split("/").last.trim
      }
    case _ => default
  }

}

object DefaultAMFValidations extends ImportUtils {

  def profiles(): List[ValidationProfile] =
    AMFRawValidations.map.map {
      case (profile, validationsInGroup) =>
        val violationValidations = parseValidation(validationsInGroup.filter(_.severity == SeverityLevels.VIOLATION))
        val infoValidations      = parseValidation(validationsInGroup.filter(_.severity == SeverityLevels.INFO))
        val warningValidations   = parseValidation(validationsInGroup.filter(_.severity == SeverityLevels.WARNING))

        // sorting parser side validation for this profile
        val violationParserSideValidations = Validations.validations
          .filter { v =>
            Validations
              .level(v.id, profile) == SeverityLevels.VIOLATION
          }
          .map(_.name)
        val infoParserSideValidations = Validations.validations
          .filter { v =>
            Validations.level(v.id, profile) == SeverityLevels.INFO
          }
          .map(_.name)
        val warningParserSideValidations = Validations.validations
          .filter { v =>
            Validations.level(v.id, profile) == SeverityLevels.WARNING
          }
          .map(_.name)

        ValidationProfile(
          name = profile,
          baseProfile = if (profile == AmfProfile) None else Some(AmfProfile),
          infoLevel = infoParserSideValidations ++ infoValidations.map(_.name),
          warningLevel = warningParserSideValidations ++ warningValidations.map(_.name),
          violationLevel = violationParserSideValidations ++ violationValidations.map(_.name),
          validations = infoValidations ++ warningValidations ++ violationValidations ++ Validations.validations
        )
    }.toList

  private def parseValidation(validations: Seq[AMFValidation]): Seq[ValidationSpecification] = {
    validations.flatMap { validation =>
      val uri = validation.uri match {
        case Some(s) => s.trim
        case _       => validationId(validation)
      }

      val spec = ValidationSpecification(
        name = uri,
        message = validation.message.getOrElse(""),
        ramlMessage = Some(validation.ramlErrorMessage),
        oasMessage = Some(validation.openApiErrorMessage),
        targetClass = Seq(validation.owlClass)
      )

      Namespace.expand(validation.target.trim).iri() match {
        case "http://www.w3.org/ns/shacl#path" =>
          val valueType = if (validation.constraint.trim.contains("#")) {
            val strings = validation.constraint.trim.split("#")
            ValueType(Namespace.find(strings.head).get, strings.last)
          } else Namespace.expand(validation.constraint)
          valueType match {
            case sh @ ValueType(Namespace.Shacl, _) =>
              Seq(spec.copy(propertyConstraints = Seq(parsePropertyConstraint(s"$uri/prop", validation, sh))))
            case sh @ ValueType(Namespace.Shapes, _) =>
              findComplexShaclConstraint(sh) match {
                case Some(specs) => specs
                case _           => Seq(spec.copy(functionConstraint = Option(parseFunctionConstraint(s"$uri/prop", validation, sh))))
              }

            case _ => Seq(spec)
          }

        case "http://www.w3.org/ns/shacl#targetObjectsOf" =>
          Seq(spec.copy(
            targetObject = Seq(validation.owlProperty),
            nodeConstraints = Seq(NodeConstraint(validation.constraint, validation.value))
          ))
        case _ => throw new Exception(s"Unknown validation target ${validation.target}")
      }
    }
  }

  private def parsePropertyConstraint(constraintUri: String,
                                      validation: AMFValidation,
                                      sh: ValueType): PropertyConstraint = {
    val constraint = PropertyConstraint(
      ramlPropertyId = validation.owlProperty,
      name = constraintUri,
      message = validation.message
    )
    sh.iri() match {
      case "http://www.w3.org/ns/shacl#minCount"     => constraint.copy(minCount = Some(validation.value))
      case "http://www.w3.org/ns/shacl#maxCount"     => constraint.copy(maxCount = Some(validation.value))
      case "http://www.w3.org/ns/shacl#pattern"      => constraint.copy(pattern = Some(validation.value))
      case "http://www.w3.org/ns/shacl#minExclusive" => constraint.copy(minExclusive = Some(validation.value))
      case "http://www.w3.org/ns/shacl#maxExclusive" => constraint.copy(maxExclusive = Some(validation.value))
      case "http://www.w3.org/ns/shacl#minInclusive" => constraint.copy(minInclusive = Some(validation.value))
      case "http://www.w3.org/ns/shacl#maxInclusive" => constraint.copy(maxInclusive = Some(validation.value))
      case "http://www.w3.org/ns/shacl#minLength"    => constraint.copy(minLength = Some(validation.value))
      case "http://www.w3.org/ns/shacl#maxLength"    => constraint.copy(maxLength = Some(validation.value))
      case "http://www.w3.org/ns/shacl#in"           => constraint.copy(in = validation.value.split("\\s*,\\s*"))
      case "http://www.w3.org/ns/shacl#node"         => constraint.copy(node = Some(validation.value))
      case "http://www.w3.org/ns/shacl#datatype"     => constraint.copy(datatype = Some(validation.value))
      case "http://www.w3.org/ns/shacl#class"        => constraint.copy(`class` = Seq(validation.value))
      case _                                         => throw new Exception(s"Unsupported constraint ${validation.constraint}")
    }
  }

  private def parseFunctionConstraint(validation: AMFValidation, sh: ValueType): FunctionConstraint = {
    FunctionConstraint(
      message = validation.message,
      functionName = None, // i have to ignore the function name so it will be taken from the generated js library
      code = JsCustomValidations(sh.name),
      internalFunction = Some(sh.name)
    )
  }

  private def findComplexShaclConstraint(sh: ValueType): Option[Seq[ValidationSpecification]] = {
    complexShaclCustomValidations.defintions.get(sh.name)
  }
}

object complexShaclCustomValidations {

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
          node = Some("http://a.ml/vocabularies/amf/parser#xml-non-scalar-attribute_or_1_raml-shapes.xmlSerialization_node")
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
          node = Some("http://a.ml/vocabularies/amf/parser#xml-non-scalar-attribute_or_2_raml-shapes.xmlSerialization_node"),
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
      oasMessage =  Some("Parameter of type file must set property 'in' to formData"),
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
      oasMessage =  Some("Parameter of type file must set property 'in' to formData"),
      notConstraint = Some("http://a.ml/vocabularies/amf/parser#file-parameter-in-form-data_or_0_not"),
      nested = Some("http://a.ml/vocabularies/amf/parser#file-parameter-in-form-data")
    ),
    ValidationSpecification(
      name = "http://a.ml/vocabularies/amf/parser#file-parameter-in-form-data_or_1",
      message = "Parameter of type file must set property 'in' to formData",
      ramlMessage = Some("Parameter of type file must set property 'in' to formData"),
      oasMessage =  Some("Parameter of type file must set property 'in' to formData"),
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
      oasMessage =  Some("Parameter of type file must set property 'in' to formData"),
      propertyConstraints = ArrayBuffer(
        PropertyConstraint(
          ramlPropertyId = "http://a.ml/vocabularies/shapes#schema",
          name = "raml-shapes.schema",
          node = Some("http://a.ml/vocabularies/amf/parser#file-parameter-in-form-data_or_0_not_raml-shapes.schema_node"),
        )
      ),
      nested = Some("http://a.ml/vocabularies/amf/parser#file-parameter-in-form-data")
    ),
    ValidationSpecification(
      name = "http://a.ml/vocabularies/amf/parser#file-parameter-in-form-data_or_0_not_raml-shapes.schema_node",
      message = "Parameter of type file must set property 'in' to formData",
      ramlMessage = Some("Parameter of type file must set property 'in' to formData"),
      oasMessage =  Some("Parameter of type file must set property 'in' to formData"),
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
      oasMessage =  Some("Parameter of type file must set property 'in' to formData"),
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
      oasMessage =  Some("Parameter of type file must set property 'in' to formData"),
      propertyConstraints = ArrayBuffer(
        PropertyConstraint(
          ramlPropertyId = "http://a.ml/vocabularies/shapes#schema",
          name = "raml-shapes.schema",
          node = Some("http://a.ml/vocabularies/amf/parser#file-parameter-in-form-data_or_1_and_1_raml-shapes.schema_node"),
        )
      ),
      nested = Some("http://a.ml/vocabularies/amf/parser#file-parameter-in-form-data")
    ),
    ValidationSpecification(
      name = "http://a.ml/vocabularies/amf/parser#file-parameter-in-form-data_or_1_and_1_raml-shapes.schema_node",
      message = "Parameter of type file must set property 'in' to formData",
      ramlMessage = Some("Parameter of type file must set property 'in' to formData"),
      oasMessage =  Some("Parameter of type file must set property 'in' to formData"),
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
      unionConstraints = List(
        "http://a.ml/vocabularies/amf/parser#path-parameter-required_or_0",
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
      propertyConstraints = ArrayBuffer(
        PropertyConstraint(
          ramlPropertyId = "http://www.w3.org/ns/shacl#minInclusive",
          name = "shacl.minInclusive",
          lessThanOrEqualsToProperty = Some("http://www.w3.org/ns/shacl#maxInclusive"))),
      replacesFunctionConstraint = Some("minimumMaximumValidation")))
  val minMaxItemsValidation = List(
    ValidationSpecification(
      name = "http://a.ml/vocabularies/amf/parser#min-max-items",
      message = "MaxItems must be greater than or equal to minItems",
      ramlMessage = Some("MaxItems must be greater than or equal to minItems"),
      oasMessage = Some("MaxItems must be greater than or equal to minItems"),
      targetClass = List("http://a.ml/vocabularies/shapes#ArrayShape"),
      propertyConstraints = ArrayBuffer(
        PropertyConstraint(
          ramlPropertyId = "http://www.w3.org/ns/shacl#minCount",
          name = "shacl.minCount",
          lessThanOrEqualsToProperty = Some("http://www.w3.org/ns/shacl#maxCount"))),
      replacesFunctionConstraint = Some("minMaxItemsValidation")))
  val minMaxLengthValidation = List(
    ValidationSpecification(
      name = "http://a.ml/vocabularies/amf/parser#min-max-length",
      message = "MaxItems must be greater than or equal to minItems",
      ramlMessage = Some("MaxLength must be greater than or equal to minLength"),
      oasMessage = Some("MaxLength must be greater than or equal to minLength"),
      targetClass = List("http://a.ml/vocabularies/shapes#Shape"),
      propertyConstraints = ArrayBuffer(
        PropertyConstraint(
          ramlPropertyId = "http://www.w3.org/ns/shacl#minLength",
          name = "shacl.minLength",
          lessThanOrEqualsToProperty = Some("http://www.w3.org/ns/shacl#maxLength"))),
      replacesFunctionConstraint = Some("minMaxLengthValidation")))
  val minMaxPropertiesValidation = List(ValidationSpecification(
    name = "http://a.ml/vocabularies/amf/parser#min-max-properties",
    message = "MaxProperties must be greater than or equal to minProperties",
    ramlMessage = Some("MaxProperties must be greater than or equal to minProperties"),
    oasMessage = Some("MaxProperties must be greater than or equal to minProperties"),
    targetClass = List("http://www.w3.org/ns/shacl#NodeShape"),
    propertyConstraints = ArrayBuffer(
      PropertyConstraint(
        ramlPropertyId = "http://a.ml/vocabularies/shapes#minProperties",
        name = "shapes.minProperties",
        lessThanOrEqualsToProperty = Some("http://a.ml/vocabularies/shapes#maxProperties"))),
    replacesFunctionConstraint = Some("minMaxPropertiesValidation")))

  val defintions: Map[String, Seq[ValidationSpecification]] = Map(
    "xmlWrappedScalar" -> xmlWrappedScalar,
    "xmlNonScalarAttribute" -> xmlNonScalarAttribute,
    "fileParameterMustBeInFormData" -> fileParameterMustBeInFormData,
    "pathParameterRequiredProperty" -> pathParameterRequiredProperty,
    "exampleMutuallyExclusiveFields" -> exampleMutuallyExclusiveFields,
    "minimumMaximumValidation" -> minimumMaximumValidation,
    "minMaxItemsValidation" -> minMaxItemsValidation,
    "minMaxLengthValidation" -> minMaxLengthValidation,
    "minMaxPropertiesValidation" -> minMaxPropertiesValidation
  )

}

object JsCustomValidations {
  val functions: Map[String, String] = Map(
    "patternValidation" ->
      """|function(shape) {
         |  var pattern = shape["shacl:pattern"];
         |  try {
         |    if(pattern) new RegExp(pattern);
         |    return true;
         |  } catch(e) {
         |    return false;
         |  }
         |}
      """.stripMargin,
    "nonEmptyListOfProtocols" ->
      """
        |function(shape) {
        |  var protocolsArray = shape["apiContract:scheme"];
        |  return !Array.isArray(protocolsArray) || protocolsArray.length > 0;
        |}
      """.stripMargin,
    "requiredFlowsInOAuth2" ->
      """
        |function(shape) {
        |  return true;
        |}
      """.stripMargin, // TODO pending JS implementation
    "requiredOpenIdConnectUrl" ->
      """
        |function(shape) {
        |  return true;
        |}
      """.stripMargin,
    "validCallbackExpression" ->
      """
        |function(shape) {
        |  return true;
        |}
      """.stripMargin,
    "validLinkRequestBody" ->
      """
        |function(shape) {
        |  return true;
        |}
      """.stripMargin,
    "validLinkParameterExpressions" ->
      """
        |function(shape) {
        |  return true;
        |}
      """.stripMargin,
    "headerParamNameMustBeAscii" ->
      """
        |function(shape) {
        |  return true;
        |}
      """.stripMargin,
    "mandatoryHeadersObjectNode" ->
      """
        |function(shape) {
        |  return true;
        |}
      """.stripMargin,
    "mandatoryHeaderNamePattern" ->
      """
        |function(shape) {
        |  return true;
        |}
      """.stripMargin,
    "mandatoryHeaderBindingNamePattern" ->
      """
        |function(shape) {
        |  return true;
        |}
      """.stripMargin
  )

  def apply(name: String): Option[String] = functions.get(name)
}
