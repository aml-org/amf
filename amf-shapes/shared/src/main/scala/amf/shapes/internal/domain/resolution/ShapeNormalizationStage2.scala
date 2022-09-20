package amf.shapes.internal.domain.resolution

import amf.core.client.common.validation.ProfileName
import amf.core.client.scala.AMFGraphConfiguration
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.{DomainElement, Shape}
import amf.core.client.scala.transform.TransformationStep
import amf.core.internal.transform.stages.elements.resolution.ElementStageTransformer
import amf.core.internal.transform.stages.selectors.ShapeSelector
import amf.shapes.internal.domain.resolution.shape_normalization2.{NormalizationContext2, ShapeInheritanceResolver}

/** Computes the canonical form for all the shapes in the model We are assuming certain pre-conditions in the state of
  * the shape:
  *   - All type references have been replaced by their expanded forms
  */
class ShapeNormalizationStage2(profile: ProfileName, val keepEditingInfo: Boolean) extends TransformationStep {
  override def transform(
      model: BaseUnit,
      errorHandler: AMFErrorHandler,
      configuration: AMFGraphConfiguration
  ): BaseUnit =
    new ShapeNormalization2(profile, keepEditingInfo)(errorHandler).transform(model, configuration)

  private class ShapeNormalization2(profile: ProfileName, val keepEditingInfo: Boolean)(implicit
      val errorHandler: AMFErrorHandler
  ) {

    protected var m: Option[BaseUnit] = None
    protected val context             = new NormalizationContext2(errorHandler, keepEditingInfo, profile)

    def transform[T <: BaseUnit](model: T, configuration: AMFGraphConfiguration): T = {
      m = Some(model)

      // Performance?
      model.iterator().foreach {
        case s: Shape => ShapeInheritanceResolver(s, context)
        case _        =>
      }
      model.transform(ShapeSelector, transformReferences(_, _, configuration)).asInstanceOf[T]
    }

    protected def transformReferences(
        element: DomainElement,
        isCycle: Boolean,
        configuration: AMFGraphConfiguration
    ): Option[DomainElement] = {
      element match {
        case shape: Shape => referencesTransformer.transform(shape, configuration)
        case other        => Some(other)
      }
    }

    def referencesTransformer: ElementStageTransformer[Shape] = new ShapeTransformer2(context)
  }
}
