package amf.shapes.internal.spec.jsonschema.semanticjsonschema

import amf.core.client.common.validation.ProfileName
import amf.core.client.scala.vocabulary.Namespace
import amf.core.client.scala.vocabulary.Namespace.AmfParser
import amf.core.internal.validation.Validations
import amf.core.internal.validation.core.ValidationSpecification

object SemanticJsonSchemaValidations extends Validations {
  override val specification: String = (Namespace.Shapes + "SemanticJsonSchema").iri()
  override val namespace: Namespace  = AmfParser

  val UnknownTransformedMappingType =
    validation("unknown-transformed-mapping-type", "Unknown transformed mapping type")
  val UnsupportedConstraint = validation("unknown-constraint", "Unsupported constraint")

  override val validations: List[ValidationSpecification] = List(
    UnknownTransformedMappingType,
    UnsupportedConstraint
  )
  override val levels: Map[String, Map[ProfileName, String]] = Map.empty
}
