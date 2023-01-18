package amf.shapes.internal.domain.resolution

import amf.core.client.common.validation.ProfileName
import amf.core.client.scala.AMFGraphConfiguration
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.{DomainElement, Shape}
import amf.core.client.scala.transform.TransformationStep
import amf.core.internal.transform.stages.selectors.ShapeSelector
import amf.shapes.internal.domain.resolution.shape_normalization.{NormalizationContext, ShapeInheritanceResolver}

/** Computes the canonical form for all the shapes in the model We are assuming certain pre-conditions in the state of
 * the shape:
 *   - All type references have been replaced by their expanded forms
 */
class ShapeNormalizationStage(profile: ProfileName, val keepEditingInfo: Boolean) extends TransformationStep {
  override def transform(
                          model: BaseUnit,
                          errorHandler: AMFErrorHandler,
                          configuration: AMFGraphConfiguration
                        ): BaseUnit =
    new ShapeNormalization(profile, keepEditingInfo)(errorHandler).transform(model, configuration)

  private class ShapeNormalization(profile: ProfileName, val keepEditingInfo: Boolean)(implicit
                                                                                       val errorHandler: AMFErrorHandler
  ) {

    protected var m: Option[BaseUnit] = None
    private val context = new NormalizationContext(errorHandler, keepEditingInfo, profile)

    def transform[T <: BaseUnit](model: T, configuration: AMFGraphConfiguration): T = {
      m = Some(model)

      // Performance?
      model.iterator().foreach {
        case s: Shape => ShapeInheritanceResolver(s, context)
        case _ =>
      }
      model.transform(ShapeSelector, updateReferences(_, _, configuration)).asInstanceOf[T]
    }

    private def updateReferences(
                                  element: DomainElement,
                                  isCycle: Boolean,
                                  configuration: AMFGraphConfiguration
                                ): Option[DomainElement] = {
      element match {

        case shape: Shape =>
          val updater = new ShapeReferencesUpdaterTransformer(context)
          updater.transform(shape, configuration)

        case other => Some(other)
      }
    }

  }

}
