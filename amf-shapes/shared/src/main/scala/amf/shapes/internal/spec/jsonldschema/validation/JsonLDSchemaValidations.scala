package amf.shapes.internal.spec.jsonldschema.validation

import amf.core.client.common.validation.ProfileName
import amf.core.client.common.validation.SeverityLevels.VIOLATION
import amf.core.client.scala.vocabulary.Namespace
import amf.core.client.scala.vocabulary.Namespace.AmfParser
import amf.core.internal.remote.JsonLDSchema
import amf.core.internal.validation.Validations
import amf.core.internal.validation.core.ValidationSpecification

object JsonLDSchemaValidations extends Validations {

  val UnsupportedShape: ValidationSpecification = ValidationSpecification(
    "unsupported-shape",
    "Cannot parse an instance using abstract core SHAPE as structure definition"
  )

  val UnsupportedConditionalShape: ValidationSpecification = ValidationSpecification(
    "unsupported-conditional-shape",
    "Conditional shape class is not supported by current defined shape"
  )

  val IncompatibleNodes: ValidationSpecification = ValidationSpecification(
    "incompatible-nodes",
    "Parsed nodes for conditional are no compatible, using more specific one"
  )

  val IncompatibleItemNodes: ValidationSpecification = ValidationSpecification(
    "incompatible-items-nodes",
    "Parsed array items value for conditional are no compatible, using more specific one"
  )

  val IncompatibleScalarDataType: ValidationSpecification = ValidationSpecification(
    "incompatible-scala-datatype",
    "Scalar data types for conditional are no compatible, using more specific one"
  )

  val IncompatibleDomainElement: ValidationSpecification = ValidationSpecification(
    "incompatible-domain-element",
    "Encoded domain element is incompatible with jsonschema ld parsing"
  )

  val UnsupportedScalarTagType: ValidationSpecification = ValidationSpecification(
    "unsupported-scalar-tagType",
    "Unsupported scalar tag type"
  )

  val IncompatibleScalarTagType: ValidationSpecification = ValidationSpecification(
    "incompatible-scalar-tag-type",
    "Scalar data type does not match with tag type"
  )

  val InvalidCharacteristicsUse: ValidationSpecification = ValidationSpecification(
    "invalid-characteristics-use",
    "Characteristics can only be used at inlined types of property ranges. Usage at encodes and declared shapes and array items is not allowed"
  )

  val UnsupportedScalarRootLevel: ValidationSpecification = ValidationSpecification(
    "unsupported-scalar-root-level",
    "Scalars are not supported as root element"
  )

  val UnsupportedRootLevel: ValidationSpecification = ValidationSpecification(
    "unsupported-root-level",
    "Unsupported as root element"
  )

  override val specification: String                      = JsonLDSchema.id
  override val namespace: Namespace                       = AmfParser
  override val validations: List[ValidationSpecification] = List(UnsupportedShape)
  override val levels: Map[String, Map[ProfileName, String]] =
    Map((UnsupportedShape.id, all(VIOLATION)), (UnsupportedConditionalShape.id, all(VIOLATION)))
}
