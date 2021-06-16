package amf.shapes.internal.validation.definitions

import amf.core.client.common.validation.ProfileName
import amf.core.client.scala.vocabulary.Namespace
import amf.core.client.scala.vocabulary.Namespace.AmfRender
import amf.core.internal.validation.Validations
import amf.core.internal.validation.core.ValidationSpecification
import amf.core.internal.validation.core.ValidationSpecification.RENDER_SIDE_VALIDATION

// noinspection TypeAnnotation
object RenderSideValidations extends Validations {
  override val specification: String = RENDER_SIDE_VALIDATION
  override val namespace: Namespace  = AmfRender

  val RenderValidation = validation(
    "render-validation",
    "Default render validation"
  )

  val UnknownVendor = validation(
    "unknown-vendor",
    "Unknown vendor provided"
  )

  override val levels: Map[String, Map[ProfileName, String]] = Map()

  override val validations: List[ValidationSpecification] = List(RenderValidation, UnknownVendor)
}
