package amf.shapes.internal.domain.resolution

import amf.core.client.common.validation.ProfileName
import amf.core.client.scala.AMFGraphConfiguration
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.document.FieldsFilter.All
import amf.core.client.scala.model.domain.{DomainElement, Shape}
import amf.core.client.scala.transform.TransformationStep
import amf.core.internal.transform.stages.selectors.ShapeSelector
import amf.shapes.internal.domain.resolution.shape_normalization.{
  NormalizationContext,
  ShapeInheritanceResolver,
  ShapeReferencesUpdater
}

/** Computes the canonical form for all the shapes in the model We are assuming certain pre-conditions in the state of
  * the shape:
  *   - All type references have been replaced by their expanded forms
  */
class ShapeNormalizationForUnitStage(profile: ProfileName, val keepEditingInfo: Boolean) extends TransformationStep {
  override def transform(
      model: BaseUnit,
      eh: AMFErrorHandler,
      configuration: AMFGraphConfiguration
  ): BaseUnit = {
    val context = new NormalizationContext(eh, keepEditingInfo, profile)

    // Step 1: resolve inheritance
    model.iterator(fieldsFilter = All).foreach {
      case s: Shape => {
        ShapeInheritanceResolver(context).normalize(s)
      }
      case _ =>
    }

    def updateReferences(element: DomainElement): Option[DomainElement] = {
      element match {
        case shape: Shape => Some(ShapeReferencesUpdater(context).update(shape))
        case other        => Some(other)
      }
    }

    // Step 2: update references & place RecursiveShapes
    model.transform(ShapeSelector, (elem, _) => updateReferences(elem))(eh)
  }
}
