package amf.plugins.features.validation

import amf._
import amf.core.validation.core.ValidationSpecification
import amf.core.validation.core.ValidationSpecification.RENDER_SIDE_VALIDATION
import amf.core.vocabulary.Namespace
import amf.core.vocabulary.Namespace.AmfRender

// noinspection TypeAnnotation
object RenderSideValidations extends Validations {
  override val specification: String = RENDER_SIDE_VALIDATION
  override val namespace: Namespace  = AmfRender

  val RenderValidation = validation(
    "render-validation",
    "Default render validation"
  )

  override val levels: Map[String, Map[ProfileName, String]] = Map()

  override val validations: List[ValidationSpecification] = List(RenderValidation)
}
