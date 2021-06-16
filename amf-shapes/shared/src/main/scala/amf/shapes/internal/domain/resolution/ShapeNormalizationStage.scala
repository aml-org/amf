package amf.shapes.internal.domain.resolution

import amf.core.client.common.validation.ProfileName
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.{DomainElement, Shape}
import amf.core.client.scala.transform.stages.TransformationStep
import amf.core.client.scala.transform.stages.elements.resolution.ElementStageTransformer
import amf.core.client.scala.transform.stages.selectors.ShapeSelector
import amf.shapes.internal.domain.resolution.shape_normalization._

/**
  * Computes the canonical form for all the shapes in the model
  * We are assuming certain pre-conditions in the state of the shape:
  *  - All type references have been replaced by their expanded forms
  */
class ShapeNormalizationStage(profile: ProfileName, val keepEditingInfo: Boolean) extends TransformationStep {
  override def transform(model: BaseUnit, errorHandler: AMFErrorHandler): BaseUnit =
    new ShapeNormalization(profile, keepEditingInfo)(errorHandler).resolve(model)

  private class ShapeNormalization(profile: ProfileName, val keepEditingInfo: Boolean)(
      implicit val errorHandler: AMFErrorHandler) {

    protected var m: Option[BaseUnit] = None
    protected val context             = new NormalizationContext(errorHandler, keepEditingInfo, profile)

    def resolve[T <: BaseUnit](model: T): T = {
      m = Some(model)
      model.transform(ShapeSelector, transform).asInstanceOf[T]
    }

    protected def transform(element: DomainElement, isCycle: Boolean): Option[DomainElement] = {
      element match {
        case shape: Shape => transformer.transform(shape)
        case other        => Some(other)
      }
    }

    def transformer: ElementStageTransformer[Shape] = new ShapeTransformer(context)
  }
}
