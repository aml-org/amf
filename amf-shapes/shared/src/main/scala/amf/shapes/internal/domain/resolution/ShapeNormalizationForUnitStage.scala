package amf.shapes.internal.domain.resolution

import amf.core.client.common.validation.ProfileName
import amf.core.client.scala.AMFGraphConfiguration
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.document.FieldsFilter.All
import amf.core.client.scala.model.domain.{AmfObject, DomainElement, Shape}
import amf.core.client.scala.transform.TransformationStep
import amf.core.client.scala.traversal.iterator.{AmfElementStrategy, DomainElementStrategy, InstanceCollector}
import amf.core.internal.transform.stages.selectors.ShapeSelector
import amf.shapes.internal.domain.resolution.shape_normalization.{
  NormalizationContext,
  ShapeNormalizationInheritanceResolver,
  ShapeNormalizationRecursionAnalyzer,
  ShapeNormalizationReferencesUpdater
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
    implicit val context: NormalizationContext = new NormalizationContext(eh, keepEditingInfo, profile)

    // Step 1: resolve inheritance
    resolveInheritance(model)

    // Step 2: update references
    updateReferences(model)

    // Step 3: validate & place recursions
    validateAndPlaceRecursions(model)

    model
  }

  private def resolveInheritance(model: BaseUnit)(implicit context: NormalizationContext): Unit = {
    model.iterator(fieldsFilter = All).foreach {
      case s: Shape => ShapeNormalizationInheritanceResolver(context).normalize(s)
      case _        => // nothing
    }
  }

  private def updateReferences(model: BaseUnit)(implicit context: NormalizationContext): Unit = {
    if (!context.resolvedInheritanceIndex.isEmpty) {
      val updater = ShapeNormalizationReferencesUpdater(context)

      val iterator = AmfUpdaterIterator(model, e => updater.update(e))
      iterator.foreach {
        case o: AmfObject => updater.updateFields(o)
        case _            => // skip
      }
    }
  }

  private def validateAndPlaceRecursions(model: BaseUnit)(implicit context: NormalizationContext): Unit = {
    val recursionAnalyzer = ShapeNormalizationRecursionAnalyzer(context)

    def placeRecursions(element: DomainElement): Option[DomainElement] = {
      element match {
        case shape: Shape => Some(recursionAnalyzer.analyze(shape))
        case other        => Some(other)
      }
    }

    model.transform(ShapeSelector, (elem, _) => placeRecursions(elem))(context.errorHandler)
  }
}
