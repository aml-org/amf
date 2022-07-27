package amf.shapes.internal.validation.model

import amf.core.client.common.validation.{JsonSchemaProfile, ProfileName, SeverityLevels}
import amf.core.client.scala.vocabulary.Namespace.XsdTypes
import amf.core.client.scala.vocabulary.{Namespace, ValueType}
import amf.shapes.internal.validation.model.AMFRawValidations.AMFValidation

object AMFRawValidations extends CommonValidationDefinitions {

  /** @param uri
    *   URI of the validation, null to auto-generate
    * @param message
    *   Optional message for the validation, propagates to all spec-specific messages if they're all empty
    * @param owlClass
    *   Optional OWL class target of the validation
    * @param owlProperty
    *   Optional OWL property target of the validation
    * @param target
    *   Default is "sh(path)"
    * @param constraint
    *   URI of the constraint component
    * @param value
    *   Value for the constraint component. Default is "0"
    * @param ramlErrorMessage
    *   (optional) specify the validation message thrown in raml
    * @param openApiErrorMessage
    *   (optional) specify the validation message thrown in Oas
    * @param severity
    *   The severity of the validation: VIOLATION | WARNING | INFO. Default is VIOLATION
    */
  class AMFValidation(
      val uri: Option[String],
      val message: Option[String],
      val owlClass: String,
      val owlProperty: String,
      val target: String,
      val constraint: String,
      val value: String,
      val ramlErrorMessage: String,
      val openApiErrorMessage: String,
      val severity: String
  )

  object AMFValidation {
    def fromStrings(
        uri: String = "",
        message: String = "",
        owlClass: String,
        owlProperty: String,
        target: String = "sh:path",
        constraint: String,
        value: String = "0",
        ramlErrorMessage: String = "",
        openApiErrorMessage: String = "",
        severity: String = SeverityLevels.VIOLATION
    ): AMFValidation = {

      def iri(s: String) = Namespace.defaultAliases.uri(s).iri()
      val sameMessage    = message.nonEmpty && ramlErrorMessage.isEmpty && openApiErrorMessage.isEmpty

      new AMFValidation(
        uri = optional(uri).map(Namespace.defaultAliases.uri(_).iri()),
        message = optional(message),
        owlClass = iri(owlClass),
        owlProperty = iri(owlProperty),
        target = iri(target),
        constraint = iri(constraint),
        value = adaptValue(constraint, value),
        ramlErrorMessage = if (sameMessage) message else ramlErrorMessage,
        openApiErrorMessage = if (sameMessage) message else openApiErrorMessage,
        severity = severity
      )
    }

    def apply(
        uri: Option[ValueType] = None,
        message: String = "",
        owlClass: ValueType,
        owlProperty: ValueType,
        target: ValueType = sh("path"),
        constraint: ValueType,
        value: String = "0",
        ramlErrorMessage: String = "",
        openApiErrorMessage: String = "",
        severity: String = SeverityLevels.VIOLATION
    ): AMFValidation = {

      val sameMessage = message.nonEmpty && ramlErrorMessage.isEmpty && openApiErrorMessage.isEmpty

      new AMFValidation(
        uri = uri.map(_.iri()),
        message = optional(message),
        owlClass = owlClass.iri(),
        owlProperty = owlProperty.iri(),
        target = target.iri(),
        constraint = constraint.iri(),
        value = adaptValue(constraint.iri(), value),
        ramlErrorMessage = if (sameMessage) message else ramlErrorMessage,
        openApiErrorMessage = if (sameMessage) message else openApiErrorMessage,
        severity = severity
      )
    }

    def adaptValue(constraint: String, value: String): String =
      if (constraint.endsWith("pattern")) value
      else Namespace.defaultAliases.uri(value).iri() // this might not be a URI, but trying to expand it is still safe

    def optional(s: String): Option[String] = if (s.isEmpty) None else Some(s.trim)
  }

  object AmfShapeValidations {
    private lazy val result = Seq(
      AMFValidation(
        owlClass = shape("ObjectShape"),
        owlProperty = shape("minProperties"),
        constraint = sh("minInclusive"),
        ramlErrorMessage = "minProperties for a RAML Object type cannot be negative",
        openApiErrorMessage = "minProperties for a Schema object cannot be negative"
      ),
      AMFValidation(
        owlClass = shape("ObjectShape"),
        owlProperty = shape("minProperties"),
        constraint = dataType,
        value = integer,
        ramlErrorMessage = "minProperties for a RAML Object type must be an integer",
        openApiErrorMessage = "minProperties for a Schema object must be an integer"
      ),
      AMFValidation(
        owlClass = shape("ObjectShape"),
        owlProperty = shape("maxProperties"),
        constraint = sh("minInclusive"),
        ramlErrorMessage = "maxProperties for a RAML Object type cannot be negative",
        openApiErrorMessage = "maxProperties for a Schema object cannot be negative"
      ),
      AMFValidation(
        owlClass = shape("ObjectShape"),
        owlProperty = shape("maxProperties"),
        constraint = dataType,
        value = integer,
        ramlErrorMessage = "maxProperties for a RAML Object type must be an integer",
        openApiErrorMessage = "maxProperties for a Schema object must be an integer"
      ),
      AMFValidation(
        owlClass = shape("ObjectShape"),
        owlProperty = sh("closed"),
        constraint = dataType,
        value = boolean,
        ramlErrorMessage = "additionalProperties for a RAML Object type must be a boolean",
        openApiErrorMessage = "additionalProperties for a Schema object must be a boolean"
      ),
      AMFValidation(
        owlClass = shape("ObjectShape"),
        owlProperty = shape("discriminator"),
        constraint = dataType,
        value = string,
        ramlErrorMessage = "discriminator for RAML Object type must be a string value",
        openApiErrorMessage = "discriminator for a Schema object must be a string value"
      ),
      AMFValidation(
        owlClass = shape("ObjectShape"),
        owlProperty = shape("discriminatorValue"),
        constraint = dataType,
        value = string,
        ramlErrorMessage = "x-discriminatorValue for RAML Object type must be a string value",
        openApiErrorMessage = "discriminatorValue for a Schema object must be a string value"
      ),
      AMFValidation(
        owlClass = shape("ObjectShape"),
        owlProperty = shape("readOnly"),
        constraint = dataType,
        value = boolean,
        ramlErrorMessage = "(readOnly) for a RAML Object type must be a boolean",
        openApiErrorMessage = "readOnly for a Schema object must be a boolean"
      ),
      AMFValidation(
        owlClass = shape("ArrayShape"),
        owlProperty = minCount,
        constraint = dataType,
        value = integer,
        ramlErrorMessage = "minItems for a RAML Array type must be an integer",
        openApiErrorMessage = "minItems of a Schema object of type 'array' must be an integer"
      ),
      AMFValidation(
        owlClass = shape("ArrayShape"),
        owlProperty = minCount,
        constraint = sh("minInclusive"),
        ramlErrorMessage = "maxItems for a RAML Array type must be greater than 0",
        openApiErrorMessage = "maxItems of a Schema object of type 'array' must be greater than 0"
      ),
      AMFValidation(
        owlClass = shape("ArrayShape"),
        owlProperty = sh("maxCount"),
        constraint = dataType,
        value = integer,
        ramlErrorMessage = "maxItems for a RAML Array type must be an integer",
        openApiErrorMessage = "maxItems of a Schema object of type 'array' must be an integer"
      ),
      AMFValidation(
        owlClass = shape("ArrayShape"),
        owlProperty = minCount,
        constraint = sh("minInclusive"),
        ramlErrorMessage = "minItems for a RAML Array type must be greater than 0",
        openApiErrorMessage = "minItems of a Schema object of type 'array' must be greater than 0"
      ),
      AMFValidation(
        owlClass = shape("ArrayShape"),
        owlProperty = sh("maxCount"),
        constraint = sh("minInclusive"),
        ramlErrorMessage = "maxItems for a RAML Array type must be greater than 0",
        openApiErrorMessage = "maxItems of a Schema object of type 'array' must be greater than 0"
      ),
      AMFValidation(
        owlClass = shape("ArrayShape"),
        owlProperty = shape("uniqueItems"),
        constraint = dataType,
        value = boolean,
        ramlErrorMessage = "uniqueItems for a RAML Array type must be a boolean",
        openApiErrorMessage = "uniqueItems of a Schema object of type 'array' must be a boolean"
      ),
      AMFValidation(
        message = "minContains for an array type must be an integer",
        owlClass = shape("ArrayShape"),
        owlProperty = sh("qualifiedMinCount"),
        constraint = dataType,
        value = integer
      ),
      AMFValidation(
        message = "maxContains for an array type must be an integer",
        owlClass = shape("ArrayShape"),
        owlProperty = sh("qualifiedMaxCount"),
        constraint = dataType,
        value = integer
      ),
      AMFValidation(
        message = "minContains facet should be greater or equal than 0",
        owlClass = shape("ArrayShape"),
        owlProperty = sh("qualifiedMinCount"),
        constraint = sh("minInclusive")
      ),
      AMFValidation(
        message = "maxContains facet should be greater or equal than 0",
        owlClass = shape("ArrayShape"),
        owlProperty = sh("qualifiedMaxCount"),
        constraint = sh("minInclusive")
      ),
      AMFValidation(
        owlClass = shape("ScalarShape"),
        owlProperty = sh("pattern"),
        constraint = dataType,
        value = string,
        ramlErrorMessage = "pattern facet for a RAML scalar type must be a string",
        openApiErrorMessage = "pattern for scalar Schema object of scalar type must be a string"
      ),
      AMFValidation(
        owlClass = shape("ScalarShape"),
        owlProperty = sh("minLength"),
        constraint = dataType,
        value = integer,
        ramlErrorMessage = "minLength facet for a RAML scalar type must be a integer",
        openApiErrorMessage = "minLength for scalar Schema object of scalar type must be a integer"
      ),
      AMFValidation(
        owlClass = shape("ScalarShape"),
        owlProperty = sh("maxLength"),
        constraint = dataType,
        value = integer,
        ramlErrorMessage = "maxLength facet for a RAML scalar type must be a integer",
        openApiErrorMessage = "maxLength for scalar Schema object of scalar type must be a integer"
      ),
      AMFValidation(
        owlClass = shape("ScalarShape"),
        owlProperty = sh("minInclusive"),
        constraint = dataType,
        value = "xsd:double",
        ramlErrorMessage = "minimum facet for a RAML scalar type must be a number",
        openApiErrorMessage = "minimum for scalar Schema object of scalar type must be a integer"
      ),
      AMFValidation(
        owlClass = shape("ScalarShape"),
        owlProperty = sh("maxInclusive"),
        constraint = dataType,
        value = "xsd:double",
        ramlErrorMessage = "maximum facet for a RAML scalar type must be a number",
        openApiErrorMessage = "maximum for scalar Schema object of scalar type must be a integer"
      ),
      AMFValidation(
        owlClass = shape("ScalarShape"),
        owlProperty = sh("minExclusive"),
        constraint = dataType,
        value = boolean,
        ramlErrorMessage = "x-exclusiveMinimum facet for a RAML scalar type must be a boolean",
        openApiErrorMessage = "exclusiveMinimum for scalar Schema object of scalar type must be a boolean"
      ),
      AMFValidation(
        owlClass = shape("ScalarShape"),
        owlProperty = sh("maxExclusive"),
        constraint = dataType,
        value = boolean,
        ramlErrorMessage = "x-exclusiveMaximum facet for a RAML scalar type must be a boolean",
        openApiErrorMessage = "exclusiveMaximum for scalar Schema object of scalar type must be a boolean"
      ),
      AMFValidation(
        message = "Min length facet should be greater or equal than 0",
        owlClass = shape("ScalarShape"),
        owlProperty = sh("minLength"),
        constraint = sh("minInclusive")
      ),
      AMFValidation(
        message = "Max length facet should be greater or equal than 0",
        owlClass = shape("ScalarShape"),
        owlProperty = sh("maxLength"),
        constraint = sh("minInclusive")
      ),
      AMFValidation(
        message = "Min length facet should be greater or equal than 0",
        owlClass = shape("FileShape"),
        owlProperty = sh("minLength"),
        constraint = sh("minInclusive")
      ),
      AMFValidation(
        message = "Max length facet should be greater or equal than 0",
        owlClass = shape("FileShape"),
        owlProperty = sh("maxLength"),
        constraint = sh("minInclusive")
      ),
      AMFValidation(
        owlClass = shape("ScalarShape"),
        owlProperty = shape("format"),
        constraint = dataType,
        value = string,
        ramlErrorMessage = "format facet for a RAML scalar type must be a string",
        openApiErrorMessage = "format for scalar Schema object of scalar type must be a string"
      ),
      AMFValidation(
        owlClass = shape("ScalarShape"),
        owlProperty = shape("multipleOf"),
        constraint = dataType,
        value = "xsd:double",
        ramlErrorMessage = "multipleOf facet for a RAML scalar type must be a number",
        openApiErrorMessage = "multipleOf for scalar Schema object of scalar type must be a number"
      ),
      AMFValidation(
        owlClass = shape("ScalarShape"),
        owlProperty = shape("multipleOf"),
        constraint = sh("minExclusive"),
        ramlErrorMessage = "multipleOf facet for a RAML scalar type must be greater than 0",
        openApiErrorMessage = "multipleOf for scalar Schema object of scalar type must be greater than 0"
      ),
      AMFValidation(
        owlClass = shape("ScalarShape"),
        owlProperty = dataType,
        constraint = minCount,
        value = "1",
        ramlErrorMessage = "type information for a RAML scalar is required",
        openApiErrorMessage = "type information fo a Schema object of scalar type is required"
      ),
      AMFValidation(
        uri = amfParser("pattern-validation"),
        message = "Pattern is not valid",
        owlClass = shape("ScalarShape"),
        owlProperty = sh("pattern"),
        constraint = shape("patternValidation")
      ),
      AMFValidation(
        owlClass = sh("NodeShape"),
        owlProperty = shape("unevaluatedPropertiesSchema"),
        constraint = maxCount,
        severity = SeverityLevels.WARNING,
        message = "Unevaluated properties facet won't be taken into account in validation"
      ),
      AMFValidation(
        owlClass = shape("ArrayShape"),
        owlProperty = shape("unevaluatedItemsSchema"),
        constraint = maxCount,
        severity = SeverityLevels.WARNING,
        message = "Unevaluated items facet won't be taken into account in validation"
      ),
      AMFValidation(
        owlClass = shape("ArrayShape"),
        owlProperty = sh("qualifiedMinCount"),
        constraint = maxCount,
        severity = SeverityLevels.WARNING,
        message = "minContains facet won't be taken into account in validation"
      ),
      AMFValidation(
        owlClass = shape("ArrayShape"),
        owlProperty = sh("qualifiedMaxCount"),
        constraint = maxCount,
        severity = SeverityLevels.WARNING,
        message = "maxContains facet won't be taken into account in validation"
      ),
      AMFValidation(
        uri = amfParser("xml-wrapped-scalar"),
        message = "XML property 'wrapped' must be false for scalar types",
        owlClass = shape("ScalarShape"),
        owlProperty = sh("xmlSerialization"),
        constraint = shape("xmlWrappedScalar")
      ),
      AMFValidation(
        uri = amfParser("xml-non-scalar-attribute"),
        message = "XML property 'attribute' must be false for non-scalar types",
        owlClass = shape("Shape"),
        owlProperty = sh("xmlSerialization"),
        constraint = shape("xmlNonScalarAttribute")
      )
    )

    def validations(): Seq[AMFValidation] = result
  }

  object RamlShapeValidations {
    private lazy val result = Seq(
      AMFValidation(
        uri = amfParser("min-max-inclusive"),
        message = "Maximum must be greater than or equal to minimum",
        owlClass = shape("ScalarShape"),
        owlProperty = sh("minInclusive"),
        constraint = shape("minimumMaximumValidation")
      ),
      AMFValidation(
        uri = amfParser("min-max-items"),
        message = "MaxItems must be greater than or equal to minItems",
        owlClass = shape("ArrayShape"),
        owlProperty = minCount,
        constraint = shape("minMaxItemsValidation")
      ),
      AMFValidation(
        uri = amfParser("min-max-length"),
        message = "MaxLength must be greater than or equal to minLength",
        owlClass = shape("ScalarShape"),
        owlProperty = sh("minLength"),
        constraint = shape("minMaxLengthValidation")
      ),
      AMFValidation(
        uri = amfParser("min-max-length"),
        message = "MaxLength must be greater than or equal to minLength",
        owlClass = shape("FileShape"),
        owlProperty = sh("minLength"),
        constraint = shape("minMaxLengthValidation")
      ),
      AMFValidation(
        uri = amfParser("min-max-properties"),
        message = "MaxProperties must be greater than or equal to minProperties",
        owlClass = sh("NodeShape"),
        owlProperty = shape("minProperties"),
        constraint = shape("minMaxPropertiesValidation")
      )
    )

    def validations(): Seq[AMFValidation] = result
  }

  object Raml08ShapeValidations {
    private lazy val result = Seq(
      AMFValidation(
        uri = amfParser("min-max-properties"),
        message = "MaxProperties must be greater than or equal to minProperties",
        owlClass = sh("NodeShape"),
        owlProperty = shape("minProperties"),
        constraint = shape("minMaxPropertiesValidation")
      )
    )

    def validations(): Seq[AMFValidation] = result
  }

  object Raml10ShapeValidations {
    private lazy val result = Seq(
      AMFValidation(
        owlClass = sh("NodeShape"),
        owlProperty = sh("properties"),
        constraint = shape("duplicatePropertyNames")
      )
    )

    def validations(): Seq[AMFValidation] = result
  }

  object OasShapeValidations {
    private lazy val result = Seq(
      AMFValidation(
        uri = amfParser("empty-enum"),
        message = "Enum in types cannot be empty",
        owlClass = shape("Shape"),
        owlProperty = sh("in"),
        constraint = sh("node"),
        value = amfParser("NonEmptyList").get.iri(),
        ramlErrorMessage = "Property 'enum' must have at least one value",
        openApiErrorMessage = "Property 'enum' for a Schema object must have at least one value"
      ),
      AMFValidation(
        uri = amfParser("array-shape-items-mandatory"),
        message = "Declaration of the type of the items for an array is required",
        owlClass = shape("ArrayShape"),
        owlProperty = shape("items"),
        constraint = minCount,
        value = "1",
        ramlErrorMessage = "items facet of RAML Array type is required",
        openApiErrorMessage = "items property of Schema objects of type 'array' is required"
      )
    )

    def validations(): Seq[AMFValidation] = result
  }

  object AsyncShapeValidations {
    private lazy val result = Seq(
      AMFValidation(
        message = "Discriminator must be in the objects required properties",
        constraint = shape("discriminatorInRequiredProperties"),
        owlClass = sh("NodeShape"),
        owlProperty = shape("discriminator")
      )
    )

    def validations(): Seq[AMFValidation] = result
  }

  object ShapeValidations extends ProfileValidations {
    private lazy val result =
      AmfShapeValidations.validations() ++ RamlShapeValidations.validations() ++ Raml10ShapeValidations
        .validations() ++ Raml08ShapeValidations.validations() ++ OasShapeValidations
        .validations() ++ AsyncShapeValidations.validations()
    override def validations(): Seq[AMFValidation] = result
  }

  override val profileToValidationMap: Map[ProfileName, ProfileValidations] = Map(
    JsonSchemaProfile -> forProfile(JsonSchemaProfile)
  )

  override def forProfile(p: ProfileName): ProfileValidations = {
    p match {
      case JsonSchemaProfile => ShapeValidations
      case _                 => () => Seq.empty
    }
  }

}

trait ProfileValidations {
  def validations(): Seq[AMFValidation]
}

trait CommonValidationDefinitions {

  // constraints
  def shape(name: String): ValueType             = ValueType(Namespace.Shapes, name)
  def sh(name: String): ValueType                = ValueType(Namespace.Shacl, name)
  def amfParser(name: String): Option[ValueType] = Some(ValueType(Namespace.AmfParser, name))

  val dataType: ValueType = sh("datatype")
  val minCount: ValueType = sh("minCount")
  val maxCount: ValueType = sh("maxCount")

  // values
  val string: String  = XsdTypes.xsdString.iri()
  val boolean: String = XsdTypes.xsdBoolean.iri()
  val integer: String = XsdTypes.xsdInteger.iri()

  val profileToValidationMap: Map[ProfileName, ProfileValidations]

  protected def forProfile(p: ProfileName): ProfileValidations

}
